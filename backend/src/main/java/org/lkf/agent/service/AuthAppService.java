package org.lkf.agent.service;

import org.lkf.agent.dto.RegisterRequestObject;
import org.lkf.agent.entity.UserAccountEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.lkf.agent.common.exception.BusinessException;
import org.lkf.agent.dto.LoginRequestObject;
import org.lkf.agent.mapper.UserAccountMapper;

/**
 * 认证应用服务。
 */
@Service
public class AuthAppService {

    /**
     * 用户数据访问对象。
     */
    private final UserAccountMapper userAccountMapper;

    /**
     * 密码编码器。
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 构造器。
     *
     * @param userAccountMapper 用户数据访问对象
     */
    public AuthAppService(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    /**
     * 注册用户。
     *
     * @param requestObject 注册请求对象
     * @throws BusinessException 用户已存在时抛出
     */
    public void register(RegisterRequestObject requestObject) {
        UserAccountEntity existing = userAccountMapper.findByUsername(requestObject.getUsername());
        if (existing != null) {
            throw new BusinessException("用户已存在");
        }
        UserAccountEntity entity = new UserAccountEntity();
        entity.setUsername(requestObject.getUsername());
        entity.setPasswordHash(passwordEncoder.encode(requestObject.getPassword()));
        int updated = userAccountMapper.insert(entity);
        if (updated <= 0) {
            throw new BusinessException("注册失败");
        }
    }

    /**
     * 登录并返回用户名。
     *
     * @param requestObject 登录请求对象
     * @return 用户名
     * @throws BusinessException 用户不存在或密码错误时抛出
     */
    public String login(LoginRequestObject requestObject) {
        UserAccountEntity userAccountEntity = userAccountMapper.findByUsername(requestObject.getUsername());
        if (userAccountEntity == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(requestObject.getPassword(), userAccountEntity.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        return userAccountEntity.getUsername();
    }

    /**
     * 获取用户信息（必须存在）。
     *
     * @param username 用户名
     * @return 用户实体
     * @throws BusinessException 用户不存在时抛出
     */
    public UserAccountEntity getUserByUsername(String username) {
        UserAccountEntity entity = userAccountMapper.findByUsername(username);
        if (entity == null) {
            throw new BusinessException("用户不存在");
        }
        return entity;
    }
}
