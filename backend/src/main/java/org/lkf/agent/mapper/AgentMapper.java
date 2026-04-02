package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.lkf.agent.entity.AgentEntity;

import java.util.List;

@Mapper
public interface AgentMapper {

    @Insert("insert into agent(id, user_id, name, description, avatar_url, status, model_config_json, system_prompt, created_at, updated_at, del_flag) " +
            "values(#{id}, #{userId}, #{name}, #{description}, #{avatarUrl}, #{status}, cast(#{modelConfigJson} as jsonb), #{systemPrompt}, now(), now(), 0)")
    int insert(AgentEntity entity);

    @Select("select id, user_id as userId, name, description, avatar_url as avatarUrl, status, " +
            "model_config_json::text as modelConfigJson, system_prompt as systemPrompt, " +
            "created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from agent where id = #{id} and user_id = #{userId} and del_flag = 0")
    AgentEntity findByIdAndUserId(@Param("id") String id, @Param("userId") Long userId);

    @Select("select id, user_id as userId, name, description, avatar_url as avatarUrl, status, " +
            "model_config_json::text as modelConfigJson, system_prompt as systemPrompt, " +
            "created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from agent where user_id = #{userId} and del_flag = 0 order by updated_at desc")
    List<AgentEntity> listByUserId(@Param("userId") Long userId);

    @Update("update agent set name = #{name}, description = #{description}, avatar_url = #{avatarUrl}, " +
            "system_prompt = #{systemPrompt}, updated_at = now() where id = #{id} and user_id = #{userId} and del_flag = 0")
    int update(AgentEntity entity);

    @Update("update agent set del_flag = 1, updated_at = now() where id = #{id} and user_id = #{userId} and del_flag = 0")
    int softDelete(@Param("id") String id, @Param("userId") Long userId);

    @Update("update agent_tool_rel set del_flag = 1, updated_at = now() where agent_id = #{agentId} and del_flag = 0")
    int softDeleteToolRelations(@Param("agentId") String agentId);

    @Insert("insert into agent_tool_rel(id, agent_id, tool_id, priority, enabled, created_at, updated_at, del_flag) " +
            "values(#{id}, #{agentId}, #{toolId}, #{priority}, 1, now(), now(), 0)")
    int insertToolRelation(@Param("id") String id, @Param("agentId") String agentId, @Param("toolId") String toolId, @Param("priority") Integer priority);

    @Select("select tool_id from agent_tool_rel where agent_id = #{agentId} and del_flag = 0 and enabled = 1 order by priority asc, created_at asc")
    List<String> listToolIdsByAgentId(@Param("agentId") String agentId);

    @Update("update agent_kb_rel set del_flag = 1, updated_at = now() where agent_id = #{agentId} and del_flag = 0")
    int softDeleteKnowledgeBaseRelations(@Param("agentId") String agentId);

    @Insert("insert into agent_kb_rel(id, agent_id, kb_id, priority, enabled, created_at, updated_at, del_flag) " +
            "values(#{id}, #{agentId}, #{knowledgeBaseId}, #{priority}, 1, now(), now(), 0)")
    int insertKnowledgeBaseRelation(@Param("id") String id, @Param("agentId") String agentId, @Param("knowledgeBaseId") String knowledgeBaseId,
                                    @Param("priority") Integer priority);

    @Select("select kb_id from agent_kb_rel where agent_id = #{agentId} and del_flag = 0 and enabled = 1 order by priority asc, created_at asc")
    List<String> listKnowledgeBaseIdsByAgentId(@Param("agentId") String agentId);
}
