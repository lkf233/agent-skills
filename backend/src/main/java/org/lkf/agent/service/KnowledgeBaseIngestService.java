package org.lkf.agent.service;

import org.lkf.agent.common.exception.BusinessException;
import org.lkf.agent.entity.KnowledgeBaseChunkEntity;
import org.lkf.agent.entity.KnowledgeBaseFileEntity;
import org.lkf.agent.entity.KnowledgeBaseIngestTaskEntity;
import org.lkf.agent.mapper.KnowledgeBaseChunkMapper;
import org.lkf.agent.mapper.KnowledgeBaseFileMapper;
import org.lkf.agent.mapper.KnowledgeBaseIngestTaskMapper;
import org.lkf.agent.rag.processing.DocumentProcessingFactory;
import org.lkf.agent.rag.processing.DocumentProcessingStrategy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class KnowledgeBaseIngestService {

    private final KnowledgeBaseIngestTaskMapper ingestTaskMapper;
    private final KnowledgeBaseFileMapper knowledgeBaseFileMapper;
    private final KnowledgeBaseChunkMapper knowledgeBaseChunkMapper;
    private final DocumentProcessingFactory documentProcessingFactory;
    private final OpenAiEmbeddingService openAiEmbeddingService;

    public KnowledgeBaseIngestService(KnowledgeBaseIngestTaskMapper ingestTaskMapper,
                                      KnowledgeBaseFileMapper knowledgeBaseFileMapper,
                                      KnowledgeBaseChunkMapper knowledgeBaseChunkMapper,
                                      DocumentProcessingFactory documentProcessingFactory,
                                      OpenAiEmbeddingService openAiEmbeddingService) {
        this.ingestTaskMapper = ingestTaskMapper;
        this.knowledgeBaseFileMapper = knowledgeBaseFileMapper;
        this.knowledgeBaseChunkMapper = knowledgeBaseChunkMapper;
        this.documentProcessingFactory = documentProcessingFactory;
        this.openAiEmbeddingService = openAiEmbeddingService;
    }

    @Scheduled(fixedDelay = 5000)
    public void dispatchTasks() {
        List<KnowledgeBaseIngestTaskEntity> tasks = ingestTaskMapper.listPendingTasks();
        for (KnowledgeBaseIngestTaskEntity task : tasks) {
            if (ingestTaskMapper.markProcessing(task.getId()) <= 0) {
                continue;
            }
            processTask(task);
        }
    }

    private void processTask(KnowledgeBaseIngestTaskEntity task) {
        KnowledgeBaseFileEntity fileEntity = knowledgeBaseFileMapper.findById(task.getKbFileId());
        if (fileEntity == null) {
            ingestTaskMapper.markFailed(task.getId(), "文件不存在");
            return;
        }
        knowledgeBaseFileMapper.updateParseStatus(fileEntity.getId(), "PARSING", "");
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(fileEntity.getStoragePath()));
            String strategyKey = mapStrategy(fileEntity.getFileName());
            DocumentProcessingStrategy strategy = documentProcessingFactory.getStrategy(strategyKey);
            List<String> segments = strategy.process(bytes);
            List<String> embeddings = openAiEmbeddingService.embedBatch(segments);
            knowledgeBaseChunkMapper.deleteByKbFileId(fileEntity.getId());
            int index = 0;
            for (String segment : segments) {
                KnowledgeBaseChunkEntity chunkEntity = new KnowledgeBaseChunkEntity();
                chunkEntity.setId(UUID.randomUUID().toString().replace("-", ""));
                chunkEntity.setKbId(fileEntity.getKbId());
                chunkEntity.setKbFileId(fileEntity.getId());
                chunkEntity.setUserId(fileEntity.getUserId());
                chunkEntity.setChunkNo(index++);
                chunkEntity.setContent(segment);
                String vector = embeddings.get(chunkEntity.getChunkNo());
                chunkEntity.setEmbeddingVector(vector);
                knowledgeBaseChunkMapper.insert(chunkEntity);
            }
            knowledgeBaseFileMapper.updateParseStatus(fileEntity.getId(), "READY", "");
            ingestTaskMapper.markSuccess(task.getId());
        } catch (Exception e) {
            knowledgeBaseFileMapper.updateParseStatus(fileEntity.getId(), "FAILED", truncate(e.getMessage()));
            ingestTaskMapper.markFailed(task.getId(), truncate(e.getMessage()));
        }
    }

    private String mapStrategy(String fileName) {
        int lastIndex = fileName == null ? -1 : fileName.lastIndexOf(".");
        if (lastIndex < 0 || lastIndex == fileName.length() - 1) {
            throw new BusinessException("不支持的文件类型");
        }
        String extension = fileName.substring(lastIndex + 1).toLowerCase();
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

    private String truncate(String message) {
        if (message == null || message.isEmpty()) {
            return "处理失败";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
