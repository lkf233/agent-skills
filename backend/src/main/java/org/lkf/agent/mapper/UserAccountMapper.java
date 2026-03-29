package org.lkf.agent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lkf.agent.entity.UserAccountEntity;

/**
 * 用户账户数据访问接口。
 */
@Mapper
public interface UserAccountMapper {

    /**
     * 根据用户名查询用户信息。
     *
     * @param username 用户名
     * @return 用户实体，可能为null
     */
    @Select("select id, username, password_hash as passwordHash, created_at at time zone 'UTC' as createdAt, " +
            "updated_at at time zone 'UTC' as updatedAt, del_flag as delFlag " +
            "from user_account where username = #{username} and del_flag = 0")
    UserAccountEntity findByUsername(@Param("username") String username);

    /**
     * 新增用户。
     *
     * @param userAccountEntity 用户实体
     * @return 影响行数
     */
    @Insert("insert into user_account(username, password_hash, created_at, updated_at, del_flag) " +
            "values(#{username}, #{passwordHash}, now(), now(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAccountEntity userAccountEntity);
}
