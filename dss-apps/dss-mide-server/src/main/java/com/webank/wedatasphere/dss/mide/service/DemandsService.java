package com.webank.wedatasphere.dss.mide.service;

import com.webank.wedatasphere.dss.mide.entity.Demands;

import java.util.List;

public interface DemandsService {

    int delete(Long id);

    Demands save(Demands demands);

    Demands update(Demands demands);

    Demands selectById(Long id);

    List<Demands> listDemandsByProjectId(Long projectId);

}
