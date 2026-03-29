package org.lkf.agent.entity;

public class KnowledgeBaseChunkEntity {

    private String id;
    private String kbId;
    private String kbFileId;
    private Long userId;
    private Integer chunkNo;
    private String content;
    private String embeddingVector;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKbId() {
        return kbId;
    }

    public void setKbId(String kbId) {
        this.kbId = kbId;
    }

    public String getKbFileId() {
        return kbFileId;
    }

    public void setKbFileId(String kbFileId) {
        this.kbFileId = kbFileId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(Integer chunkNo) {
        this.chunkNo = chunkNo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEmbeddingVector() {
        return embeddingVector;
    }

    public void setEmbeddingVector(String embeddingVector) {
        this.embeddingVector = embeddingVector;
    }
}
