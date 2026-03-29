package org.lkf.agent.rag.processing;

import org.lkf.agent.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DocumentProcessingFactory {

    private final Map<String, DocumentProcessingStrategy> strategyMap;

    public DocumentProcessingFactory(Map<String, DocumentProcessingStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    public DocumentProcessingStrategy getStrategy(String fileType) {
        DocumentProcessingStrategy strategy = strategyMap.get(fileType);
        if (strategy == null) {
            throw new BusinessException("不支持的文件类型: " + fileType);
        }
        return strategy;
    }
}
