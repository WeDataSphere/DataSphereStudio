package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkspaceMenuDAO;
import com.webank.wedatasphere.dss.framework.appconn.entity.MenuType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author: xlinliu
 */
@Service
public class MenuTypeService {

    private final DssWorkspaceMenuDAO dssWorkspaceMenuDAO;

    @Autowired
    public MenuTypeService(DssWorkspaceMenuDAO dssWorkspaceMenuDAO) {
        this.dssWorkspaceMenuDAO = dssWorkspaceMenuDAO;
    }

    public List<MenuType> getAllMenuTypes() {
        List<MenuType> menuTypes = dssWorkspaceMenuDAO.getAllMenu();
        return menuTypes;
    }
}