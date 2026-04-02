package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "会话摘要响应对象")
public class ConversationSummaryResponseObject {

    @Schema(description = "摘要ID")
    private String id;

    @Schema(description = "起始序号")
    private Integer rangeStartSeq;

    @Schema(description = "结束序号")
    private Integer rangeEndSeq;

    @Schema(description = "摘要文本")
    private String summaryText;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public ConversationSummaryResponseObject() {
    }

    public ConversationSummaryResponseObject(String id, Integer rangeStartSeq, Integer rangeEndSeq, String summaryText,
                                             Integer version, LocalDateTime createdAt) {
        this.id = id;
        this.rangeStartSeq = rangeStartSeq;
        this.rangeEndSeq = rangeEndSeq;
        this.summaryText = summaryText;
        this.version = version;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
