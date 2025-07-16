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
package com.webank.wedatasphere.dss.framework.admin.service;

import com.webank.wedatasphere.dss.framework.admin.pojo.entity.DssAdminUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;

import java.util.List;

public interface DssAdminUserService extends IService<DssAdminUser> {

    String checkUserNameUnique(String username);

    String checkPhoneUnique(DssAdminUser user);

    String checkEmailUnique(DssAdminUser user);

    void insertOrUpdateUser(String username, Workspace workspace);

    void insertIfNotExist(String username, Workspace workspace);

    int insertUser(DssAdminUser user, Workspace workspace);

    List<DssAdminUser> selectUserList(DssAdminUser user);

    DssAdminUser selectUserById(Long userId);

    DssAdminUser selectUserByName(String username);

    int updateUser(DssAdminUser user, Workspace workspace);

    List<String> getAllUsername();

    void deleteUser(String userName);
}
