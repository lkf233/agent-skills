package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lkf.agent.entity.ConversationMessageEntity;

import java.util.List;

@Mapper
public interface ConversationMessageMapper {

    @Insert("insert into conversation_message(id, conversation_id, user_id, role, content, metadata_json, token_input, token_output, seq_no, created_at, updated_at, del_flag) " +
            "values(#{id}, #{conversationId}, #{userId}, #{role}, #{content}, cast(#{metadataJson} as jsonb), #{tokenInput}, #{tokenOutput}, #{seqNo}, now(), now(), 0)")
    int insert(ConversationMessageEntity entity);

    @Select("select coalesce(max(seq_no), 0) from conversation_message where conversation_id = #{conversationId} and del_flag = 0")
    Integer findMaxSeqNo(@Param("conversationId") String conversationId);

    @Select("select count(1) from conversation_message where conversation_id = #{conversationId} and del_flag = 0")
    Long countByConversationId(@Param("conversationId") String conversationId);

    @Select("select id, conversation_id as conversationId, user_id as userId, role, content, metadata_json::text as metadataJson, " +
            "token_input as tokenInput, token_output as tokenOutput, seq_no as seqNo, " +
            "created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from conversation_message where conversation_id = #{conversationId} and del_flag = 0 order by seq_no desc limit #{limit} offset #{offset}")
    List<ConversationMessageEntity> listByConversationId(@Param("conversationId") String conversationId,
                                                          @Param("offset") Integer offset,
                                                          @Param("limit") Integer limit);

    @Select("select id, conversation_id as conversationId, user_id as userId, role, content, metadata_json::text as metadataJson, " +
            "token_input as tokenInput, token_output as tokenOutput, seq_no as seqNo, " +
            "created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from conversation_message " +
            "where conversation_id = #{conversationId} and del_flag = 0 and seq_no between #{startSeq} and #{endSeq} order by seq_no asc")
    List<ConversationMessageEntity> listByConversationIdAndSeqRange(@Param("conversationId") String conversationId,
                                                                     @Param("startSeq") Integer startSeq,
                                                                     @Param("endSeq") Integer endSeq);
}
