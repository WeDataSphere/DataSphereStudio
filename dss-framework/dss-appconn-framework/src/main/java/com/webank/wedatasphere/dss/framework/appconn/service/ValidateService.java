package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkflowNodeUiToValidateDAO;
import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkflowNodeUiValidateDAO;
import com.webank.wedatasphere.dss.framework.appconn.entity.UiToValidate;
import com.webank.wedatasphere.dss.framework.appconn.entity.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidateService {

    private final DssWorkflowNodeUiValidateDAO ruleValidationDao;

    @Autowired
    private DssWorkflowNodeUiToValidateDAO ruleValidationToValidateDao;

    @Autowired
    public ValidateService(DssWorkflowNodeUiValidateDAO ruleValidationDao) {
        this.ruleValidationDao = ruleValidationDao;
    }

    public Validate saveValidation(Validate ruleValidation) throws IllegalArgumentException {
        validateRuleValidation(ruleValidation);
        if (ruleValidation.getId() == null) {
            ruleValidationDao.insert(ruleValidation);

        } else {
            ruleValidationDao.update(ruleValidation);

        }
        return ruleValidation;
    }

    public Validate getValidationById(Integer id) {
        return ruleValidationDao.findByPrimaryKey(id);
    }

    public List<Validate> getValidationsByUiId(Integer uiId) {
        List<Integer> validateIds = ruleValidationToValidateDao.findByUiId(uiId).stream().map(UiToValidate::getValidateId).collect(Collectors.toList());// You need to create this method in your DAO
        if(validateIds.isEmpty()) {
            return Collections.emptyList();
        }
        return ruleValidationDao.findByIds(validateIds);

    }

    public List<Validate> getAllValidations() {
        return ruleValidationDao.findAll();
    }

    private void validateRuleValidation(Validate ruleValidation) throws IllegalArgumentException {
        // Implement the validation logic here
    }
}