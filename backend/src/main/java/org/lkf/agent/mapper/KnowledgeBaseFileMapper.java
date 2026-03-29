package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.lkf.agent.entity.KnowledgeBaseFileEntity;

import java.util.List;

@Mapper
public interface KnowledgeBaseFileMapper {

    @Insert("insert into kb_file(id, kb_id, user_id, file_name, storage_path, mime_type, size_bytes, parse_status, error_message, created_at, updated_at, del_flag) " +
            "values(#{id}, #{kbId}, #{userId}, #{fileName}, #{storagePath}, #{mimeType}, #{sizeBytes}, #{parseStatus}, #{errorMessage}, now(), now(), 0)")
    int insert(KnowledgeBaseFileEntity entity);

    @Select("select id, kb_id as kbId, user_id as userId, file_name as fileName, storage_path as storagePath, " +
            "mime_type as mimeType, size_bytes as sizeBytes, parse_status as parseStatus, error_message as errorMessage, " +
            "created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from kb_file where kb_id = #{kbId} and user_id = #{userId} and del_flag = 0 order by created_at desc")
    List<KnowledgeBaseFileEntity> listByKbId(@Param("kbId") String kbId, @Param("userId") Long userId);

    @Select("select id, kb_id as kbId, user_id as userId, file_name as fileName, storage_path as storagePath, " +
            "mime_type as mimeType, size_bytes as sizeBytes, parse_status as parseStatus, error_message as errorMessage, " +
            "created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from kb_file where id = #{id} and del_flag = 0")
    KnowledgeBaseFileEntity findById(@Param("id") String id);

    @Update("update kb_file set parse_status = #{parseStatus}, error_message = #{errorMessage}, updated_at = now() where id = #{id}")
    int updateParseStatus(@Param("id") String id, @Param("parseStatus") String parseStatus, @Param("errorMessage") String errorMessage);
}
