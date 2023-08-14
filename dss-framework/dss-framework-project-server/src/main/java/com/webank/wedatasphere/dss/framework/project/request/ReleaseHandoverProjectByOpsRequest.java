
package com.webank.wedatasphere.dss.framework.project.request;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class ReleaseHandoverProjectByOpsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "权限接收者不能为空")
    private String recipient;

    @NotNull(message = "交接类型不能为空")
    private String type;

    private String workspaceName;

    private String projectName;

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public String toString() {
        return "ReleaseHandoverProjectByOpsRequest{" +
                "recipient='" + recipient + '\'' +
                ", type='" + type + '\'' +
                ", workspaceName='" + workspaceName + '\'' +
                ", projectName='" + projectName + '\'' +
                '}';
    }
}
