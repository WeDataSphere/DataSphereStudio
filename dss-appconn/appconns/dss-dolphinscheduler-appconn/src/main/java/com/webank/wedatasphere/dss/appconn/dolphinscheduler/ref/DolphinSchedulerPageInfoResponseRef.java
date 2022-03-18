package com.webank.wedatasphere.dss.appconn.dolphinscheduler.ref;

import com.webank.wedatasphere.dss.common.utils.ClassUtils;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefImpl;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author enjoyyin
 * @date 2022-03-17
 * @since 0.5.0
 */
public class DolphinSchedulerPageInfoResponseRef extends ResponseRefImpl {
    public DolphinSchedulerPageInfoResponseRef(String responseBody, int status, String errorMsg, Map<String, Object> responseMap) {
        super(responseBody, status, errorMsg, responseMap);
    }

    public int getTotalPage() {
        return (int) toMap().get("totalPage");
    }

    public int getCurrentPage() {
        return (int) toMap().get("currentPage");
    }

    public int getTotal() {
        return (int) toMap().get("total");
    }

    public List<Map<String, Object>> getTotalList() {
        if(!toMap().containsKey("totalList") || toMap().get("totalList") == null) {
            return new ArrayList<>(0);
        }
        return  (List<Map<String, Object>>) toMap().get("totalList");
    }

    public <T> List<T> getTotalList(Class<T> clazz) {
        List<Map<String, Object>> totalList = getTotalList();
        return totalList.stream().map(DSSExceptionUtils.map(map -> {
            T t = ClassUtils.getInstanceOrWarn(clazz);
            BeanUtils.populate(t, map);
            return t;
        })).collect(Collectors.toList());
    }

}