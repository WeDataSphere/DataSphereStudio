package com.webank.wedatasphere.dss.framework.workspace.restful;


import com.webank.wedatasphere.dss.common.server.beans.NoticeVo;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.client.ResourceManageClient;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.NoticeDelRequest;
import com.webank.wedatasphere.dss.common.server.service.NoticeService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


@RequestMapping(path = "/dss/framework/workspace/notice", produces = {"application/json"})
@RestController
public class DSSNoticeMangeRestful {
    private static final Logger LOGGER = LoggerFactory.getLogger(DSSNoticeMangeRestful.class);

    @Resource
    private NoticeService noticeService;
    @Autowired
    ResourceManageClient resourceManageClient;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public Message getNoticeList(HttpServletRequest request
            , @RequestParam(value = "pageSize",required = false) Integer pageSize
            , @RequestParam(value = "currentPage",required = false) Integer currentPage
            , @RequestParam(value = "sortBy",required = false) String sortBy
            , @RequestParam(value = "orderBy",required = false) String orderBy){
        //检查用户是否为超级管理员，只有超级管理员可以发布公告
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

    @RequestMapping(value = "/create",method = RequestMethod.POST)
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

    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    public Message deleteNotice(HttpServletRequest request,@RequestBody NoticeDelRequest delRequest){
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
        noticeService.deleteNotice(delRequest.getNoticeId());
        return Message.ok("成功删除公告");
    }
}
