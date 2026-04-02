package org.lkf.agent.entity;

import java.time.LocalDateTime;

public class ConversationSummaryEntity {

    private String id;
    private String conversationId;
    private Integer rangeStartSeq;
    private Integer rangeEndSeq;
    private String summaryText;
    private String summaryStructJson;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer delFlag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getRangeStartSeq() {
        return rangeStartSeq;
    }

    public void setRangeStartSeq(Integer rangeStartSeq) {
        this.rangeStartSeq = rangeStartSeq;
    }

    public Integer getRangeEndSeq() {
        return rangeEndSeq;
    }

    public void setRangeEndSeq(Integer rangeEndSeq) {
        this.rangeEndSeq = rangeEndSeq;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getSummaryStructJson() {
        return summaryStructJson;
    }

    public void setSummaryStructJson(String summaryStructJson) {
        this.summaryStructJson = summaryStructJson;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }
}
