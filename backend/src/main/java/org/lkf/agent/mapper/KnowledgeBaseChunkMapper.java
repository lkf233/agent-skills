package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lkf.agent.entity.KnowledgeBaseChunkEntity;

import java.util.List;

@Mapper
public interface KnowledgeBaseChunkMapper {

    @Delete("delete from kb_chunk where kb_file_id = #{kbFileId}")
    int deleteByKbFileId(@Param("kbFileId") String kbFileId);

    @Insert("insert into kb_chunk(id, kb_id, kb_file_id, user_id, chunk_no, content, content_tsv, embedding, created_at, updated_at, del_flag) " +
            "values(#{id}, #{kbId}, #{kbFileId}, #{userId}, #{chunkNo}, #{content}, to_tsvector('simple', #{content}), " +
            "cast(#{embeddingVector} as vector), now(), now(), 0)")
    int insert(KnowledgeBaseChunkEntity entity);

    @Select({
            "<script>",
            "select content from kb_chunk",
            "where del_flag = 0",
            "and kb_id in",
            "<foreach collection='kbIds' item='kbId' open='(' separator=',' close=')'>",
            "#{kbId}",
            "</foreach>",
            "and content ilike concat('%', #{query}, '%')",
            "order by updated_at desc",
            "limit #{limit}",
            "</script>"
    })
    List<String> searchByKbIdsAndKeyword(@Param("kbIds") List<String> kbIds, @Param("query") String query, @Param("limit") Integer limit);
}
