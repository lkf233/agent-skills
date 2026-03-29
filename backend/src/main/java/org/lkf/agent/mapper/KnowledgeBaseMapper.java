package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lkf.agent.entity.KnowledgeBaseEntity;

import java.util.List;

/**
 * 知识库数据访问接口。
 */
@Mapper
public interface KnowledgeBaseMapper {

    /**
     * 新增知识库。
     *
     * @param entity 知识库实体
     * @return 影响行数
     */
    @Insert("insert into knowledge_base(id, user_id, name, description, embedding_provider, embedding_model, status, created_at, updated_at, del_flag) " +
            "values(#{id}, #{userId}, #{name}, #{description}, #{embeddingProvider}, #{embeddingModel}, #{status}, now(), now(), 0)")
    int insert(KnowledgeBaseEntity entity);

    /**
     * 按用户查询知识库列表。
     *
     * @param userId 用户ID
     * @return 知识库列表
     */
    @Select("select id, user_id as userId, name, description, embedding_provider as embeddingProvider, " +
            "embedding_model as embeddingModel, status, created_at at time zone 'UTC' as createdAt, " +
            "updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from knowledge_base where user_id = #{userId} and del_flag = 0 order by updated_at desc")
    List<KnowledgeBaseEntity> listByUserId(@Param("userId") Long userId);

    @Select("select id, user_id as userId, name, description, embedding_provider as embeddingProvider, " +
            "embedding_model as embeddingModel, status, created_at at time zone 'UTC' as createdAt, " +
            "updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from knowledge_base where id = #{id} and user_id = #{userId} and del_flag = 0")
    KnowledgeBaseEntity findByIdAndUserId(@Param("id") String id, @Param("userId") Long userId);
}
