package org.lkf.agent.service;

import org.lkf.agent.common.exception.BusinessException;
import org.lkf.agent.dto.AgentResponseObject;
import org.lkf.agent.dto.BindKnowledgeBasesRequestObject;
import org.lkf.agent.dto.BindToolsRequestObject;
import org.lkf.agent.dto.CreateAgentRequestObject;
import org.lkf.agent.dto.UpdateAgentRequestObject;
import org.lkf.agent.entity.AgentEntity;
import org.lkf.agent.entity.UserAccountEntity;
import org.lkf.agent.mapper.AgentMapper;
import org.lkf.agent.mapper.KnowledgeBaseMapper;
import org.lkf.agent.mapper.ToolDefMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AgentAppService {

    private final AgentMapper agentMapper;
    private final ToolDefMapper toolDefMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final AuthAppService authAppService;

    public AgentAppService(AgentMapper agentMapper, ToolDefMapper toolDefMapper,
                           KnowledgeBaseMapper knowledgeBaseMapper, AuthAppService authAppService) {
        this.agentMapper = agentMapper;
        this.toolDefMapper = toolDefMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.authAppService = authAppService;
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentResponseObject createAgent(String username, CreateAgentRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        List<String> toolIds = normalizeIds(requestObject.getToolIds());
        List<String> knowledgeBaseIds = normalizeIds(requestObject.getKnowledgeBaseIds());
        validateToolsOwnedByUser(userAccountEntity.getId(), toolIds);
        validateKnowledgeBasesOwnedByUser(userAccountEntity.getId(), knowledgeBaseIds);
        AgentEntity entity = new AgentEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setUserId(userAccountEntity.getId());
        entity.setName(requireNonBlank(requestObject.getName(), "Agent名称不能为空"));
        entity.setDescription(normalizeText(requestObject.getDescription()));
        entity.setSystemPrompt(requireNonBlank(requestObject.getSystemPrompt(), "系统提示词不能为空"));
        entity.setAvatarUrl(normalizeText(requestObject.getAvatarUrl()));
        entity.setStatus("DRAFT");
        entity.setModelConfigJson("{}");
        int updated = agentMapper.insert(entity);
        if (updated <= 0) {
            throw new BusinessException("创建Agent失败");
        }
        replaceToolRelations(entity.getId(), toolIds);
        replaceKnowledgeBaseRelations(entity.getId(), knowledgeBaseIds);
        AgentEntity created = mustFindAgent(entity.getId(), userAccountEntity.getId());
        return toResponseObject(created, listToolIds(created.getId()), listKnowledgeBaseIds(created.getId()));
    }

    public List<AgentResponseObject> listAgents(String username) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        List<AgentEntity> entities = agentMapper.listByUserId(userAccountEntity.getId());
        List<AgentResponseObject> responseObjectList = new ArrayList<>();
        for (AgentEntity entity : entities) {
            responseObjectList.add(toResponseObject(entity, listToolIds(entity.getId()), listKnowledgeBaseIds(entity.getId())));
        }
        return responseObjectList;
    }

    public AgentResponseObject getAgent(String username, String agentId) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        AgentEntity entity = mustFindAgent(agentId, userAccountEntity.getId());
        return toResponseObject(entity, listToolIds(agentId), listKnowledgeBaseIds(agentId));
    }

    public void updateAgent(String username, String agentId, UpdateAgentRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        AgentEntity existing = mustFindAgent(agentId, userAccountEntity.getId());
        if (requestObject.getName() != null) {
            existing.setName(requireNonBlank(requestObject.getName(), "Agent名称不能为空"));
        }
        if (requestObject.getDescription() != null) {
            existing.setDescription(normalizeText(requestObject.getDescription()));
        }
        if (requestObject.getSystemPrompt() != null) {
            existing.setSystemPrompt(requireNonBlank(requestObject.getSystemPrompt(), "系统提示词不能为空"));
        }
        if (requestObject.getAvatarUrl() != null) {
            existing.setAvatarUrl(normalizeText(requestObject.getAvatarUrl()));
        }
        int updated = agentMapper.update(existing);
        if (updated <= 0) {
            throw new BusinessException("更新Agent失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAgent(String username, String agentId) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        mustFindAgent(agentId, userAccountEntity.getId());
        agentMapper.softDeleteToolRelations(agentId);
        agentMapper.softDeleteKnowledgeBaseRelations(agentId);
        int updated = agentMapper.softDelete(agentId, userAccountEntity.getId());
        if (updated <= 0) {
            throw new BusinessException("删除Agent失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindTools(String username, String agentId, BindToolsRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        mustFindAgent(agentId, userAccountEntity.getId());
        List<String> toolIds = normalizeIds(requestObject == null ? null : requestObject.getToolIds());
        validateToolsOwnedByUser(userAccountEntity.getId(), toolIds);
        replaceToolRelations(agentId, toolIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindKnowledgeBases(String username, String agentId, BindKnowledgeBasesRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        mustFindAgent(agentId, userAccountEntity.getId());
        List<String> knowledgeBaseIds = normalizeIds(requestObject == null ? null : requestObject.getKnowledgeBaseIds());
        validateKnowledgeBasesOwnedByUser(userAccountEntity.getId(), knowledgeBaseIds);
        replaceKnowledgeBaseRelations(agentId, knowledgeBaseIds);
    }

    private void replaceToolRelations(String agentId, List<String> toolIds) {
        agentMapper.softDeleteToolRelations(agentId);
        for (int i = 0; i < toolIds.size(); i++) {
            agentMapper.insertToolRelation(UUID.randomUUID().toString().replace("-", ""), agentId, toolIds.get(i), i + 1);
        }
    }

    private void replaceKnowledgeBaseRelations(String agentId, List<String> knowledgeBaseIds) {
        agentMapper.softDeleteKnowledgeBaseRelations(agentId);
        for (int i = 0; i < knowledgeBaseIds.size(); i++) {
            agentMapper.insertKnowledgeBaseRelation(UUID.randomUUID().toString().replace("-", ""), agentId, knowledgeBaseIds.get(i), i + 1);
        }
    }

    private AgentEntity mustFindAgent(String agentId, Long userId) {
        AgentEntity entity = agentMapper.findByIdAndUserId(agentId, userId);
        if (entity == null) {
            throw new BusinessException("Agent不存在");
        }
        return entity;
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private List<String> normalizeIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (String id : ids) {
            if (id == null || id.isBlank()) {
                throw new BusinessException("ID列表包含空值");
            }
            normalized.add(id.trim());
        }
        return normalized;
    }

    private void validateToolsOwnedByUser(Long userId, List<String> toolIds) {
        if (toolIds.isEmpty()) {
            return;
        }
        long count = toolDefMapper.countByUserIdAndIds(userId, toolIds);
        if (count != toolIds.size()) {
            throw new BusinessException("存在无权限或不存在的工具");
        }
    }

    private void validateKnowledgeBasesOwnedByUser(Long userId, List<String> knowledgeBaseIds) {
        if (knowledgeBaseIds.isEmpty()) {
            return;
        }
        long count = knowledgeBaseMapper.countByUserIdAndIds(userId, knowledgeBaseIds);
        if (count != knowledgeBaseIds.size()) {
            throw new BusinessException("存在无权限或不存在的知识库");
        }
    }

    private List<String> listToolIds(String agentId) {
        List<String> toolIds = agentMapper.listToolIdsByAgentId(agentId);
        return toolIds == null ? Collections.emptyList() : toolIds;
    }

    private List<String> listKnowledgeBaseIds(String agentId) {
        List<String> knowledgeBaseIds = agentMapper.listKnowledgeBaseIdsByAgentId(agentId);
        return knowledgeBaseIds == null ? Collections.emptyList() : knowledgeBaseIds;
    }

    private AgentResponseObject toResponseObject(AgentEntity entity, List<String> toolIds, List<String> knowledgeBaseIds) {
        return new AgentResponseObject(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSystemPrompt(),
                entity.getAvatarUrl(),
                normalizeStatus(entity.getStatus()),
                toolIds,
                knowledgeBaseIds,
                entity.getUpdatedAt()
        );
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "DRAFT";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }
}
