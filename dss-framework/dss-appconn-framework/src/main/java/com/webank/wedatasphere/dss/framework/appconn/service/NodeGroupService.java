package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkflowNodeGroupDAO;
import com.webank.wedatasphere.dss.framework.appconn.entity.NodeGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Author: xlinliu
 */
@Service
public class NodeGroupService {

    private final DssWorkflowNodeGroupDAO nodeGroupDao;

    @Autowired
    public NodeGroupService(DssWorkflowNodeGroupDAO nodeGroupDao) {
        this.nodeGroupDao = nodeGroupDao;
    }

    public List<NodeGroup> getAllNodeGroups() {
        return nodeGroupDao.findAll();
    }
}