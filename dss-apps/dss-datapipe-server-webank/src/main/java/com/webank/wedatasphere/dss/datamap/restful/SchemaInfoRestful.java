/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.dss.datamap.restful;

import com.alibaba.fastjson.JSON;
import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils;
import com.webank.wedatasphere.dss.datamap.conf.DataMapConnConf;
import com.webank.wedatasphere.dss.datamap.datamap.*;
import com.webank.wedatasphere.dss.datamap.domain.*;
import com.webank.wedatasphere.dss.datamap.domain.vo.CodeMeta;
import com.webank.wedatasphere.dss.datamap.domain.vo.SchemaBaseInfoVo;
import com.webank.wedatasphere.dss.datamap.domain.vo.TableMetaDataInfoVo;
import com.webank.wedatasphere.dss.datamap.domain.vo.TableValidateResult;
import com.webank.wedatasphere.dss.datamap.exception.DataMapException;
import com.webank.wedatasphere.dss.datamap.service.SchemaInfoService;
import com.webank.wedatasphere.dss.datamap.util.RestfulResponseUtils;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.webank.wedatasphere.dss.datamap.service.SchemaInfoService.cachedThreadPool;

/**
 * @author: jinyangrao on 2020/11/04
 * @description: by this restful api to get schema metadata.
 */

@RestController
@RequestMapping(path = "/dss/datapipe/datasource", produces = {"application/json"})
public class SchemaInfoRestful {

    private static final Logger logger = LoggerFactory.getLogger(SchemaInfoRestful.class);

    private static final String MESSAGE_ERROR_DBNAME_EMPTY = "dbName is empty[数据库名为空]";

    @Autowired
    private SchemaInfoService schemaInfoService;
    @Autowired
    HttpServletRequest req;
    private final String dbRestriction = DataMapConnConf.DB_RESTRICTION();

    @GetMapping(path = "/getSchemaBaseInfo")
    public Message getSchemaBaseInfo( @RequestParam(required = false, name = "dbName") String dbName) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to get schema base info!", userName);
            if (StringUtils.isBlank(dbName)) {
                return Message.error(MESSAGE_ERROR_DBNAME_EMPTY);
            }
            SchemaBaseInfoVo schemaBaseInfoVo = schemaInfoService.getSchemaBaseInfo(dbName, userName);
            return Message.ok().data("schemaInfo", schemaBaseInfoVo);
        }, "getSchemaBaseInfo", "Fail to get schema base info[获取数据库信息失败]");
    }


    @GetMapping(path = "/getTableMetaDataInfo")
    public Message getTableMetaDataInfo(
                                        @RequestParam(required = false, name = "dbName") String dbName,
                                        @RequestParam(required = false, name = "tableName") String tableName,
                                        @RequestParam(required = false, name = "orderBy") String orderBy,
                                        @RequestParam(name = "pageSize") Integer pageSize,
                                        @RequestParam(name = "currentPage") Integer currentPage,
                                        @RequestParam(required = false, name = "isTableOwner", defaultValue = "0") Integer isTableOwner,
                                        @RequestParam(required = false, name = "exactTableName", defaultValue = "false") Boolean exactTableName,
                                        @RequestParam(required = false, name = "tableOwner") String tableOwner,
                                        @RequestParam(required = false, name = "usageHeat") String usageHeat
    ) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to get table metadata info!", userName);
            Long workspaceId = null;
            boolean isWorkspaceExists = Arrays.stream(req.getCookies()).anyMatch((cookie) -> {
                return "workspaceId".equals(cookie.getName());
            });

            if (isWorkspaceExists) {
                workspaceId = SSOHelper.getWorkspace(req).getWorkspaceId();
            }
            logger.info("current workspace id is {}", workspaceId);
            if (StringUtils.isBlank(dbName)) {
                return Message.error(MESSAGE_ERROR_DBNAME_EMPTY);
            }
            // isTableOwner：0表示默认值我有权限的表，1表示我创建的表

            MetaDataQuery metaDataQuery = new MetaDataQuery(dbName, isTableOwner, tableName, orderBy);
            metaDataQuery.setLoginUser(userName);
            metaDataQuery.setPageSize(null == pageSize || pageSize > 50 ? 10 : pageSize);
            metaDataQuery.setCurrentPage(null == currentPage ? 1 : currentPage);
            metaDataQuery.setExactTableName(exactTableName);
            metaDataQuery.setWorkspaceId(workspaceId);
            metaDataQuery.setTableOwner(tableOwner);
            metaDataQuery.setUsageHeat(usageHeat);
            DMToTableMetaDataInfoVo dmToTableMetaDataInfoVo = schemaInfoService.getTableMetaDataInfo(metaDataQuery);

            return Message.ok().data("currentPage", currentPage).data("pageSize", pageSize)
                    .data("total", dmToTableMetaDataInfoVo.getTotalCount())
                    .data("tableList", dmToTableMetaDataInfoVo.getTableMetaDataInfoVos())
                    .data("isWorkspaceAdmin", dmToTableMetaDataInfoVo.isWorkspaceAdmin());
        }, "getTableMetaDataInfo", "Fail to get table metadata info[获取数据库元数据信息失败]");
    }


    @GetMapping(path = "/getTablesName")
    public Message getTablesName(
                                 @RequestParam(required = false, name = "dbName") String dbName,
                                 @RequestParam(required = false, name = "tableName") String tableName,
                                 @RequestParam(required = false, name = "isTableOwner", defaultValue = "0") Integer isTableOwner,
                                 @RequestParam(required = false, name = "orderBy", defaultValue = "1") Integer orderBy,
                                 @RequestParam(required = false, name = "tableOwner") String tableOwner,
                                 @RequestParam(required = false, name = "usageHeat") String usageHeat
    ) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} read to get tables name!", userName);
            if (StringUtils.isBlank(dbName)) {
                return Message.error(MESSAGE_ERROR_DBNAME_EMPTY);
            }
            List<String> tablesName = schemaInfoService.getTablesName(dbName, tableName, isTableOwner, orderBy, userName, tableOwner,usageHeat);
            return Message.ok().data("tablesName", tablesName);
        }, "getTablesName", "Fail to get table name[获取数据库表名失败]");
    }

    @PostMapping(path = "/transferTablesOwner")
    public Message transferTablesOwner(@RequestBody TransferTablesOwnerRequest transferTablesOwnerRequest) {
        return RestfulResponseUtils.doAndResponse(() -> {
            //校验数据库名
            String dbName = transferTablesOwnerRequest.getDbName();
            if (Arrays.stream(dbRestriction.split(",")).noneMatch(e -> e.trim().equals(dbName.substring(dbName.lastIndexOf("_") + 1)))) {
                return Message.error("该数据库属主不可转移，只有库名以_work,_bak结尾才可进行属主转移");
            }
            if (transferTablesOwnerRequest.getTablesName().size() > 100) {
                return Message.error("一次转移的表数量不能超过100！");
            }
            String userName = SecurityFilter.getLoginUsername(req);

            // 普通模式 使用登录用户做oldOwner,管理员用传入的oldOwner参数
            if (StringUtils.isEmpty(transferTablesOwnerRequest.getOldOwner())) {
                transferTablesOwnerRequest.setOldOwner(userName);
            }
            logger.info("input table old owner is {}", transferTablesOwnerRequest.getOldOwner());

            if (transferTablesOwnerRequest.getNewOwner().equals(transferTablesOwnerRequest.getOldOwner())) {
                return Message.error("不允许新Owner与旧Owner相同！");
            }

            logger.info("Current login user {} start to transfer the owner of tables {} in database {} from {} to {}", userName, transferTablesOwnerRequest.getTablesName(),
                    dbName, transferTablesOwnerRequest.getOldOwner(), transferTablesOwnerRequest.getNewOwner());
            Integer ITSMID = schemaInfoService.transferTablesOwner(transferTablesOwnerRequest, userName);
            AuditLogUtils.printLog(userName, null, null, TargetTypeEnum.DATAPIPE, dbName,
                    transferTablesOwnerRequest.getTablesName().toString(), OperateTypeEnum.UPDATE, transferTablesOwnerRequest);
            return Message.ok().data("message", "表转移ITSM单创建成功，单号为：" + ITSMID + "，请前往ITSM查看表单具体信息及审批状态！");
        }, "transferTablesOwner", "表转移ITSM单创建失败原因为：");
    }

    @GetMapping(path = "/downloadTableMetaData")
    public void downloadTableMetaData(HttpServletResponse response,
                                      @RequestParam(required = false, name = "dbName") String dbName,
                                      @RequestParam(required = false, name = "tableName") String tableName,
                                      @RequestParam(required = false, name = "orderBy", defaultValue = "1") String orderBy,
                                      @RequestParam(required = false, name = "isTableOwner", defaultValue = "0") Integer isTableOwner,
                                      @RequestParam(required = false, name = "exactTableName", defaultValue = "false") Boolean exactTableName,
                                      @RequestParam(required = false, name = "tableOwner") String tableOwner,
                                      @RequestParam(required = false, name = "usageHeat") String usageHeat) throws IOException {
        Workbook workbook = null;
        List<Future<List<TableMetaDataInfoVo>>> futures = new ArrayList<>();
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            long workspaceId = SSOHelper.getWorkspace(req).getWorkspaceId();
            logger.info("current login user {} ready to download table metadata info!", userName);
            logger.info("current workspace id is {}", workspaceId);
            if (StringUtils.isBlank(dbName)) {
                throw new DataMapException(MESSAGE_ERROR_DBNAME_EMPTY);
            }
            Integer pageSize = 1000;
            Integer currentPage = 1;
            MetaDataQuery metaDataQuery = new MetaDataQuery(dbName, isTableOwner, tableName, orderBy);
            metaDataQuery.setLoginUser(userName);
            metaDataQuery.setPageSize(pageSize);
            metaDataQuery.setCurrentPage(currentPage);
            metaDataQuery.setExactTableName(exactTableName);
            metaDataQuery.setTableOwner(tableOwner);
            metaDataQuery.setWorkspaceId(workspaceId);
            metaDataQuery.setUsageHeat(usageHeat);
            DMToTableMetaDataInfoVo dmToTableMetaDataInfoVo = schemaInfoService.getTableMetaDataInfo(metaDataQuery);
            Integer totalCount = dmToTableMetaDataInfoVo.getTotalCount();
            int queryCount = totalCount % pageSize == 0 ? (totalCount / pageSize) : (totalCount / pageSize + 1);
            logger.info("need to query table metadata info for {} times", queryCount);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(dbName, "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            int rownum = 0;
            workbook = new SXSSFWorkbook(5000);
            String[] title = {"序号", "表名", "表别名", "创建时间", "表大小", "表属主", "是否分区", "是否压缩", "压缩格式", "最近访问时间", "最近修改时间"};
            Sheet sheet = workbook.createSheet("result");
            Row titleRow = sheet.createRow(rownum);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.RED.getIndex());
            CellStyle style = workbook.createCellStyle();
            style.setFont(headerFont);
            Cell cell;
            for (int i = 0; i < title.length; i++) {
                cell = titleRow.createCell(i);
                cell.setCellValue(title[i]);
                cell.setCellStyle(style);
            }
            List<TableMetaDataInfoVo> tableMetaDataInfoVos = dmToTableMetaDataInfoVo.getTableMetaDataInfoVos();
            Row row;
            TableMetaDataInfoVo tableMetaDataInfoVo;
            for (int i = 0; i < tableMetaDataInfoVos.size(); i++) {
                rownum++;
                row = sheet.createRow(rownum);
                tableMetaDataInfoVo = tableMetaDataInfoVos.get(i);
                row.createCell(0).setCellValue(rownum);
                row.createCell(1).setCellValue(tableMetaDataInfoVo.getTableName() == null ? "" : tableMetaDataInfoVo.getTableName());
                row.createCell(2).setCellValue(tableMetaDataInfoVo.getTableAlias() == null ? "" : tableMetaDataInfoVo.getTableAlias());
                row.createCell(3).setCellValue(tableMetaDataInfoVo.getCreateTime() == null ? "" : tableMetaDataInfoVo.getCreateTime());
                row.createCell(4).setCellValue(tableMetaDataInfoVo.getTableSize() == null ? "" : tableMetaDataInfoVo.getTableSize());
                row.createCell(5).setCellValue(tableMetaDataInfoVo.getTableOwner() == null ? "" : tableMetaDataInfoVo.getTableOwner());
                row.createCell(6).setCellValue(tableMetaDataInfoVo.getPartitioned() == null ? "" : tableMetaDataInfoVo.getPartitioned());
                row.createCell(7).setCellValue(tableMetaDataInfoVo.getCompressed());
                row.createCell(8).setCellValue(tableMetaDataInfoVo.getCompressedFormat() == null ? "" : tableMetaDataInfoVo.getCompressedFormat());
                row.createCell(9).setCellValue(tableMetaDataInfoVo.getViewTime() == null ? "" : tableMetaDataInfoVo.getViewTime());
                row.createCell(10).setCellValue(tableMetaDataInfoVo.getModifyTime() == null ? "" : tableMetaDataInfoVo.getModifyTime());
            }

            Callable<List<TableMetaDataInfoVo>> callable;
            Future<List<TableMetaDataInfoVo>> future;
            CompletionService<List<TableMetaDataInfoVo>> completionService = new ExecutorCompletionService<>(cachedThreadPool);
            for (int i = 2; i <= queryCount; i++) {
                Integer finalCurrentPage = i;
                callable =
                        () -> schemaInfoService.getTableMetaDataInfo(metaDataQuery, finalCurrentPage).getTableMetaDataInfoVos();
                future = completionService.submit(callable);
                futures.add(future);
            }
            for (int i = 0; i < futures.size(); i++) {
                future = completionService.poll(120, TimeUnit.SECONDS);
                if (future == null) {
                    logger.error("download table metadata info failed, datamap query timed out and unable to retrieves the future");
                    response.reset();
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf-8");
                    Map<String, String> map = new HashMap<>();
                    map.put("status", "1");
                    map.put("message", "当前下载繁忙，请稍后再试");
                    response.getWriter().println(JSON.toJSONString(map));
                }
                tableMetaDataInfoVos = future.get();
                for (int j = 0; j < tableMetaDataInfoVos.size(); j++) {
                    rownum++;
                    row = sheet.createRow(rownum);
                    tableMetaDataInfoVo = tableMetaDataInfoVos.get(j);
                    row.createCell(0).setCellValue(rownum);
                    row.createCell(1).setCellValue(tableMetaDataInfoVo.getTableName() == null ? "" : tableMetaDataInfoVo.getTableName());
                    row.createCell(2).setCellValue(tableMetaDataInfoVo.getTableAlias() == null ? "" : tableMetaDataInfoVo.getTableAlias());
                    row.createCell(3).setCellValue(tableMetaDataInfoVo.getCreateTime() == null ? "" : tableMetaDataInfoVo.getCreateTime());
                    row.createCell(4).setCellValue(tableMetaDataInfoVo.getTableSize() == null ? "" : tableMetaDataInfoVo.getTableSize());
                    row.createCell(5).setCellValue(tableMetaDataInfoVo.getTableOwner() == null ? "" : tableMetaDataInfoVo.getTableOwner());
                    row.createCell(6).setCellValue(tableMetaDataInfoVo.getPartitioned() == null ? "" : tableMetaDataInfoVo.getPartitioned());
                    row.createCell(7).setCellValue(tableMetaDataInfoVo.getCompressed());
                    row.createCell(8).setCellValue(tableMetaDataInfoVo.getCompressedFormat() == null ? "" : tableMetaDataInfoVo.getCompressedFormat());
                    row.createCell(9).setCellValue(tableMetaDataInfoVo.getViewTime() == null ? "" : tableMetaDataInfoVo.getViewTime());
                    row.createCell(10).setCellValue(tableMetaDataInfoVo.getModifyTime() == null ? "" : tableMetaDataInfoVo.getModifyTime());
                }
            }
            workbook.write(response.getOutputStream());
        } catch (InterruptedException e) {
            logger.error("Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.info("output failed", e);
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = new HashMap<>();
            map.put("status", "1");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            for (Future<List<TableMetaDataInfoVo>> f : futures) {
                f.cancel(true);
            }
        }
    }

    @RequestMapping(method = RequestMethod.GET , path = "/space")
    public Message getSpace(
                                 @RequestParam(name = "clusterCode") String clusterCode,
                                 @RequestParam(required = false, name = "spaceName") String spaceName,
                                 @RequestParam(required = false, name = "pageNum") Integer pageNum,
                                 @RequestParam(required = false, name = "pageSize") Integer pageSize

    ) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to get edge index info!", userName);
            List<DMSpaceInfoBean> dMSpaceInfoBeans = schemaInfoService.getSpace(clusterCode, userName, spaceName, pageNum, pageSize);
            return Message.ok().data("dMSpaceInfoBeans", dMSpaceInfoBeans);
        }, "getSpace", "Fail to get space [获取图空间失败]");
    }

    @RequestMapping(method = RequestMethod.GET , path = "/tags")
    public Message getTags(
                            @RequestParam(name = "clusterCode") String clusterCode,
                            @RequestParam(required = false, name = "spaceName") String spaceName,
                            @RequestParam(required = false, name = "pageNum") Integer pageNum,
                            @RequestParam(required = false, name = "pageSize") Integer pageSize,
                            @RequestParam(required = false, name = "tagName") String tagName
    ) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to get edge index info!", userName);
            List<DMSTagBean> tags = schemaInfoService.getTags(clusterCode, userName, spaceName, pageNum, pageSize, tagName);
            return Message.ok().data("tags", tags);
        }, "getTags", "Fail to get tags [获取标签失败]");
    }

    @RequestMapping(method = RequestMethod.GET , path = "/tag-prop")
    public Message getTagProps(
                           @RequestParam(name = "clusterCode") String clusterCode,
                           @RequestParam(required = false, name = "spaceName") String spaceName,
                           @RequestParam(required = false, name = "pageNum") Integer pageNum,
                           @RequestParam(required = false, name = "pageSize") Integer pageSize,
                           @RequestParam(required = false, name = "tagName") String tagName
    ) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to get edge index info!", userName);
            List<DMSTagPropBean> tagProps = schemaInfoService.getTagProps(clusterCode, userName, spaceName, pageNum, pageSize, tagName);
            return Message.ok().data("tagProps", tagProps);
        }, "getTagProps", "Fail to get tag prop[获取标签属性失败]");
    }

    @RequestMapping(method = RequestMethod.GET , path = "/edges")
    public Message getEdges(
                               @RequestParam(name = "clusterCode") String clusterCode,
                               @RequestParam(required = false, name = "spaceName") String spaceName,
                               @RequestParam(required = false, name = "pageNum") Integer pageNum,
                               @RequestParam(required = false, name = "pageSize") Integer pageSize,
                               @RequestParam(required = false, name = "edgeTypeName") String edgeTypeName
    ) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to get edge index info!", userName);
            List<DMSEdgeBean> edges = schemaInfoService.getEdges(clusterCode, userName, spaceName, pageNum, pageSize, edgeTypeName);
            return Message.ok().data("edges", edges);
        }, "getEdges", "Fail to get edge[获取边类型失败]");
    }

    @RequestMapping(method = RequestMethod.GET , path = "/edge-prop")
    public Message getEdgesProp(
                            @RequestParam(name = "clusterCode") String clusterCode,
                            @RequestParam(required = false, name = "spaceName") String spaceName,
                            @RequestParam(required = false, name = "pageNum") Integer pageNum,
                            @RequestParam(required = false, name = "pageSize") Integer pageSize,
                            @RequestParam(required = false, name = "edgeTypeName") String edgeTypeName
    ) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to get edge index info!", userName);
            List<DMSEdgePropBean> edgeProps = schemaInfoService.getEdgesProp(clusterCode, userName, spaceName, pageNum, pageSize, edgeTypeName);
            return Message.ok().data("edgeProps", edgeProps);
        }, "getEdgesProp", "Fail to get edge prop [获取边类型属性失败]");
    }


    @RequestMapping(method = RequestMethod.GET , path = "/tag-index")
    public Message getTagIndex(
                                @RequestParam(name = "clusterCode") String clusterCode,
                                @RequestParam(required = false, name = "spaceName") String spaceName,
                                @RequestParam(required = false, name = "pageNum") Integer pageNum,
                                @RequestParam(required = false, name = "pageSize") Integer pageSize,
                                @RequestParam(required = false, name = "tagName") String tagName
    ) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to get edge index info!", userName);
            List<DMSTagIndexBean> indexVoes = schemaInfoService.getTagIndex(clusterCode, userName, spaceName, pageNum, pageSize, tagName);
            return Message.ok().data("indexVoes", indexVoes);
        }, "getTagIndex", "Fail to get tag index[获取标签索引失败]");
    }

    @RequestMapping(method = RequestMethod.GET , path = "/edge-index")
    public Message getEdgeIndex(
                               @RequestParam(name = "clusterCode") String clusterCode,
                               @RequestParam(required = false, name = "spaceName") String spaceName,
                               @RequestParam(required = false, name = "pageNum") Integer pageNum,
                               @RequestParam(required = false, name = "pageSize") Integer pageSize,
                               @RequestParam(required = false, name = "edgeTypeName") String edgeTypeName
    ) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to get edge index info!", userName);
            List<DMSEdgeIndexBean> indexVoes = schemaInfoService.getEdgeIndex(clusterCode, userName, spaceName, pageNum, pageSize, edgeTypeName);
            return Message.ok().data("indexVoes", indexVoes);
        }, "getEdgeIndex", "Fail to get edge index[获取边索引失败]");
    }

    @RequestMapping(method = RequestMethod.GET , path = "/permission")
    public Message getPermission(
                                @RequestParam(required = false, name = "urn") String urn,
                                @RequestParam(required = false, name = "clusterCode") String clusterCode,
                                @RequestParam(required = false, name = "roleType") String roleType,
                                @RequestParam(required = false, name = "spaceName") String spaceName,
                                @RequestParam(required = false, name = "opType") String opType,
                                @RequestParam(required = false, name = "approvalNo") String approvalNo,
                                @RequestParam(required = false, name = "executor") String executor,
                                @RequestParam(required = false, name = "executeTime") String executeTime,
                                @RequestParam(required = false, name = "pageNum") Integer pageNum,
                                @RequestParam(required = false, name = "pageSize") Integer pageSize
    ) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to get edge index info!", userName);
            DMSPermissionVos permissionVos = schemaInfoService.getPermission(urn, clusterCode, userName, roleType, spaceName, opType, approvalNo, executor, executeTime, pageNum, pageSize);
            return Message.ok().data("permissionVos", permissionVos.getDmsPermissionVos()).data("pageNum",permissionVos.getPageNum())
                    .data("pageSize",permissionVos.getPageSize()).data("totalPage",permissionVos.getTotalPage()).data("totalCount",permissionVos.getTotalCount());
        }, "getPermission", "Fail to get permission[获取图库元数据-权限失败]");
    }
    @RequestMapping(method = RequestMethod.POST , path = "/explainCodeMeta")
    public Message explainCodeMeta(@RequestBody ExplainCodeMetaRequest explainCodeMetaRequest) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to explain code meta!", userName);
            List<CodeMeta> meta = schemaInfoService.getCodeMeta(explainCodeMetaRequest,userName);
            return Message.ok().data("meta", meta);
        }, "explainCodeMeta", "Fail to get script db meta[获取脚本中的库表信息失败]");
    }

    @RequestMapping(method = RequestMethod.POST , path = "/validateTables")
    public Message validateTables(@RequestBody ValidateTablesRequest validateTablesRequest) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            List<CodeMeta> tables = validateTablesRequest.getTables();
            logger.info("current login user {} ready to validate tables!", userName);
            List<TableValidateResult> result = schemaInfoService.validateTables(tables,userName);
            return Message.ok().data("result", result);
        }, "validateTables", "Fail to validate tables[自动校验失败，请人工判断所配置的库表是否为视图。datachecker不支持视图，如遇视图会运行失败]");
    }

    @RequestMapping(method = RequestMethod.POST , path = "/validateDataCheckerHasView")
    public Message validateDataCheckerHasView(@RequestBody ValidateTablesStringRequest validateTablesRequest) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            List<CodeMeta> tables = parseTableListFromString(validateTablesRequest);
            logger.info("current login user {} ready to validate tables!", userName);
            List<TableValidateResult> result = schemaInfoService.validateTables(tables,userName);
            return Message.ok().data("result", result);
        }, "validateTables", "Fail to validate tables[自动校验失败，请人工判断所配置的库表是否为视图。datachecker不支持视图，如遇视图会运行失败]");
    }
    private List<CodeMeta> parseTableListFromString(ValidateTablesStringRequest validateTablesRequest){
        List<String> tableStr = new ArrayList<>();
        if(validateTablesRequest.getCheckObject()!=null){
            tableStr.add(validateTablesRequest.getCheckObject());
        }
        if(validateTablesRequest.getJobDesc()!=null){
            String jobDesc = validateTablesRequest.getJobDesc();
            String[] rows;
            if (jobDesc.contains("\n")) {
                rows = jobDesc.split("\n");
            }else{
                rows = jobDesc.split(";");
            }
            for (String row : rows) {
                if (row.startsWith("check.object")&&row.contains("=")) {
                    int endLocation = row.indexOf("=");
                    String checkObject = row.substring(endLocation + 1);
                    tableStr.add(checkObject);
                }
            }
            tableStr.addAll(Arrays.asList(rows));
        }
        List<CodeMeta> codeMetas = new ArrayList<>(tableStr.size());
        for (String s : tableStr) {
            CodeMeta c = parseTableFromString(s);
            if (c != null) {
                codeMetas.add(c);
            }
        }
        return codeMetas;
    }

    private CodeMeta parseTableFromString(String dataObjectStr){
        if(!dataObjectStr.contains(".")){
            return null;
        }
        String dbName = dataObjectStr.split("\\.")[0];
        String tableName = dataObjectStr.split("\\.")[1];
        CodeMeta codeMeta;
        if (dataObjectStr.contains("{")) {
            String partitionName = "";
            Pattern pattern = Pattern.compile("\\{([^\\}]+)\\}");
            Matcher matcher = pattern.matcher(dataObjectStr);
            if (matcher.find()) {
                partitionName = matcher.group(1);
            }
            partitionName = partitionName.replace("\'", "").replace("\"", "");
            tableName = tableName.split("\\{")[0];
            codeMeta = new CodeMeta(dbName, tableName, partitionName);
        }else{
            codeMeta=new CodeMeta(dbName,tableName,null);
        }
        return codeMeta;
    }


    @RequestMapping(method = RequestMethod.POST , path = "/getDetailDatasetMaskInfo")
    public Message getDetailDatasetMaskInfo(@RequestBody DMSDatasetMaskRequest datasetMaskRequest) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to getDetailDatasetMaskInfo!", userName);
            datasetMaskRequest.setLoginUser(userName);
            DMSDatasetMaskInfoResult result = schemaInfoService.getDetailDatasetMaskInfo(datasetMaskRequest);
            return Message.ok().data("result", result);
        }, "getDetailDatasetMaskInfo", "Fail to getDetailDatasetMaskInfo[查询源表基本信息失败]");
    }

    @RequestMapping(method = RequestMethod.POST , path = "/getColumnMaskInfoList")
    public Message getColumnMaskInfoList(@RequestBody DMSDatasetMaskRequest datasetMaskRequest) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to getColumnMaskInfoList!", userName);
            datasetMaskRequest.setLoginUser(userName);
            List<DMSColumnMaskInfo> result = schemaInfoService.getColumnMaskInfoList(datasetMaskRequest);
            return Message.ok().data("result", result);
        }, "getColumnMaskInfoList", "Fail to getColumnMaskInfoList[查询源表的表字段信息失败]");
    }

    @RequestMapping(method = RequestMethod.POST , path = "/getBdpTableDetail")
    public Message getBdpTableDetail(@RequestBody DMSDatasetMaskRequest datasetMaskRequest) {
        return RestfulResponseUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            logger.info("current login user {} ready to getBdpTableDetail!", userName);
            datasetMaskRequest.setLoginUser(userName);
            DMSBdpTableInfo result = schemaInfoService.getBdpTableDetail(datasetMaskRequest);
            return Message.ok().data("result", result);
        }, "getBdpTableDetail", "Fail to getBdpTableDetail[查询表的统计信息失败]");
    }
}