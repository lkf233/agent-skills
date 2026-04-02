"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Bot, Loader2, MessageSquare, Plus, SendHorizontal } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import { listAgents, type AgentResponseObject } from "@/lib/services/agent-service";
import {
  createConversation,
  listConversations,
  listConversationMessages,
  listConversationSummaries,
  streamConversation,
  type ConversationResponseObject,
} from "@/lib/services/conversation-service";
import { cn } from "@/lib/utils";

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

export default function ExplorePage() {
  const queryClient = useQueryClient();
  const [selectedAgentId, setSelectedAgentId] = useState<string | null>(null);
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);
  const [messageInput, setMessageInput] = useState("");
  const [streamingReply, setStreamingReply] = useState("");

  const agentsQuery = useQuery({
    queryKey: ["agents", "chat-explore"],
    queryFn: () => listAgents(),
  });
  const effectiveSelectedAgentId = selectedAgentId ?? agentsQuery.data?.[0]?.id ?? null;
  const conversationsQuery = useQuery({
    queryKey: ["conversations", effectiveSelectedAgentId],
    queryFn: () => listConversations(effectiveSelectedAgentId ?? undefined),
    enabled: Boolean(effectiveSelectedAgentId),
  });
  const filteredConversations = useMemo(() => {
    return conversationsQuery.data ?? [];
  }, [conversationsQuery.data]);
  const effectiveSelectedConversationId = useMemo(() => {
    if (!filteredConversations.length) {
      return null;
    }
    if (selectedConversationId && filteredConversations.some((item) => item.id === selectedConversationId)) {
      return selectedConversationId;
    }
    return filteredConversations[0].id;
  }, [filteredConversations, selectedConversationId]);

  const messagesQuery = useQuery({
    queryKey: ["conversation-messages", effectiveSelectedConversationId],
    queryFn: () => listConversationMessages(effectiveSelectedConversationId as string, 1, 100),
    enabled: Boolean(effectiveSelectedConversationId),
  });
  const summariesQuery = useQuery({
    queryKey: ["conversation-summaries", effectiveSelectedConversationId],
    queryFn: () => listConversationSummaries(effectiveSelectedConversationId as string, 10),
    enabled: Boolean(effectiveSelectedConversationId),
  });

  const currentAgent = useMemo(
    () => (agentsQuery.data ?? []).find((item) => item.id === effectiveSelectedAgentId) ?? null,
    [agentsQuery.data, effectiveSelectedAgentId]
  );
  const currentConversation = useMemo(
    () => filteredConversations.find((item) => item.id === effectiveSelectedConversationId) ?? null,
    [filteredConversations, effectiveSelectedConversationId]
  );

  const createConversationMutation = useMutation({
    mutationFn: ({ title, agentId }: { title: string; agentId: string }) => createConversation({ title, agentId }),
    onSuccess: async (conversation) => {
      await queryClient.invalidateQueries({ queryKey: ["conversations"] });
      setSelectedConversationId(conversation.id);
      toast.success("会话创建成功");
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "会话创建失败";
      toast.error(message);
    },
  });

  const sendMessageMutation = useMutation({
    mutationFn: async ({ conversationId, content }: { conversationId: string; content: string }) => {
      setStreamingReply("");
      await streamConversation(conversationId, content, {
        onToken: (token) => {
          setStreamingReply((prev) => `${prev}${token}`);
        },
        onError: (message) => {
          throw new Error(message);
        },
        onDone: () => {},
      });
    },
    onSuccess: async (_, variables) => {
      setStreamingReply("");
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["conversation-messages", variables.conversationId] }),
        queryClient.invalidateQueries({ queryKey: ["conversation-summaries", variables.conversationId] }),
        queryClient.invalidateQueries({ queryKey: ["conversations", effectiveSelectedAgentId] }),
      ]);
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "发送失败";
      setStreamingReply("");
      toast.error(message);
    },
  });

  const onCreateConversation = async () => {
    if (!currentAgent) {
      toast.error("请先选择Agent");
      return;
    }
    const title = `${currentAgent.name} 对话 ${new Date().toLocaleString("zh-CN", { hour12: false })}`;
    const conversation = await createConversationMutation.mutateAsync({
      title,
      agentId: currentAgent.id,
    });
    setSelectedConversationId(conversation.id);
  };

  const onSendMessage = async () => {
    const content = messageInput.trim();
    if (!content) {
      return;
    }
    if (!currentAgent) {
      toast.error("请先选择Agent");
      return;
    }
    let conversationId = effectiveSelectedConversationId;
    if (!conversationId) {
      const title = `${currentAgent.name} 对话 ${new Date().toLocaleString("zh-CN", { hour12: false })}`;
      const conversation = await createConversationMutation.mutateAsync({
        title,
        agentId: currentAgent.id,
      });
      conversationId = conversation.id;
      setSelectedConversationId(conversation.id);
    }
    setMessageInput("");
    await sendMessageMutation.mutateAsync({
      conversationId,
      content,
    });
  };

  return (
    <section className="h-[calc(100vh-120px)] overflow-hidden rounded-2xl border bg-background">
      <div className="flex h-full">
        <aside className="flex w-64 flex-col gap-3 border-r p-3">
          <div className="flex items-center gap-2">
            <Bot className="text-primary" />
            <p className="text-sm font-semibold">Agent 列表</p>
          </div>
          <div className="flex-1 overflow-y-auto">
            <div className="flex flex-col gap-2">
              {agentsQuery.isLoading ? (
                <>
                  <Skeleton className="h-9 w-full" />
                  <Skeleton className="h-9 w-full" />
                  <Skeleton className="h-9 w-full" />
                </>
              ) : null}
              {(agentsQuery.data ?? []).map((agent: AgentResponseObject) => (
                <button
                  type="button"
                  key={agent.id}
                  onClick={() => setSelectedAgentId(agent.id)}
                  className={cn(
                    "flex w-full items-center justify-between rounded-lg border px-3 py-2 text-left text-sm transition",
                    effectiveSelectedAgentId === agent.id ? "border-primary bg-primary/5 text-primary" : "hover:bg-muted"
                  )}
                >
                  <span className="truncate">{agent.name}</span>
                </button>
              ))}
              {!agentsQuery.isLoading && (agentsQuery.data ?? []).length === 0 ? (
                <p className="rounded-lg border border-dashed px-3 py-2 text-xs text-muted-foreground">暂无Agent</p>
              ) : null}
            </div>
          </div>
        </aside>

        <aside className="flex w-72 flex-col gap-3 border-r p-3">
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <MessageSquare className="text-primary" />
              <p className="text-sm font-semibold">会话列表</p>
            </div>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onCreateConversation}
              disabled={!currentAgent || createConversationMutation.isPending}
            >
              {createConversationMutation.isPending ? <Loader2 className="animate-spin" /> : <Plus />}
              新建
            </Button>
          </div>
          <Input
            value={currentAgent?.name ?? ""}
            readOnly
            className="h-8"
            placeholder="请先选择Agent"
          />
          <div className="flex-1 overflow-y-auto">
            <div className="flex flex-col gap-2">
              {conversationsQuery.isLoading ? (
                <>
                  <Skeleton className="h-16 w-full" />
                  <Skeleton className="h-16 w-full" />
                </>
              ) : null}
              {filteredConversations.map((conversation: ConversationResponseObject) => (
                <button
                  type="button"
                  key={conversation.id}
                  onClick={() => setSelectedConversationId(conversation.id)}
                  className={cn(
                    "flex w-full flex-col gap-1 rounded-lg border px-3 py-2 text-left text-xs transition",
                    effectiveSelectedConversationId === conversation.id ? "border-primary bg-primary/5" : "hover:bg-muted"
                  )}
                >
                  <span className="truncate text-sm font-medium">{conversation.title}</span>
                  <span className="truncate text-muted-foreground">{conversation.id}</span>
                </button>
              ))}
              {!conversationsQuery.isLoading && filteredConversations.length === 0 ? (
                <p className="rounded-lg border border-dashed px-3 py-2 text-xs text-muted-foreground">
                  当前Agent暂无会话，点击右上角新建
                </p>
              ) : null}
            </div>
          </div>
        </aside>

        <div className="flex flex-1 flex-col overflow-hidden">
          <header className="flex items-center justify-between gap-3 border-b px-4 py-3">
            <div className="flex flex-col gap-1">
              <h1 className="text-base font-semibold">{currentAgent?.name || "AI助手"}</h1>
              <p className="text-xs text-muted-foreground">{currentConversation?.title || "请选择会话开始聊天"}</p>
            </div>
          </header>
          <div className="flex-1 overflow-y-auto bg-muted/20 px-4 py-3">
            <div className="flex flex-col gap-3">
              {(messagesQuery.data?.records ?? []).map((message) => {
                const isAssistant = message.role.toLowerCase() === "assistant";
                return (
                  <div key={message.id} className={cn("flex", isAssistant ? "justify-start" : "justify-end")}>
                    <div
                      className={cn(
                        "max-w-[80%] rounded-xl px-3 py-2 text-sm",
                        isAssistant ? "bg-background text-foreground" : "bg-primary text-primary-foreground"
                      )}
                    >
                      <p className="whitespace-pre-wrap break-words">{message.content}</p>
                      <p className={cn("mt-1 text-[10px]", isAssistant ? "text-muted-foreground" : "text-primary-foreground/70")}>
                        {formatTime(message.createdAt)}
                      </p>
                    </div>
                  </div>
                );
              })}
              {sendMessageMutation.isPending && streamingReply ? (
                <div className="flex justify-start">
                  <div className="max-w-[80%] rounded-xl bg-background px-3 py-2 text-sm text-foreground">
                    <p className="whitespace-pre-wrap break-words">{streamingReply}</p>
                    <p className="mt-1 text-[10px] text-muted-foreground">生成中...</p>
                  </div>
                </div>
              ) : null}
              {!effectiveSelectedConversationId ? (
                <p className="rounded-xl border border-dashed bg-background px-4 py-6 text-center text-sm text-muted-foreground">
                  选择左侧会话或直接发送消息，系统将自动创建新会话
                </p>
              ) : null}
              {effectiveSelectedConversationId && !messagesQuery.isLoading && (messagesQuery.data?.records ?? []).length === 0 ? (
                <p className="rounded-xl border border-dashed bg-background px-4 py-6 text-center text-sm text-muted-foreground">
                  当前会话暂无消息，开始提问吧
                </p>
              ) : null}
            </div>
          </div>
          <div className="border-t px-4 py-3">
            <div className="flex items-end gap-2">
              <Textarea
                value={messageInput}
                onChange={(event) => setMessageInput(event.target.value)}
                placeholder="输入消息，Enter 换行，点击发送"
                className="min-h-20"
              />
              <Button type="button" onClick={onSendMessage} disabled={sendMessageMutation.isPending || !messageInput.trim()}>
                {sendMessageMutation.isPending ? <Loader2 className="animate-spin" /> : <SendHorizontal />}
                发送
              </Button>
            </div>
            {effectiveSelectedConversationId ? (
              <div className="mt-3 flex max-h-24 flex-col gap-1 overflow-y-auto rounded-lg bg-muted/40 px-3 py-2">
                <p className="text-xs font-medium text-muted-foreground">历史摘要</p>
                {(summariesQuery.data ?? []).length === 0 ? (
                  <p className="text-xs text-muted-foreground">暂无摘要</p>
                ) : (
                  (summariesQuery.data ?? []).map((summary) => (
                    <p key={summary.id} className="line-clamp-2 text-xs text-muted-foreground">
                      [{summary.rangeStartSeq}-{summary.rangeEndSeq}] {summary.summaryText}
                    </p>
                  ))
                )}
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </section>
  );
}
