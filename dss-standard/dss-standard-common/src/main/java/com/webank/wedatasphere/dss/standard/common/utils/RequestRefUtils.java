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
package com.webank.wedatasphere.dss.standard.common.utils;

import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationFailedException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

/**
 * @author enjoyyin
 * @date 2022-03-11
 * @since 0.5.0
 */
public class RequestRefUtils {
    /**
     * 获取operation的RequestRef具体实现类的一个实例
     * @param operation operation
     * @return 一个具体的RequestRef实例
     * @param <T> RequestRef的具体实现类
     */
    public static <T> T getRequestRef(Object operation) {
        ParameterizedType parameterizedType = (ParameterizedType) operation.getClass().getGenericSuperclass();
        return newInstance(operation, parameterizedType);
    }

    @Deprecated
    public static <T> T getRequestRef(Object operation, Class<T> clazz) {
        return Stream.of(operation.getClass().getGenericInterfaces())
            .filter(c -> clazz.isAssignableFrom((Class<?>) c)).map(c -> {
                ParameterizedType parameterizedType = (ParameterizedType) c;
                return (T) newInstance(operation, parameterizedType);
        }).findAny().orElseThrow(() -> new ExternalOperationFailedException(50063, "Cannot find the real requestRef of " + operation.getClass() + "."));
    }

    private static <T> T newInstance(Object operation, ParameterizedType parameterizedType) {
        Type[] types = parameterizedType.getActualTypeArguments();
        if(types.length > 0 && types.length <= 2) {
            Class<T> t = (Class<T>) types[0];
            try {
                // please notice, don't try to use ClassUtils.getInstance() to create it.
                return t.newInstance();
            } catch (Exception e) {
                throw new ExternalOperationFailedException(50063, "create the instance of " + types[0] + " failed.", e);
            }
        } else {
            throw new ExternalOperationFailedException(50063, "Cannot find the real requestRef of " + operation.getClass().getSimpleName() + ".");
        }
    }

}
