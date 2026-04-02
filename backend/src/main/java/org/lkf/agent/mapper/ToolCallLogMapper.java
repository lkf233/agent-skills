package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.lkf.agent.entity.ToolCallLogEntity;

@Mapper
public interface ToolCallLogMapper {

    @Insert("insert into tool_call_log(id, conversation_id, message_id, tool_def_id, tool_name, request_json, response_json, status, latency_ms, created_at) " +
            "values(#{id}, #{conversationId}, #{messageId}, #{toolDefId}, #{toolName}, cast(#{requestJson} as jsonb), cast(#{responseJson} as jsonb), #{status}, #{latencyMs}, now())")
    int insert(ToolCallLogEntity entity);
}
