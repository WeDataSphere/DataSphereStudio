package com.webank.wedatasphere.dss.mide;

import com.webank.wedatasphere.dss.common.utils.DSSMainHelper;
import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.server.utils.LinkisMainHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSSMIDEServerApp {

    private static final Logger LOG = LoggerFactory.getLogger(DSSMIDEServerApp.class);


    public static void main(String[] args) throws ReflectiveOperationException {
        String serviceName = System.getProperty("serviceName","dss-mide-server");
        DSSMainHelper.formatPropertyFiles(serviceName);
        String[] allArgs = (String[]) ArrayUtils.addAll(args, DSSMainHelper.getExtraSpringOptions());
        String argsString = StringUtils.join(allArgs, "\n");
        String startLog = String.format("Ready to start %s with args: %s.", serviceName, argsString);
        LOG.info(startLog);
        DataWorkCloudApplication.main(allArgs);
    }
}
