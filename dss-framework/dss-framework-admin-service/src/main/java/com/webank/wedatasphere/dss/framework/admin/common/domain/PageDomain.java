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
package com.webank.wedatasphere.dss.framework.admin.common.domain;

import com.webank.wedatasphere.dss.framework.admin.common.utils.StringUtils;

public class PageDomain {

        /**
         * 当前记录起始索引
         */
        private Integer pageNum;

        /**
         * 每页显示记录数
         */
        private Integer pageSize;

        /**
         * 排序列
         */
        private String orderByColumn;

        /**
         * 排序的方向desc或者asc
         */
        private String isAsc = "asc";

        public String getOrderBy() {
            if (StringUtils.isEmpty(orderByColumn)) {
                return "";
            }
            return StringUtils.toUnderScoreCase(orderByColumn) + " " + isAsc;
        }

        public Integer getPageNum() {
            return pageNum;
        }

        public void setPageNum(Integer pageNum) {
            this.pageNum = pageNum;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }

        public String getOrderByColumn() {
            return orderByColumn;
        }

        public void setOrderByColumn(String orderByColumn) {
            this.orderByColumn = orderByColumn;
        }

        public String getIsAsc() {
            return isAsc;
        }

        public void setIsAsc(String isAsc) {
            this.isAsc = isAsc;
        }
    }
