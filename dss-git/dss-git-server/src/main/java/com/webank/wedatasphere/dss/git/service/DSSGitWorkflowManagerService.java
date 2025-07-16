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
package com.webank.wedatasphere.dss.git.service;


import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.git.common.protocol.request.*;
import com.webank.wedatasphere.dss.git.common.protocol.response.*;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface DSSGitWorkflowManagerService {
    GitDiffResponse diff(GitDiffRequest request) throws DSSErrorException;

    GitCommitResponse commit(GitCommitRequest request) throws DSSErrorException;

    GitSearchResponse search(GitSearchRequest request) throws DSSErrorException;

    GitDeleteResponse delete(GitDeleteRequest request) throws DSSErrorException;

    GitFileContentResponse getFileContent(GitFileContentRequest request) throws DSSErrorException;

    GitHistoryResponse getHistory(GitHistoryRequest request) throws DSSErrorException;

    GitCommitResponse getCurrentCommit(GitCurrentCommitRequest request) throws DSSErrorException;

    GitCommitResponse gitCheckOut(GitRevertRequest request) throws DSSErrorException;

    GitCommitResponse removeFile(GitRemoveRequest request) throws DSSErrorException;

    GitCommitResponse rename(GitRenameRequest request) throws DSSErrorException;

    GitHistoryResponse getHistory(GitCommitInfoBetweenRequest request) throws DSSErrorException;

    GitDiffResponse diffGit(GitDiffTargetCommitRequest request) throws DSSErrorException;

    GitCommitResponse batchCommit(GitBatchCommitRequest request) throws DSSErrorException;

    Repository getRepository(File repoDir, String projectName, Long workspaceId, String gitUser, String gitToken, String gitUrl) throws DSSErrorException;

    GitDiffFileContentResponse getDiffFileContent(GitDiffFileContentRequest request) throws DSSErrorException;

}
