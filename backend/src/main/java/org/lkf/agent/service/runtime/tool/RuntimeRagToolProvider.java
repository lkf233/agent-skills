package org.lkf.agent.service.runtime.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lkf.agent.mapper.KnowledgeBaseChunkMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RuntimeRagToolProvider {

    private final KnowledgeBaseChunkMapper knowledgeBaseChunkMapper;
    private final ObjectMapper objectMapper;

    public RuntimeRagToolProvider(KnowledgeBaseChunkMapper knowledgeBaseChunkMapper, ObjectMapper objectMapper) {
        this.knowledgeBaseChunkMapper = knowledgeBaseChunkMapper;
        this.objectMapper = objectMapper;
    }

    public JsonNode search(List<String> kbIds, String query) {
        List<String> recalls = knowledgeBaseChunkMapper.searchByKbIdsAndKeyword(kbIds, query, 6);
        List<String> safeRecalls = recalls == null ? new ArrayList<>() : recalls;
        return objectMapper.createObjectNode()
                .put("query", query)
                .set("snippets", objectMapper.valueToTree(safeRecalls));
    }
}
