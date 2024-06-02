package com.webank.wedatasphere.dss.framework.appconn.dao;

import com.webank.wedatasphere.dss.framework.appconn.entity.NodeToGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 节点与分类关联表数据库访问层
 */
@Mapper
public interface DssWorkflowNodeToGroupDAO{

    /**
     * 新增
     *
     * @param nodeToGroup
     */
    int insert(NodeToGroup nodeToGroup);

    /**
     * 更新
     *
     * @param nodeToGroup
     */
    int update(NodeToGroup nodeToGroup);

    /**
    * 根据主键查询
    * @return {@link NodeToGroup}
    */
    NodeToGroup findByPrimaryKey(Integer id);
    /**
    *根据nodeId查询
    * @return {@link NodeToGroup}
    */
    List<NodeToGroup> findByNodeId(Integer nodeId);

    List<NodeToGroup> findByNodeIds(List<Integer> list);
    /**
     * 批量删除
     */
    int deleteByNodeId(Integer nodeId);


    /**
    * 根据主键删除
    */
    int deleteByPrimaryKey(Integer id);

}