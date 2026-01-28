package com.webank.wedatasphere.dss.mide.dao;

import com.webank.wedatasphere.dss.mide.entity.Demands;
import java.util.List;

public interface DemandsMapper {
    int delete(Long id);

    int insert(Demands demands);

    int insertDynamic(Demands demands);

    int updateDynamic(Demands demands);

    int update(Demands demands);

    Demands selectById(Long id);

    List<Demands> listDemandsByProjectId(Long projectId);

    /*List<Demands> findPageWithResult(DemandsDTO demandsDTO);

    Integer findPageWithCount(DemandsDTO demandsDTO);*/
}
