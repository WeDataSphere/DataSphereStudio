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

    public void removeUiFromValidate(Integer uiId, Integer validateId) throws IllegalArgumentException {
        uiToValidateDao.deleteByUiIdAndValidateId(uiId, validateId); // You need to create this method in your DAO
    }

    private void validateUiToValidate(UiToValidate uiToValidate) throws IllegalArgumentException {
        // Implement the validation logic here
    }
}