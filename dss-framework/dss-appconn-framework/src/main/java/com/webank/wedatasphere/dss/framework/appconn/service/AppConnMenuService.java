package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.appconn.dao.AppConnMapper;
import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkspaceMenuAppconnDAO;
import com.webank.wedatasphere.dss.framework.appconn.dao.DssWorkspaceMenuDAO;
import com.webank.wedatasphere.dss.framework.appconn.entity.AppConnMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Author: xlinliu
 */

@Service
public class AppConnMenuService {

    @Autowired
    private AppConnMapper appConnMapper;

    @Autowired
    private DssWorkspaceMenuAppconnDAO menuAppconnDao;
    @Autowired
    private DssWorkspaceMenuDAO menuDAO;

    @Transactional
    public AppConnMenu saveMenu(AppConnMenu menu)  {
        if (menu.getAppconnId() == null || appConnMapper.getAppConnBeanById(menu.getAppconnId().longValue()) == null) {
            throw new IllegalArgumentException("关联的appconnId不能为空且必须存在");
        }

        if (menu.getMenuId() == null || menuDAO.findByPrimaryKey(menu.getMenuId()) == null) {
            throw new IllegalArgumentException("菜单分类不存在,请指定正确的menuId");
        }

        if (menu.getIsActive() == null || (menu.getIsActive() != 0 && menu.getIsActive() != 1)) {
            throw new IllegalArgumentException("isActive不能为空且只能是0或1");
        }

        if (menu.getTitleCn() == null || menu.getTitleCn().length() > 64) {
            throw new IllegalArgumentException("titleCn不能为空且长度不超过64");
        }

        if (menu.getTitleEn() == null || menu.getTitleEn().length() > 128) {
            throw new IllegalArgumentException("titleEn不能为空且长度不超过128");
        }

        // 判断是否是新建菜单还是更新菜单
        if (menu.getId() == null) {
            menuAppconnDao.insert(menu);
        } else {
            if(menuAppconnDao.findByPrimaryKey(menu.getId()) == null) {
                throw new IllegalArgumentException("菜单不存在,无法更新");
            }

            menuAppconnDao.update(menu);
        }

        return menu;
    }


    /**
     * 通过appconnId查询顶部菜单
     */
    public List<AppConnMenu> getMenusByAppconnId(Integer appconnId)  {
        if(appconnId == null || appConnMapper.getAppConnBeanById(appconnId.longValue()) == null) {
            throw new IllegalArgumentException("关联的appconnId不能为空且必须存在");
        }
        return menuAppconnDao.findByAppconnId(appconnId);
    }

    /**
     * 根据appconnId删除顶部菜单
     */
    @Transactional
    public void deleteMenusByAppconnId(Integer appconnId)   {
        if(appconnId == null ) {
            throw new IllegalArgumentException("关联的appconnId不能为空");
        }
        int rowsDeleted = menuAppconnDao.deleteByAppconnId(appconnId);
        if (rowsDeleted <= 0) {
            throw new DSSRuntimeException( "删除失败，没有找到匹配的菜单");
        }
    }
}