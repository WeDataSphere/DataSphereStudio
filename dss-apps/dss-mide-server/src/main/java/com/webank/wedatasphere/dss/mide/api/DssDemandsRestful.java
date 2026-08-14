package com.webank.wedatasphere.dss.mide.api;

import com.webank.wedatasphere.dss.mide.entity.Demands;
import com.webank.wedatasphere.dss.mide.service.DemandsService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@RequestMapping(path = "/mide/projects", produces = {"application/json"})
@RestController
public class DssDemandsRestful {

    private static final Logger LOG = LoggerFactory.getLogger(DssDemandsRestful.class);

    @Autowired
    private DemandsService demandsService;

    private ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/{projectId}/demands/list", method = RequestMethod.GET)
    public Message list(HttpServletRequest req, @PathVariable("projectId") Long projectId) {
        Message message = null;
        try {

            List<Demands> demands = demandsService.listDemandsByProjectId(projectId);
            message = Message.ok();
            message.data("demands", demands);
        } catch (Throwable e) {
            LOG.error("Failed to list demands by projectId {}: ", projectId, e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "/{projectId}/demands/{id}", method = RequestMethod.GET)
    public Message get(HttpServletRequest req, @PathVariable("projectId") Long projectId, @PathVariable("id") Long id) {
        Message message = null;
        try {
            Demands demands = demandsService.selectById(id);
            message = Message.ok();
            message.data("demands", demands);
        } catch (Throwable e) {
            LOG.error("Failed to get demands by id {}: ", id, e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "/{projectId}/demands/{id}", method = RequestMethod.DELETE)
    public Message delete(HttpServletRequest req, @PathVariable("projectId") Long projectId, @PathVariable("id") Long id) {
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            Demands demands = demandsService.selectById(id);
            LOG.info("user {} begin to delete demands projectId:{},id: {}", userName, id);
            if (null != demands) {
                if (userName.equalsIgnoreCase(demands.getCreator())) {
                    demandsService.delete(id);
                    message = Message.ok();
                } else {
                    message = Message.error("User does not have permission, please contact creator " + demands.getCreator());
                }
            } else {
                message = Message.error(" demands not exists! " + id);
            }
        } catch (Throwable e) {
            LOG.error("Failed to delete demands by id {}: ", id, e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "/{projectId}/demands/{id}", method = RequestMethod.PUT)
    public Message update(HttpServletRequest req, @PathVariable("projectId") Long projectId, @PathVariable("id") Long id, JsonNode json) {
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            LOG.info("user {} begin to update demands, projectId: {}, id: {}, params: {}", userName, projectId, id, json);
            Demands demands = demandsService.selectById(id);
            if (null != demands) {
                Demands newDemands = mapper.readValue(json, Demands.class);
                newDemands.setId(id);
                newDemands.setProjectId(projectId);
                newDemands.setModifier(userName);
                demandsService.update(newDemands);
                message = Message.ok();
                message.data("id", id);
                message.data("projectId", projectId);
            } else {
                message = Message.error(" demands not exists! " + id);
            }
        } catch (DuplicateKeyException e) {
            LOG.error("Failed to update demands by projectId {}: ", projectId, e);
            message = Message.error("名称不能重复");
        } catch (Throwable e) {
            LOG.error("Failed to update demands by id {}: ", id, e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "/{projectId}/demands/", method = RequestMethod.POST)
    public Message insert(HttpServletRequest req, @PathVariable("projectId") Long projectId, JsonNode json) {
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            LOG.info("user {} begin to insert demands by projectId:{},params: {}",userName, projectId, json);
            Demands newDemands = mapper.readValue(json, Demands.class);
            newDemands.setProjectId(projectId);
            newDemands.setCreator(userName);
            newDemands.setModifier(userName);
            demandsService.save(newDemands);
            message = Message.ok();
            message.data("id", newDemands.getId());
            message.data("projectId", projectId);
        } catch (DuplicateKeyException e) {
            LOG.error("Failed to update demands by projectId {}: ", projectId, e);
            message = Message.error("名称不能重复");
        } catch (Throwable e) {
            LOG.error("Failed to update demands by projectId {}: ", projectId, e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

}
