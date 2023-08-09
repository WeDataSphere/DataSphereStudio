package com.webank.wedatasphere.dss.common.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.server.beans.NoticeContent;
import com.webank.wedatasphere.dss.common.server.beans.NoticeVo;
import com.webank.wedatasphere.dss.common.server.dao.NoticeMapper;
import com.webank.wedatasphere.dss.common.server.service.NoticeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {
    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public List<NoticeContent> getNoticeContent() {
        QueryWrapper<NoticeContent> queryWrapper = new QueryWrapper<>();
        Date now = new Date();
        queryWrapper.gt("end_time", now);
        queryWrapper.lt("start_time",now);
        queryWrapper.orderByDesc("id");
        return noticeMapper.selectList(queryWrapper);
    }

    @Override
    public List<NoticeVo> getAllNotice(String sortBy,String orderBy) {
        List<NoticeVo> retNoticeVos = new ArrayList<>();
        QueryWrapper<NoticeContent> queryWrapper = new QueryWrapper<>();
        String orderColumn = "id";
        if(!StringUtils.isEmpty(sortBy)){
           switch (sortBy){
               case "startTime":
                   orderColumn = "start_time";
                   break;
               case "endTime":
                   orderColumn = "end_time";
                   break;
               case "createTime":
                   orderColumn = "create_time";
                   break;
               default:
                   orderColumn="id";
                   break;
           }
        }
        boolean ifDesc = true;
        if(!StringUtils.isEmpty(orderBy)){
            if("ascend".equals(orderBy)){
                ifDesc = false;
            }
        }
        if(ifDesc) {
            queryWrapper.orderByDesc(orderColumn);
        }else {
            queryWrapper.orderByAsc(orderColumn);
        }
        List<NoticeContent> noticeContents = noticeMapper.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(noticeContents)){
            noticeContents.forEach(item->{retNoticeVos.add(toNoticeVo(item));});
        }
        return retNoticeVos;
    }

    @Override
    public void createNotice(NoticeVo notice) {
        NoticeContent noticeContent = new NoticeContent();
        if(notice != null){
            BeanUtils.copyProperties(notice,noticeContent);
            Date createTime = new Date();
            noticeContent.setCreateTime(createTime);
           // String userName = UserInfoHolder.getUserName();
           //  noticeContent.setCreateUser(userName);
            noticeMapper.insert(noticeContent);
        }
    }

    @Override
    public void deleteNotice(Integer noticeId) {
        if(noticeId == null){
            return;
        }
        NoticeContent content = noticeMapper.selectById(noticeId);
        if(content == null){
            return;
        }
        Date now = new Date();
        if(now.after(content.getEndTime())){
            throw new DSSRuntimeException("已过期公告不能删除");
        }
        noticeMapper.deleteById(noticeId);
    }

    public NoticeVo toNoticeVo(NoticeContent noticeContent){
        NoticeVo noticeVo = new NoticeVo();
        BeanUtils.copyProperties(noticeContent,noticeVo);
        Date now = new Date();
        Date startTime = noticeContent.getStartTime();
        Date endTime = noticeContent.getEndTime();
        if(now.before(startTime)){
            noticeVo.setStatus(0);
        }else if(now.after(startTime) && now.before(endTime)){
            noticeVo.setStatus(1);
        }else{
            noticeVo.setStatus(2);
        }
        return noticeVo;
    }

}
