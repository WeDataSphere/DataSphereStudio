package com.webank.wedatasphere.dss.scriptis.restful;

import com.alibaba.fastjson.JSONObject;
import com.webank.wedatasphere.dss.common.server.beans.NoticeContent;
import com.webank.wedatasphere.dss.common.server.beans.NoticeVo;
import com.webank.wedatasphere.dss.common.server.conf.CommonServerConfiguration;
import com.webank.wedatasphere.dss.common.server.enums.ReleaseTypeEnum;
import com.webank.wedatasphere.dss.common.server.service.NoticeService;
import com.webank.wedatasphere.dss.common.server.service.ReleaseNoteService;
import com.webank.wedatasphere.dss.common.server.service.UserAccessAuditService;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.client.ResourceManageClient;
import com.webank.wedatasphere.dss.common.server.beans.ReleaseNoteContent;
import com.webank.wedatasphere.dss.common.server.beans.ReleaseNoteVO;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.client.impl.HttpResourceManageClient;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.GovernanceStationAdminResponse;
import com.webank.wedatasphere.dss.framework.proxy.conf.ProxyUserConfiguration;
import com.webank.wedatasphere.dss.framework.proxy.service.DssProxyUserService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/dss/scriptis", produces = {"application/json"})
public class ScriptisRestful {
    private final String SERVER_NAME="Scriptis";
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    ResourceManageClient resourceManageClient;
    @Autowired
    ReleaseNoteService releaseNoteService;
    @Autowired
    UserAccessAuditService userAccessAuditService;
    @Autowired
    DssProxyUserService dssProxyUserService;
    @Autowired
    NoticeService noticeService;
    /**
     * 获取用户状态阶段
     */
    @GetMapping(path = "getUserStage")
    public Message getUserStage(@RequestParam("userName") String userName){
        String stage;


        try {
            //linkis工作空间初始化要考虑代理用户
            String dirUser=ProxyUserConfiguration.isProxyUserEnable()?dssProxyUserService.getProxyUser(httpServletRequest):userName;
            boolean initiated = resourceManageClient.testUserInitiated(dirUser);
            if (!initiated) {
                stage = "uninit";
            } else if (userAccessAuditService.getAndIncreaseLoginCount(userName)==0L) {
                // 第一次登录
                stage = "new";
            } else {
                stage = "senior";
            }
        }catch (Exception e){
            stage = "senior";
        }
        return Message.ok().data("stage",stage);
    }
    @GetMapping("getReleaseNote")
    public Message getReleaseNote(){
        List<ReleaseNoteContent> contents=releaseNoteService.getReleaseNoteContent(ReleaseTypeEnum.SCRIPTIS);

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

    @RequestMapping(value = "/notice/list",method = RequestMethod.GET)
    public Message getNoticeList(HttpServletRequest request
            , @RequestParam(value = "pageSize",required = false) Integer pageSize
            , @RequestParam(value = "currentPage",required = false) Integer currentPage
            , @RequestParam(value = "sortBy",required = false) String sortBy
            , @RequestParam(value = "orderBy",required = false) String orderBy){
        //检查是否在白名单
        String userName = SecurityFilter.getLoginUsername(request);
        try {
            boolean admin = resourceManageClient.checkSuperAdmin(userName);
            if(!admin){
                return Message.error("当前用户无超级管理员权限，不能进行公告配置");
            }
        } catch (Exception e){
            return Message.error("查询用户超级管理员身份失败",e);
        }
        List<NoticeVo> noticeList = noticeService.getAllNotice(sortBy, orderBy);
        int total = noticeList.size();
        List<NoticeVo> retList = noticeList;
        if((pageSize!=null && currentPage!=null)){
            if(pageSize<0 || currentPage<=0) {
                return Message.error("分页信息参数错误");
            }
            int offset = (currentPage-1)*pageSize;
            retList = noticeList.stream().skip(offset).limit(pageSize).collect(Collectors.toList());
        }
        return Message.ok("查询成功")
                .data("total",total)
                .data("noticeList",retList);
    }

    @RequestMapping(value = "/notice/create",method = RequestMethod.POST)
    public Message createNotice(HttpServletRequest request, @RequestBody NoticeVo noticeRequest){
        //1、白名单校验
        //检查是否在白名单
        String userName = SecurityFilter.getLoginUsername(request);
        try {
            boolean admin = resourceManageClient.checkSuperAdmin(userName);
            if(!admin){
                return Message.error("当前用户无超级管理员权限，不能进行公告配置");
            }
        } catch (Exception e){
            return Message.error("查询用户超级管理员身份失败",e);
        }
        //2、新增
        noticeRequest.setCreateUser(userName);
        noticeService.createNotice(noticeRequest);
        return Message.ok("新增公告成功");
    }

    @RequestMapping(value = "/notice/delete",method = RequestMethod.POST)
    public Message deleteNotice(HttpServletRequest request,@RequestBody JSONObject delRequest){
        //1、白名单校验
        String userName = SecurityFilter.getLoginUsername(request);
        try {
            boolean admin = resourceManageClient.checkSuperAdmin(userName);
            if(!admin){
                return Message.error("当前用户无超级管理员权限，不能进行公告配置");
            }
        } catch (Exception e){
            return Message.error("查询用户超级管理员身份失败",e);
        }
        //2、删除
        noticeService.deleteNotice(delRequest.getInteger("noticeId"));
        return Message.ok("成功删除公告");
    }
}
