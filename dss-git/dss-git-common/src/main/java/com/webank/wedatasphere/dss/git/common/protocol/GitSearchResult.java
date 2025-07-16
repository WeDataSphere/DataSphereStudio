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
package com.webank.wedatasphere.dss.git.common.protocol;

import java.util.List;

public class GitSearchResult {
    private String path;
    private List<GitSearchLine> keyLines;

    public GitSearchResult() {
    }

    public GitSearchResult(String path, List<GitSearchLine> keyLines) {
        this.path = path;
        this.keyLines = keyLines;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<GitSearchLine> getKeyLines() {
        return keyLines;
    }

    public void setKeyLines(List<GitSearchLine> keyLines) {
        this.keyLines = keyLines;
    }
}
