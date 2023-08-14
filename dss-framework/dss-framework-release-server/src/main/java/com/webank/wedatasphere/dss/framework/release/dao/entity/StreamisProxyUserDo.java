package com.webank.wedatasphere.dss.framework.release.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.webank.wedatasphere.dss.framework.proxy.pojo.entity.DssProxyUserImpl;

import java.util.Date;

/**
 * Streamis 代理用户表DO对象
 * Author: xlinliu
 * Date: 2022/11/9
 */
@TableName(value = "dss_streamis_proxy_user")
public class StreamisProxyUserDo extends DssProxyUserImpl {

        private static final long serialVersionUID = 1L;

        @TableId(type = IdType.AUTO)
        private Long id;
        private String createBy;
        private Date createTime;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(String createBy) {
            this.createBy = createBy;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

}
