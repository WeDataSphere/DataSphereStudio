package com.webank.wedatasphere.dss.framework.appconn.dao;

import org.apache.ibatis.annotations.Mapper;
import com.webank.wedatasphere.dss.framework.appconn.entity.UiToValidate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * UI属性与校验规则关联表数据库访问层
 */
@Mapper
public interface DssWorkflowNodeUiToValidateDAO{

    /**
     * 新增
     *
     * @param uiToValidate
     */
    int insert(UiToValidate uiToValidate);

    /**
     * 更新
     *
     * @param uiToValidate
     */
    int update(UiToValidate uiToValidate);

    /**
    * 根据主键查询
    * @return {@link UiToValidate}
    */
    UiToValidate findByPrimaryKey(Integer id);

    int deleteByUiIdAndValidateId(UiToValidate uiToValidate);


    List<UiToValidate> findByUiId(Integer uiId);

    List<UiToValidate> findByUiIdAndValidateId(UiToValidate uiToValidate);

//    List<UiToValidate> findByValidateId(Integer validateId);
//
//    List<UiToValidate> findByUiIdAndValidateId(Integer uiId, Integer validateId);

    /**
    * 根据主键删除
    */
    int deleteByPrimaryKey(Integer id);

}