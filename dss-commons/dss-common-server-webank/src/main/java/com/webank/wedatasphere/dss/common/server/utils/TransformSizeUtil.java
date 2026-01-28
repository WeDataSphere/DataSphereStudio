package com.webank.wedatasphere.dss.common.server.utils;

import java.util.Locale;

public class TransformSizeUtil {


    public static long parseByteString(String str){
        str = str.trim();
        double numberValue = Double.parseDouble(str.substring(0, str.length() - 2).trim());
        if(str.endsWith("TB")||str.endsWith("tb")){
            return (long) numberValue *(1L << 40);
        }
        else if(str.endsWith("GB")||str.endsWith("gb")){
            return (long) numberValue *(1L << 30);
        }else if(str.endsWith("MB")||str.endsWith("mb")){
            return (long) numberValue *(1L << 20);
        }else if(str.endsWith("KB")||str.endsWith("kb")){
            return (long) numberValue *(1L << 10);
        }else{
            return (long) numberValue;
        }
    }

    public static String bytesToString(long size) {
        long TB = 1L << 40;
        long GB = 1L << 30;
        long MB = 1L << 20;
        long KB = 1L << 10;

        double value;
        String unit;
        if (size >= 2 * TB || -2 * TB >= size) {
            value = size * 1f / TB;
            unit = "TB";
        } else if (size >= 2 * GB || -2 * GB >= size) {
            value = size * 1f / GB;
            unit = "GB";
        } else if (size >= 2 * MB || -2 * MB >= size) {
            value = size * 1f / MB;
            unit = "MB";
        } else if (size >= 2 * KB || -2 * KB >= size) {
            value = size * 1f / KB;
            unit = "KB";
        } else {
            value = size * 1f;
            unit = "B";
        }
        return String.format(Locale.US, "%.1f %s", value, unit);
    }

}
