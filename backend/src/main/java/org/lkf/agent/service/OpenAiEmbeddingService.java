package org.lkf.agent.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.lkf.agent.common.exception.BusinessException;
import org.lkf.agent.config.EmbeddingProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAiEmbeddingService {

    private final EmbeddingProperties embeddingProperties;

    public OpenAiEmbeddingService(EmbeddingProperties embeddingProperties) {
        this.embeddingProperties = embeddingProperties;
    }

    public List<String> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }
        validateConfig();
        try {
            OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                    .baseUrl(normalizeBaseUrl(embeddingProperties.getBaseUrl()))
                    .apiKey(embeddingProperties.getApiKey())
                    .modelName(embeddingProperties.getModel())
                    .dimensions(embeddingProperties.getDimensions())
                    .timeout(Duration.ofSeconds(embeddingProperties.getTimeoutSeconds()))
                    .build();
            List<String> vectors = new ArrayList<>();
            int batchSize = resolveBatchSize();
            for (int start = 0; start < texts.size(); start += batchSize) {
                int end = Math.min(start + batchSize, texts.size());
                List<String> batchTexts = texts.subList(start, end);
                List<TextSegment> segments = batchTexts.stream().map(TextSegment::from).toList();
                Response<List<Embedding>> response = embeddingModel.embedAll(segments);
                List<Embedding> data = response.content();
                if (data == null || data.isEmpty()) {
                    throw new BusinessException("Embedding模型响应为空");
                }
                if (data.size() != batchTexts.size()) {
                    throw new BusinessException("Embedding返回数量与输入不一致");
                }
                for (Embedding embedding : data) {
                    float[] vector = embedding.vector();
                    if (vector == null || vector.length == 0) {
                        throw new BusinessException("Embedding向量缺失");
                    }
                    if (embeddingProperties.getDimensions() != null && vector.length != embeddingProperties.getDimensions()) {
                        throw new BusinessException("Embedding向量维度不匹配，期望" + embeddingProperties.getDimensions() + "，实际" + vector.length);
                    }
                    vectors.add(toVectorLiteral(vector));
                }
            }
            return vectors;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Embedding模型调用异常: " + e.getMessage());
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        String base = baseUrl.trim();
        if (base.endsWith("/v1/embeddings")) {
            return base.substring(0, base.length() - "/embeddings".length());
        }
        if (base.endsWith("/v1")) {
            return base;
        }
        if (base.endsWith("/")) {
            return base + "v1";
        }
        return base + "/v1";
    }

    private String toVectorLiteral(float[] embedding) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(embedding[i]);
        }
        builder.append("]");
        return builder.toString();
    }

    private void validateConfig() {
        if (embeddingProperties.getBaseUrl() == null || embeddingProperties.getBaseUrl().isBlank()) {
            throw new BusinessException("未配置Embedding baseUrl");
        }
        if (embeddingProperties.getApiKey() == null || embeddingProperties.getApiKey().isBlank()) {
            throw new BusinessException("未配置Embedding apiKey");
        }
        if (embeddingProperties.getModel() == null || embeddingProperties.getModel().isBlank()) {
            throw new BusinessException("未配置Embedding model");
        }
    }

    private int resolveBatchSize() {
        int configured = embeddingProperties.getBatchSize() == null ? 10 : embeddingProperties.getBatchSize();
        if (configured <= 0) {
            configured = 10;
        }
        return Math.min(configured, 10);
    }
}
