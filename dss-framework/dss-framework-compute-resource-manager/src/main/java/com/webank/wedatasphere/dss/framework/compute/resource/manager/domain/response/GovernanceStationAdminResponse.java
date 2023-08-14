package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response;

public class GovernanceStationAdminResponse {
    private int status;
    private String message;
    private Data data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
    public static class Data{
        private Boolean historyAdmin=false;

        private String errorMsgTip;

        private Boolean admin=false;

        private Object solution;

        public Boolean getAdmin(){
            return this.admin;
        }

        public void setAdmin(boolean admin){
            this.admin = admin;
        }

        public String getErrorMsgTip(){
            return this.errorMsgTip;
        }

        public void setErrorMsgTip(String errorMsgTip){
            this.errorMsgTip = errorMsgTip;
        }

        public Boolean getHistoryAdmin(){
            return this.getHistoryAdmin();
        }

        public void setHistoryAdmin(boolean historyAdmin){
            this.historyAdmin = historyAdmin;
        }

        public Object getSolution(){
            return this.solution;
        }

        public void setSolution(Object solution){
            this.solution = solution;
        }
    }
}
