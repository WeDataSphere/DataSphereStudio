package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkflowNodeUiToValidateDAO;
import com.webank.wedatasphere.dss.framework.appconn.entity.UiToValidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UiToValidateService {

    private final DssWorkflowNodeUiToValidateDAO uiToValidateDao;

    @Autowired
    public UiToValidateService(DssWorkflowNodeUiToValidateDAO uiToValidateDao) {
        this.uiToValidateDao = uiToValidateDao;
    }

    public UiToValidate addUiToValidate(UiToValidate uiToValidate) throws IllegalArgumentException {
        validateUiToValidate(uiToValidate);
        uiToValidateDao.insert(uiToValidate);
        return uiToValidate;

    }

    public void removeUiFromValidate(UiToValidate uiToValidate) throws IllegalArgumentException {
        uiToValidateDao.deleteByUiIdAndValidateId(uiToValidate); // You need to create this method in your DAO
    }

    private void validateUiToValidate(UiToValidate uiToValidate) throws IllegalArgumentException {
        // Implement the validation logic here
        if(uiToValidate.getUiId() == null || uiToValidate.getValidateId() == null) {
            throw new IllegalArgumentException("uiId and validateId cannot be null");
        }
        if(!uiToValidateDao.findByUiIdAndValidateId(uiToValidate).isEmpty()){
            throw new IllegalArgumentException("关联关系已经存在，请勿重复添加");
        }

    }
}