package com.webank.wedatasphere.dss;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.webank.wedatasphere.dss.bean.DWSFlow;
import com.webank.wedatasphere.dss.bean.DWSProject;
import com.webank.wedatasphere.dss.bean.flowcomponent.*;
import com.webank.wedatasphere.dss.service.DSSFlowExportService;
import com.webank.wedatasphere.dss.service.OrcMetaGenerator;
import com.webank.wedatasphere.dss.service.WorkflowMetaGenerator;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;

/**
 * Hello world!
 *
 */
public class MigrateMain
{

    public static void main( String[] args ) throws Exception {
//        if(args.length!=3){
//            System.out.println("使用错误。请输入 [cookie]  [项目名]  [导出位置]");
//            return;
//        }

        System.out.println();


        String cookie = "bdp-user-ticket-id=gH5ybDg3I8q5T7maKMAWCe1cvjvAW/i3fHf6aLW48JU=";
        String host = "http://127.0.0.1:20817";
        DWSAPIClient client = new DWSAPIClient(host, cookie);
        String outputDir = "D:\\projects\\wedatasphere-dataspherestudio\\plugins\\dws-migrate-to-dss\\target";
        String projectName = "htest0525";
        DWSProject dwsProject= client.getAllProject().stream().filter(e -> projectName.equals(e.getProjectName())).findFirst().orElseGet(null);
        if(dwsProject==null){
            return;
        }

        DSSFlowExportService dssFlowExportService = new DSSFlowExportService(new OrcMetaGenerator(), new WorkflowMetaGenerator(), client, outputDir);

            dssFlowExportService.exportWholeProjectFlows(dwsProject.getProjectName(), dwsProject.getProjectTaxonomyID(), dwsProject.getProjectVersionID());



    }


}
