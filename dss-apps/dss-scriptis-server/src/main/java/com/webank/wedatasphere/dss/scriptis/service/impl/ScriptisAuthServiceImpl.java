package com.webank.wedatasphere.dss.scriptis.service.impl;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.common.conf.DSSCommonConf;
import com.webank.wedatasphere.dss.common.utils.GlobalLimitsUtils;
import com.webank.wedatasphere.dss.scriptis.dao.ScriptisAuthMapper;
import com.webank.wedatasphere.dss.scriptis.pojo.entity.DssUserLimit;
import com.webank.wedatasphere.dss.scriptis.service.ScriptisAuthService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.server.conf.ServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptisAuthServiceImpl implements ScriptisAuthService {

    @Autowired
    private ScriptisAuthMapper authMapper;

    @Autowired
    private static final String  downloadCount=  "downloadCount";

    @Override
    public Map<String, Object> getGlobalLimits(String username) {
        return GlobalLimitsUtils.getAllGlobalLimits();
    }

    public Map<String, Object> getUserLimits(String username, String limitName) {
        List<DssUserLimit> userLimits = authMapper.getUserLimits(limitName);
        Map<String, Object> res = new HashMap<>();
        userLimits.forEach(dssUserLimit -> {
            String key = dssUserLimit.getLimitName();
            Object value = StringUtils.isNumeric(dssUserLimit.getValue()) ? Integer.parseInt(dssUserLimit.getValue()) : dssUserLimit.getValue();
            Object retVal = StringUtils.contains(dssUserLimit.getUserName(),username) ? value : null;
            res.put(key,retVal);
        });

        // 数据库中未配置用户下载限制,且是cib数据操作间的用户,则返回下载限制条数
        if (res.get(downloadCount) == null && ServerConfiguration.LINKIE_USERNAME_SUFFIX_ENABLE()){

            // _cfor_f后缀则是cib数据操作间
            String suffix = DSSCommonConf.DSS_USER_NAME_SUFFIX.getValue();

            // cib数据操作间的用户
            if(StringUtils.endsWithIgnoreCase(username,suffix)){
                Integer limit = DSSCommonConf.DSS_SCRIPTS_DOWNLOAD_LIMIT.getValue();
                res.put(downloadCount,limit);
            }
        }

        return  res;

    }
}
