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
package com.webank.wedatasphere.dss.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author enjoyyin
 * @date 2022-03-12
 * @since 0.5.0
 */
public class MapUtils {

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }

    public static <K, V> Map<K, V> newMap(K key, V value) {
        Map<K, V> map = new HashMap<>(1);
        map.put(key, value);
        return map;
    }

    public static <K, V> Map<K, V> newMap(K key1, V value1, K key2, V value2) {
        Map<K, V> map = new HashMap<>(2);
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static <K, V> MapBuilder<K, V> newMapBuilder() {
        return new MapBuilder<K, V>();
    }

    public static Map<String, Object> newCommonMap(String key, Object value) {
        return newMap(key, value);
    }

    public static Map<String, Object> newCommonMap(String key1, Object value1, String key2, Object value2) {
        return newMap(key1, value1, key2, value2);
    }

    public static MapBuilder<String, Object> newCommonMapBuilder() {
        return new MapBuilder<String, Object>();
    }

    public static class MapBuilder<K, V> {

        private Map<K, V> map = new HashMap<>();

        public MapBuilder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public Map<K, V> build() {
            return map;
        }
    }
}
