package com.webank.wedatasphere.dss.framework.project.enums;

import java.util.Arrays;

/**
 * 项目操作记录状态枚举
 */
public enum ProjectOperateRecordStatusEnum {
    /**
     * 初始化状态
     */
    INIT(2,"初始化","Init"),
    /**
     * 成功
     */
    SUCCESS(0 ,"成功","Success"),
    /**
     * 失败
     */
    FAILED(-1, "失败","Failed"),
    /**
     * 执行中
     */
    RUNNING(1, "执行中","Running"),
    ;

    ProjectOperateRecordStatusEnum(int code, String caption,String captionEn) {
        this.code = code;
        this.caption = caption;
        this.captionEn=captionEn;
    }

    /**
     * 状态代码
     */
    private int code;
    /**
     * 状态中文显示值
     */
    private String caption;
    /**
     * 状态英文显示值
     */
    private String captionEn;

    public int getCode() {
        return code;
    }

    public String getCaption() {
        return caption;
    }

    public String getCaptionEn() {
        return captionEn;
    }

    /**
     * 根据code获取对应枚举值
     * @param code 枚举值的code
     */
    public static ProjectOperateRecordStatusEnum getByCode(int code){
        return Arrays.stream(values()).filter(e -> e.getCode() == code).findAny().orElse(null);
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
