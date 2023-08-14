package com.webank.wedatasphere.dss.workflow.utils;

public class TimeFormater {

    Integer mi = 60; // m
    Integer hh = 60 * 60; // h
    Integer dd = 60 * 60 * 24; // d

    public String format(long originalSecond) {
        Long day = originalSecond / dd;
        Long hour = (originalSecond - day * dd) / hh;
        Long minute = (originalSecond - day * dd - hour * hh) / mi;
        Long second = originalSecond - day * dd - hour * hh - minute * mi;

        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day + "d");
        }
        if (hour > 0) {
            sb.append(hour + "h");
        }
        if (minute > 0) {
            sb.append(minute + "m");
        }
        if (second > 0) {
            sb.append(second + "s");
        }
        return sb.toString();
    }
}
