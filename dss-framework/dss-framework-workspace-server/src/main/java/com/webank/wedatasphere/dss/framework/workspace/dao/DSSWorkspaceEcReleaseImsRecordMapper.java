package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECReleaseImsRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DSSWorkspaceEcReleaseImsRecordMapper {

    /**
     * 插入ims发送ec历史记录
     * @param record 插入的记录
     */
    void insert(ECReleaseImsRecordDO record);

    /**
     * 查询指定时间以内的记录
     * @param intervalTime 间隔时间
     * @return
     */
    @Select("select * from dss_ec_release_ims_record where create_time >=  DATE_SUB(NOW(), INTERVAL #{intervalTime} MINUTE) and strategy_id = #{strategyId}")
    List<ECReleaseImsRecordDO> selectRecordIntervalTime(@Param("intervalTime") int intervalTime, @Param("strategyId") String strategyId);

    /**
     * 更新ims发送状态
     * @param status
     * @param recordId
     */
    void updateImsRecordStatus(@Param("status") int status, @Param("recordId") String recordId);
}
