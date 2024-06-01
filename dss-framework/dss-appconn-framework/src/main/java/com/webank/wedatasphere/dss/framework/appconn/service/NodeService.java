package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkflowNodeDAO;
import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkflowNodeGroupDAO;
import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkflowNodeToGroupDAO;
import com.webank.wedatasphere.dss.framework.appconn.entity.Node;
import com.webank.wedatasphere.dss.framework.appconn.entity.NodeToGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: xlinliu
 */
@Service
public class NodeService {
    @Autowired
    private  DssWorkflowNodeDAO nodeDao;
   @Autowired
    private DssWorkflowNodeGroupDAO nodeGroupDao;
   @Autowired
   private DssWorkflowNodeToGroupDAO nodeToGroupDao;


    /**
     *
     * 增加或修改一个工作流节点，工作流画布中就能拖拽这个节点了。工作流节点一定要绑定到一个已有的appconn中。
     * @param node
     * @return
     * @throws Exception
     */
    public Node saveNode(Node node)   {
        validateNode(node,node.getId() != null);
        if (node.getId() == null) {
            // Insert new node
            int rowsInserted = nodeDao.insert(node);
            if (rowsInserted <= 0)  {
                throw new DSSRuntimeException("Failed to insert new node");
            }
        } else {
            // Update existing node
            int rowsUpdated = nodeDao.update(node);
            if (rowsUpdated <= 0) {
                throw new DSSRuntimeException("Failed to update existing node");
            }
        }
        //处理节点分类关系
        List<NodeToGroup> oldRef =nodeToGroupDao.findByNodeId(node.getId());
        if(oldRef.isEmpty()){
            NodeToGroup ref = new NodeToGroup();
            ref.setNodeId(node.getId());
            ref.setGroupId(node.getNodeGroup());
            nodeToGroupDao.insert(ref);
        }else if(oldRef.size()==1){
            NodeToGroup ref = oldRef.get(0);
            ref.setGroupId(node.getNodeGroup());
            nodeToGroupDao.update(ref);
        }else{
            nodeToGroupDao.deleteByNodeId(node.getId());
            NodeToGroup ref = new NodeToGroup();
            ref.setNodeId(node.getId());
            ref.setGroupId(node.getNodeGroup());
            nodeToGroupDao.insert(ref);
        }
        return node;
    }

    public void deleteNode(Integer id) {
        // Here you might want to check for any linked attributes and handle accordingly
        int rowsDeleted = nodeDao.deleteByPrimaryKey(id);
        if (rowsDeleted <= 0) {
            throw new DSSRuntimeException("Failed to delete node with id: " + id);
        }
        nodeToGroupDao.deleteByNodeId(id);
    }

    public Node getNodeById(Integer id) {
        return nodeDao.findByPrimaryKey(id);
    }

    public List<Node> getNodesByAppconnName(String appconnName) {
        // You may need to add a method in your DAO to handle this
         return nodeDao.findByAppconnName(appconnName);
    }

    public List<Node> getNodesByName(String name) {
        // You may need to add a method in your DAO to handle this
        return nodeDao.findByAppconnName(name);
    }

    public List<Node> getAllNodes() {
        // You may need to add a method in your DAO to handle this
         return nodeDao.findAll();
    }

    private void validateNode(Node node,boolean isUpdateNode) {
    //nodeGroup必须是已有的分类。
        if(node.getNodeGroup()==null|| nodeGroupDao.findByPrimaryKey(node.getNodeGroup())==null){
            throw new IllegalArgumentException("NodeGroup is not exist");
        }

        //name不能与已有的节点重名。
        if(node.getName()==null){
            throw new IllegalArgumentException( "Node name can't be null");
        }
        if (!isUpdateNode && !nodeDao.findByName(node.getName()).isEmpty()) {
            throw new IllegalArgumentException("Node name is already exist");
        }


        //jumpType只能是0、1、2
        if(node.getSupportJump()==0|| (node.getJumpType()!=1&&node.getJumpType()!=2)){
            throw new IllegalArgumentException("JumpType is invalid");
        }

        //supportJump只能是0或1
        if(node.getSupportJump()!=0&&node.getSupportJump()!=1){
            throw new IllegalArgumentException("SupportJump is invalid");
        }

        //submitToScheduler只能是0或1
        if(node.getSubmitToScheduler()!=0&&node.getSubmitToScheduler()!=1){
            throw new IllegalArgumentException("SubmitToScheduler is invalid");
        }
        //enableCopy只能是0或1
        if(node.getEnableCopy()!=0&&node.getEnableCopy()!=1){
            throw new IllegalArgumentException("EnableCopy is invalid");
        }
        //shouldCreationBeforeNode只能是0或1
        if(node.getShouldCreationBeforeNode()!=0&&node.getShouldCreationBeforeNode()!=1){
            throw new IllegalArgumentException("ShouldCreationBeforeNode is invalid");
        }

    }
}
