package com.webank.wedatasphere.dss.datamap.datamap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

/**
 * @author: jinyangrao on 2021/1/26
 */
public class DMUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DMUtils.class);


    private static int GB = 1024 * 1024 * 1024;//GB
    private static int MB = 1024 * 1024;//MB
    private static int KB = 1024;//KB

    private static DecimalFormat df = new DecimalFormat("0.00");

    // dm return byteSize now, we need to convert it to the appropriate representation.
    public static String sizeTranslator(String byteSizeStr) {
        String realSize;

        long byteSize = 0;
        try {
            byteSize = Long.parseLong(byteSizeStr);
            if(byteSize < 0) {
                byteSize = 0;
            }
        } catch (NumberFormatException nfe) {
            LOG.warn("The format of byte size returned by DM is illega: {}", byteSizeStr);
            return byteSizeStr;
        }

        if (byteSize >= GB) {
            realSize = df.format(byteSize / (double) GB) + "GB";
        } else if (byteSize >= MB) {
            realSize = df.format(byteSize / (double) MB) + "MB";
        } else if (byteSize >= KB) {
            realSize = df.format(byteSize / (double) KB) + "KB";;
        } else {
            realSize = byteSize + "B";
        }
        return realSize;
    }
}
