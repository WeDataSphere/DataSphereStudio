package com.webank.wedatasphere.dss.framework.appconn.dao;

import org.apache.ibatis.annotations.Mapper;
import com.webank.wedatasphere.dss.framework.appconn.entity.NodeToUi;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 节点与UI属性关联表数据库访问层
 */
@Mapper
public interface DssWorkflowNodeToUiDAO{

    /**
     * 新增
     *
     * @param nodeToUi
     */
    int insert(NodeToUi nodeToUi);

    /**
     * 更新
     *
     * @param nodeToUi
     */
    int update(NodeToUi nodeToUi);

    /**
    * 根据主键查询
    * @return {@link NodeToUi}
    */
    NodeToUi findByPrimaryKey(Integer id);


    List<NodeToUi> findByNodeId(Integer nodeId);
    List<NodeToUi> findByNodeIdAndUiId(NodeToUi nodeToUi);

    List<NodeToUi> findByUiId(Integer uiId);


    /**
     * 根据NodeId和UiId删除
     */
    int deleteByNodeIdAndUiId(@Param("nodeId") Integer nodeId, @Param("uiId") Integer uiId);

    /**
    * 根据主键删除
    */
    int deleteByPrimaryKey(Integer id);

}