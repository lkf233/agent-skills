"use client";

import { type ChangeEvent, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useParams } from "next/navigation";
import ReactMarkdown from "react-markdown";
import rehypeSanitize from "rehype-sanitize";
import remarkGfm from "remark-gfm";
import {
  ArrowLeft,
  ArrowUpDown,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  ChevronUp,
  FileCog,
  FileText,
  Search,
  SlidersHorizontal,
  Upload,
} from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button, buttonVariants } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  getKnowledgeBaseFileContent,
  listKnowledgeBaseFiles,
  type KnowledgeBaseFileContentResponseObject,
  type KnowledgeBaseFileResponseObject,
  uploadKnowledgeBaseFile,
} from "@/lib/services/knowledge-base-service";

function formatSize(sizeBytes: number) {
  if (sizeBytes >= 1024 * 1024) {
    return `${(sizeBytes / (1024 * 1024)).toFixed(1)} MB`;
  }
  if (sizeBytes >= 1024) {
    return `${(sizeBytes / 1024).toFixed(1)} KB`;
  }
  return `${sizeBytes} B`;
}

function formatDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "-";
  }
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function statusLabel(status: string) {
  if (status === "READY") {
    return "已就绪";
  }
  if (status === "FAILED") {
    return "失败";
  }
  if (status === "PARSING") {
    return "解析中";
  }
  if (status === "QUEUED") {
    return "排队中";
  }
  return status;
}

function statusVariant(status: string): "default" | "secondary" | "destructive" {
  if (status === "READY") {
    return "default";
  }
  if (status === "FAILED") {
    return "destructive";
  }
  return "secondary";
}

function canPreviewFile(item: KnowledgeBaseFileResponseObject) {
  const lowerName = item.fileName.toLowerCase();
  return lowerName.endsWith(".txt") || lowerName.endsWith(".md") || lowerName.endsWith(".markdown");
}

function isMarkdownFile(item: KnowledgeBaseFileResponseObject | null) {
  if (!item) {
    return false;
  }
  const lowerName = item.fileName.toLowerCase();
  return lowerName.endsWith(".md") || lowerName.endsWith(".markdown");
}

const parseStatusOptions: Array<{ value: string; label: string }> = [
  { value: "", label: "全部" },
  { value: "READY", label: "已就绪" },
  { value: "FAILED", label: "失败" },
  { value: "PARSING", label: "解析中" },
  { value: "QUEUED", label: "排队中" },
];

export default function KnowledgeFilesPage() {
  const queryClient = useQueryClient();
  const params = useParams<{ kbId: string }>();
  const kbId = Array.isArray(params.kbId) ? params.kbId[0] : params.kbId;
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [parseStatus, setParseStatus] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [sortBy, setSortBy] = useState<"createdAt" | "recallCount">("createdAt");
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc");
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [previewFile, setPreviewFile] = useState<KnowledgeBaseFileResponseObject | null>(null);
  const [previewData, setPreviewData] = useState<KnowledgeBaseFileContentResponseObject | null>(null);

  const listQuery = useQuery({
    queryKey: ["knowledge-base-files", kbId, parseStatus, searchKeyword, sortBy, sortOrder, page, pageSize],
    queryFn: () =>
      listKnowledgeBaseFiles(kbId, {
        parseStatus: parseStatus || undefined,
        fileName: searchKeyword.trim() || undefined,
        sortBy,
        sortOrder,
        page,
        pageSize,
      }),
    enabled: Boolean(kbId),
  });

  const uploadMutation = useMutation({
    mutationFn: (file: File) => uploadKnowledgeBaseFile(kbId, file),
    onSuccess: async () => {
      toast.success("文件上传成功");
      await queryClient.invalidateQueries({ queryKey: ["knowledge-base-files", kbId] });
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "上传失败，请稍后重试";
      toast.error(message);
    },
  });

  const previewMutation = useMutation({
    mutationFn: (fileId: string) => getKnowledgeBaseFileContent(kbId, fileId),
    onSuccess: (data) => {
      setPreviewData(data);
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "预览加载失败，请稍后重试";
      toast.error(message);
    },
  });

  const totalPages = listQuery.data?.totalPages ?? 1;
  const currentPage = listQuery.data?.page ?? page;
  const currentRows = listQuery.data?.records ?? [];

  useEffect(() => {
    if (listQuery.isError) {
      toast.error("加载文档列表失败，请稍后重试");
    }
  }, [listQuery.isError]);

  const onPickFile = () => {
    if (uploadMutation.isPending) {
      return;
    }
    fileInputRef.current?.click();
  };

  const onFileChange = async (event: ChangeEvent<HTMLInputElement>) => {
    const targetFile = event.target.files?.[0];
    if (!targetFile) {
      return;
    }
    await uploadMutation.mutateAsync(targetFile);
    event.target.value = "";
  };

  const parseStatusLabel = parseStatusOptions.find((option) => option.value === parseStatus)?.label ?? "全部";

  const openPreview = (item: KnowledgeBaseFileResponseObject) => {
    if (!canPreviewFile(item)) {
      toast.error("当前仅支持txt和markdown在线预览");
      return;
    }
    setPreviewFile(item);
    setPreviewData(null);
    setPreviewOpen(true);
    previewMutation.mutate(item.fileId);
  };

  const renderSortIcon = (field: "createdAt" | "recallCount") => {
    if (sortBy !== field) {
      return <ArrowUpDown className="size-3.5 text-muted-foreground" />;
    }
    if (sortOrder === "asc") {
      return <ChevronUp className="size-3.5 text-foreground" />;
    }
    return <ChevronDown className="size-3.5 text-foreground" />;
  };

  return (
    <section className="flex min-h-[calc(100vh-120px)] flex-col">
      <div className="mb-3 flex items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <Link href={kbId ? `/knowledge/${kbId}` : "/knowledge/demo-kb"} className={buttonVariants({ variant: "outline", size: "sm" })}>
            <ArrowLeft className="size-4" />
            返回知识库
          </Link>
          <h1 className="text-lg font-semibold text-[#101828]">文档列表</h1>
        </div>
        <input ref={fileInputRef} type="file" className="hidden" onChange={onFileChange} />
        <Button type="button" onClick={onPickFile} disabled={uploadMutation.isPending}>
          <Upload className="size-4" />
          {uploadMutation.isPending ? "上传中..." : "添加文件"}
        </Button>
      </div>

      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <DropdownMenu>
            <DropdownMenuTrigger
              render={<Button type="button" variant="outline" size="sm" className="justify-between gap-2 px-2.5" />}
            >
              {parseStatusLabel}
              <ChevronDown className="size-3.5 text-muted-foreground" />
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-32">
              {parseStatusOptions.map((option) => (
                <DropdownMenuItem
                  key={option.value || "all"}
                  onClick={() => {
                    setParseStatus(option.value);
                    setPage(1);
                  }}
                >
                  {option.label}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>
          <div className="relative">
            <Search className="pointer-events-none absolute left-2 top-1/2 size-3.5 -translate-y-1/2 text-muted-foreground" />
            <input
              value={searchKeyword}
              onChange={(event) => {
                setSearchKeyword(event.target.value);
                setPage(1);
              }}
              placeholder="搜索"
              className="h-8 w-[220px] rounded-md border bg-background pl-7 pr-2 text-xs outline-none transition focus:border-primary/40"
            />
          </div>
          <Button
            type="button"
            variant="outline"
            size="sm"
            className="justify-between gap-1.5 px-2.5 text-xs"
            onClick={() => {
              setSortBy("createdAt");
              setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"));
              setPage(1);
            }}
          >
            排序：{sortBy === "createdAt" ? "上传时间" : "召回次数"}
            {sortOrder === "asc" ? <ChevronUp className="size-3.5 text-muted-foreground" /> : <ChevronDown className="size-3.5 text-muted-foreground" />}
          </Button>
          <Button type="button" variant="ghost" size="icon-sm" className="text-muted-foreground">
            <SlidersHorizontal className="size-4" />
          </Button>
        </div>
        <Button type="button" variant="outline" size="sm">
          <FileCog className="size-4" />
          元数据
        </Button>
      </div>

      <div className="overflow-hidden rounded-xl border bg-background">
        <Table>
          <TableHeader className="bg-muted/20">
            <TableRow className="hover:bg-muted/20">
              <TableHead className="h-9 w-14 text-xs font-medium text-muted-foreground">#</TableHead>
              <TableHead className="h-9 text-xs font-medium text-muted-foreground">名称</TableHead>
              <TableHead className="h-9 w-28 text-xs font-medium text-muted-foreground">字符数</TableHead>
              <TableHead className="h-9 w-28 text-xs font-medium text-muted-foreground">
                <button
                  type="button"
                  className="inline-flex items-center gap-1 text-muted-foreground hover:text-foreground"
                  onClick={() => {
                    if (sortBy === "recallCount") {
                      setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"));
                    } else {
                      setSortBy("recallCount");
                      setSortOrder("desc");
                    }
                    setPage(1);
                  }}
                >
                  召回次数
                  {renderSortIcon("recallCount")}
                </button>
              </TableHead>
              <TableHead className="h-9 w-44 text-xs font-medium text-muted-foreground">
                <button
                  type="button"
                  className="inline-flex items-center gap-1 text-muted-foreground hover:text-foreground"
                  onClick={() => {
                    if (sortBy === "createdAt") {
                      setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"));
                    } else {
                      setSortBy("createdAt");
                      setSortOrder("desc");
                    }
                    setPage(1);
                  }}
                >
                  上传时间
                  {renderSortIcon("createdAt")}
                </button>
              </TableHead>
              <TableHead className="h-9 w-32 text-xs font-medium text-muted-foreground">状态</TableHead>
              <TableHead className="h-9 w-28 text-xs font-medium text-muted-foreground">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {listQuery.isLoading ? (
              <TableRow>
                <TableCell colSpan={7} className="py-8 text-center text-muted-foreground">
                  加载中...
                </TableCell>
              </TableRow>
            ) : null}

            {!listQuery.isLoading && currentRows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-10 text-center text-muted-foreground">
                  暂无文档，请先上传文件
                </TableCell>
              </TableRow>
            ) : null}

            {currentRows.map((item: KnowledgeBaseFileResponseObject, index: number) => (
              <TableRow key={item.fileId} className="h-10">
                <TableCell className="text-xs text-muted-foreground">{(currentPage - 1) * pageSize + index + 1}</TableCell>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <FileText className="size-4 text-[#309BEC]" />
                    <span className="line-clamp-1 text-[13px] text-[#344054]">{item.fileName}</span>
                  </div>
                </TableCell>
                <TableCell className="text-[13px]">{formatSize(item.sizeBytes)}</TableCell>
                <TableCell className="text-[13px]">{item.recallCount}</TableCell>
                <TableCell className="text-[13px] text-muted-foreground">{formatDate(item.createdAt)}</TableCell>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <span className="size-2 rounded-[3px] bg-muted-foreground/40" />
                    <Badge variant={statusVariant(item.parseStatus)} className="h-auto rounded-md px-1.5 py-0 text-[11px] font-normal">
                      {statusLabel(item.parseStatus)}
                    </Badge>
                  </div>
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <Button
                      type="button"
                      variant="outline"
                      size="xs"
                      className="h-6 px-2 text-[11px]"
                      onClick={() => openPreview(item)}
                      disabled={!canPreviewFile(item)}
                    >
                      预览
                    </Button>
                    <Button type="button" variant="ghost" size="icon-xs" className="text-muted-foreground" disabled>
                      <SlidersHorizontal className="size-3.5" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <div className="mt-auto flex items-center justify-between py-3">
        <div className="flex items-center gap-1 rounded-xl bg-muted/40 p-0.5">
          <Button type="button" variant="ghost" size="icon-xs" disabled={currentPage <= 1} onClick={() => setPage((prev) => Math.max(1, prev - 1))}>
            <ChevronLeft className="size-3.5" />
          </Button>
          <div className="px-2 text-xs text-muted-foreground">
            {currentPage} / {totalPages}
          </div>
          <Button
            type="button"
            variant="ghost"
            size="icon-xs"
            disabled={currentPage >= totalPages}
            onClick={() => setPage((prev) => Math.min(totalPages, prev + 1))}
          >
            <ChevronRight className="size-3.5" />
          </Button>
        </div>

        <div className="flex items-center justify-center">
          <button type="button" className="flex min-w-8 items-center justify-center rounded-md bg-muted/60 px-2 py-1 text-xs text-foreground">
            {currentPage}
          </button>
        </div>

        <div className="flex items-center gap-1 rounded-xl bg-muted/40 p-0.5">
          {[10, 25, 50].map((size) => (
            <button
              key={size}
              type="button"
              onClick={() => {
                setPageSize(size);
                setPage(1);
              }}
              className={buttonVariants({
                variant: pageSize === size ? "outline" : "ghost",
                size: "xs",
                className: "h-7 min-w-8 px-2 text-xs",
              })}
            >
              {size}
            </button>
          ))}
        </div>
      </div>

      <Dialog open={previewOpen} onOpenChange={setPreviewOpen}>
        <DialogContent className="sm:max-w-4xl">
          <DialogHeader>
            <DialogTitle>{previewFile ? `在线预览：${previewFile.fileName}` : "在线预览"}</DialogTitle>
          </DialogHeader>
          <div className="max-h-[70vh] overflow-auto rounded-lg border bg-muted/20 p-4">
            {previewMutation.isPending ? <p className="text-sm text-muted-foreground">加载中...</p> : null}
            {!previewMutation.isPending && previewData ? (
              isMarkdownFile(previewFile) ? (
                <div className="text-sm leading-6 text-foreground [&_a]:text-primary [&_a]:underline [&_blockquote]:border-l-2 [&_blockquote]:border-border [&_blockquote]:pl-3 [&_code]:rounded [&_code]:bg-muted [&_code]:px-1 [&_code]:py-0.5 [&_h1]:text-2xl [&_h1]:font-semibold [&_h1]:leading-9 [&_h2]:mt-6 [&_h2]:text-xl [&_h2]:font-semibold [&_h3]:mt-5 [&_h3]:text-lg [&_h3]:font-medium [&_hr]:my-4 [&_li]:my-1 [&_ol]:list-decimal [&_ol]:pl-5 [&_p]:my-3 [&_pre]:my-3 [&_pre]:overflow-x-auto [&_pre]:rounded-md [&_pre]:bg-muted [&_pre]:p-3 [&_table]:my-3 [&_table]:w-full [&_table]:border-collapse [&_table]:text-left [&_td]:border [&_td]:p-2 [&_th]:border [&_th]:bg-muted/50 [&_th]:p-2 [&_ul]:list-disc [&_ul]:pl-5">
                  <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeSanitize]}>
                    {previewData.content}
                  </ReactMarkdown>
                </div>
              ) : (
                <pre className="whitespace-pre-wrap break-words text-sm leading-6 text-foreground">{previewData.content}</pre>
              )
            ) : null}
            {!previewMutation.isPending && !previewData ? (
              <p className="text-sm text-muted-foreground">暂无预览内容</p>
            ) : null}
          </div>
        </DialogContent>
      </Dialog>
    </section>
  );
}
