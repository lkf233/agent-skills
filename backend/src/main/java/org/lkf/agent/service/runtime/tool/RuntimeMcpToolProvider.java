package org.lkf.agent.service.runtime.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lkf.agent.dto.ToolManifestSnapshotResponseObject;
import org.lkf.agent.service.ToolAppService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RuntimeMcpToolProvider {

    private final ToolAppService toolAppService;
    private final ObjectMapper objectMapper;

    public RuntimeMcpToolProvider(ToolAppService toolAppService, ObjectMapper objectMapper) {
        this.toolAppService = toolAppService;
        this.objectMapper = objectMapper;
    }

    public Map<String, ToolManifestSnapshotResponseObject> refreshSnapshots(String username, List<String> toolIds) {
        Map<String, ToolManifestSnapshotResponseObject> snapshotMap = new HashMap<>();
        if (toolIds == null || toolIds.isEmpty()) {
            return snapshotMap;
        }
        for (String toolId : toolIds) {
            if (toolId == null || toolId.isBlank()) {
                continue;
            }
            ToolManifestSnapshotResponseObject snapshot = toolAppService.refreshToolManifestSnapshot(username, toolId.trim());
            if (snapshot != null) {
                snapshotMap.put(toolId.trim(), snapshot);
            }
        }
        return snapshotMap;
    }

    public String buildToolCatalogContext(Map<String, ToolManifestSnapshotResponseObject> toolSnapshotMap) {
        if (toolSnapshotMap == null || toolSnapshotMap.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, ToolManifestSnapshotResponseObject> entry : toolSnapshotMap.entrySet()) {
            String toolId = entry.getKey();
            ToolManifestSnapshotResponseObject snapshot = entry.getValue();
            if (toolId == null || toolId.isBlank() || snapshot == null || snapshot.getManifest() == null || !snapshot.getManifest().isArray()) {
                continue;
            }
            builder.append("toolId=").append(toolId.trim()).append(":");
            for (JsonNode toolNode : snapshot.getManifest()) {
                if (toolNode == null) {
                    continue;
                }
                String name = toolNode.get("name") == null ? "" : toolNode.get("name").asText();
                String description = toolNode.get("description") == null ? "" : toolNode.get("description").asText();
                String inputSchema = toolNode.get("inputSchema") == null ? "{}" : writeJson(toolNode.get("inputSchema"));
                if (!name.isBlank()) {
                    builder.append(name.trim())
                            .append("(")
                            .append(description.trim())
                            .append(",inputSchema=")
                            .append(inputSchema)
                            .append("),");
                }
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    public ToolSelection selectTool(List<String> toolIds, Map<String, ToolManifestSnapshotResponseObject> toolSnapshotMap,
                                    String candidateToolId, String candidateRemoteToolName) {
        if (toolIds == null || toolIds.isEmpty()) {
            return new ToolSelection("", "");
        }
        String normalizedToolId = candidateToolId == null ? "" : candidateToolId.trim();
        String normalizedToolName = candidateRemoteToolName == null ? "" : candidateRemoteToolName.trim();
        if (!normalizedToolId.isBlank()) {
            ToolManifestSnapshotResponseObject snapshot = toolSnapshotMap.get(normalizedToolId);
            if (snapshot != null && containsToolName(snapshot.getManifest(), normalizedToolName)) {
                return new ToolSelection(normalizedToolId, normalizedToolName);
            }
            String fallback = firstToolName(snapshot == null ? null : snapshot.getManifest());
            if (!fallback.isBlank()) {
                return new ToolSelection(normalizedToolId, fallback);
            }
        }
        for (String toolId : toolIds) {
            if (toolId == null || toolId.isBlank()) {
                continue;
            }
            String normalized = toolId.trim();
            ToolManifestSnapshotResponseObject snapshot = toolSnapshotMap.get(normalized);
            if (snapshot == null || snapshot.getManifest() == null || !snapshot.getManifest().isArray()) {
                continue;
            }
            if (!normalizedToolName.isBlank() && containsToolName(snapshot.getManifest(), normalizedToolName)) {
                return new ToolSelection(normalized, normalizedToolName);
            }
        }
        for (String toolId : toolIds) {
            if (toolId == null || toolId.isBlank()) {
                continue;
            }
            String normalized = toolId.trim();
            ToolManifestSnapshotResponseObject snapshot = toolSnapshotMap.get(normalized);
            String firstTool = firstToolName(snapshot == null ? null : snapshot.getManifest());
            if (!firstTool.isBlank()) {
                return new ToolSelection(normalized, firstTool);
            }
        }
        return new ToolSelection("", "");
    }

    public JsonNode execute(String username, String toolId, String remoteToolName, JsonNode arguments) {
        return toolAppService.executeRemoteTool(username, toolId, remoteToolName, arguments);
    }

    private boolean containsToolName(JsonNode manifest, String remoteToolName) {
        if (manifest == null || !manifest.isArray() || remoteToolName == null || remoteToolName.isBlank()) {
            return false;
        }
        for (JsonNode item : manifest) {
            if (item != null && item.get("name") != null && remoteToolName.trim().equals(item.get("name").asText())) {
                return true;
            }
        }
        return false;
    }

    private String firstToolName(JsonNode manifest) {
        if (manifest == null || !manifest.isArray() || manifest.isEmpty()) {
            return "";
        }
        JsonNode first = manifest.get(0);
        if (first == null || first.get("name") == null || first.get("name").asText().isBlank()) {
            return "";
        }
        return first.get("name").asText().trim();
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception exception) {
            return "{}";
        }
    }

    public record ToolSelection(String toolId, String remoteToolName) {
    }
}
