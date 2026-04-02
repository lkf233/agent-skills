"use client";

import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { BookOpen, Ellipsis, Pencil, Plus, Search, SlidersHorizontal, Trash2 } from "lucide-react";
import {
  createKnowledgeBase,
  deleteKnowledgeBase,
  listKnowledgeBases,
  type CreateKnowledgeBaseParams,
  type KnowledgeBaseResponseObject,
  updateKnowledgeBase,
} from "@/lib/services/knowledge-base-service";

const DEFAULT_EMBEDDING_PROVIDER = "dashscope";
const DEFAULT_EMBEDDING_MODEL = "text-embedding-v3";

export default function KnowledgePage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [pendingDeleteKb, setPendingDeleteKb] = useState<KnowledgeBaseResponseObject | null>(null);
  const [editingKb, setEditingKb] = useState<KnowledgeBaseResponseObject | null>(null);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const listQuery = useQuery({
    queryKey: ["knowledge-bases"],
    queryFn: listKnowledgeBases,
  });

  const createMutation = useMutation({
    mutationFn: (params: CreateKnowledgeBaseParams) => createKnowledgeBase(params),
    onSuccess: async () => {
      setName("");
      setDescription("");
      setDialogOpen(false);
      toast.success("知识库创建成功");
      await queryClient.invalidateQueries({ queryKey: ["knowledge-bases"] });
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "创建失败，请稍后重试";
      toast.error(message);
    },
  });

  const updateMutation = useMutation({
    mutationFn: (params: { kbId: string; name: string; description?: string }) =>
      updateKnowledgeBase(params.kbId, { name: params.name, description: params.description, status: "ACTIVE" }),
    onSuccess: async () => {
      setName("");
      setDescription("");
      setEditingKb(null);
      setDialogOpen(false);
      toast.success("知识库更新成功");
      await queryClient.invalidateQueries({ queryKey: ["knowledge-bases"] });
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "编辑失败，请稍后重试";
      toast.error(message);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (kbId: string) => deleteKnowledgeBase(kbId),
    onSuccess: async () => {
      toast.success("知识库已删除");
      await queryClient.invalidateQueries({ queryKey: ["knowledge-bases"] });
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "删除失败，请稍后重试";
      toast.error(message);
    },
  });

  const onCreate = () => {
    const nextName = name.trim();
    const nextDescription = description.trim();
    if (!nextName) {
      toast.error("请输入知识库名称");
      return;
    }
    if (editingKb) {
      updateMutation.mutate({
        kbId: editingKb.kbId,
        name: nextName,
        description: nextDescription,
      });
      return;
    }
    createMutation.mutate({
      name: nextName,
      description: nextDescription,
      embeddingProvider: DEFAULT_EMBEDDING_PROVIDER,
      embeddingModel: DEFAULT_EMBEDDING_MODEL,
    });
  };

  const openCreateDialog = () => {
    setEditingKb(null);
    setName("");
    setDescription("");
    setDialogOpen(true);
  };

  const openEditDialog = (item: KnowledgeBaseResponseObject) => {
    setEditingKb(item);
    setName(item.name);
    setDescription(item.description || "");
    setDialogOpen(true);
  };

  const onDelete = (item: KnowledgeBaseResponseObject) => {
    setPendingDeleteKb(item);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = () => {
    if (!pendingDeleteKb) {
      return;
    }
    deleteMutation.mutate(pendingDeleteKb.kbId);
    setDeleteDialogOpen(false);
    setPendingDeleteKb(null);
  };

  const openFileList = (item: KnowledgeBaseResponseObject) => {
    router.push(`/knowledge/${item.kbId}/files`);
  };

  useEffect(() => {
    if (listQuery.isError) {
      toast.error("加载知识库列表失败，请刷新后重试");
    }
  }, [listQuery.isError]);

  return (
    <section className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-semibold text-[#101828]">知识库</h1>
          <p className="text-[12px] text-muted-foreground">创建并管理当前账号下的知识库</p>
        </div>
        <Button onClick={openCreateDialog}>
          <Plus className="size-4" />
          创建知识库
        </Button>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogContent className="sm:max-w-lg">
            <DialogHeader>
              <DialogTitle>{editingKb ? "编辑知识库" : "知识库设置"}</DialogTitle>
              <DialogDescription>{editingKb ? "修改后保存即可生效" : "填写知识库基础信息后即可创建"}</DialogDescription>
            </DialogHeader>
            <div className="space-y-4 px-1 pb-1">
              <div className="space-y-2">
                <Label htmlFor="kb-name">知识库名称</Label>
                <Input
                  id="kb-name"
                  placeholder="例如：产品文档库"
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="kb-description">描述</Label>
                <Textarea
                  id="kb-description"
                  placeholder="可选：填写知识库用途"
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label>Embedding 配置</Label>
                <div className="flex h-9 items-center rounded-md border px-3 text-sm text-muted-foreground">
                  {DEFAULT_EMBEDDING_PROVIDER} / {DEFAULT_EMBEDDING_MODEL}
                </div>
              </div>
            </div>
            <DialogFooter className="pt-6">
              <Button
                variant="outline"
                onClick={() => setDialogOpen(false)}
                disabled={createMutation.isPending || updateMutation.isPending}
              >
                取消
              </Button>
              <Button type="button" onClick={onCreate} disabled={createMutation.isPending || updateMutation.isPending}>
                {createMutation.isPending || updateMutation.isPending ? "保存中..." : "保存"}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <div className="flex items-center justify-end gap-2">
        <Button variant="outline" className="h-8 text-[12px] text-muted-foreground">
          所有知识库
        </Button>
        <Button variant="outline" className="h-8 text-[12px] text-muted-foreground">
          <SlidersHorizontal className="size-3.5" />
          全部标签
        </Button>
        <div className="flex h-8 items-center gap-1 rounded-md border bg-background px-2 text-[12px] text-muted-foreground">
          <Search className="size-3.5" />
          <span>搜索</span>
        </div>
      </div>

      {listQuery.isLoading ? <p className="text-sm text-muted-foreground">加载中...</p> : null}
      {listQuery.data && listQuery.data.length === 0 ? (
        <p className="text-sm text-muted-foreground">暂无知识库，请先创建一个知识库</p>
      ) : null}

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {listQuery.data && listQuery.data.length > 0
          ? listQuery.data.map((item) => (
              <div
                key={item.kbId}
                className="group relative h-[188px] rounded-xl border bg-background p-4 transition hover:border-primary/30 hover:shadow-sm"
                role="button"
                tabIndex={0}
                onClick={() => openFileList(item)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    openFileList(item);
                  }
                }}
              >
                <div
                  className="absolute right-3 top-3 opacity-0 transition group-hover:opacity-100 group-focus-within:opacity-100"
                  onClick={(event) => event.stopPropagation()}
                >
                  <DropdownMenu>
                    <DropdownMenuTrigger
                      render={
                        <Button type="button" variant="outline" size="icon-sm" className="rounded-full bg-background" />
                      }
                    >
                      <Ellipsis className="size-4" />
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" className="w-28">
                      <DropdownMenuItem
                        onClick={(event) => {
                          event.stopPropagation();
                          openEditDialog(item);
                        }}
                      >
                        <Pencil className="size-4" />
                        编辑
                      </DropdownMenuItem>
                      <DropdownMenuItem
                        variant="destructive"
                        onClick={(event) => {
                          event.stopPropagation();
                          onDelete(item);
                        }}
                      >
                        <Trash2 className="size-4" />
                        删除
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
                <div className="flex items-start justify-between gap-3 border-b border-border/80 pb-3">
                  <div className="flex items-start gap-3">
                    <div className="mt-0.5 inline-flex size-10 items-center justify-center rounded-[10px] bg-[#ffe8d6] text-primary">
                      <BookOpen className="size-5" />
                    </div>
                    <div className="space-y-0.5">
                      <p className="line-clamp-1 text-[14px] font-semibold text-[#344054]">{item.name}</p>
                      <p className="text-[10px] text-muted-foreground">编辑于刚刚</p>
                      <p className="line-clamp-1 text-[10px] uppercase tracking-wide text-muted-foreground">
                        通用 · 高质量 · 混合检索
                      </p>
                    </div>
                  </div>
                </div>
                <p className="mt-3 line-clamp-2 text-[13px] text-[#475467]">{item.description || "暂无描述"}</p>
                <div className="mt-3 flex items-center gap-3 text-[11px] text-muted-foreground">
                  <span>📄 1</span>
                  <span>·</span>
                  <span>{item.embeddingProvider || "-"}</span>
                  <span>{item.embeddingModel || "-"}</span>
                </div>
              </div>
            ))
          : null}
      </div>

      <Dialog
        open={deleteDialogOpen}
        onOpenChange={(open) => {
          setDeleteDialogOpen(open);
          if (!open) {
            setPendingDeleteKb(null);
          }
        }}
      >
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>确认删除知识库</DialogTitle>
            <DialogDescription>
              {pendingDeleteKb ? `将删除“${pendingDeleteKb.name}”，删除后不可恢复。` : "删除后不可恢复。"}
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
    </section>
  );
}
