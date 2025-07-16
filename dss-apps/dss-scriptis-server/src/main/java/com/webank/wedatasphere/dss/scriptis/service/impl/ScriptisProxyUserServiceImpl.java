/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.scriptis.service.impl;

import com.webank.wedatasphere.dss.common.entity.DSSWorkspace;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.framework.proxy.pojo.entity.DssProxyUser;
import com.webank.wedatasphere.dss.framework.proxy.service.DssProxyUserService;
import com.webank.wedatasphere.dss.scriptis.dao.ScriptisProxyUserMapper;
import com.webank.wedatasphere.dss.scriptis.pojo.entity.ScriptisProxyUser;
import com.webank.wedatasphere.dss.scriptis.service.ScriptisProxyUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ScriptisProxyUserServiceImpl implements DssProxyUserService, ScriptisProxyUserService {

    @Resource
    private ScriptisProxyUserMapper dssProxyUserMapper;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public List<DssProxyUser> selectProxyUserList(String userName, DSSWorkspace workspace) {
        String expireTime = simpleDateFormat.format(new Date());
        return new ArrayList<>(dssProxyUserMapper.selectProxyUserList(userName,expireTime));
    }

    @Override
    public void revokeProxyUser(String userName, String[] proxyUserNames) {
        dssProxyUserMapper.deleteProxyUser(userName, proxyUserNames);
    }

    @Override
    public int insertProxyUser(ScriptisProxyUser dssProxyUser) throws  DSSErrorException {

        // 查看用户是否有相同的代理用户
        ScriptisProxyUser scriptisProxyUser= dssProxyUserMapper.selectProxyUserByUser(dssProxyUser.getUserName(),dssProxyUser.getProxyUserName());

        if(StringUtils.isEmpty(dssProxyUser.getExpireTime())){
            dssProxyUser.setExpireTime("2099-12-31 23:59:59");
        }

        if(scriptisProxyUser != null){

            scriptisProxyUser.setExpireTime(dssProxyUser.getExpireTime());
            // 修改过期时间
            return dssProxyUserMapper.updateByUser(scriptisProxyUser);
        }


        return dssProxyUserMapper.insertUser(dssProxyUser);
    }

}
