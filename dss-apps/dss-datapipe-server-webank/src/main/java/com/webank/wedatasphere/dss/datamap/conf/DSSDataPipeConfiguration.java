package com.webank.wedatasphere.dss.datamap.conf;

import com.webank.wedatasphere.dss.datamap.datamap.transferor.TablesOwnerTransferor;
import com.webank.wedatasphere.dss.datamap.datamap.transferor.CustomTransferor;
import org.apache.linkis.common.conf.CommonVars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DSSDataPipeConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DSSDataPipeConfiguration.class);

    private static final TablesOwnerTransferor TRANSFEROR = createTransferor();

    private static TablesOwnerTransferor createTransferor() {

        String transferorClassName = DataMapConnConf.TRANSFEROR_CLASS();

        try {
            logger.info("Use user config Transferor {}", transferorClassName);
            return (TablesOwnerTransferor) DSSDataPipeConfiguration.class.getClassLoader().loadClass(transferorClassName).newInstance();
        } catch (Exception e) {
            logger.warn("Use CustomTransferor {}", transferorClassName, e);
            return new CustomTransferor();
        }

    }

    public static TablesOwnerTransferor getTransferor() {
        return TRANSFEROR;
    }

    public static String DATASET_SENSITIVE_SCAN_BG_LIST =CommonVars.apply("wds.dss.dataset.sensitive.scan.bg.list",
            "企业及机构金融事业群").getValue();


}
