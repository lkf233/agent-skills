package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.lkf.agent.entity.ToolDefEntity;

import java.util.List;

@Mapper
public interface ToolDefMapper {

    @Insert("insert into tool_def(id, user_id, name, description, tool_type, config_json, auth_config_json, status, created_at, updated_at, del_flag) " +
            "values(#{id}, #{userId}, #{name}, #{description}, #{toolType}, cast(#{configJson} as jsonb), cast(#{authConfigJson} as jsonb), #{status}, now(), now(), 0)")
    int insert(ToolDefEntity entity);

    @Select({
            "<script>",
            "select id, user_id as userId, name, description, tool_type as toolType,",
            "config_json::text as configJson, auth_config_json::text as authConfigJson, status,",
            "created_at at time zone 'UTC' as createdAt, updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag",
            "from tool_def",
            "where user_id = #{userId} and del_flag = 0",
            "<if test='keyword != null'>",
            "and (name like concat('%', #{keyword}, '%') or description like concat('%', #{keyword}, '%'))",
            "</if>",
            "<if test='toolType != null'>",
            "and tool_type = #{toolType}",
            "</if>",
            "<if test='status != null'>",
            "and status = #{status}",
            "</if>",
            "order by updated_at desc",
            "</script>"
    })
    List<ToolDefEntity> listByUserId(@Param("userId") Long userId, @Param("keyword") String keyword,
                                     @Param("toolType") String toolType, @Param("status") String status);

    @Select("select id, user_id as userId, name, description, tool_type as toolType, config_json::text as configJson, " +
            "auth_config_json::text as authConfigJson, status, created_at at time zone 'UTC' as createdAt, " +
            "updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from tool_def where id = #{id} and user_id = #{userId} and del_flag = 0")
    ToolDefEntity findByIdAndUserId(@Param("id") String id, @Param("userId") Long userId);

    @Update("update tool_def set name = #{name}, description = #{description}, status = #{status}, " +
            "config_json = cast(#{configJson} as jsonb), auth_config_json = cast(#{authConfigJson} as jsonb), " +
            "updated_at = now() where id = #{id} and user_id = #{userId} and del_flag = 0")
    int update(ToolDefEntity entity);

    @Update("update tool_def set del_flag = 1, updated_at = now() where id = #{id} and user_id = #{userId} and del_flag = 0")
    int softDelete(@Param("id") String id, @Param("userId") Long userId);
}
