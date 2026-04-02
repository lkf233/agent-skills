package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "会话消息分页响应对象")
public class ConversationMessagePageResponseObject {

    @Schema(description = "当前页码", example = "1")
    private Integer page;

    @Schema(description = "每页条数", example = "20")
    private Integer pageSize;

    @Schema(description = "总条数", example = "108")
    private Long total;

    @Schema(description = "总页数", example = "6")
    private Integer totalPages;

    @Schema(description = "当前页数据")
    private List<ConversationMessageResponseObject> records;

    public ConversationMessagePageResponseObject() {
    }

    public ConversationMessagePageResponseObject(Integer page, Integer pageSize, Long total, Integer totalPages,
                                                 List<ConversationMessageResponseObject> records) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = totalPages;
        this.records = records;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public List<ConversationMessageResponseObject> getRecords() {
        return records;
    }

    public void setRecords(List<ConversationMessageResponseObject> records) {
        this.records = records;
    }
}
