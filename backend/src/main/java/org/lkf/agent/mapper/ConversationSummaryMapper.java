package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lkf.agent.entity.ConversationSummaryEntity;

import java.util.List;

@Mapper
public interface ConversationSummaryMapper {

    @Insert("insert into conversation_summary(id, conversation_id, range_start_seq, range_end_seq, summary_text, summary_struct_json, version, created_at, updated_at, del_flag) " +
            "values(#{id}, #{conversationId}, #{rangeStartSeq}, #{rangeEndSeq}, #{summaryText}, cast(#{summaryStructJson} as jsonb), #{version}, now(), now(), 0)")
    int insert(ConversationSummaryEntity entity);

    @Select("select id, conversation_id as conversationId, range_start_seq as rangeStartSeq, range_end_seq as rangeEndSeq, " +
            "summary_text as summaryText, summary_struct_json::text as summaryStructJson, version, " +
            "created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from conversation_summary where conversation_id = #{conversationId} and del_flag = 0 order by range_end_seq desc limit 1")
    ConversationSummaryEntity findLatestByConversationId(@Param("conversationId") String conversationId);

    @Select("select id, conversation_id as conversationId, range_start_seq as rangeStartSeq, range_end_seq as rangeEndSeq, " +
            "summary_text as summaryText, summary_struct_json::text as summaryStructJson, version, " +
            "created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from conversation_summary where conversation_id = #{conversationId} and del_flag = 0 order by range_end_seq desc limit #{limit}")
    List<ConversationSummaryEntity> listByConversationId(@Param("conversationId") String conversationId, @Param("limit") Integer limit);
}
