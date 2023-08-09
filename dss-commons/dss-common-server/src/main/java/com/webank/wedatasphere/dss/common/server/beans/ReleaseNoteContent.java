package com.webank.wedatasphere.dss.common.server.beans;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName(value = "dss_release_note_content")
public class ReleaseNoteContent  implements Serializable {
    /**
     * 名称
     */
    private String name;
    /**
     *  标题
     */
    private String title;

    private String url;
    /**
     * url类型: 0-内部系统，1-外部系统；默认是内部
     */
    private Integer urlType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getUrlType() {
        return urlType;
    }

    public void setUrlType(Integer urlType) {
        this.urlType = urlType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}