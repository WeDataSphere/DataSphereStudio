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

        // 数据库中未配置用户下载限制,且是联合分析环境,则判断用户后缀
        if (MapUtils.isEmpty(res) && ServerConfiguration.LINKIE_USERNAME_SUFFIX_ENABLE()){

            // _cfor_f后缀则是cib数据操作间,_c后缀是普通数据操作间
            List<String> suffixes = Lists.newArrayList(DSSCommonConf.DSS_USER_NAME_SUFFIX.getValue(),
                    ServerConfiguration.LINKIE_USERNAME_SUFFIX_NAME());

            // 用户满足后缀 则返回下载限制条数
            if(suffixes.stream().anyMatch(suffix -> StringUtils.endsWith(username,suffix))){

                Integer limit = DSSCommonConf.DSS_SCRIPTS_DOWNLOAD_LIMIT.getValue();
                res.put(limitName,limit);
            }
        }

        return  res;

    }
}
