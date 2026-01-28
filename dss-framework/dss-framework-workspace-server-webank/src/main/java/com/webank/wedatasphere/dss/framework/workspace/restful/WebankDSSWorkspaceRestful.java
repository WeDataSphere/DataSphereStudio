package com.webank.wedatasphere.dss.framework.workspace.restful;

import com.webank.wedatasphere.dss.common.server.beans.NoticeContent;
import com.webank.wedatasphere.dss.common.server.beans.ReleaseNoteContent;
import com.webank.wedatasphere.dss.common.server.beans.ReleaseNoteVO;
import com.webank.wedatasphere.dss.common.server.conf.CommonServerConfiguration;
import com.webank.wedatasphere.dss.common.server.enums.ReleaseTypeEnum;
import com.webank.wedatasphere.dss.common.server.service.NoticeService;
import com.webank.wedatasphere.dss.common.server.service.ReleaseNoteService;
import com.webank.wedatasphere.dss.common.server.service.UserAccessAuditService;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.client.ResourceManageClient;
import com.webank.wedatasphere.dss.framework.workspace.conf.WorkspaceConfiguration;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspacePrivService;
import com.webank.wedatasphere.dss.framework.workspace.service.ECReleaseSummaryInfoNotificationService;
import com.webank.wedatasphere.dss.framework.workspace.util.WorkspaceDBHelper;
import org.apache.linkis.server.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 工作空间内资源管理先关接口
 * Author: xlinliu
 * Date: 2022/11/22
 */
@RequestMapping(path = "/dss/framework/workspace", produces = {"application/json"})
@RestController
public class WebankDSSWorkspaceRestful {
    private final String SERVER_NAME="DSS";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebankDSSWorkspaceRestful.class);
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    ResourceManageClient resourceManageClient;

    @Autowired
    DSSWorkspacePrivService dssWorkspacePrivService;
    @Autowired
    WorkspaceDBHelper workspaceDBHelper;
    @Autowired
    ReleaseNoteService releaseNoteService;
    @Autowired
    UserAccessAuditService userAccessAuditService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    ECReleaseSummaryInfoNotificationService notificationService;

    @GetMapping("getReleaseNote")
    public Message getReleaseNote(){
        List<ReleaseNoteContent> dssContents=releaseNoteService.getReleaseNoteContent(ReleaseTypeEnum.DSS);
        List<ReleaseNoteContent> scriptiscontents=releaseNoteService.getReleaseNoteContent(ReleaseTypeEnum.SCRIPTIS);
        List<ReleaseNoteContent> contents= Stream.of(dssContents,scriptiscontents).flatMap(Collection::stream).collect(Collectors.toList());

        String dssVersion = CommonServerConfiguration.DSS_SERVER_RELEASE_VERSION;
        String releaseTile=String.format("%s%s版本功能介绍",SERVER_NAME, dssVersion);
        String releaseName=String.format("%s%sReleaseNote",SERVER_NAME, dssVersion);
        String releaseNoteUrl=String.format("_book/版本动态与公告/%s.html",dssVersion);
        ReleaseNoteVO noteVO=new ReleaseNoteVO();
        noteVO.setName(releaseName);
        noteVO.setTitle(releaseTile);
        noteVO.setContents(contents);
        return Message.ok("获取releaseNote成功").data("releaseNote",Collections.singletonList(noteVO))
                .data("version",dssVersion)
                .data("releaseNoteUrl",releaseNoteUrl);
    }

    @GetMapping("getNotice")
    public Message getNotice(){
        List<NoticeContent> noticeContent= noticeService.getNoticeContent();
        return Message.ok("公告获取成功").data("notices", noticeContent);
    }

    @GetMapping(path = "getUserStage")
    public Message getUserStage(@RequestParam("userName") String userName){
        String stage;
        try {
            if(WorkspaceConfiguration.DSS_NEED_INIT_USER_STAGE&&!resourceManageClient.testUserInitiated(userName)){
                stage = "uninit";
            }else if (userAccessAuditService.getAndIncreaseLoginCount(userName)==0L) {
                stage = "new";
            } else {
                stage = "senior";
            }
        }catch (Exception e){
            stage = "senior";
        }
        return Message.ok().data("stage",stage);
    }

}
