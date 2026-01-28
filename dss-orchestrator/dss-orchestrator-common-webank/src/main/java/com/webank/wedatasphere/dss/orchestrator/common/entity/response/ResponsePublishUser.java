package com.webank.wedatasphere.dss.orchestrator.common.entity.response;


import java.io.Serializable;
import java.util.List;

/**
 * @author v_wbzwchen
 */
public class ResponsePublishUser implements Serializable {

    private List<String> userList;

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }
}
