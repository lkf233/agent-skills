"use client";

import { use, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus, Server, Trash2, Wrench } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Textarea } from "@/components/ui/textarea";
import {
  createTool,
  deleteTool,
  listRemoteTools,
  listTools,
  testTool,
  type CreateToolParams,
  type ToolResponseObject,
} from "@/lib/services/tool-service";

type StudioPageProps = {
  params: Promise<{ agentId: string }>;
};

function parseCustomHeaders(input: string) {
  const result: Record<string, string> = {};
  const lines = input
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean);
  for (const line of lines) {
    const separatorIndex = line.indexOf(":");
    if (separatorIndex <= 0 || separatorIndex >= line.length - 1) {
      return { valid: false as const, data: {} as Record<string, string> };
    }
    const key = line.slice(0, separatorIndex).trim();
    const value = line.slice(separatorIndex + 1).trim();
    if (!key || !value) {
      return { valid: false as const, data: {} as Record<string, string> };
    }
    result[key] = value;
  }
  return { valid: true as const, data: result };
}

function readEndpoint(tool: ToolResponseObject) {
  const endpoint = tool.configJson.endpoint;
  return typeof endpoint === "string" ? endpoint : "-";
}

function formatTime(value: string) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString("zh-CN", { hour12: false });
}

export default function StudioPage({ params }: StudioPageProps) {
  const { agentId } = use(params);
  const queryClient = useQueryClient();
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [pendingDeleteTool, setPendingDeleteTool] = useState<ToolResponseObject | null>(null);
  const [asideOpen, setAsideOpen] = useState(false);
  const [keyword, setKeyword] = useState("");
  const [form, setForm] = useState({
    name: "",
    description: "",
    endpoint: "",
    bearerToken: "",
    apiKeyHeaderName: "x-api-key",
    apiKey: "",
    customHeadersText: "",
  });
  const [selectedToolId, setSelectedToolId] = useState<string | null>(null);
  const [connectivityMap, setConnectivityMap] = useState<Record<string, "unknown" | "online" | "offline">>({});
  const [remoteToolsMap, setRemoteToolsMap] = useState<Record<string, unknown[]>>({});

  const listQuery = useQuery({
    queryKey: ["tools", keyword],
    queryFn: () => listTools({ keyword: keyword.trim() || undefined }),
  });

  const createMutation = useMutation({
    mutationFn: (payload: CreateToolParams) => createTool(payload),
    onSuccess: async (tool) => {
      toast.success("MCP服务接入成功");
      setForm({
        name: "",
        description: "",
        endpoint: "",
        bearerToken: "",
        apiKeyHeaderName: "x-api-key",
        apiKey: "",
        customHeadersText: "",
      });
      setSelectedToolId(tool.id);
      setCreateDialogOpen(false);
      await queryClient.invalidateQueries({ queryKey: ["tools"] });
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "接入失败，请稍后重试";
      toast.error(message);
    },
  });

  const testMutation = useMutation({
    mutationFn: (toolId: string) => testTool(toolId),
    onSuccess: (result, toolId) => {
      setConnectivityMap((prev) => ({ ...prev, [toolId]: result.connected ? "online" : "offline" }));
      if (result.connected) {
        toast.success(result.message || "连通成功");
        return;
      }
      toast.error(result.message || "连通失败");
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "连通测试失败";
      toast.error(message);
    },
  });

  const remoteToolsMutation = useMutation({
    mutationFn: (toolId: string) => listRemoteTools(toolId),
    onSuccess: (result, toolId) => {
      setRemoteToolsMap((prev) => ({ ...prev, [toolId]: result.toolsJson }));
      if (result.validMcpService) {
        toast.success(`已获取 ${result.toolsJson.length} 个远程工具`);
        return;
      }
      toast.error(result.message || "该服务不是有效MCP工具服务");
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "获取远程工具列表失败";
      toast.error(message);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (toolId: string) => deleteTool(toolId),
    onSuccess: async (_, toolId) => {
      toast.success("已删除MCP服务");
      setConnectivityMap((prev) => {
        const next = { ...prev };
        delete next[toolId];
        return next;
      });
      setRemoteToolsMap((prev) => {
        const next = { ...prev };
        delete next[toolId];
        return next;
      });
      if (selectedToolId === toolId) {
        setSelectedToolId(null);
        setAsideOpen(false);
      }
      await queryClient.invalidateQueries({ queryKey: ["tools"] });
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "删除失败";
      toast.error(message);
    },
  });

  const selectedTool = useMemo(
    () => listQuery.data?.find((item) => item.id === selectedToolId) ?? null,
    [listQuery.data, selectedToolId]
  );
  const selectedRemoteTools = selectedToolId ? remoteToolsMap[selectedToolId] ?? [] : [];

  const onCreate = () => {
    const name = form.name.trim();
    const endpoint = form.endpoint.trim();
    if (!name) {
      toast.error("请输入服务名称");
      return;
    }
    if (!endpoint) {
      toast.error("请输入MCP服务地址");
      return;
    }
    const parsedHeaders = parseCustomHeaders(form.customHeadersText);
    if (!parsedHeaders.valid) {
      toast.error("自定义请求头格式错误，请使用 key:value");
      return;
    }
    createMutation.mutate({
      name,
      description: form.description.trim(),
      endpoint,
      bearerToken: form.bearerToken.trim(),
      apiKeyHeaderName: form.apiKeyHeaderName.trim(),
      apiKey: form.apiKey.trim(),
      customHeaders: parsedHeaders.data,
      status: "ACTIVE",
    });
  };

  const openAside = (tool: ToolResponseObject) => {
    setSelectedToolId(tool.id);
    setAsideOpen(true);
    if (!remoteToolsMap[tool.id]) {
      remoteToolsMutation.mutate(tool.id);
    }
  };

  const requestDelete = (tool: ToolResponseObject) => {
    setPendingDeleteTool(tool);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = () => {
    if (!pendingDeleteTool) {
      return;
    }
    deleteMutation.mutate(pendingDeleteTool.id);
    setDeleteDialogOpen(false);
    setPendingDeleteTool(null);
  };

  const renderConnectivity = (toolId: string) => {
    const state = connectivityMap[toolId] ?? "unknown";
    if (state === "online") {
      return <Badge>已连通</Badge>;
    }
    if (state === "offline") {
      return <Badge variant="destructive">连通异常</Badge>;
    }
    return <Badge variant="outline">未检测</Badge>;
  };

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="space-y-1">
          <h1 className="text-2xl font-semibold text-[#101828]">工具</h1>
          <p className="text-sm text-muted-foreground">
            当前 Agent：{agentId}。接入MCP服务后可查看工具详情与远程Tool List。
          </p>
        </div>
        <Button type="button" onClick={() => setCreateDialogOpen(true)}>
          <Plus className="size-4" />
          新增MCP工具
        </Button>
      </div>

      <div className="space-y-3">
        <div className="flex flex-wrap items-center gap-3">
          <h2 className="text-base font-semibold text-[#101828]">已接入MCP服务</h2>
          <Input
            className="h-8 w-56"
            placeholder="按名称或描述搜索"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <Button type="button" variant="outline" className="h-8" onClick={() => listQuery.refetch()}>
            刷新
          </Button>
        </div>

        {listQuery.isLoading ? <p className="text-sm text-muted-foreground">加载中...</p> : null}
        {listQuery.data && listQuery.data.length === 0 ? (
          <p className="text-sm text-muted-foreground">暂无已接入服务，请点击右上角新增MCP工具。</p>
        ) : null}

        {listQuery.data && listQuery.data.length > 0 ? (
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            {listQuery.data.map((item) => (
              <div
                key={item.id}
                className="group relative h-[188px] cursor-pointer rounded-xl border bg-background p-4 transition hover:border-primary/30 hover:shadow-sm"
                onClick={() => openAside(item)}
                role="button"
                tabIndex={0}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    openAside(item);
                  }
                }}
              >
                <div className="flex items-start justify-between gap-2 border-b border-border/80 pb-3">
                  <div className="flex items-start gap-3">
                    <div className="mt-0.5 inline-flex size-10 items-center justify-center rounded-[10px] bg-[#ffe8d6] text-primary">
                      <Wrench className="size-5" />
                    </div>
                    <div className="space-y-0.5">
                      <p className="line-clamp-1 text-[14px] font-semibold text-[#344054]">{item.name}</p>
                      <p className="text-[10px] text-muted-foreground">更新于 {formatTime(item.updatedAt)}</p>
                      <p className="line-clamp-1 text-[10px] uppercase tracking-wide text-muted-foreground">
                        {item.toolType} · {item.status}
                      </p>
                    </div>
                  </div>
                  {renderConnectivity(item.id)}
                </div>
                <p className="mt-3 line-clamp-2 text-[13px] text-[#475467]">{item.description || "暂无描述"}</p>
                <div className="mt-3 flex items-center gap-2 text-[11px] text-muted-foreground">
                  <Server className="size-3.5" />
                  <span className="truncate">{readEndpoint(item)}</span>
                </div>
                <div className="mt-3 flex items-center gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    className="h-7 text-[12px]"
                    onClick={(event) => {
                      event.stopPropagation();
                      setSelectedToolId(item.id);
                      testMutation.mutate(item.id);
                    }}
                    disabled={testMutation.isPending}
                  >
                    测试连通
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    className="h-7 text-[12px]"
                    onClick={(event) => {
                      event.stopPropagation();
                      requestDelete(item);
                    }}
                    disabled={deleteMutation.isPending}
                  >
                    <Trash2 className="size-3.5" />
                    删除
                  </Button>
                </div>
              </div>
            ))}
          </div>
        ) : null}
      </div>

      <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
        <DialogContent className="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>新增MCP工具</DialogTitle>
            <DialogDescription>填写服务信息后即可完成接入。</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="tool-name">服务名称</Label>
              <Input
                id="tool-name"
                placeholder="例如：CRM MCP"
                value={form.name}
                onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="tool-endpoint">MCP服务地址</Label>
              <Input
                id="tool-endpoint"
                placeholder="https://example.com/mcp"
                value={form.endpoint}
                onChange={(event) => setForm((prev) => ({ ...prev, endpoint: event.target.value }))}
              />
            </div>
            <div className="space-y-2 md:col-span-2">
              <Label htmlFor="tool-description">描述</Label>
              <Textarea
                id="tool-description"
                placeholder="可选：说明该MCP服务用途"
                value={form.description}
                onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="tool-bearer">Bearer Token</Label>
              <Input
                id="tool-bearer"
                placeholder="可选"
                value={form.bearerToken}
                onChange={(event) => setForm((prev) => ({ ...prev, bearerToken: event.target.value }))}
              />
            </div>
            <div className="grid grid-cols-2 gap-2">
              <div className="space-y-2">
                <Label htmlFor="tool-api-key-header">API Key Header</Label>
                <Input
                  id="tool-api-key-header"
                  placeholder="x-api-key"
                  value={form.apiKeyHeaderName}
                  onChange={(event) => setForm((prev) => ({ ...prev, apiKeyHeaderName: event.target.value }))}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="tool-api-key">API Key</Label>
                <Input
                  id="tool-api-key"
                  placeholder="可选"
                  value={form.apiKey}
                  onChange={(event) => setForm((prev) => ({ ...prev, apiKey: event.target.value }))}
                />
              </div>
            </div>
            <div className="space-y-2 md:col-span-2">
              <Label htmlFor="tool-custom-headers">自定义请求头</Label>
              <Textarea
                id="tool-custom-headers"
                placeholder={"每行一个，格式 key:value\n例如 X-Tenant:demo"}
                value={form.customHeadersText}
                onChange={(event) => setForm((prev) => ({ ...prev, customHeadersText: event.target.value }))}
              />
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setCreateDialogOpen(false)}>
              取消
            </Button>
            <Button type="button" onClick={onCreate} disabled={createMutation.isPending}>
              {createMutation.isPending ? "接入中..." : "确认新增"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog
        open={deleteDialogOpen}
        onOpenChange={(open) => {
          setDeleteDialogOpen(open);
          if (!open) {
            setPendingDeleteTool(null);
          }
        }}
      >
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>确认删除MCP工具</DialogTitle>
            <DialogDescription>
              {pendingDeleteTool ? `将删除“${pendingDeleteTool.name}”，删除后不可恢复。` : "删除后不可恢复。"}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDialogOpen(false)} disabled={deleteMutation.isPending}>
              取消
            </Button>
            <Button variant="destructive" onClick={confirmDelete} disabled={deleteMutation.isPending}>
              {deleteMutation.isPending ? "删除中..." : "确认删除"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Sheet open={asideOpen} onOpenChange={setAsideOpen}>
        <SheetContent side="right" className="w-full sm:max-w-xl">
          <SheetHeader>
            <SheetTitle>{selectedTool?.name || "MCP工具详情"}</SheetTitle>
            <SheetDescription>{selectedTool?.description || "查看服务信息与远程Tool List"}</SheetDescription>
          </SheetHeader>
          <div className="space-y-5 overflow-y-auto px-4 pb-4">
            {selectedTool ? (
              <>
                <div className="space-y-2 rounded-lg border p-3">
                  <div className="flex items-center justify-between gap-2">
                    <p className="text-sm font-medium">服务信息</p>
                    {renderConnectivity(selectedTool.id)}
                  </div>
                  <p className="text-xs text-muted-foreground">地址：{readEndpoint(selectedTool)}</p>
                  <p className="text-xs text-muted-foreground">状态：{selectedTool.status}</p>
                  <p className="text-xs text-muted-foreground">更新时间：{formatTime(selectedTool.updatedAt)}</p>
                  <div className="flex gap-2 pt-1">
                    <Button
                      type="button"
                      variant="outline"
                      className="h-8"
                      onClick={() => testMutation.mutate(selectedTool.id)}
                      disabled={testMutation.isPending}
                    >
                      测试连通
                    </Button>
                    <Button
                      type="button"
                      variant="outline"
                      className="h-8"
                      onClick={() => remoteToolsMutation.mutate(selectedTool.id)}
                      disabled={remoteToolsMutation.isPending}
                    >
                      刷新Tool List
                    </Button>
                  </div>
                </div>

                <div className="space-y-2 rounded-lg border p-3">
                  <p className="text-sm font-medium">Tool List</p>
                  {remoteToolsMutation.isPending && selectedToolId === selectedTool.id ? (
                    <p className="text-sm text-muted-foreground">加载中...</p>
                  ) : null}
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>名称</TableHead>
                        <TableHead>描述</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {selectedRemoteTools.length > 0 ? (
                        selectedRemoteTools.map((tool, index) => {
                          const toolName = typeof tool === "object" && tool !== null ? (tool as { name?: unknown }).name : null;
                          const toolDescription =
                            typeof tool === "object" && tool !== null ? (tool as { description?: unknown }).description : null;
                          return (
                            <TableRow key={`${selectedTool.id}-${index}`}>
                              <TableCell className="max-w-[180px] truncate">{typeof toolName === "string" ? toolName : "-"}</TableCell>
                              <TableCell className="max-w-[240px] truncate text-muted-foreground">
                                {typeof toolDescription === "string" ? toolDescription : "-"}
                              </TableCell>
                            </TableRow>
                          );
                        })
                      ) : (
                        <TableRow>
                          <TableCell colSpan={2} className="text-center text-muted-foreground">
                            暂无远程工具，请点击刷新Tool List
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </>
            ) : (
              <p className="text-sm text-muted-foreground">请选择一个MCP工具卡片查看详情。</p>
            )}
          </div>
        </SheetContent>
      </Sheet>
    </section>
  );
}
