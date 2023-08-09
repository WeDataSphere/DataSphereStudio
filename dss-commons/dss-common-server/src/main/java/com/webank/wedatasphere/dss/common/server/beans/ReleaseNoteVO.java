package com.webank.wedatasphere.dss.common.server.beans;

import java.util.List;

public class ReleaseNoteVO {
    private String name;


    private String title;

    private List<ReleaseNoteContent> contents;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ReleaseNoteContent> getContents() {
        return contents;
    }

    public void setContents(List<ReleaseNoteContent> contents) {
        this.contents = contents;
    }
}