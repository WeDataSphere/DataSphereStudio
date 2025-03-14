/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.git.receiver

import com.webank.wedatasphere.dss.git.common.protocol.request.{GitAddMemberRequest, GitArchiveProjectRequest, GitBatchCommitRequest, GitCheckProjectRequest, GitCommitInfoBetweenRequest, GitCommitRequest, GitConnectRequest, GitCreateProjectRequest, GitCurrentCommitRequest, GitDeleteRequest, GitDiffRequest, GitDiffTargetCommitRequest, GitFileContentRequest, GitHistoryRequest, GitRemoveRequest, GitRenameRequest, GitRevertRequest, GitSearchRequest, GitUserByWorkspaceIdRequest, GitUserInfoByRequest, GitUserInfoRequest, GitUserUpdateRequest,GitDiffFileContentRequest}
import com.webank.wedatasphere.dss.git.manage.GitProjectManager
import com.webank.wedatasphere.dss.git.service.{DSSGitProjectManagerService, DSSGitWorkflowManagerService}
import org.apache.linkis.rpc.{Receiver, Sender}

import scala.concurrent.duration.Duration

class DSSGitReceiver(gitProjectManagerService: DSSGitProjectManagerService, gitWorkflowManagerService: DSSGitWorkflowManagerService) extends Receiver {

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any  = message match {
    case gitCreateProjectRequest: GitCreateProjectRequest =>
      gitProjectManagerService.create(gitCreateProjectRequest)
    case gitArchiveProjectRequest: GitArchiveProjectRequest =>
      gitProjectManagerService.archive(gitArchiveProjectRequest)
    case gitCheckProjectRequest: GitCheckProjectRequest =>
      gitProjectManagerService.checkProject(gitCheckProjectRequest)
    case gitDiffRequest: GitDiffRequest =>
      gitWorkflowManagerService.diff(gitDiffRequest)
    case gitCommitRequest: GitCommitRequest =>
      gitWorkflowManagerService.commit(gitCommitRequest)
    case gitSearchRequest: GitSearchRequest =>
      gitWorkflowManagerService.search(gitSearchRequest)
    case gitDeleteRequest: GitDeleteRequest =>
      gitWorkflowManagerService.delete(gitDeleteRequest)
    case gitFileContentRequest: GitFileContentRequest =>
      gitWorkflowManagerService.getFileContent(gitFileContentRequest)
    case gitHistoryRequest: GitHistoryRequest =>
      gitWorkflowManagerService.getHistory(gitHistoryRequest)
    case gitCommitInfoBetweenRequest: GitCommitInfoBetweenRequest =>
      gitWorkflowManagerService.getHistory(gitCommitInfoBetweenRequest)
    case gitUserUpdateRequest: GitUserUpdateRequest =>
      GitProjectManager.associateGit(gitUserUpdateRequest)
    case gitCurrentCommitRequest: GitCurrentCommitRequest =>
      gitWorkflowManagerService.getCurrentCommit(gitCurrentCommitRequest)
    case gitRevertRequest: GitRevertRequest =>
      gitWorkflowManagerService.gitCheckOut(gitRevertRequest)
    case gitRemoveRequest: GitRemoveRequest =>
      gitWorkflowManagerService.removeFile(gitRemoveRequest)
    case gitRenameRequest: GitRenameRequest =>
      gitWorkflowManagerService.rename(gitRenameRequest)
    case gitUserInfoByTypeRequest: GitUserInfoByRequest =>
      GitProjectManager.getGitUserByType(gitUserInfoByTypeRequest)
    case gitDiffTargetCommitRequest: GitDiffTargetCommitRequest =>
      gitWorkflowManagerService.diffGit(gitDiffTargetCommitRequest)
    case gitBatchCommitRequest: GitBatchCommitRequest =>
      gitWorkflowManagerService.batchCommit(gitBatchCommitRequest)
    case gitUserByWorkspaceIdRequest: GitUserByWorkspaceIdRequest =>
      gitProjectManagerService.getProjectGitUserInfo(gitUserByWorkspaceIdRequest)
    case gitAddMemberRequest: GitAddMemberRequest =>
      gitProjectManagerService.addMember(gitAddMemberRequest)
    case  gitDiffFileContentRequest: GitDiffFileContentRequest =>
      gitWorkflowManagerService.getDiffFileContent(gitDiffFileContentRequest)
    case _ => None
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}
}
