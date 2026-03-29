package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.lkf.agent.entity.KnowledgeBaseChunkEntity;

@Mapper
public interface KnowledgeBaseChunkMapper {

    @Delete("delete from kb_chunk where kb_file_id = #{kbFileId}")
    int deleteByKbFileId(@Param("kbFileId") String kbFileId);

    @Insert("insert into kb_chunk(id, kb_id, kb_file_id, user_id, chunk_no, content, content_tsv, embedding, created_at, updated_at, del_flag) " +
            "values(#{id}, #{kbId}, #{kbFileId}, #{userId}, #{chunkNo}, #{content}, to_tsvector('simple', #{content}), " +
            "cast(#{embeddingVector} as vector), now(), now(), 0)")
    int insert(KnowledgeBaseChunkEntity entity);
}
