package com.webank.wedatasphere.dss.bean.flowcomponent;

/**
 * dss 工作流的边
 * Author: xlinliu
 * Date: 2023/10/12
 */
public class Edge {
    private String source;
    private String target;
    private String sourceLocation;
    private String targetLocation;

    public Edge() {
    }

    public Edge(String source, String target, String sourceLocation, String targetLocation) {
        this.source = source;
        this.target = target;
        this.sourceLocation = sourceLocation;
        this.targetLocation = targetLocation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }
}
