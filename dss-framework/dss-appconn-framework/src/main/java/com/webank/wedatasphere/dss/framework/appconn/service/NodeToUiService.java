package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkflowNodeToUiDAO;
import com.webank.wedatasphere.dss.framework.appconn.entity.NodeToUi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NodeToUiService {

    private final DssWorkflowNodeToUiDAO nodeToUiDao;

    @Autowired
    public NodeToUiService(DssWorkflowNodeToUiDAO nodeToUiDao) {
        this.nodeToUiDao = nodeToUiDao;
    }

    public NodeToUi addNodeToUi(NodeToUi nodeToUi){
        validateNodeToUi(nodeToUi);
        int rowsInserted = nodeToUiDao.insert(nodeToUi);
        if (rowsInserted > 0) {
            return nodeToUi;
        } else {
            throw new IllegalArgumentException("Failed to insert new NodeToUi");
        }
    }

    public void removeNodeFromUi(Integer nodeId, Integer uiId) throws IllegalArgumentException {
        // Here you might want to check for valid nodeId and uiId
        nodeToUiDao.deleteByNodeIdAndUiId(nodeId, uiId); // You need to create this method in your DAO
    }

    private void validateNodeToUi(NodeToUi nodeToUi) throws IllegalArgumentException {
        //todo Implement your validation logic here
    }
}