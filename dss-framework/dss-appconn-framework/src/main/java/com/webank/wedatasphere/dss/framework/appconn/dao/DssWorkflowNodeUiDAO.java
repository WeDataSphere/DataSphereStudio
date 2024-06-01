package com.webank.wedatasphere.dss.framework.appconn.dao;

import org.apache.ibatis.annotations.Mapper;
import com.webank.wedatasphere.dss.framework.appconn.entity.NodeUi;

import java.util.List;

/**
 * UI属性表(NodeUi)表数据库访问层
 */
@Mapper
public interface DssWorkflowNodeUiDAO{

    /**
     * 新增
     *
     * @param nodeUi
     */
    int insert(NodeUi nodeUi);

    /**
     * 更新
     *
     * @param nodeUi
     */
    int update(NodeUi nodeUi);

    /**
    * 根据主键查询
    * @return {@link NodeUi}
    */
    NodeUi findByPrimaryKey(Integer id);

    List<NodeUi> findByIds(List<Integer> list);

    List<NodeUi>  findAll();

    /**
    * 根据主键删除
    */
    int deleteByPrimaryKey(Integer id);

}