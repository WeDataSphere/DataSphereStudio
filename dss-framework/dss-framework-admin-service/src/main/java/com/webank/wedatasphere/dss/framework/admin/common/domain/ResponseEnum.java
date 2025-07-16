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

//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.ToString;

//@Getter
//@ToString
//@AllArgsConstructor
public enum ResponseEnum {
    /*
        case -1 => 401
                case 0 => 200
                case 1 => 400
                case 2 => 412
                case 3 => 403
                case 4 => 206*/
    //-1 no login, 0 success, 1 error, 2 validate failed, 3 auth failed, 4 warning
    NO_LOGIN(-1, "no login"),
    SUCCESS(0, "success"),
    ERROR(1, "error"),
    VALIDATE_FAILED(2, "validate failed"),
    AUTH_FAILED(3, "auth failed"),
    WARNING(4,"warning");

    ResponseEnum(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResponseEnum{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }

    private Integer status;
    private String  message;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
