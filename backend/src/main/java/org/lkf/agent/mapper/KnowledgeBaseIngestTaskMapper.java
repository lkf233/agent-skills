package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.lkf.agent.entity.KnowledgeBaseIngestTaskEntity;

import java.util.List;

@Mapper
public interface KnowledgeBaseIngestTaskMapper {

    @Insert("insert into kb_ingest_task(id, kb_file_id, user_id, task_status, retry_count, error_message, created_at, updated_at, del_flag) " +
            "values(#{id}, #{kbFileId}, #{userId}, #{taskStatus}, #{retryCount}, #{errorMessage}, now(), now(), 0)")
    int insert(KnowledgeBaseIngestTaskEntity entity);

    @Select("select id, kb_file_id as kbFileId, user_id as userId, task_status as taskStatus, retry_count as retryCount, " +
            "error_message as errorMessage, created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt " +
            "from kb_ingest_task where task_status in ('PENDING','FAILED') and del_flag = 0 and retry_count < 3 order by updated_at asc limit 5")
    List<KnowledgeBaseIngestTaskEntity> listPendingTasks();

    @Update("update kb_ingest_task set task_status = 'PROCESSING', updated_at = now() where id = #{id} and task_status in ('PENDING','FAILED')")
    int markProcessing(@Param("id") String id);

    @Update("update kb_ingest_task set task_status = 'SUCCESS', error_message = '', updated_at = now() where id = #{id}")
    int markSuccess(@Param("id") String id);

    @Update("update kb_ingest_task set task_status = 'FAILED', retry_count = retry_count + 1, error_message = #{errorMessage}, updated_at = now() where id = #{id}")
    int markFailed(@Param("id") String id, @Param("errorMessage") String errorMessage);
}
