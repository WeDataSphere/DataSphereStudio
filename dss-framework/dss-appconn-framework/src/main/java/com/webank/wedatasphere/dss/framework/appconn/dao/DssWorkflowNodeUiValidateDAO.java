package com.webank.wedatasphere.dss.framework.appconn.dao;

import org.apache.ibatis.annotations.Mapper;
import com.webank.wedatasphere.dss.framework.appconn.entity.Validate;

import java.util.List;

/**
 * 校验规则表(Validate)表数据库访问层
 */
@Mapper
public interface DssWorkflowNodeUiValidateDAO{

    /**
     * 新增
     *
     * @param validate
     */
    int insert(Validate validate);

    /**
     * 更新
     *
     * @param validate
     */
    int update(Validate validate);

    /**
    * 根据主键查询
    * @return {@link Validate}
    */
    Validate findByPrimaryKey(Integer id);

    List<Validate> findByIds(List<Integer> list);

    List<Validate> findAll();

    /**
    * 根据主键删除
    */
    int deleteByPrimaryKey(Integer id);

}