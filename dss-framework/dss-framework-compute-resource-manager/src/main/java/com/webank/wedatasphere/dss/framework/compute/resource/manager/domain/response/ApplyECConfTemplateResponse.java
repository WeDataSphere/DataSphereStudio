package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response;

import java.util.List;

/**
 * Author: xlinliu
 * Date: 2023/6/29
 */
public class ApplyECConfTemplateResponse {
private Item success;
private Item error;

    public Item getSuccess() {
        return success;
    }

    public void setSuccess(Item success) {
        this.success = success;
    }

    public Item getError() {
        return error;
    }

    public void setError(Item error) {
        this.error = error;
    }

    public static class Item{
        private  Integer num;
        private List<User> infoList;

        public Integer getNum() {
            return num;
        }

        public void setNum(Integer num) {
            this.num = num;
        }

        public List<User> getInfoList() {
            return infoList;
        }

        public void setInfoList(List<User> infoList) {
            this.infoList = infoList;
        }
    }

    public  static class User{
        private String user;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }
}
