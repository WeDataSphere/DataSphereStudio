package com.webank.wedatasphere.dss.datamap.constant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public enum UsageHeatTypeEnum {
    // 最近3个月访问
    VISITED_IN_THREE_MONTHS("VISITED_IN_THREE_MONTHS", -3,"最近3个月访问"),
    // 最近6个月访问
    VISITED_IN_SIX_MONTHS("VISITED_IN_SIX_MONTHS", -6,"最近6个月访问"),
    // 最近12个月访问
    VISITED_IN_TWELVE_MONTHS("VISITED_IN_TWELVE_MONTHS", -12,"最近12个月访问"),

    // 3个月未访问
    NOT_VISITED_IN_THREE_MONTHS("NOT_VISITED_IN_THREE_MONTHS", -3,"最近3个月未访问"),
    // 6个月未访问
    NOT_VISITED_IN_SIX_MONTHS("NOT_VISITED_IN_SIX_MONTHS", -6,"6个月未访问"),
    // 12个月未访问
    NOT_VISITED_IN_TWELVE_MONTHS("NOT_VISITED_IN_TWELVE_MONTHS", -12,"12个月未访问"),

    ;


    // 热度类型
    private String type;

    private int num;

    private String desc;

    // 热度处理开始的时间戳
    private Long accessStartTime;

    // 热度处理的结束的时间戳
    private Long accessEndTime;

    private String startTime;

    private String endTime;

    UsageHeatTypeEnum(String type, int num,String desc) {
        this.type = type;
        this.num = num;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public int getNum() {
        return num;
    }


    public Long getAccessStartTime() {
        return accessStartTime;
    }

    public void setAccessStartTime(Long accessStartTime) {
        this.accessStartTime = accessStartTime;
    }

    public Long getAccessEndTime() {
        return accessEndTime;
    }

    public void setAccessEndTime(Long accessEndTime) {
        this.accessEndTime = accessEndTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static UsageHeatTypeEnum getUsageHeatTypeEnumByType(String usageHeatType) throws ParseException {

        UsageHeatTypeEnum usageHeatTypeEnum = Arrays.stream(UsageHeatTypeEnum.values())
                .filter(value -> value.getType().equalsIgnoreCase(usageHeatType)).findFirst().orElse(null);

        if (usageHeatTypeEnum != null) {
            parseTimeRange(usageHeatTypeEnum);
        }

        return  usageHeatTypeEnum;
    }


    private static void  parseTimeRange(UsageHeatTypeEnum usageHeatTypeEnum) throws  ParseException{


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, usageHeatTypeEnum.getNum());

        List<String> notVisitedInType = Arrays.asList(NOT_VISITED_IN_THREE_MONTHS.getType(),NOT_VISITED_IN_SIX_MONTHS.getType()
                ,NOT_VISITED_IN_TWELVE_MONTHS.getType());

        if(notVisitedInType.contains(usageHeatTypeEnum.getType())){
            // 未访问的, 需要 -1天 ,DMS 接口 accessStartTime、 accessEndTime 左右都包括
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            String end = simpleDateFormat.format(calendar.getTime()) + " 23:59:59";
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            usageHeatTypeEnum.setEndTime(end);
            usageHeatTypeEnum.setAccessEndTime(simpleDateFormat.parse(end).getTime() / 1000);

        }else{
            // 已访问
            String end = simpleDateFormat.format(date) + " 23:59:59";
            String start = simpleDateFormat.format(calendar.getTime()) + " 00:00:00";
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            usageHeatTypeEnum.setAccessEndTime(simpleDateFormat.parse(end).getTime() /1000);
            usageHeatTypeEnum.setAccessStartTime(simpleDateFormat.parse(start).getTime() /1000);
            usageHeatTypeEnum.setStartTime(start);
            usageHeatTypeEnum.setEndTime(end);
        }

    }


}
