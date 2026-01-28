package com.webank.wedatasphere.dss.datamap.dao;

import com.webank.wedatasphere.dss.datamap.dao.entity.DatasetScanRecordDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Author: xlinliu
 * Date: 2024/10/31
 */
@Mapper
public interface DatasetScanRecordMapper {

    @Insert("INSERT INTO dss_dataset_scan_record (path, task_id, row_size, has_sensitive_info, scan_info, " +
            "read_history, create_time, update_time) " +
            "VALUES (#{path}, #{taskId}, #{rowSize}, #{hasSensitiveInfo}, #{scanInfo}, #{readHistory}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(DatasetScanRecordDO datasetScanRecordDO);

    @Insert("<script>"+
            "INSERT INTO dss_dataset_scan_record (path, task_id, row_size, has_sensitive_info, scan_info, " +
            "read_history, create_time, update_time) " +
            "VALUES" +
            "<foreach collection='datasetScanRecordDOList' item='item' separator=',' index='index'>" +
            "(#{item.path}, #{item.taskId}, #{item.rowSize}, #{item.hasSensitiveInfo}, #{item.scanInfo}," +
            " #{item.readHistory},now(), now())" +
            "</foreach>"+
            "</script>"
    )
    void batchInsert(@Param("datasetScanRecordDOList") List<DatasetScanRecordDO> datasetScanRecordDOList);

    @Select("SELECT * FROM dss_dataset_scan_record WHERE id = #{id}")
    DatasetScanRecordDO findById(Long id);

    @Select("SELECT * FROM dss_dataset_scan_record WHERE task_id = #{taskId}")
    List<DatasetScanRecordDO> findByTaskId(String taskId);

    @Select("SELECT * FROM dss_dataset_scan_record WHERE task_id = #{taskId} AND path = #{path}")
    List<DatasetScanRecordDO> findByTaskIdAndPath(@Param("taskId") String taskId, @Param("path") String path);

    @Select("SELECT id, path, task_d, row_size, has_sensitive_info, read_history, create_time, update_time FROM " +
            "dss_dataset_scan_record WHERE task_id = #{taskId} AND path = #{path}")
    List<DatasetScanRecordDO> findByTaskIdAndPathNoScanInfo( @Param("taskId") String taskId, @Param("path") String path);


    @Update("UPDATE dss_dataset_scan_record SET path = #{path}, row_size = #{rowSize}, has_sensitive_info = #{hasSensitiveInfo}, scan_info = #{scanInfo}, read_history = #{readHistory}, update_time = #{updateTime} WHERE id = #{id}")
    void update(DatasetScanRecordDO datasetScanRecordDO);

    @Update("UPDATE dss_dataset_scan_record SET read_history = #{readHistory}, update_time = now() WHERE id = #{id}")
    void updateReadHistory(@Param("id") Long id, @Param("readHistory") String readHistory);


    @Delete("DELETE FROM dss_dataset_scan_record WHERE id = #{id}")
    void deleteById(Long id);


}