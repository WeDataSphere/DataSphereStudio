package com.webank.wedatasphere.dss.framework.appconn.dao;

import org.apache.ibatis.annotations.Mapper;
import com.webank.wedatasphere.dss.framework.appconn.entity.AppConnMenu;

import java.util.List;

/**
 * appconn菜单mapper
 */
@Mapper
public interface DssWorkspaceMenuAppconnDAO{

    /**
     * 新增
     *
     * @param appConnMenu
     */
    int insert(AppConnMenu appConnMenu);

    /**
     * 更新
     *
     * @param appConnMenu
     */
    int update(AppConnMenu appConnMenu);

    /**
    * 根据关联的appconn查询
    * @return {@link AppConnMenu}
    */
    List<AppConnMenu> findByAppconnId(Integer appconnId);


    /**
     * 根据主键查询
     * @return {@link AppConnMenu}
     */
    AppConnMenu findByPrimaryKey(Integer id);

    /**
    * 根据主键删除
    */
    int deleteByAppconnId(Integer appconnId);

}