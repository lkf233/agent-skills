package org.lkf.agent.service;

import org.lkf.agent.dto.CreateKnowledgeBaseRequestObject;
import org.lkf.agent.dto.KnowledgeBaseFilePageResponseObject;
import org.lkf.agent.dto.KnowledgeBaseFileResponseObject;
import org.lkf.agent.dto.KnowledgeBaseResponseObject;
import org.lkf.agent.entity.KnowledgeBaseFileEntity;
import org.lkf.agent.entity.KnowledgeBaseIngestTaskEntity;
import org.lkf.agent.entity.KnowledgeBaseEntity;
import org.lkf.agent.entity.UserAccountEntity;
import org.lkf.agent.mapper.KnowledgeBaseFileMapper;
import org.lkf.agent.mapper.KnowledgeBaseIngestTaskMapper;
import org.lkf.agent.mapper.KnowledgeBaseMapper;
import org.lkf.agent.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 知识库应用服务。
 */
@Service
public class KnowledgeBaseAppService {

    /**
     * 知识库数据访问对象。
     */
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeBaseFileMapper knowledgeBaseFileMapper;
    private final KnowledgeBaseIngestTaskMapper knowledgeBaseIngestTaskMapper;

    /**
     * 认证应用服务。
     */
    private final AuthAppService authAppService;

    /**
     * 构造器。
     *
     * @param knowledgeBaseMapper 知识库数据访问对象
     * @param authAppService 认证应用服务
     */
    public KnowledgeBaseAppService(KnowledgeBaseMapper knowledgeBaseMapper, KnowledgeBaseFileMapper knowledgeBaseFileMapper,
                                   KnowledgeBaseIngestTaskMapper knowledgeBaseIngestTaskMapper, AuthAppService authAppService) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeBaseFileMapper = knowledgeBaseFileMapper;
        this.knowledgeBaseIngestTaskMapper = knowledgeBaseIngestTaskMapper;
        this.authAppService = authAppService;
    }

    /**
     * 创建知识库。
     *
     * @param username 用户名
     * @param requestObject 创建请求
     * @return 知识库响应对象
     */
    public KnowledgeBaseResponseObject createKnowledgeBase(String username, CreateKnowledgeBaseRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setUserId(userAccountEntity.getId());
        entity.setName(requestObject.getName());
        entity.setDescription(requestObject.getDescription() == null ? "" : requestObject.getDescription());
        entity.setEmbeddingProvider(requestObject.getEmbeddingProvider());
        entity.setEmbeddingModel(requestObject.getEmbeddingModel());
        entity.setStatus("ACTIVE");
        entity.setDelFlag(0);
        knowledgeBaseMapper.insert(entity);
        return new KnowledgeBaseResponseObject(entity.getId(), entity.getName(), entity.getDescription(),
                entity.getEmbeddingProvider(), entity.getEmbeddingModel(), entity.getStatus());
    }

    /**
     * 查询知识库列表。
     *
     * @param username 用户名
     * @return 知识库响应对象列表
     */
    public List<KnowledgeBaseResponseObject> listKnowledgeBases(String username) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        List<KnowledgeBaseEntity> entityList = knowledgeBaseMapper.listByUserId(userAccountEntity.getId());
        List<KnowledgeBaseResponseObject> responseObjectList = new ArrayList<>();
        for (KnowledgeBaseEntity entity : entityList) {
            responseObjectList.add(new KnowledgeBaseResponseObject(entity.getId(), entity.getName(), entity.getDescription(),
                    entity.getEmbeddingProvider(), entity.getEmbeddingModel(), entity.getStatus()));
        }
        return responseObjectList;
    }

    public KnowledgeBaseFileResponseObject uploadFile(String username, String kbId, MultipartFile file) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        KnowledgeBaseEntity knowledgeBaseEntity = knowledgeBaseMapper.findByIdAndUserId(kbId, userAccountEntity.getId());
        if (knowledgeBaseEntity == null) {
            throw new BusinessException("知识库不存在");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String originalFileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        mapStrategy(extension);
        String fileId = UUID.randomUUID().toString().replace("-", "");
        Path storagePath = buildStoragePath(userAccountEntity.getId(), kbId, fileId, originalFileName);
        try {
            Files.createDirectories(storagePath.getParent());
            Files.write(storagePath, file.getBytes());
        } catch (IOException e) {
            throw new BusinessException("文件存储失败");
        }
        KnowledgeBaseFileEntity fileEntity = new KnowledgeBaseFileEntity();
        fileEntity.setId(fileId);
        fileEntity.setKbId(kbId);
        fileEntity.setUserId(userAccountEntity.getId());
        fileEntity.setFileName(originalFileName);
        fileEntity.setStoragePath(storagePath.toString());
        fileEntity.setMimeType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        fileEntity.setSizeBytes(file.getSize());
        fileEntity.setParseStatus("QUEUED");
        fileEntity.setErrorMessage("");
        knowledgeBaseFileMapper.insert(fileEntity);
        enqueueIngestTask(fileEntity.getId(), userAccountEntity.getId());
        KnowledgeBaseFileEntity refreshed = knowledgeBaseFileMapper.findById(fileId);
        return toFileResponseObject(refreshed);
    }

    public KnowledgeBaseFilePageResponseObject listFiles(String username, String kbId, String parseStatus, String fileName,
                                                         String sortBy, String sortOrder, Integer page, Integer pageSize) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        KnowledgeBaseEntity knowledgeBaseEntity = knowledgeBaseMapper.findByIdAndUserId(kbId, userAccountEntity.getId());
        if (knowledgeBaseEntity == null) {
            throw new BusinessException("知识库不存在");
        }
        String normalizedStatus = normalizeParseStatus(parseStatus);
        String normalizedSortBy = normalizeSortBy(sortBy);
        String normalizedSortOrder = normalizeSortOrder(sortOrder);
        int normalizedPage = normalizePage(page);
        int normalizedPageSize = normalizePageSize(pageSize);
        int offset = (normalizedPage - 1) * normalizedPageSize;
        Long total = knowledgeBaseFileMapper.countByCondition(kbId, userAccountEntity.getId(), normalizedStatus, normalizeFileName(fileName));
        List<KnowledgeBaseFileEntity> fileEntities = knowledgeBaseFileMapper.listByCondition(
                kbId,
                userAccountEntity.getId(),
                normalizedStatus,
                normalizeFileName(fileName),
                normalizedSortBy,
                normalizedSortOrder,
                normalizedPageSize,
                offset
        );
        List<KnowledgeBaseFileResponseObject> responseObjectList = new ArrayList<>();
        for (KnowledgeBaseFileEntity fileEntity : fileEntities) {
            responseObjectList.add(toFileResponseObject(fileEntity));
        }
        int totalPages = total == 0 ? 0 : (int) ((total + normalizedPageSize - 1) / normalizedPageSize);
        return new KnowledgeBaseFilePageResponseObject(normalizedPage, normalizedPageSize, total, totalPages, responseObjectList);
    }

    private void enqueueIngestTask(String kbFileId, Long userId) {
        KnowledgeBaseIngestTaskEntity taskEntity = new KnowledgeBaseIngestTaskEntity();
        taskEntity.setId(UUID.randomUUID().toString().replace("-", ""));
        taskEntity.setKbFileId(kbFileId);
        taskEntity.setUserId(userId);
        taskEntity.setTaskStatus("PENDING");
        taskEntity.setRetryCount(0);
        taskEntity.setErrorMessage("");
        knowledgeBaseIngestTaskMapper.insert(taskEntity);
    }

    private String mapStrategy(String extension) {
        if ("txt".equals(extension)) {
            return "txt";
        }
        if ("md".equals(extension) || "markdown".equals(extension)) {
            return "markdown";
        }
        if ("docx".equals(extension) || "doc".equals(extension)) {
            return "word";
        }
        throw new BusinessException("仅支持txt、md、docx、doc类型文件");
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex < 0 || lastIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastIndex + 1).toLowerCase();
    }

    private Path buildStoragePath(Long userId, String kbId, String fileId, String originalFileName) {
        String safeName = (originalFileName == null ? "unknown" : originalFileName).replace("\\", "_").replace("/", "_");
        return Paths.get(System.getProperty("user.dir"), "storage", "kb", String.valueOf(userId), kbId, fileId + "_" + safeName);
    }

    private KnowledgeBaseFileResponseObject toFileResponseObject(KnowledgeBaseFileEntity entity) {
        return new KnowledgeBaseFileResponseObject(entity.getId(), entity.getFileName(), entity.getMimeType(),
                entity.getSizeBytes(), entity.getParseStatus(), entity.getErrorMessage(), entity.getCreatedAt(),
                entity.getRecallCount());
    }

    private String normalizeParseStatus(String parseStatus) {
        if (parseStatus == null || parseStatus.isBlank()) {
            return null;
        }
        String normalized = parseStatus.trim().toUpperCase(Locale.ROOT);
        if (!"QUEUED".equals(normalized) && !"PARSING".equals(normalized)
                && !"READY".equals(normalized) && !"FAILED".equals(normalized)) {
            throw new BusinessException("parseStatus仅支持 QUEUED、PARSING、READY、FAILED");
        }
        return normalized;
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        return fileName.trim();
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }
        String normalized = sortBy.trim();
        if (!"createdAt".equals(normalized) && !"recallCount".equals(normalized)) {
            throw new BusinessException("sortBy仅支持 createdAt、recallCount");
        }
        return normalized;
    }

    private String normalizeSortOrder(String sortOrder) {
        if (sortOrder == null || sortOrder.isBlank()) {
            return "desc";
        }
        String normalized = sortOrder.trim().toLowerCase(Locale.ROOT);
        if (!"asc".equals(normalized) && !"desc".equals(normalized)) {
            throw new BusinessException("sortOrder仅支持 asc、desc");
        }
        return normalized;
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            throw new BusinessException("page必须大于等于1");
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            throw new BusinessException("pageSize必须大于等于1");
        }
        if (pageSize > 100) {
            throw new BusinessException("pageSize不能超过100");
        }
        return pageSize;
    }

}
