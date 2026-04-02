package org.lkf.agent.service;

import org.lkf.agent.common.exception.BusinessException;
import org.lkf.agent.dto.ConversationMessagePageResponseObject;
import org.lkf.agent.dto.ConversationMessageResponseObject;
import org.lkf.agent.dto.ConversationResponseObject;
import org.lkf.agent.dto.ConversationSummaryResponseObject;
import org.lkf.agent.dto.CreateConversationRequestObject;
import org.lkf.agent.dto.SendMessageRequestObject;
import org.lkf.agent.entity.ConversationEntity;
import org.lkf.agent.entity.ConversationMessageEntity;
import org.lkf.agent.entity.ConversationSummaryEntity;
import org.lkf.agent.entity.UserAccountEntity;
import org.lkf.agent.mapper.AgentMapper;
import org.lkf.agent.mapper.ConversationMessageMapper;
import org.lkf.agent.mapper.ConversationMapper;
import org.lkf.agent.mapper.ConversationSummaryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ConversationAppService {

    private final ConversationMapper conversationMapper;
    private final ConversationMessageMapper conversationMessageMapper;
    private final ConversationSummaryMapper conversationSummaryMapper;
    private final AgentMapper agentMapper;
    private final AuthAppService authAppService;

    public ConversationAppService(ConversationMapper conversationMapper,
                                  ConversationMessageMapper conversationMessageMapper,
                                  ConversationSummaryMapper conversationSummaryMapper,
                                  AgentMapper agentMapper,
                                  AuthAppService authAppService) {
        this.conversationMapper = conversationMapper;
        this.conversationMessageMapper = conversationMessageMapper;
        this.conversationSummaryMapper = conversationSummaryMapper;
        this.agentMapper = agentMapper;
        this.authAppService = authAppService;
    }

    public ConversationResponseObject createConversation(String username, CreateConversationRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        ConversationEntity conversationEntity = new ConversationEntity();
        conversationEntity.setId(UUID.randomUUID().toString().replace("-", ""));
        conversationEntity.setUserId(userAccountEntity.getId());
        String agentId = requestObject.getAgentId().trim();
        if (agentMapper.findByIdAndUserId(agentId, userAccountEntity.getId()) == null) {
            throw new BusinessException("Agent不存在");
        }
        conversationEntity.setAgentId(agentId);
        conversationEntity.setTitle(requestObject.getTitle());
        conversationEntity.setStatus("ACTIVE");
        conversationEntity.setDelFlag(0);
        conversationMapper.insert(conversationEntity);
        return new ConversationResponseObject(conversationEntity.getId(), conversationEntity.getTitle(), conversationEntity.getAgentId());
    }

    public List<ConversationResponseObject> listConversations(String username, String agentId) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        List<ConversationEntity> entityList;
        if (agentId == null || agentId.isBlank()) {
            entityList = conversationMapper.listByUserId(userAccountEntity.getId());
        } else {
            String safeAgentId = agentId.trim();
            if (agentMapper.findByIdAndUserId(safeAgentId, userAccountEntity.getId()) == null) {
                throw new BusinessException("Agent不存在");
            }
            entityList = conversationMapper.listByUserIdAndAgentId(userAccountEntity.getId(), safeAgentId);
        }
        List<ConversationResponseObject> responseObjectList = new ArrayList<>();
        for (ConversationEntity entity : entityList) {
            responseObjectList.add(new ConversationResponseObject(entity.getId(), entity.getTitle(), entity.getAgentId()));
        }
        return responseObjectList;
    }

    @Transactional(rollbackFor = Exception.class)
    public ConversationMessageResponseObject sendMessage(String username, String conversationId, SendMessageRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        assertConversationOwnedByUser(conversationId, userAccountEntity.getId());
        String content = normalizeContent(requestObject.getContent());
        int seqNo = nextSeqNo(conversationId);
        ConversationMessageEntity messageEntity = new ConversationMessageEntity();
        messageEntity.setId(UUID.randomUUID().toString().replace("-", ""));
        messageEntity.setConversationId(conversationId);
        messageEntity.setUserId(userAccountEntity.getId());
        messageEntity.setRole("USER");
        messageEntity.setContent(content);
        messageEntity.setMetadataJson("{}");
        messageEntity.setTokenInput(0);
        messageEntity.setTokenOutput(0);
        messageEntity.setSeqNo(seqNo);
        messageEntity.setCreatedAt(LocalDateTime.now());
        messageEntity.setDelFlag(0);
        int updated = conversationMessageMapper.insert(messageEntity);
        if (updated <= 0) {
            throw new BusinessException("消息写入失败");
        }
        applyCompressionIfNeeded(conversationId);
        return new ConversationMessageResponseObject(messageEntity.getId(), messageEntity.getRole(), messageEntity.getContent(),
                messageEntity.getSeqNo(), messageEntity.getCreatedAt());
    }

    public ConversationMessagePageResponseObject listMessages(String username, String conversationId, Integer page, Integer pageSize) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        assertConversationOwnedByUser(conversationId, userAccountEntity.getId());
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int offset = (safePage - 1) * safePageSize;
        Long total = conversationMessageMapper.countByConversationId(conversationId);
        List<ConversationMessageEntity> entities = conversationMessageMapper.listByConversationId(conversationId, offset, safePageSize);
        if (entities == null || entities.isEmpty()) {
            return new ConversationMessagePageResponseObject(safePage, safePageSize, total, calculateTotalPages(total, safePageSize),
                    Collections.emptyList());
        }
        Collections.reverse(entities);
        List<ConversationMessageResponseObject> records = new ArrayList<>();
        for (ConversationMessageEntity entity : entities) {
            records.add(new ConversationMessageResponseObject(entity.getId(), normalizeRole(entity.getRole()), entity.getContent(),
                    entity.getSeqNo(), entity.getCreatedAt()));
        }
        return new ConversationMessagePageResponseObject(safePage, safePageSize, total, calculateTotalPages(total, safePageSize), records);
    }

    public List<ConversationSummaryResponseObject> listSummaries(String username, String conversationId, Integer limit) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        assertConversationOwnedByUser(conversationId, userAccountEntity.getId());
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        List<ConversationSummaryEntity> entities = conversationSummaryMapper.listByConversationId(conversationId, safeLimit);
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        List<ConversationSummaryResponseObject> responseObjects = new ArrayList<>();
        for (ConversationSummaryEntity entity : entities) {
            responseObjects.add(new ConversationSummaryResponseObject(
                    entity.getId(),
                    entity.getRangeStartSeq(),
                    entity.getRangeEndSeq(),
                    entity.getSummaryText(),
                    entity.getVersion(),
                    entity.getCreatedAt()
            ));
        }
        return responseObjects;
    }

    private void applyCompressionIfNeeded(String conversationId) {
        Integer maxSeqNo = conversationMessageMapper.findMaxSeqNo(conversationId);
        int maxSeq = maxSeqNo == null ? 0 : maxSeqNo;
        int keepRecentCount = 12;
        int minimumCompressionBatch = 16;
        int compressionEndSeq = maxSeq - keepRecentCount;
        if (compressionEndSeq <= 0) {
            return;
        }
        ConversationSummaryEntity latest = conversationSummaryMapper.findLatestByConversationId(conversationId);
        int startSeq = latest == null || latest.getRangeEndSeq() == null ? 1 : latest.getRangeEndSeq() + 1;
        if (compressionEndSeq < startSeq || compressionEndSeq - startSeq + 1 < minimumCompressionBatch) {
            return;
        }
        List<ConversationMessageEntity> sourceMessages =
                conversationMessageMapper.listByConversationIdAndSeqRange(conversationId, startSeq, compressionEndSeq);
        if (sourceMessages == null || sourceMessages.isEmpty()) {
            return;
        }
        ConversationSummaryEntity summaryEntity = new ConversationSummaryEntity();
        summaryEntity.setId(UUID.randomUUID().toString().replace("-", ""));
        summaryEntity.setConversationId(conversationId);
        summaryEntity.setRangeStartSeq(startSeq);
        summaryEntity.setRangeEndSeq(compressionEndSeq);
        summaryEntity.setSummaryText(buildSummaryText(sourceMessages));
        summaryEntity.setSummaryStructJson("{}");
        summaryEntity.setVersion(latest == null || latest.getVersion() == null ? 1 : latest.getVersion() + 1);
        summaryEntity.setDelFlag(0);
        conversationSummaryMapper.insert(summaryEntity);
    }

    private String buildSummaryText(List<ConversationMessageEntity> sourceMessages) {
        StringBuilder builder = new StringBuilder();
        int maxLines = Math.min(sourceMessages.size(), 40);
        for (int i = 0; i < maxLines; i++) {
            ConversationMessageEntity item = sourceMessages.get(i);
            String role = normalizeRole(item.getRole());
            String content = item.getContent() == null ? "" : item.getContent().trim();
            if (content.length() > 120) {
                content = content.substring(0, 120);
            }
            builder.append(role).append(": ").append(content).append("\n");
        }
        return builder.toString().trim();
    }

    private int nextSeqNo(String conversationId) {
        Integer current = conversationMessageMapper.findMaxSeqNo(conversationId);
        return (current == null ? 0 : current) + 1;
    }

    private void assertConversationOwnedByUser(String conversationId, Long userId) {
        ConversationEntity entity = conversationMapper.findByIdAndUserId(conversationId, userId);
        if (entity == null) {
            throw new BusinessException("会话不存在");
        }
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("消息内容不能为空");
        }
        return content.trim();
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private int calculateTotalPages(Long total, Integer pageSize) {
        if (total == null || total <= 0) {
            return 0;
        }
        return (int) ((total + pageSize - 1) / pageSize);
    }
}
