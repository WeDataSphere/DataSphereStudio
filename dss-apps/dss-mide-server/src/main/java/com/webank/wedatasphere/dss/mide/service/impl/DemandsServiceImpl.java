package com.webank.wedatasphere.dss.mide.service.impl;

import com.webank.wedatasphere.dss.mide.dao.DemandsMapper;
import com.webank.wedatasphere.dss.mide.entity.Demands;
import com.webank.wedatasphere.dss.mide.service.DemandsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemandsServiceImpl implements DemandsService {

    @Autowired
    private DemandsMapper demandsMapper;

    private static final Logger LOG = LoggerFactory.getLogger(DemandsServiceImpl.class);

    @Override
    public int delete(Long id) {
        if (id > 0) {
            LOG.info("To delete demands{}", id);
            demandsMapper.delete(id);
        }
        return 0;
    }

    @Override
    public Demands save(Demands demands) {
        if (null != demands) {
            LOG.info("Start to insert demands{}", demands);
            demandsMapper.insert(demands);
            LOG.info("Finished to insert demands{}", demands);
        }
        return demands;
    }

    @Override
    public Demands update(Demands demands) {
        if (null != demands) {
            LOG.info("To update demands{}", demands);
            demandsMapper.updateDynamic(demands);
        }
        return demands;
    }

    @Override
    public Demands selectById(Long id) {
        return demandsMapper.selectById(id);
    }

    @Override
    public List<Demands> listDemandsByProjectId(Long projectId) {
        return demandsMapper.listDemandsByProjectId(projectId);
    }
}
