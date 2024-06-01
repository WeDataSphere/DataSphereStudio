package com.webank.wedatasphere.dss.framework.appconn.dao;

import org.apache.ibatis.annotations.Mapper;
import com.webank.wedatasphere.dss.framework.appconn.entity.MenuType;

import java.util.List;

/**
 * 菜单类型表(MenuType)表数据库访问层
 */
@Mapper
public interface DssWorkspaceMenuDAO{

    /**
     * 新增
     *
     * @param menuType
     */
    int insert(MenuType menuType);

    /**
     * 更新
     *
     * @param menuType
     */
    int update(MenuType menuType);

    /**
    * 根据主键查询
    * @return {@link MenuType}
    */
    MenuType findByPrimaryKey(Integer id);

    /**
     * 根据主键查询
     * @return {@link MenuType}
     */
    List<MenuType> getAllMenu();

    /**
    * 根据主键删除
    */
    int deleteByPrimaryKey(Integer id);

}