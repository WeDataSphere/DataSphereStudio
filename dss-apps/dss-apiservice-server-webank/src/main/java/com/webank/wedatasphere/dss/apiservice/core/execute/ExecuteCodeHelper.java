package com.webank.wedatasphere.dss.apiservice.core.execute;

import com.webank.wedatasphere.dss.apiservice.core.action.ApiServiceGetAction;
import com.webank.wedatasphere.dss.apiservice.core.action.ResultSetDownloadAction;
import com.webank.wedatasphere.dss.apiservice.core.action.ResultWorkspaceIds;
import com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiExecuteException;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.nodes.TTableList;
import gudusoft.gsqlparser.stmt.TUseDatabase;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.governance.common.entity.task.RequestPersistTask;
import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.request.ResultSetAction;
import org.apache.linkis.ujes.client.request.ResultSetListAction;
import org.apache.linkis.ujes.client.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration.DOWNLOAD_MAX_SIZE;
import static com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration.RESULT_ROW_MAX_SIZE;

/**
 * created by cooperyang on 2020/8/26
 * Description:
 */
public class ExecuteCodeHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCodeHelper.class);

    private static final String RELEASE_SCALA_HEADER = "import org.apache.spark.sql.DataFrame\n" +
            "import org.apache.linkis.engineplugin.spark.metadata.MetaDataInfoTool\n";

    private static final String RELEASE_SCALA_TEMPLATE_BATCH =
            "val %s = %s\n\n" +
            "val %s = sqlContext.sql(%s)\n" +
            "val %s = MetaDataInfoTool.getMetaDataInfo(sqlContext, %s, %s.asInstanceOf[DataFrame])\n";

    private static final String EXECUTE_SCALA_TEMPLATE_BATCH =
            "val %s = %s\n" +
            "val %s = sqlContext.sql(%s)\n" +
            "val %s = MetaDataInfoTool.getMetaDataInfo(sqlContext, %s, %s.asInstanceOf[DataFrame])\n" +
            "if (\"%s\".contains(%s.replaceAll(\"[\\\\[\\\\]]\", \"\"))){\n" +
            "    showDF(sparkContext, jobGroup.toString, %s,\"\","+ RESULT_ROW_MAX_SIZE.getValue() +", engineExecutionContextFactory.getEngineExecutionContext)\n" +
            "} else {\n" +
            "    throw new Exception(\"库表名和发布时的不一致,不能进行执行\")\n" +
            "}\n\n";

    private static final String RELEASE_SCALA_METADATA_HEADER = "import org.apache.spark.sql.DataFrame\n" +
            "import org.apache.linkis.engineplugin.spark.metadata.MetaDataInfoTool\n" +
            "import scala.collection.mutable.ListBuffer\n\n" +
            "val allInputTables = ListBuffer[String]()\n";

    private static final String RELEASE_SCALA_METADATA_TEMPLATE =
            "\n" +
                    "val combinedString = s\"[${allInputTables.mkString(\", \")}]\"\n" +
                    "println(combinedString)";

    private static final String SCALA_MARK = "\"\"\"";

    public static String packageCodeToRelease(String executeCode){
        StringBuilder sb = new StringBuilder();
        sb.append(RELEASE_SCALA_METADATA_HEADER);
        // 去掉分割后的空字符
        List<String> nonEmptySqls = removeEmptyStr(executeCode);
        for (int i = 0; i < nonEmptySqls.size(); i++) {
            String allInputTables = String.format("allInputTables += %s.replaceAll(\"[\\\\[\\\\]]\", \"\")\n\n", "inputTables" + i);
            sb.append(String.format(RELEASE_SCALA_TEMPLATE_BATCH, "sql" + i, SCALA_MARK + nonEmptySqls.get(i) + SCALA_MARK, "df" + i, "sql" + i, "inputTables" + i, "sql" + i, "df" + i));
            sb.append(allInputTables);
        }
        sb.append(RELEASE_SCALA_METADATA_TEMPLATE);
        return sb.toString();
    }

//    public static String packageCodeToExecute(String executeCode, String metaDataInfo){
//        String retStr = String.format(EXECUTE_SCALA_TEMPLATE, SCALA_MARK + executeCode + SCALA_MARK, SCALA_MARK + metaDataInfo + SCALA_MARK);
//        LOGGER.info("execute scala code is {}", retStr);
//        return retStr;
//    }

    public static String packageCodeToExecute(String executeCode, String metaDataInfo){
        StringBuilder sb = new StringBuilder();
        sb.append(RELEASE_SCALA_HEADER);
        List<String> nonEmptySqls = removeEmptyStr(executeCode);
        for (int i = 0; i < nonEmptySqls.size(); i++) {
            sb.append(String.format(EXECUTE_SCALA_TEMPLATE_BATCH, "executeCode" + i, SCALA_MARK + nonEmptySqls.get(i) + SCALA_MARK, "df" + i, "executeCode" + i, "realMetaInfo" + i, "executeCode" + i, "df" + i, metaDataInfo.replaceAll("[\\[\\]]", ""),"realMetaInfo" + i,"df" + i));
        }
        return sb.toString();
    }

    public static String packageCodeToExplain(String executeCode){
        StringBuilder retStr = new StringBuilder();
        removeEmptyStr(executeCode).forEach(item ->{
            retStr.append("EXPLAIN DEPENDENCY ").append(item.trim()).append(";");
        });
        LOGGER.info("execute explain code is {}", retStr);
        return retStr.toString();
    }

    private static List<String> removeEmptyStr(String executeCode){
        return Arrays.stream(executeCode.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    public static Map<String,Object>  getMetaDataInfoByExecute(String user,
                                             String executeCode,
                                             Map<String, Object> params,
                                             String scriptPath) throws Exception {
        Map<String, String> props = new HashMap<>();
        Map<String,Object>  resultMap = new HashMap<>();
        UJESClient client = LinkisJobSubmit.getClient(props);
        ApiServiceExecuteJob job = new DefaultApiServiceJob();
        //sql代码封装成scala执行
        job.setCode(ExecuteCodeHelper.packageCodeToRelease(executeCode));
        job.setEngineType("spark");
        job.setRunType("scala");
        job.setUser(user);
        job.setParams(null);
        job.setRuntimeParams((Map<String,Object>)params.get("variable"));// pattern注入
        job.setScriptePath(scriptPath);
        JobExecuteResult jobExecuteResult = LinkisJobSubmit.execute(job,client, "IDE");
        job.setJobExecuteResult(jobExecuteResult);
        try {
            waitForComplete(job,client);
        } catch (ApiExecuteException e){
            LOGGER.error("Reason for failure: " + e.getMessage(),e);
            throw e;
        }catch (Exception e) {
            LOGGER.warn("Failed to execute job", e);
            String reason = getLog(job,client);
            LOGGER.error("Reason for failure: " + reason);
            throw new ApiExecuteException(800024,"数据服务SQL执行出错,请检查SQL后重新执行。"+e.getMessage());
        }
        int resultSize = getResultSize(job,client);
        for(int i =0; i < resultSize; i++){
            String result = getResult(job, i, ApiServiceConfiguration.RESULT_PRINT_SIZE.getValue().intValue(),client);
            LOGGER.info("The content of the " + (i + 1) + "th resultset is :"
                    +  result);
            resultMap.put(Integer.toString(i),result);

        }

        LOGGER.info("Finished to execute job");
        return  resultMap;
    }


    public static  void waitForComplete(ApiServiceExecuteJob job,UJESClient client) throws Exception {
        JobInfoResult jobInfo = client.getJobInfo(job.getJobExecuteResult());
        while (!jobInfo.isCompleted()) {
            LOGGER.info("Update Progress info:{}" , getProgress(job,client));
            LOGGER.info("<----linkis log ---->");
            Utils.sleepQuietly(ApiServiceConfiguration.LINKIS_JOB_REQUEST_STATUS_TIME.getValue(job.getJobProps()));
            jobInfo = client.getJobInfo(job.getJobExecuteResult());
        }
        if (!jobInfo.isSucceed()) {
            RequestPersistTask taskInfo = jobInfo.getRequestPersistTask();
            throw new ApiExecuteException(taskInfo.getErrCode(),taskInfo.getErrDesc());
        }
    }


    public static void cancel(ApiServiceExecuteJob job,UJESClient client) throws Exception {
        client.kill(job.getJobExecuteResult());
    }


    public static  double getProgress(ApiServiceExecuteJob job,UJESClient client) {
        return client.progress(job.getJobExecuteResult()).getProgress();
    }


    public static  Boolean isCompleted(ApiServiceExecuteJob job,UJESClient client) {
        return client.getJobInfo(job.getJobExecuteResult()).isCompleted();
    }

    public static  String getResult(ApiServiceExecuteJob job, int index, int maxSize,UJESClient client) {
        String resultContent = null;
        JobInfoResult jobInfo = client.getJobInfo(job.getJobExecuteResult());
        String[] resultSetList = jobInfo.getResultSetList(LinkisJobSubmit.getClient(job.getJobProps()));
        if (resultSetList != null && resultSetList.length > 0) {
            Object fileContent = client.resultSet(ResultSetAction.builder()
                    .setPath(resultSetList[index])
                    .setUser(job.getJobExecuteResult().getUser())
                    .setPageSize(maxSize).build()).getFileContent();
            if (fileContent instanceof ArrayList) {
                ArrayList<ArrayList<String>> resultSetRow = (ArrayList<ArrayList<String>>) fileContent;
                resultContent = StringUtils.join(resultSetRow.get(0), "\n");
            } else {
                resultContent = fileContent.toString();
            }
        }
        return resultContent;
    }


    public static  int getResultSize(ApiServiceExecuteJob job,UJESClient client) {
        JobInfoResult jobInfo = client.getJobInfo(job.getJobExecuteResult());
        if (jobInfo.isSucceed()) {
            String[] resultSetList = jobInfo.getResultSetList(LinkisJobSubmit.getClient(job.getJobProps()));
            if (resultSetList != null && resultSetList.length > 0) {
                return resultSetList.length;
            }
        }
        return 0;
    }


    public static String getLog(ApiServiceExecuteJob job, UJESClient client) {

        JobLogResult jobLogResult = client
                .log(job.getJobExecuteResult(),
                        0,
                         50);

        ArrayList<String> logArray = jobLogResult.getLog();

        if (logArray != null && logArray.size()
                >= ApiServiceConfiguration.LOG_ARRAY_LEN.getValue()
                && StringUtils.isNotEmpty(logArray.get(3))) {
            return logArray.get(3);
        }
        return null;
    }

    public static  String getResultList(JobExecuteResult executeResult,UJESClient client, String path) {
        ResultSetListResult resultList = (ResultSetListResult) client.executeUJESJob(ResultSetListAction.builder()
                .setUser(executeResult.getUser()).setPath(path).build());
        return resultList.getResponseBody();
    }



    public static  String getResultContent(String user, String path, int maxSize,UJESClient client,boolean  enableLimit,
                                           int columnPage,int columnPageSize,int page,
                                           String maskedFieldNames,String truncateColumn) {

//        String fileContent = client.resultSet(ResultSetAction.builder()
//                    .setPath(path)
//                    .setUser(user)
//                    .setEnableLimit(enableLimit)
//                        .setPage(page)
//                        .setColumnPage(columnPage)
//                        .setColumnPageSize(columnPageSize)
//                    .setPageSize(maxSize).build()).getResponseBody();
//        return  fileContent;

        ResultSetResult resultList = (ResultSetResult)client.executeUJESJob(com.webank.wedatasphere.dss.apiservice.core.action.ResultSetAction.builder()
                .setPath(path).setUser(user)
                .setEnableLimit(enableLimit)
                .setPageSize(maxSize)
                .setPage(page)
                .setColumnPage(columnPage)
                .setColumnPageSize(columnPageSize)
                .setMaskedFieldNames(maskedFieldNames)
                .setTruncateColumn(truncateColumn)
                .build());

        return resultList.getResponseBody();
    }

    public static InputStream downloadResultSet(String user,
                                                String path,
                                                String charset,
                                                String outputFileType,
                                                String csvSeperator,
                                                String outputFileName,
                                                String sheetName,
                                                String nullValue,
                                                UJESClient client,
                                                Boolean isFullExcel) {

        ResultSetDownloadAction resultSetDownloadAction = new ResultSetDownloadAction();
        // 全量excel下载,请求url不同,需要根据参数判断
        resultSetDownloadAction.setUrls(isFullExcel);
        resultSetDownloadAction.setUser(user);
        resultSetDownloadAction.setParameter("path",path);
        resultSetDownloadAction.setParameter("charset",charset);
        resultSetDownloadAction.setParameter("outputFileType",outputFileType);
        resultSetDownloadAction.setParameter("csvSeperator",csvSeperator);
        resultSetDownloadAction.setParameter("outputFileName",outputFileName);
        resultSetDownloadAction.setParameter("sheetName",sheetName);
        resultSetDownloadAction.setParameter("nullValue",nullValue);
        resultSetDownloadAction.setParameter("limit",DOWNLOAD_MAX_SIZE.getValue());
        client.executeUJESJob(resultSetDownloadAction);
        return resultSetDownloadAction.getInputStream();
    }







    public static  Map<String, Object> getTaskInfoById(JobExecuteResult jobExecuteResult, UJESClient client) {

        Map<String, Object> taskInfo = (Map<String, Object>)client.getJobInfo(jobExecuteResult).getTask();

        return taskInfo;
    }




    public static String getUserWorkspaceIds(String userName,UJESClient client){
        ApiServiceGetAction apiServiceGetAction = new ApiServiceGetAction();
        apiServiceGetAction.setUser(userName);
        apiServiceGetAction.setParameter("userName",userName);
        ResultWorkspaceIds userWorkspaceIds = (ResultWorkspaceIds)client.executeUJESJob(apiServiceGetAction);
        return userWorkspaceIds.getUserWorkspaceIds();

    }


    public static  List<String> parseSqlTable(String executeCode){

        LOGGER.info("start parseSqlTable");

        List<String> nonEmptySqlList = removeEmptyStr(executeCode);
        List<String> list = new ArrayList<>();
        String defaultDb= null;
        for(String sql : nonEmptySqlList){

            TGSqlParser parser = new TGSqlParser(EDbVendor.dbvmysql);
            parser.setSqltext(sql);
            if(parser.parse() == 0){
                LOGGER.info("start parse sql {}" , sql);
                TCustomSqlStatement tCustomSqlStatement = parser.getSqlstatements().get(0);
                if(tCustomSqlStatement instanceof TUseDatabase){
                    defaultDb = ((TUseDatabase) tCustomSqlStatement).getDatabaseName().getDatabaseString();
                    continue;
                }
                TTableList tableList = tCustomSqlStatement.getTables();
                LOGGER.info("tableList is {}", tableList);
                for (TTable table : tableList) {
                    String name = table.getFullName();

                    if(!name.contains(".")){

                        if(StringUtils.isEmpty(defaultDb)){
                            throw new DSSRuntimeException("No database information was obtained from the code, table name : " +
                                    "(未获取到sql中的数据库信息,表名称):" + name);
                        }

                        name = defaultDb + "." + name;
                    }

                    list.add(name);
                }
                LOGGER.info("end parse sql {}" , sql);
            }
        }

        LOGGER.info("end parseSqlTable, table is {}",String.join(",", list));

        return  list;
    }


    public static String jdbcPackageCodeToExplain(String executeCode){
        StringBuilder retStr = new StringBuilder();
        List<String> lines = removeEmptyStr(executeCode);
        String explain = "EXPLAIN";
        for(String line: lines){

            String sql = line.trim();
            // use 和show 开头的语句 不做分析
            if(StringUtils.startsWithIgnoreCase(sql,"use") ||
                    StringUtils.startsWithIgnoreCase(sql,"show")){

                retStr.append(sql).append(";");

            }else{

                retStr.append(explain).append(" ").append(sql).append(";");
            }

        }

        LOGGER.info("execute explain code is {}", retStr);
        // 没有EXPLAIN关键字 说明没有查询语句
        if(!retStr.toString().contains(explain)){
            return  null;
        }

        return retStr.toString();
    }





}
