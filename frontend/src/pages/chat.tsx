import { useState, useRef, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { chatApi } from "@/api/services";
import type { ChatResponse } from "@/api/types";
import { getErrorMessage } from "@/api/api-client";
import { useSessions } from "@/hooks/use-sessions";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Separator } from "@/components/ui/separator";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import {
    Plus,
    Send,
    MessageSquare,
    Trash2,
    MoreVertical,
    Pencil,
    AlertTriangle,
    Bot,
    User,
    MessageCircleOff
} from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { format } from "date-fns";

export default function ChatPage() {
    const [activeId, setActiveId] = useState<string | null>(null);
    const [input, setInput] = useState("");
    const scrollRef = useRef<HTMLDivElement>(null);
    const queryClient = useQueryClient();
    const { sessions, addSession, deleteSession, renameSession, updateSessionTime } = useSessions();

    const { data: messages = [] } = useQuery({
        queryKey: ["messages", activeId],
        queryFn: () => chatApi.getMessages(activeId!),
        enabled: !!activeId,
    });

    const sendMessageMutation = useMutation({
        mutationFn: (text: string) => chatApi.sendMessage(activeId, text),
        onSuccess: (data: ChatResponse, text) => {
            const isNew = !activeId;
            if (isNew) {
                addSession(data.conversationId, text);
                setActiveId(data.conversationId);
            } else {
                updateSessionTime(activeId);
            }
            queryClient.invalidateQueries({ queryKey: ["messages", data.conversationId] });
            setInput("");

            if (data.blocked) {
                toast.warning("Message blocked by content filter", {
                    description: data.reason || data.blockedReason || "Inappropriate content detected",
                });
            }
        },
        onError: (error: unknown) => {
            toast.error("Failed to send message", {
                description: getErrorMessage(error, "Unable to contact the chat API"),
            });
        },
    });

    useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [messages, sendMessageMutation.isPending]);

    const handleSend = (e: React.FormEvent) => {
        e.preventDefault();
        if (!input.trim() || sendMessageMutation.isPending) return;
        sendMessageMutation.mutate(input.trim());
    };

    const handleNewChat = () => {
        setActiveId(null);
        setInput("");
    };

    return (
        <div className="flex h-[calc(100vh-64px)] overflow-hidden">
            {/* Sidebar */}
            <aside className="w-80 border-r bg-zinc-900/50 flex flex-col">
                <div className="p-4">
                    <Button onClick={handleNewChat} className="w-full justify-start gap-2" variant="secondary">
                        <Plus className="h-4 w-4" />
                        New Chat
                    </Button>
                </div>
                <Separator />
                <ScrollArea className="flex-1">
                    <div className="p-2 space-y-1">
                        <div className="px-2 py-1.5 text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            Local Sessions
                        </div>
                        {sessions.length === 0 ? (
                            <div className="p-4 text-center text-sm text-muted-foreground italic">
                                No recent chats
                            </div>
                        ) : (
                            sessions.map((s) => (
                                <div
                                    key={s.conversationId}
                                    className={cn(
                                        "group relative flex items-center rounded-lg px-3 py-2 text-sm transition-colors cursor-pointer",
                                        activeId === s.conversationId ? "bg-accent text-accent-foreground" : "hover:bg-accent/50 text-muted-foreground"
                                    )}
                                    onClick={() => setActiveId(s.conversationId)}
                                >
                                    <MessageSquare className="mr-2 h-4 w-4 shrink-0" />
                                    <span className="truncate flex-1">{s.title}</span>
                                    <DropdownMenu>
                                        <DropdownMenuTrigger asChild>
                                            <Button variant="ghost" size="icon" className="h-7 w-7 opacity-0 group-hover:opacity-100 transition-opacity">
                                                <MoreVertical className="h-3 w-3" />
                                            </Button>
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent align="end">
                                            <DropdownMenuItem onClick={(e) => {
                                                e.stopPropagation();
                                                const title = prompt("Rename chat", s.title);
                                                if (title) renameSession(s.conversationId, title);
                                            }}>
                                                <Pencil className="mr-2 h-4 w-4" /> Rename
                                            </DropdownMenuItem>
                                            <DropdownMenuItem
                                                onClick={(e) => { e.stopPropagation(); deleteSession(s.conversationId); if (activeId === s.conversationId) setActiveId(null); }}
                                                className="text-destructive focus:text-destructive"
                                            >
                                                <Trash2 className="mr-2 h-4 w-4" /> Delete
                                            </DropdownMenuItem>
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                </div>
                            ))
                        )}
                    </div>
                </ScrollArea>
            </aside>

            {/* Main Chat Area */}
            <div className="flex-1 flex flex-col bg-background relative">
                <div className="flex-1 overflow-y-auto" ref={scrollRef}>
                    <div className="max-w-3xl mx-auto py-8 px-4 space-y-6">
                        {!activeId && !sendMessageMutation.isPending && (
                            <div className="flex flex-col items-center justify-center py-20 text-center space-y-4">
                                <div className="p-6 bg-primary/5 rounded-full">
                                    <Bot className="h-16 w-16 text-primary/40" />
                                </div>
                                <div className="space-y-2">
                                    <h2 className="text-2xl font-bold tracking-tight">How can I help you today?</h2>
                                    <p className="text-muted-foreground max-w-[400px]">
                                        Start a new conversation and I'll do my best to provide helpful responses while keeping content safe.
                                    </p>
                                </div>
                            </div>
                        )}

                        {messages.map((m) => (
                            <div key={m.id} className={cn("flex gap-3", m.role === "USER" ? "flex-row-reverse" : "flex-row")}>
                                <div className={cn(
                                    "h-8 w-8 rounded-full flex items-center justify-center shrink-0 border",
                                    m.role === "ASSISTANT" ? "bg-primary/10 border-primary/20" : "bg-zinc-800"
                                )}>
                                    {m.role === "ASSISTANT" ? <Bot className="h-4 w-4 text-primary" /> : <User className="h-4 w-4" />}
                                </div>
                                <div className={cn(
                                    "flex flex-col max-w-[80%] space-y-1",
                                    m.role === "USER" ? "items-end" : "items-start"
                                )}>
                                    <div className={cn(
                                        "rounded-2xl px-4 py-2.5 text-sm shadow-sm",
                                        m.role === "USER" ? "bg-primary text-primary-foreground" : "bg-muted border border-border/50",
                                        m.blocked && "border-destructive/30 bg-destructive/5"
                                    )}>
                                        {m.content}
                                        {m.blocked && (
                                            <div className="mt-2 flex items-center gap-1.5 pt-2 border-t border-destructive/20 text-[11px] font-medium text-destructive">
                                                <MessageCircleOff className="h-3 w-3" />
                                                Blocked Message
                                                <TooltipProvider>
                                                    <Tooltip>
                                                        <TooltipTrigger>
                                                            <AlertTriangle className="h-3 w-3 ml-1" />
                                                        </TooltipTrigger>
                                                        <TooltipContent>
                                                            <p>{m.blockedReason || "Inappropriate content detected"}</p>
                                                        </TooltipContent>
                                                    </Tooltip>
                                                </TooltipProvider>
                                            </div>
                                        )}
                                    </div>
                                    <span className="text-[10px] text-muted-foreground px-1">
                                        {format(new Date(m.createdAt), "HH:mm")}
                                    </span>
                                </div>
                            </div>
                        ))}

                        {sendMessageMutation.isPending && (
                            <div className="flex gap-3 animate-pulse">
                                <div className="h-8 w-8 rounded-full bg-primary/5 border border-primary/10 flex items-center justify-center shrink-0">
                                    <Bot className="h-4 w-4 text-primary/40" />
                                </div>
                                <div className="bg-muted rounded-2xl px-4 py-2.5 text-sm border border-border/50 text-muted-foreground">
                                    Thinking...
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Composer */}
                <div className="border-t bg-background/50 backdrop-blur pb-6 pt-4">
                    <form onSubmit={handleSend} className="max-w-3xl mx-auto px-4 flex gap-2">
                        <div className="relative flex-1">
                            <Input
                                placeholder="Message chatbot..."
                                value={input}
                                onChange={(e) => setInput(e.target.value)}
                                className="pr-12 py-6 bg-zinc-900/50 border-zinc-800 rounded-xl focus:ring-primary shadow-inner"
                                disabled={sendMessageMutation.isPending}
                            />
                            <Button
                                type="submit"
                                size="icon"
                                className="absolute right-2 top-1.5 h-9 w-9 rounded-lg transition-all"
                                disabled={!input.trim() || sendMessageMutation.isPending}
                            >
                                <Send className="h-4 w-4" />
                            </Button>
                        </div>
                    </form>
                    <div className="max-w-3xl mx-auto px-4 mt-2 text-center">
                        <p className="text-[10px] text-muted-foreground">
                            Assistant responses are filtered for safety. Local sessions are stored only in your browser.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
