package com.webank.wedatasphere.dss.data.api.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedatasphere.dss.data.api.server.dao.DataSourceMapper;
import com.webank.wedatasphere.dss.data.api.server.entity.DataSource;
import com.webank.wedatasphere.dss.data.api.server.restful.CryptoUtils;
import com.webank.wedatasphere.dss.data.api.server.service.ApiDataSourceService;
import com.webank.wedatasphere.dss.data.api.server.util.JdbcUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ApiDataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource> implements ApiDataSourceService {
    @Resource
    private DataSourceMapper dataSourceMapper;
    private Logger logger;

    @Override
    public DataSource selectById(Integer datasourceId) {

        return dataSourceMapper.selectById(datasourceId);
    }

    @Override
    public List<DataSource> getAllConnections(Integer workspaceId, String type) {
        DataSource dataSource = new DataSource();
        dataSource.setWorkspaceId(workspaceId);
        dataSource.setType(type);
        return dataSourceMapper.selectByTypeAndWorkspaceId(dataSource);
    }

    @Override
    public List<DataSource> getAvailableConns(List<DataSource> allConnections) {

        Connection connection = null;
        List<DataSource> availableConns = new ArrayList<DataSource>();
        for (DataSource datasource : allConnections) {
            try {
                datasource.setPwd(CryptoUtils.string2Object(datasource.getPwd()).toString());
                System.out.println(datasource.getPwd());
                connection = JdbcUtil.getConnection(datasource);
                datasource.setPwd("***");
                availableConns.add(datasource);
            } catch (Exception e) {
                logger.warning(e.getMessage());
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        logger.info(e.getMessage());
                    }
                }
            }

        }
        return availableConns;

    }

    @Override
    public void addDatasource(DataSource dataSource) {
        dataSourceMapper.addDatasource(dataSource);
    }
}
