package com.webank.wedatasphere.dss.framework.appconn.dao;

import org.apache.ibatis.annotations.Mapper;
import com.webank.wedatasphere.dss.framework.appconn.entity.Node;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 节点表(Node)表数据库访问层
 */
@Mapper
public interface DssWorkflowNodeDAO{

    /**
     * 新增
     *
     * @param node
     */
    int insert(Node node);

    /**
     * 更新
     *
     * @param node
     */
    int update(Node node);

    /**
    * 根据主键查询
    * @return {@link Node}
    */
    Node findByPrimaryKey(Integer id);

    /**
     * 根据name或nodeType查询
     * @return {@link Node}
     */
    List<Node> findByNameOrNodeType(@Param(value = "name") String name, @Param(value = "nodeType") String nodeType);

    /**
     * 根据appconnName查询
     * @return {@link Node}
     */
    List<Node> findByAppconnName(String appconnName);

    List<Node> findByName(String name);

    /**
     * 查询所有
     * @return {@link List<Node>}
     */

    List<Node> findAll();

    /**
    * 根据主键删除
    */
    int deleteByPrimaryKey(Integer id);

}