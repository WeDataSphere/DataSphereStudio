package com.webank.wedatasphere.dss.framework.appconn.dao;

import com.webank.wedatasphere.dss.framework.appconn.entity.NodeGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 节点分类表(NodeGroup)表数据库访问层
 * @author arionliu
 */
@Mapper
public interface DssWorkflowNodeGroupDAO{

    /**
     * 新增
     *
     * @param nodeGroup
     */
    int insert(NodeGroup nodeGroup);

    /**
     * 更新
     *
     * @param nodeGroup
     */
    int update(NodeGroup nodeGroup);

    /**
    * 根据主键查询
    * @return {@link NodeGroup}
    */
    NodeGroup findByPrimaryKey(Integer id);


    /**
     * 查询所有
     * @return {@link NodeGroup}
     */
    List<NodeGroup> findAll();
    /**
    * 根据主键删除
    */
    int deleteByPrimaryKey(Integer id);

}