package com.webank.wedatasphere.dss.common.exception;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum MessageErrorCodeSummary implements LinkisErrorCode {

    GIT_KEY_LENGTH_VERIFICATION(800001,"The key length must be greater than 16 bits" +
            "(密钥长度必须大于16位)"),
    GIT_PASSWORD_OR_TOKEN_EMPTY(800001,"Password or token is empty(密码或token为空)"),

    GIT_PASSWORD_ENCRYPTION_FAILED(800001,"Encryption failed due to the following reason" +
            "(加密失败,原因为):{0}"),

    GIT_USERNAME_NOT_MATCH_TOKEN(800001,"The current username does not match the token, " +
            "please check(当前用户名 token 不匹配，请检查)"),

    GIT_TOKEN_ERROR(800001,"Please check if the token is correct(请检查token是否正确)"),

    GIT_TOKEN_VERIFICATION_FAILED(800001,"verification failed(校验失败):{0}"),

    GIT_SERVICE_ACCESS_ERROR(800001,"Token verification failed, please confirm if the current environment git " +
            "can access it normally(校验token失败，请确认当前环境git是否可以正常访问):{0}"),

    GIT_USERNAME_NOT_CHANGED(80001,"Git username cannot be changed(Git用户名不允许更换)"),

    GIT_USERNAME_EXISTS(80001,"The Git user has already been configured on {0}. " +
            "Please change the Git user and try again(该Git用户已在{0}配置，请更换Git用户重试)"),

    GIT_SERVICE_EXCEPTION(800001,
            "Failed to obtain workflow CommitId, please check if the workflow is empty or " +
                    "if the git service is abnormal(获取工作流CommitId失败，请检查工作流是否为空或git服务是否异常)"),

    GIT_PROJECT_EXISTS(80101,"project with the same name {1} already exists under git account: {0}. " +
            "Please change your git account or project name(git账号: {0} 下已存在同名项目{1}，请更换git账号或项目名称)"),

    GIT_WORKFLOW_NOT_COMMIT(80001,"The current workflow or workflow node has not been submitted to Git. " +
            "Please submit before jumping (当前工作流或工作流节点未提交到Git，请先提交后再跳转)"),

    GIT_OPERATING(80001,"There is already a workflow performing Git operations in the current project. " +
            "Please try again later(当前项目下已有工作流在进行git操作，请稍后重试)"),

    GIT_PROJECT_NOT_ASSOCIATED(80001,"DSS project: {0} is not associated with Git " +
            "(DSS项目:{0} 未关联git)"),

    GIT_UPDATE_PROJECT_FAILED(80001,"Updating the local Git project failed due to the following reason" +
            "(更新本地git项目失败，原因为):{0}"),

    GIT_CREATE_PROJECT_FAILED(80001,"Failed to create Git project, please check if the workspace " +
            "token has expired(创建Git项目失败，请检查工作空间token是否过期)"),

    GIT_PULL_FAILED(80001,"Pulling the latest Git code failed due to the following reason:" +
            "(拉取git最新代码失败，原因为):{0}"),

    GIT_SUBMIT_FAILED(80105,"Submission failed, please retry or check if the token has expired" +
            " (提交失败，请重试或检查token是否过期)"),

    GIT_TOKEN_EXPIRED(80108,"Failed to check project name, please check if workspace token " +
            "has expired (检查项目名称失败，请检查工作空间token是否过期)"),

    GIT_USER_ACTIVATE(80109,"Failed to retrieve userId, please check if the user is a Git user " +
            "and activate it (获取userId失败，请检查该用户是否为git用户并激活)"),

    GIT_SERVICE_FAILED(80109,"Failed to retrieve userId, please check if the edited user token " +
            "has expired or if the Git service is functioning properly" +
            "(获取userId失败，请检查编辑用户token是否过期或git服务是否正常)"),

    GIT_RETRIEVE_USERID_FAILED(80109,"Failed to retrieve the git user ID due to the following reason" +
            "(获取该git用户Id失败，原因为)"),

    GIT_PROJECT_CREATE_FAILED(80110,"Project creation failed, please try again later" +
            "(项目创建失败，请稍后重试)"),

    GIT_SERVICE_ABNORMAL(80110,"Please check if the user token has expired or if the Git service is " +
            "functioning properly(请检查编辑用户token是否过期或git服务是否正常)"),

    GIT_RETRIEVE_ID_FAILED(80110,"Failed to retrieve the git ID of the project due to the " +
            "following reason (获取该项目git Id 失败，原因为)"),

    GIT_ADD_USER_FAILED(80111,"Failed to add user, please check if read-only user exists or if " +
            "editing user token has expired(添加用户失败，请检查只读用户是否存在或编辑用户token是否过期)"),

    GIT_ADD_USER_SERVICE_ABNORMAL(80111,"Failed to add user, please check if the user token " +
            "has expired or if the Git service is functioning properly" +
            "(添加用户失败，请检查编辑用户token是否过期或git服务是否正常)"),

    GIT_CHECK_PROJECT_NAME_FAILED(80113,"Failed to check project name, please check if workspace " +
            "token has expired (检查项目名称失败，请检查工作空间token是否过期)"),

    GIT_JSON_PARSE_FAILED(80113,"Failed to parse JSON while checking project name, " +
            "please confirm if git is currently accessible (检查项目名称时解析JSON失败，请确认git当前是否可访问)"),

    GIT_ARCHIVE_FAILED(80115,"Archiving failed, please check if the current token has expired" +
            "(归档失败，请检查当前token是否过期)"),


    GIT_SAME_PROJECT_NAME(71000,"There is a project with the same name in Git. Please change the name " +
            "or delete the project with the same name and try again(git中存在同名项目，请更换名字或删去同名项目再重试)"),


    GIT_READ_WRITE_ACCOUNT_USERNAME(71000,"The Git read-write account username must be a virtual user" +
            " and cannot be a DSS real name user! (Git读写账号用户名必须为虚拟用户，不能是DSS实名用户！)"),

    GIT_CHECK_DUPLICATE_PROJECT(71000,"Failed to initiate a check for duplicate project names in Git, " +
            "please try again later (向Git发起检查工程名是否重复失败，请稍后重试)"),



    USER_NOT_PROJECT_RELEASE(800001,"User {0} does not have permission to publish the project {1}. " +
            "Please check and republish(用户{0}没有项目{1}发布权限，请检查后重新发布)"),


    ORCHESTRATOR_NOT_EXISTS(800001,"Orchestrator not exists(编排不存在)"),
    ORCHESTRATOR_RELEASE_VERIFICATION(800001,
            "Please submit the workflow before publishing(发布前请先提交工作流)"),

    ORCHESTRATOR_QUERY_FAILED(80001,
            "Query orchestration failed, please confirm if orchestration exists(查询编排失败，请确认编排是否存在)"),


    ORCHESTRATOR_VERSION_NOT_EXISTS(90038,"The version number of this Orchestrator does not exist. " +
            "Please check if the version number is correct (该Orchestrator的版本号不存在，请检查版本号是否正确.)"),


    ORCHESTRATOR_NOT_IN_PROJECT(90003,"{0} workflow does not belong to {1} project ({0}工作流不属于{1}项目)"),

    WORKFLOW_NOT_EXISTS(90003,"The copied workflow does not exist(复制的工作流不存在)"),

    WORKFLOW_CHANGED_NOT_SUBMITTED(800001,"The workflow has not been changed or the changes have not " +
            "been submitted. Please confirm the changes and save them before submitting " +
            " (工作流无改动或改动未提交，请确认改动并保存再进行提交)"),

    WORKFLOW_BATCH_SUBMISSION_EMPTY(80001,"The workflow for batch submission cannot be empty" +
            "(批量提交的工作流不能为空)"),

    WORKFLOW_COOKIE_INCONSISTENT(90058,"The workspace corresponding to the workflow is inconsistent " +
            "with the cookie. Please refresh the page and try again (工作流对应的工作空间与cookie中不一致，请刷新页面后重试)"),


    WORKFLOW_EXPORT_EMPTY(90037,"There are no workflows available for export in this project. " +
            "Please check if all workflows are empty(该工程没有可以导出的工作流,请检查工作流是否都为空)"),

    WORKFLOW_EXPORT_PATH_EMPTY(90067,"The workflow export generation path is empty (工作流导出生成路径为空)"),

    WORKFLOW_EXPORT_FAILED(100098,"Workflow export failed due to the following reason " +
            "(工作流导出失败，原因为):{0}"),

    WORKFLOW_LOCK_UPDATE_EXPIRED(60057,"The editing lock has expired, please refresh the page " +
            "(编辑锁已过期，请刷新页面)"),

    WORKFLOW_LOCK_UPDATE_FAILED(60059,"Workflow editing lock update error, please refresh the page " +
            "(工作流编辑锁更新出错，请刷新页面)"),


    WORKFLOW_LOCKED(80001,"The current workflow has been locked for editing by user {0}, " +
            "and the content you edited cannot be saved anymore. If you have any questions, please confirm with {0}" +
            "(当前工作流被用户{0}已锁定编辑，您编辑的内容不能再被保存。如有疑问，请与{0}确认)"),

    WORKFLOW_LOCKED_ERROR_CODE(60056,"The user has locked the editing error code, editLockInfo:{0}" +
            "(用户已锁定编辑错误码，editLockInfo: {0})"),

    WORKFLOW_LOCKED_ERROR(80001,"The current workflow {0} has been locked for editing by user {1}, " +
            "and the content you edited cannot be saved anymore. If you have any questions, please confirm with {1} " +
            "(当前工作流｛0｝被用户 ｛1｝已锁定编辑，您编辑的内容不能再被保存。如有疑问，请与｛1｝确认)"),

    WORKFLOW_NAME_DUPLICATED(90003,"Workflow names cannot be duplicated (工作流名不能重复)"),

    WORKFLOW_SUB_NAME_DUPLICATED(90003,"Sub workflow names cannot be duplicated (子工作流名不能重复)"),

    WORKFLOW_DIFF_FAILED(80001,"Failed to retrieve workflow diff content, " +
            "reason is(获取工作流diff内容失败，原因为):{0}"),


    WORKFLOW_CLUSTER_REFERENCED(90054,"Cluster {0} is referenced in the workflow node and cannot be deleted " +
            "(集群 ｛0｝ 在工作流节点中被引用，不允许删除)"),

    WORKFLOW_NODE_NOT_IN_ORCHESTRATION(80001,"This workflow node does not have a corresponding orchestration " +
            "(该工作流节点没有对应编排)"),

    WORKFLOW_EXPORT_SIZE_LIMIT(100098,"Workflow export failed due to the total export size exceeding {0}GB" +
            "(工作流导出失败，原因为本次导出总大小超过{0}GB)"),

    WORKFLOW_EXIT_PRODUCTION_FAILED(80001,"Failed to exit the production center correctly or lost production, " +
            "please refresh the page and try again (未正确退出生产中心或流失生产中，请刷新页面后重试！)"),


    WORKFLOW_COOKIE_INCONSISTENT_ERROR(63335,"The workspace where the workflow is located is inconsistent with the cookie. " +
            "Please refresh the page and publish again! (工作流所在工作空间和cookie中不一致，请刷新页面后，再次发布！)"),


    NODE_EXPORT_TIMEOUT(90071,"Export node timeout (导出节点超时)"),

    NODE_EXECUTE_DURING(90014,"The current operation is not allowed during job execution " +
            "(job执行中，不允许执行当前操作)"),

    NODE_EXPORT_FAILED(90070,"Node export failed, please try again (有节点导出失败，请重试):{0}"),

    NODE_INFO_NOT_FOUND(90004,"{0} node information not found, please check if the node has been deleted," +
            " (未找到｛0｝节点信息，请查看节点是否被删除)"),

    NODE_NAME_DUPLICATED(80001,"Duplicate node names. There are duplicate nodes in different workflows " +
            "(or sub workflows) in the project. Please modify the node names to avoid duplication. " +
            "Duplicate nodes cannot have the same name:{0} " +
            "(重复的节点名称。项目中不同工作流（或子工作流）里存在重名节点，请修改节点名避免重名。重名节点: {0})"),

    NODE_CONVERT_TO_DOLPHINSCHEDULER_FAILED(90321,"Failed to convert workflow node {0} to DolphinScheduler node! " +
            "工作流节点 {0} 转换成DolphinScheduler节点失败！"),

    WORKSPACE_COOKIE_INCONSISTENT(90053,"The current workspace is inconsistent with the cookie. " +
            "Please refresh the page and proceed with the operation (当前工作空间与cookie中的不一致，重新刷新页面后在操作)"),


    WORKSPACE_USER_NOT_ADMIN(90054,"{0} user is not the current workspace administrator and " +
            "does not have permission to perform this operation! ({0} 用户不是当前工作空间管理员,无权限进行该操作!)"),

    WORKSPACE_NOT_EXISTS(30021,"{0} workspace does not exist!({0} 工作空间不存在!)"),

    WORKSPACE_PERMISSION_FAILED(80000,"Unauthorized operation (无权限操作)"),

    WORKSPACE_ID_INCONSISTENT(80001,"The request parameter's workspace ID does not match the workspace ID in the " +
            "cookie. Please switch to the correct workspace before proceeding. " +
            "(请求参数的workspaceId和cookie中的workspaceId不一致，请切换至正确的workspace再操作。)"),



    PROJECT_NOT_IN_WORKSPACE(90003,"The project does not exist in the current workspace" +
            "(项目不存在于当前工作空间中)"),

    PROJECT_NOT_EXISTS(600001,"The project does not exist! (工程不存在!)"),

    PROJECT_DELETE_FAILED(600002,"Failed to delete project, do not have deletion permission!" +
            "(刪除工程失敗，沒有删除权限!)"),


    DATA_SERVICE_GET_LIBRARY_TABLE_FAILED(800024,"Failed to retrieve library table information, " +
            "script execution error! (获取库表信息失败，执行脚本出错！)"),

    DATA_SERVICE_API_CONTENT_ABNORMAL(800002,"Abnormal query of data service API content" +
            "(查询数据服务API内容异常)"),

    DATA_SERVICE_API_CONTENT_NO_PERMISSION(800003,"not have permission to view the content of the data service API. " +
                          "Please authorize the bill of lading first (没有权限查看数据服务API内容，请先提单授权)"),


    DATA_SERVICE_API_VERSION_EMPTY(800032,"The data service API version record is empty {0}" +
            "(数据服务API版本记录为空 {0})"),

    WTSS_PUBLISHING_USER_NOT_EXISTS(100323,"The currently set publishing user: {0} does not exist in the Schedulis system. " +
            "Please follow the instructions provided in 'View Solution' to submit a ticket on ITSM!" +
            " (当前设置的发布用户: {0}, 在 Schedulis 系统中不存在，请根据'查看解决方案'中给出的指引在ITSM上提单！)"),

    WTSS_NEW_PROJECT_FAILED(90008,"Schedulis new project failed, reason: {0} (Schedulis 新建工程失败, 原因: {0})")



    ;



    private int errorCode;
    private String errorDesc;


    MessageErrorCodeSummary(int errorCode, String errorDesc) {
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorDesc() {
        return errorDesc;
    }


}
