package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lkf.agent.entity.ConversationEntity;

import java.util.List;

/**
 * 会话数据访问接口。
 */
@Mapper
public interface ConversationMapper {

    /**
     * 新增会话。
     *
     * @param conversationEntity 会话实体
     * @return 影响行数
     */
    @Insert("insert into conversation(id, user_id, title, status, created_at, updated_at, del_flag) " +
            "values(#{id}, #{userId}, #{title}, #{status}, now(), now(), 0)")
    int insert(ConversationEntity conversationEntity);

    /**
     * 按用户查询会话列表。
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @Select("select id, user_id as userId, title, status, created_at at time zone 'UTC' as createdAt, " +
            "updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from conversation where user_id = #{userId} and status = 'ACTIVE' and del_flag = 0 order by created_at desc")
    List<ConversationEntity> listByUserId(@Param("userId") Long userId);
}
