package com.webank.wedatasphere.dss.framework.project.enums;

import java.util.Arrays;

public enum ProjectOperateTypeEnum {
    /**
     * 创建工程
     */
    CREATE_PROJECT("项目创建","Create Project",0),
    /**
     * 导入编排
     */
    IMPORT("导入","Import",1),
    /**
     * 导出编排
     */
    EXPORT("导出","Export",2),
    /**
     * 发布编排到调度系统
     */
    PUBLISH("发布","Publish",3),
    /**
     * 删除编排，表示在DSS里把编排删除掉
     */
    DELETE_ORCHESTRATOR("删除工作流","Delete Orchestrator",4),
    /**
     * 删除编排调度，只是删除调度信息
     */
    DELETE_SCHEDULE("删除调度","Delete Schedule",5),
    /**
     * 禁用调度
     */
    DISABLE_SCHEDULE("禁用","Disable Schedule",6),
    /**
     * 项目授权
     */
    PROJECT_GRANT("项目授权","Project Grant",7),

    /**
     * 删除工程
     */
    DELETE_PROJECT("项目删除","Create Project",8)
    ;

    /**
     * 中文显示值
     */
    private String caption;
    /**
     * 英文显示值
     */
    private String captionEn;
    /**
     * 枚举标号，唯一表示一个枚举值
     */
    private int code;

    ProjectOperateTypeEnum(String caption, String captionEn, int code) {
        this.caption = caption;
        this.captionEn = captionEn;
        this.code = code;
    }

    public String getCaption() {
        return caption;
    }

    public String getCaptionEn() {
        return captionEn;
    }

    public int getCode() {
        return code;
    }

    /**
     * 通过code获取枚举值
     * @param code code值
     * @return 操作类型枚举
     */
    public static ProjectOperateTypeEnum getByCode(int code){
        return Arrays.stream(values()).filter(e->code==e.getCode()).findAny().orElse(null);
    }

    public VO toVO(){
        return new VO(code,caption,captionEn);
    }

    /**
     * 枚举类的VO类
     */
    public static class VO {
        private int code;
        private String caption;
        private String captionEn;

        public VO(int code, String caption, String captionEn) {
            this.code = code;
            this.caption = caption;
            this.captionEn = captionEn;
        }

        public int getCode() {
            return code;
        }

        public String getCaption() {
            return caption;
        }

        public String getCaptionEn() {
            return captionEn;
        }
    }
}
