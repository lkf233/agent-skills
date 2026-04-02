"use client";

import { use, useMemo, useState, type Dispatch, type SetStateAction } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Bot, Ellipsis, Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
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
import {
  AgentCreateDialog,
  type AgentFormState,
  type ToggleItem,
} from "@/components/workspace/agent-create-dialog";
import {
  createAgent as createAgentRequest,
  deleteAgent,
  listAgents,
  type AgentResponseObject,
  updateAgent,
} from "@/lib/services/agent-service";
import { listKnowledgeBases } from "@/lib/services/knowledge-base-service";
import { listTools } from "@/lib/services/tool-service";

type WorkspacePageProps = {
  params: Promise<{ id: string }>;
};

type AgentItem = {
  id: string;
  name: string;
  description: string;
  toolsCount: number;
  knowledgeCount: number;
  updatedAt: string;
};

const defaultForm: AgentFormState = {
  name: "",
  description: "",
  systemPrompt: "你是一个有用的AI助手。",
  welcomeMessage: "你好！我是你的AI助手，有什么可以帮助你的吗？",
};

export default function WorkspacePage({ params }: WorkspacePageProps) {
  const { id } = use(params);
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [activeStep, setActiveStep] = useState("basic");
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [editingAgent, setEditingAgent] = useState<AgentResponseObject | null>(null);
  const [pendingDeleteAgent, setPendingDeleteAgent] = useState<AgentResponseObject | null>(null);
  const [editName, setEditName] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [editSystemPrompt, setEditSystemPrompt] = useState("");
  const [form, setForm] = useState<AgentFormState>(defaultForm);
  const [selectedToolIds, setSelectedToolIds] = useState<string[]>([]);
  const [selectedKnowledgeBaseIds, setSelectedKnowledgeBaseIds] = useState<string[]>([]);

  const listAgentsQuery = useQuery({
    queryKey: ["agents"],
    queryFn: () => listAgents(),
  });
  const listToolsQuery = useQuery({
    queryKey: ["tools", "workspace-create"],
    queryFn: () => listTools(),
  });
  const listKnowledgeBasesQuery = useQuery({
    queryKey: ["knowledge-bases", "workspace-create"],
    queryFn: () => listKnowledgeBases(),
  });

  const createAgentMutation = useMutation({
    mutationFn: createAgentRequest,
    onSuccess: async () => {
      toast.success("Agent 创建成功");
      await queryClient.invalidateQueries({ queryKey: ["agents"] });
      setDialogOpen(false);
      setActiveStep("basic");
      setForm(defaultForm);
      setSelectedToolIds([]);
      setSelectedKnowledgeBaseIds([]);
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "创建 Agent 失败";
      toast.error(message);
    },
  });

  const updateAgentMutation = useMutation({
    mutationFn: (params: { agentId: string; name: string; description: string; systemPrompt: string }) =>
      updateAgent(params.agentId, {
        name: params.name,
        description: params.description,
        systemPrompt: params.systemPrompt,
      }),
    onSuccess: async () => {
      toast.success("Agent 更新成功");
      setEditDialogOpen(false);
      setEditingAgent(null);
      await queryClient.invalidateQueries({ queryKey: ["agents"] });
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "更新 Agent 失败";
      toast.error(message);
    },
  });

  const deleteAgentMutation = useMutation({
    mutationFn: (agentId: string) => deleteAgent(agentId),
    onSuccess: async () => {
      toast.success("Agent 已删除");
      setDeleteDialogOpen(false);
      setPendingDeleteAgent(null);
      await queryClient.invalidateQueries({ queryKey: ["agents"] });
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "删除 Agent 失败";
      toast.error(message);
    },
  });

  const tools: ToggleItem[] = useMemo(
    () =>
      (listToolsQuery.data ?? []).map((item) => ({
        id: item.id,
        name: item.name,
        enabled: selectedToolIds.includes(item.id),
      })),
    [listToolsQuery.data, selectedToolIds]
  );

  const knowledgeBases: ToggleItem[] = useMemo(
    () =>
      (listKnowledgeBasesQuery.data ?? []).map((item) => ({
        id: item.kbId,
        name: item.name,
        enabled: selectedKnowledgeBaseIds.includes(item.kbId),
      })),
    [listKnowledgeBasesQuery.data, selectedKnowledgeBaseIds]
  );

  const selectedToolCount = useMemo(
    () => tools.filter((item) => item.enabled).length,
    [tools]
  );
  const selectedKnowledgeCount = useMemo(
    () => knowledgeBases.filter((item) => item.enabled).length,
    [knowledgeBases]
  );

  const onToolsChange: Dispatch<SetStateAction<ToggleItem[]>> = (updater) => {
    const next = typeof updater === "function" ? updater(tools) : updater;
    setSelectedToolIds(next.filter((item) => item.enabled).map((item) => item.id));
  };

  const onKnowledgeBasesChange: Dispatch<SetStateAction<ToggleItem[]>> = (updater) => {
    const next = typeof updater === "function" ? updater(knowledgeBases) : updater;
    setSelectedKnowledgeBaseIds(next.filter((item) => item.enabled).map((item) => item.id));
  };

  const agents: AgentItem[] = useMemo(
    () =>
      (listAgentsQuery.data ?? []).map((agent) => ({
        id: agent.id,
        name: agent.name,
        description: agent.description || "暂无描述",
        toolsCount: agent.toolIds.length,
        knowledgeCount: agent.knowledgeBaseIds.length,
        updatedAt: agent.updatedAt,
      })),
    [listAgentsQuery.data]
  );

  const onCreateAgent = () => {
    if (!form.name.trim()) {
      toast.error("请先填写助理名称");
      return;
    }
    if (!form.systemPrompt.trim()) {
      toast.error("请先填写系统提示词");
      return;
    }
    createAgentMutation.mutate({
      name: form.name,
      description: form.description,
      systemPrompt: form.systemPrompt,
      toolIds: selectedToolIds,
      knowledgeBaseIds: selectedKnowledgeBaseIds,
    });
  };

  const openEditDialog = (agent: AgentResponseObject) => {
    setEditingAgent(agent);
    setEditName(agent.name);
    setEditDescription(agent.description || "");
    setEditSystemPrompt(agent.systemPrompt || "");
    setEditDialogOpen(true);
  };

  const onSaveEdit = () => {
    if (!editingAgent) {
      return;
    }
    const name = editName.trim();
    const systemPrompt = editSystemPrompt.trim();
    if (!name) {
      toast.error("请输入助理名称");
      return;
    }
    if (!systemPrompt) {
      toast.error("请输入系统提示词");
      return;
    }
    updateAgentMutation.mutate({
      agentId: editingAgent.id,
      name,
      description: editDescription.trim(),
      systemPrompt,
    });
  };

  const onDelete = (agent: AgentResponseObject) => {
    setPendingDeleteAgent(agent);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = () => {
    if (!pendingDeleteAgent) {
      return;
    }
    deleteAgentMutation.mutate(pendingDeleteAgent.id);
  };

  return (
    <section className="flex flex-col gap-6">
      <header className="flex flex-wrap items-start justify-between gap-4 rounded-2xl border bg-white p-5">
        <div className="space-y-2">
          <h1 className="text-2xl font-bold text-zinc-900">工作台</h1>
          <p className="text-sm text-zinc-600">
            在当前工作区 {id} 中创建和管理 Agent，快速完成业务协作与自动化执行。
          </p>
        </div>
        <Button onClick={() => setDialogOpen(true)}>
          <Plus data-icon="inline-start" />
          创建你的Agent
        </Button>
      </header>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {agents.length === 0 && !listAgentsQuery.isLoading ? (
          <div className="col-span-full rounded-2xl border border-dashed bg-zinc-50 p-10 text-center text-sm text-zinc-500">
            当前暂无 Agent，点击右上角“创建你的Agent”开始创建。
          </div>
        ) : (
          agents.map((agent) => (
            <Card key={agent.id} className="border-zinc-200 bg-white">
              <CardHeader className="gap-3">
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-2">
                    <div className="flex size-9 items-center justify-center rounded-full bg-blue-100 text-blue-600">
                      <Bot />
                    </div>
                    <CardTitle>{agent.name}</CardTitle>
                  </div>
                  <DropdownMenu>
                    <DropdownMenuTrigger
                      render={<Button type="button" variant="outline" size="icon-sm" className="rounded-full" />}
                    >
                      <Ellipsis />
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" className="w-28">
                      <DropdownMenuItem
                        onClick={() => {
                          const target = listAgentsQuery.data?.find((item) => item.id === agent.id);
                          if (target) {
                            openEditDialog(target);
                          }
                        }}
                      >
                        <Pencil />
                        编辑
                      </DropdownMenuItem>
                      <DropdownMenuItem
                        variant="destructive"
                        onClick={() => {
                          const target = listAgentsQuery.data?.find((item) => item.id === agent.id);
                          if (target) {
                            onDelete(target);
                          }
                        }}
                      >
                        <Trash2 />
                        删除
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
                <CardDescription>{agent.description}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-2 text-sm text-zinc-600">
                <p>工具数量：{agent.toolsCount}</p>
                <p>知识库数量：{agent.knowledgeCount}</p>
              </CardContent>
              <CardFooter className="text-xs text-zinc-500">
                最近更新时间：{agent.updatedAt ? new Date(agent.updatedAt).toLocaleString("zh-CN") : "-"}
              </CardFooter>
            </Card>
          ))
        )}
      </div>

      <AgentCreateDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        activeStep={activeStep}
        onActiveStepChange={setActiveStep}
        form={form}
        onFormChange={setForm}
        tools={tools}
        onToolsChange={onToolsChange}
        knowledgeBases={knowledgeBases}
        onKnowledgeBasesChange={onKnowledgeBasesChange}
        selectedToolCount={selectedToolCount}
        selectedKnowledgeCount={selectedKnowledgeCount}
        toolsLoading={listToolsQuery.isLoading}
        knowledgeBasesLoading={listKnowledgeBasesQuery.isLoading}
        creating={createAgentMutation.isPending}
        onCancel={() => setDialogOpen(false)}
        onConfirmCreate={onCreateAgent}
      />

      <Dialog
        open={editDialogOpen}
        onOpenChange={(open) => {
          setEditDialogOpen(open);
          if (!open) {
            setEditingAgent(null);
          }
        }}
      >
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>编辑Agent</DialogTitle>
            <DialogDescription>修改后保存即可生效</DialogDescription>
          </DialogHeader>
          <div className="space-y-4 px-1 pb-1">
            <div className="space-y-2">
              <Label htmlFor="agent-edit-name">助理名称</Label>
              <Input id="agent-edit-name" value={editName} onChange={(event) => setEditName(event.target.value)} />
            </div>
            <div className="space-y-2">
              <Label htmlFor="agent-edit-description">描述</Label>
              <Textarea
                id="agent-edit-description"
                value={editDescription}
                onChange={(event) => setEditDescription(event.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="agent-edit-system-prompt">系统提示词</Label>
              <Textarea
                id="agent-edit-system-prompt"
                value={editSystemPrompt}
                onChange={(event) => setEditSystemPrompt(event.target.value)}
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setEditDialogOpen(false)}
              disabled={updateAgentMutation.isPending}
            >
              取消
            </Button>
            <Button onClick={onSaveEdit} disabled={updateAgentMutation.isPending}>
              {updateAgentMutation.isPending ? "保存中..." : "保存"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog
        open={deleteDialogOpen}
        onOpenChange={(open) => {
          setDeleteDialogOpen(open);
          if (!open) {
            setPendingDeleteAgent(null);
          }
        }}
      >
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>确认删除Agent</DialogTitle>
            <DialogDescription>
              {pendingDeleteAgent ? `将删除“${pendingDeleteAgent.name}”，删除后不可恢复。` : "删除后不可恢复。"}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDialogOpen(false)} disabled={deleteAgentMutation.isPending}>
              取消
            </Button>
            <Button variant="destructive" onClick={confirmDelete} disabled={deleteAgentMutation.isPending}>
              {deleteAgentMutation.isPending ? "删除中..." : "确认删除"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </section>
  );
}
