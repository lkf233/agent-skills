"use client";

import { type Dispatch, type ReactNode, type SetStateAction } from "react";
import {
  CakeSlice,
  Database,
  FileUp,
  Sparkles,
  Wrench,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";

export type AgentFormState = {
  name: string;
  description: string;
  systemPrompt: string;
  welcomeMessage: string;
};

export type ToggleItem = {
  id: string;
  name: string;
  enabled: boolean;
};

type AgentCreateDialogProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  activeStep: string;
  onActiveStepChange: (step: string) => void;
  form: AgentFormState;
  onFormChange: Dispatch<SetStateAction<AgentFormState>>;
  tools: ToggleItem[];
  onToolsChange: Dispatch<SetStateAction<ToggleItem[]>>;
  knowledgeBases: ToggleItem[];
  onKnowledgeBasesChange: Dispatch<SetStateAction<ToggleItem[]>>;
  selectedToolCount: number;
  selectedKnowledgeCount: number;
  toolsLoading: boolean;
  knowledgeBasesLoading: boolean;
  creating: boolean;
  onCancel: () => void;
  onConfirmCreate: () => void;
};

function ToggleCard({
  title,
  icon,
  enabled,
  onToggle,
}: {
  title: string;
  icon: ReactNode;
  enabled: boolean;
  onToggle: () => void;
}) {
  return (
    <label
      className={`flex w-full cursor-pointer items-center justify-between rounded-xl border px-4 py-3 text-left transition ${
        enabled
          ? "border-blue-300 bg-blue-50 hover:bg-blue-100"
          : "border-zinc-200 bg-white hover:bg-zinc-50"
      }`}
    >
      <span className="flex items-center gap-2 text-base font-medium text-zinc-900">
        <span className="text-blue-600">{icon}</span>
        <span>{title}</span>
      </span>
      <input
        type="checkbox"
        checked={enabled}
        onChange={onToggle}
        className="size-4 accent-blue-600"
      />
    </label>
  );
}

export function AgentCreateDialog({
  open,
  onOpenChange,
  activeStep,
  onActiveStepChange,
  form,
  onFormChange,
  tools,
  onToolsChange,
  knowledgeBases,
  onKnowledgeBasesChange,
  selectedToolCount,
  selectedKnowledgeCount,
  toolsLoading,
  knowledgeBasesLoading,
  creating,
  onCancel,
  onConfirmCreate,
}: AgentCreateDialogProps) {
  const currentStep = activeStep === "knowledge" ? "tools" : activeStep;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="h-[calc(100vh-40px)] w-[calc(100vw-40px)] max-h-[calc(100vh-40px)] max-w-[calc(100vw-40px)] sm:h-[calc(100vh-40px)] sm:w-[calc(100vw-40px)] sm:max-h-[calc(100vh-40px)] sm:max-w-[calc(100vw-40px)] overflow-hidden rounded-2xl p-0"
        showCloseButton={false}
      >
        <div className="grid h-full min-h-0 grid-cols-1 bg-white lg:grid-cols-12">
          <div className="col-span-7 flex h-full flex-col border-r border-zinc-200 bg-white xl:col-span-8">
            <DialogHeader className="shrink-0 border-b border-zinc-200 px-6 py-5">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <DialogTitle className="text-4xl font-bold text-zinc-950">
                    创建新的助理
                  </DialogTitle>
                  <DialogDescription className="mt-1 text-base text-zinc-500">
                    配置你的智能助理，支持工具调用和知识库集成。
                  </DialogDescription>
                </div>
                <Button variant="outline" className="px-5" onClick={onCancel}>
                  取消
                </Button>
              </div>
            </DialogHeader>

            <Tabs
              value={currentStep}
              onValueChange={onActiveStepChange}
              className="flex h-full min-h-0 flex-1 flex-col overflow-hidden"
            >
              <div className="shrink-0 border-b border-zinc-100 px-6 py-4">
                <TabsList
                  variant="default"
                  className="grid h-11 w-full grid-cols-3 bg-zinc-100 p-1"
                >
                  <TabsTrigger value="basic">基础信息</TabsTrigger>
                  <TabsTrigger value="prompt">提示词</TabsTrigger>
                  <TabsTrigger value="tools">工具 & 知识库</TabsTrigger>
                </TabsList>
              </div>

              <div className="min-h-0 flex-1 overflow-y-auto px-6 py-6">
                <TabsContent value="basic" className="mt-0 flex flex-col gap-8">
                  <div className="rounded-xl border border-zinc-100 p-4">
                    <h3 className="font-semibold text-zinc-900">名称 & 头像</h3>
                    <div className="mt-4 grid gap-5 md:grid-cols-[1fr_180px]">
                      <div className="flex flex-col gap-2">
                        <Label htmlFor="agent-name">名称</Label>
                        <Input
                          id="agent-name"
                          value={form.name}
                          onChange={(event) =>
                            onFormChange((prev) => ({
                              ...prev,
                              name: event.target.value,
                            }))
                          }
                          placeholder="给你的功能性助理起个名字"
                        />
                      </div>
                      <div className="flex flex-col gap-2">
                        <Label>头像</Label>
                        <div className="flex items-center gap-3">
                          <div className="flex size-12 items-center justify-center rounded-full border border-zinc-200 bg-white text-zinc-500">
                            <CakeSlice />
                          </div>
                          <Button type="button" variant="outline">
                            <FileUp data-icon="inline-start" />
                            上传头像
                          </Button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="rounded-xl border border-zinc-100 p-4">
                    <h3 className="font-semibold text-zinc-900">描述</h3>
                    <div className="mt-3 flex flex-col gap-2">
                      <Label htmlFor="agent-description">描述</Label>
                      <Textarea
                        id="agent-description"
                        rows={4}
                        value={form.description}
                        onChange={(event) =>
                          onFormChange((prev) => ({
                            ...prev,
                            description: event.target.value,
                          }))
                        }
                        placeholder="输入功能性助理的描述"
                      />
                    </div>
                  </div>

                </TabsContent>

                <TabsContent value="prompt" className="mt-0 flex flex-col gap-8">
                  <div className="rounded-xl border border-zinc-100 p-4">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <h3 className="font-semibold text-zinc-900">
                          系统提示词
                        </h3>
                        <p className="mt-1 text-sm text-zinc-600">
                          定义聊天助理的角色、能力和行为限制，或使用AI自动生成。
                        </p>
                      </div>
                      <Button type="button" variant="outline">
                        <Sparkles data-icon="inline-start" />
                        生成
                      </Button>
                    </div>
                    <Textarea
                      className="mt-3"
                      rows={8}
                      value={form.systemPrompt}
                      onChange={(event) =>
                        onFormChange((prev) => ({
                          ...prev,
                          systemPrompt: event.target.value,
                        }))
                      }
                      placeholder="你是一个有用的AI助手。"
                    />
                  </div>

                  <div className="rounded-xl border border-zinc-100 p-4">
                    <h3 className="font-semibold text-zinc-900">欢迎消息</h3>
                    <p className="mt-1 text-sm text-zinc-600">
                      用户首次与聊天助理交互时显示的消息
                    </p>
                    <Textarea
                      className="mt-3"
                      rows={5}
                      value={form.welcomeMessage}
                      onChange={(event) =>
                        onFormChange((prev) => ({
                          ...prev,
                          welcomeMessage: event.target.value,
                        }))
                      }
                      placeholder="你好！我是你的AI助手，有什么可以帮助你的吗？"
                    />
                  </div>
                </TabsContent>

                <TabsContent value="tools" className="mt-0 flex flex-col gap-8">
                  <div className="rounded-xl border border-zinc-100 p-4">
                    <h3 className="font-semibold text-zinc-900">可用工具</h3>
                    <p className="mt-1 text-sm text-zinc-600">
                      选择功能性助理可以使用的工具
                    </p>
                    <div className="mt-4 grid gap-3 lg:grid-cols-2">
                      {toolsLoading ? (
                        <p className="text-sm text-zinc-500">工具列表加载中...</p>
                      ) : null}
                      {!toolsLoading && tools.length === 0 ? (
                        <p className="text-sm text-zinc-500">暂无可用工具</p>
                      ) : null}
                      {!toolsLoading && tools.map((item) => (
                        <ToggleCard
                          key={item.id}
                          title={item.name}
                          icon={<Wrench />}
                          enabled={item.enabled}
                          onToggle={() =>
                            onToolsChange((prev) =>
                              prev.map((tool) =>
                                tool.id === item.id
                                  ? { ...tool, enabled: !tool.enabled }
                                  : tool
                              )
                            )
                          }
                        />
                      ))}
                    </div>
                  </div>

                  <div className="rounded-xl border border-zinc-100 p-4">
                    <h3 className="font-semibold text-zinc-900">知识库</h3>
                    <p className="mt-1 text-sm text-zinc-600">
                      选择助理可调用的知识库
                    </p>
                    <div className="mt-4 grid gap-3 lg:grid-cols-2">
                      {knowledgeBasesLoading ? (
                        <p className="text-sm text-zinc-500">知识库列表加载中...</p>
                      ) : null}
                      {!knowledgeBasesLoading && knowledgeBases.length === 0 ? (
                        <p className="text-sm text-zinc-500">暂无可用知识库</p>
                      ) : null}
                      {!knowledgeBasesLoading && knowledgeBases.map((item) => (
                        <ToggleCard
                          key={item.id}
                          title={item.name}
                          icon={<Database />}
                          enabled={item.enabled}
                          onToggle={() =>
                            onKnowledgeBasesChange((prev) =>
                              prev.map((kb) =>
                                kb.id === item.id
                                  ? { ...kb, enabled: !kb.enabled }
                                  : kb
                              )
                            )
                          }
                        />
                      ))}
                    </div>
                  </div>
                </TabsContent>
              </div>
            </Tabs>

            <div className="shrink-0 flex items-center justify-end gap-2 border-t border-zinc-100 px-6 py-4">
              <Button variant="outline" onClick={onCancel}>
                取消
              </Button>
              <Button
                disabled={creating}
                className="bg-blue-600 text-white hover:bg-blue-700"
                onClick={onConfirmCreate}
              >
                {creating ? "创建中..." : "确认创建"}
              </Button>
            </div>
          </div>

          <aside className="col-span-5 flex h-full flex-col bg-zinc-50 xl:col-span-4">
            <div className="border-b border-zinc-200 px-5 py-5">
              <h3 className="text-4xl font-bold text-zinc-950">预览</h3>
              <p className="mt-1 text-base text-zinc-600">
                与你的Agent进行实时对话，预览实际效果
              </p>
              <div className="mt-4 flex items-center justify-between rounded-xl border border-blue-200 bg-blue-50 px-3 py-2">
                <div className="flex flex-col">
                  <span className="text-sm font-semibold text-blue-700">当前使用模型</span>
                  <span className="text-xs text-blue-600">切换模型按应用到预览对话</span>
                </div>
                <div className="rounded-md border bg-white px-3 py-1 text-sm font-medium text-zinc-700">
                  gpt-4o-mini
                </div>
              </div>
            </div>

            <div className="flex min-h-0 flex-1 flex-col gap-4 px-4 py-4">
              <div className="rounded-2xl border border-zinc-200 bg-white p-4">
                <h4 className="text-base font-semibold text-zinc-900">预览对话</h4>
                <p className="mt-2 text-sm text-zinc-600">
                  当前阶段仅支持 Agent 基础信息、工具和知识库配置，预览对话功能暂未接入。
                </p>
              </div>

              <div className="rounded-2xl border border-zinc-200 bg-white p-4">
                <h4 className="text-2xl font-bold text-zinc-900">配置摘要</h4>
                <div className="mt-3 space-y-2 text-sm text-zinc-700">
                  <p className="flex items-center justify-between">
                    <span>类型</span>
                    <span className="font-semibold">智能助理</span>
                  </p>
                  <p className="flex items-center justify-between">
                    <span>工具数量</span>
                    <span className="font-semibold">{selectedToolCount}</span>
                  </p>
                  <p className="flex items-center justify-between">
                    <span>知识库数量</span>
                    <span className="font-semibold">{selectedKnowledgeCount}</span>
                  </p>
                  <p className="flex items-center justify-between">
                    <span>状态</span>
                    <Badge className="bg-blue-600 text-white">启用</Badge>
                  </p>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </DialogContent>
    </Dialog>
  );
}
