package com.webank.wedatasphere.dss.bean.flowcomponent;

import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import org.springframework.util.StringUtils;
import static  com.webank.wedatasphere.dss.GsonUtils.getString;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dss的dataChecker节点
 * Author: xlinliu
 * Date: 2023/12/4
 */
public class DSSDataCheckerNode extends DSSCommonNode {

    private static Map<String, Object> getConfiguration() {
        Map<String, Object> configuration = new HashMap<>();
        //默认初始化params.configuration
        Map<String, String> startup = new HashMap<>(1);
        startup.put("ReuseEngine", "true");
        configuration.put("special", new HashMap<>());
        configuration.put("runtime", new HashMap<>());
        configuration.put("startup", startup);
        return configuration;
    }

    public static DSSDataCheckerNode fromDwsNode(JsonObject dwsNode, long modifyTime, String modifyUser){
        DSSDataCheckerNode dssNode = DSSCommonUtils.COMMON_GSON.fromJson(dwsNode, DSSDataCheckerNode.class);
        Map<String, Object> configuration = getConfiguration();
        dssNode.getParams().put("configuration", configuration);
        dssNode.setModifyTime(modifyTime);
        dssNode.setModifyUser(modifyUser);
        dssNode.setJobType(convertType(dssNode.getJobType()));
        JsonObject jobParams=dwsNode.getAsJsonObject("jobContent").getAsJsonObject("jobParams");
        String sourceType = getString(jobParams,"sourceType");
        if(!StringUtils.isEmpty(sourceType)){
            if(sourceType.equalsIgnoreCase("job")){
                sourceType="hivedb";
            } else if (sourceType.equalsIgnoreCase("bdp")) {
                sourceType = "maskdb";
            }else {
                throw new DSSRuntimeException("unknown datachecker source.type:"+sourceType);
            }
        }
        String dataObject = getString(jobParams,"dataObject");
        String waitTime = getString(jobParams,"waitTime");
//        String queryFrequency =getString( jobParams,"queryFrequency");
//        String timeScape = getString(jobParams,"timeScape");
        String jobDesc = getString(jobParams,"jobDesc");
        if (!StringUtils.isEmpty(jobDesc)) {
            String[] rows = jobDesc.contains("\n") ? jobDesc.split("\n") : jobDesc.split(";");
            jobDesc = Arrays.stream(rows)
                            .map(row -> row.trim().startsWith("data.object") && row.contains("=") ?
                                    row.replaceFirst("data\\.object", "check.object") :
                                    row
                    ).collect(Collectors.joining("\n"));
        }


        Map<String, String> runtimeMap = new HashMap<>();
        runtimeMap.put("source.type", sourceType);
        runtimeMap.put("check.object", dataObject);
        runtimeMap.put("max.check.hours", waitTime);
        runtimeMap.put("job.desc", jobDesc);
        configuration.put("runtime", runtimeMap);

        return dssNode;
    }
}
