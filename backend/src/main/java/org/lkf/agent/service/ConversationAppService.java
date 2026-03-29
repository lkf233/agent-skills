package org.lkf.agent.service;

import org.lkf.agent.dto.ConversationResponseObject;
import org.lkf.agent.entity.ConversationEntity;
import org.lkf.agent.entity.UserAccountEntity;
import org.springframework.stereotype.Service;
import org.lkf.agent.dto.CreateConversationRequestObject;
import org.lkf.agent.mapper.ConversationMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 会话应用服务。
 */
@Service
public class ConversationAppService {

    /**
     * 会话数据访问对象。
     */
    private final ConversationMapper conversationMapper;

    /**
     * 认证应用服务。
     */
    private final AuthAppService authAppService;

    /**
     * 构造器。
     *
     * @param conversationMapper 会话数据访问对象
     * @param authAppService 认证应用服务
     */
    public ConversationAppService(ConversationMapper conversationMapper, AuthAppService authAppService) {
        this.conversationMapper = conversationMapper;
        this.authAppService = authAppService;
    }

    /**
     * 创建会话。
     *
     * @param username 用户名
     * @param requestObject 创建会话请求对象
     * @return 会话响应对象
     */
    public ConversationResponseObject createConversation(String username, CreateConversationRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        ConversationEntity conversationEntity = new ConversationEntity();
        conversationEntity.setId(UUID.randomUUID().toString().replace("-", ""));
        conversationEntity.setUserId(userAccountEntity.getId());
        conversationEntity.setTitle(requestObject.getTitle());
        conversationEntity.setStatus("ACTIVE");
        conversationEntity.setDelFlag(0);
        conversationMapper.insert(conversationEntity);
        return new ConversationResponseObject(conversationEntity.getId(), conversationEntity.getTitle());
    }

    /**
     * 查询会话列表。
     *
     * @param username 用户名
     * @return 会话响应对象列表
     */
    public List<ConversationResponseObject> listConversations(String username) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        List<ConversationEntity> entityList = conversationMapper.listByUserId(userAccountEntity.getId());
        List<ConversationResponseObject> responseObjectList = new ArrayList<>();
        for (ConversationEntity entity : entityList) {
            responseObjectList.add(new ConversationResponseObject(entity.getId(), entity.getTitle()));
        }
        return responseObjectList;
    }
}
