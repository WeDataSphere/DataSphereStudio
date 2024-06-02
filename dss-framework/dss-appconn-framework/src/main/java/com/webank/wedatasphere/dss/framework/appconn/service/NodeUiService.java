package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkflowNodeToUiDAO;
import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkflowNodeUiDAO;
import com.webank.wedatasphere.dss.framework.appconn.entity.NodeToUi;
import com.webank.wedatasphere.dss.framework.appconn.entity.NodeUi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NodeUiService {

    private final DssWorkflowNodeUiDAO nodeUiDao;

    @Autowired
    private DssWorkflowNodeToUiDAO nodeToUiDao;


    @Autowired
    public NodeUiService(DssWorkflowNodeUiDAO nodeUiDao) {
        this.nodeUiDao = nodeUiDao;
    }

    public NodeUi saveNodeUi(NodeUi nodeUi) throws IllegalArgumentException {
        validateNodeUi(nodeUi);
        if (nodeUi.getId() == null) {
            // Insert new ui property
            nodeUiDao.insert(nodeUi);

        } else {
            // Update existing ui property
            nodeUiDao.update(nodeUi);

        }
        return nodeUi;
    }

    public void deleteNodeUi(Integer id) throws IllegalArgumentException {
         nodeUiDao.deleteByPrimaryKey(id);
    }

    public NodeUi getNodeUiById(Integer id) {
        return nodeUiDao.findByPrimaryKey(id);
    }

    public List<NodeUi> getNodeUisByNodeId(Integer nodeId) {
        List<Integer> uiIds = nodeToUiDao.findByNodeId(nodeId).stream().map(NodeToUi::getUiId).collect(Collectors.toList());
        if(uiIds.isEmpty()){
            return Collections.emptyList();
        }
        return nodeUiDao.findByIds(uiIds); // You need to create this method in your DAO
    }

    public List<NodeUi> getAllNodeUis() {
        return nodeUiDao.findAll();
    }


    public boolean isUsed(Integer uiId){
        return !nodeToUiDao.findByUiId(uiId).isEmpty();
    }

    private void validateNodeUi(NodeUi nodeUi) throws IllegalArgumentException {
        //todo Implement your validation logic here
    }
}