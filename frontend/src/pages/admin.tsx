import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/api/services";
import type { CreatePatternRequest } from "@/api/types";
import { getErrorMessage } from "@/api/api-client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow
} from "@/components/ui/table";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
    Plus,
    Trash2,
    Search,
    ShieldAlert,
    Loader2,
    RefreshCw,
    Filter
} from "lucide-react";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { format } from "date-fns";

export default function AdminPage() {
    const [search, setSearch] = useState("");
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [newPattern, setNewPattern] = useState<CreatePatternRequest>({
        pattern: "",
        type: "KEYWORD",
        severity: "MEDIUM",
        notes: "",
    });

    const queryClient = useQueryClient();

    const { data: patterns = [], isLoading, refetch, isRefetching } = useQuery({
        queryKey: ["patterns"],
        queryFn: adminApi.getPatterns,
    });

    const createMutation = useMutation({
        mutationFn: adminApi.createPattern,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["patterns"] });
            setIsCreateOpen(false);
            setNewPattern({ pattern: "", type: "KEYWORD", severity: "MEDIUM", notes: "" });
            toast.success("Pattern created successfully");
        },
        onError: (error: unknown) => toast.error("Failed to create pattern", {
            description: getErrorMessage(error, "Unable to create the pattern"),
        }),
    });

    const toggleMutation = useMutation({
        mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
            adminApi.togglePattern(id, enabled),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["patterns"] });
            toast.success("Pattern status updated");
        },
        onError: (error: unknown) => toast.error("Failed to update pattern", {
            description: getErrorMessage(error, "Unable to update the pattern"),
        }),
    });

    const deleteMutation = useMutation({
        mutationFn: adminApi.deletePattern,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["patterns"] });
            toast.success("Pattern deleted");
        },
        onError: (error: unknown) => toast.error("Failed to delete pattern", {
            description: getErrorMessage(error, "Unable to delete the pattern"),
        }),
    });

    const filteredPatterns = patterns.filter(p =>
        p.pattern.toLowerCase().includes(search.toLowerCase()) ||
        p.notes?.toLowerCase().includes(search.toLowerCase())
    );

    const getSeverityColor = (sev: string) => {
        switch (sev) {
            case "HIGH": return "bg-red-500/10 text-red-500 border-red-500/20";
            case "MEDIUM": return "bg-amber-500/10 text-amber-500 border-amber-500/20";
            case "LOW": return "bg-blue-500/10 text-blue-500 border-blue-500/20";
            default: return "";
        }
    };

    return (
        <div className="container mx-auto py-8 px-4 sm:px-8 max-w-6xl">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Content Filtering</h1>
                    <p className="text-muted-foreground mt-1">
                        Manage banned words and regex patterns to protect your community.
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    <Button variant="outline" size="icon" onClick={() => refetch()} disabled={isLoading || isRefetching}>
                        <RefreshCw className={cn("h-4 w-4", (isLoading || isRefetching) && "animate-spin")} />
                    </Button>
                    <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
                        <DialogTrigger asChild>
                            <Button className="gap-2">
                                <Plus className="h-4 w-4" /> Add Pattern
                            </Button>
                        </DialogTrigger>
                        <DialogContent className="sm:max-w-md bg-zinc-900 border-zinc-800">
                            <DialogHeader>
                                <DialogTitle>Create Banned Pattern</DialogTitle>
                                <DialogDescription>
                                    Define a new keyword or regex pattern to trigger the content filter.
                                </DialogDescription>
                            </DialogHeader>
                            <div className="grid gap-4 py-4">
                                <div className="grid gap-2">
                                    <Label htmlFor="pattern">Pattern</Label>
                                    <Input
                                        id="pattern"
                                        placeholder="e.g. badword"
                                        value={newPattern.pattern}
                                        onChange={e => setNewPattern({ ...newPattern, pattern: e.target.value })}
                                        className="bg-zinc-950 border-zinc-800"
                                    />
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="grid gap-2">
                                        <Label htmlFor="type">Type</Label>
                                        <Select
                                            value={newPattern.type}
                                            onValueChange={(value: "KEYWORD" | "REGEX") => setNewPattern({ ...newPattern, type: value })}
                                        >
                                            <SelectTrigger id="type" className="bg-zinc-950 border-zinc-800">
                                                <SelectValue />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="KEYWORD">Keyword</SelectItem>
                                                <SelectItem value="REGEX">Regex</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <div className="grid gap-2">
                                        <Label htmlFor="severity">Severity</Label>
                                        <Select
                                            value={newPattern.severity}
                                            onValueChange={(value: "LOW" | "MEDIUM" | "HIGH") => setNewPattern({ ...newPattern, severity: value })}
                                        >
                                            <SelectTrigger id="severity" className="bg-zinc-950 border-zinc-800">
                                                <SelectValue />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="LOW">Low</SelectItem>
                                                <SelectItem value="MEDIUM">Medium</SelectItem>
                                                <SelectItem value="HIGH">High</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="notes">Notes (Optional)</Label>
                                    <Textarea
                                        id="notes"
                                        placeholder="Why is this pattern enabled?"
                                        value={newPattern.notes}
                                        onChange={e => setNewPattern({ ...newPattern, notes: e.target.value })}
                                        className="bg-zinc-950 border-zinc-800"
                                    />
                                </div>
                            </div>
                            <DialogFooter>
                                <Button variant="outline" onClick={() => setIsCreateOpen(false)}>Cancel</Button>
                                <Button
                                    onClick={() => createMutation.mutate(newPattern)}
                                    disabled={!newPattern.pattern || createMutation.isPending}
                                >
                                    {createMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                                    Save Pattern
                                </Button>
                            </DialogFooter>
                        </DialogContent>
                    </Dialog>
                </div>
            </div>

            <div className="flex items-center gap-4 mb-6">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                        placeholder="Search patterns or notes..."
                        className="pl-9 bg-zinc-900/50 border-zinc-800"
                        value={search}
                        onChange={e => setSearch(e.target.value)}
                    />
                </div>
                <Button variant="outline" className="gap-2 border-zinc-800">
                    <Filter className="h-4 w-4" /> Filters
                </Button>
            </div>

            <div className="rounded-xl border border-zinc-800 bg-zinc-900/30 overflow-hidden shadow-sm">
                <Table>
                    <TableHeader className="bg-zinc-900/50">
                        <TableRow className="border-zinc-800 hover:bg-transparent">
                            <TableHead className="w-[300px]">Pattern</TableHead>
                            <TableHead>Type</TableHead>
                            <TableHead>Severity</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Created</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={6} className="h-40 text-center">
                                    <Loader2 className="h-8 w-8 animate-spin mx-auto text-muted-foreground/20" />
                                </TableCell>
                            </TableRow>
                        ) : filteredPatterns.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={6} className="h-40 text-center text-muted-foreground italic">
                                    No patterns found matching your search.
                                </TableCell>
                            </TableRow>
                        ) : (
                            filteredPatterns.map(p => (
                                <TableRow key={p.id} className="border-zinc-800 group hover:bg-zinc-800/20">
                                    <TableCell className="font-medium">
                                        <div className="flex flex-col">
                                            <span>{p.pattern}</span>
                                            {p.notes && <span className="text-[10px] text-muted-foreground font-normal mt-0.5">{p.notes}</span>}
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant="outline" className="font-mono text-[10px] bg-zinc-950/50">
                                            {p.type}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        <Badge className={cn("text-[10px] font-bold border", getSeverityColor(p.severity))}>
                                            {p.severity}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        <Switch
                                            checked={p.enabled}
                                            onCheckedChange={(checked) => toggleMutation.mutate({ id: p.id, enabled: checked })}
                                            disabled={toggleMutation.isPending}
                                        />
                                    </TableCell>
                                    <TableCell className="text-muted-foreground text-xs">
                                        {format(new Date(p.createdAt), "MMM d, yyyy")}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            className="h-8 w-8 text-muted-foreground hover:text-destructive opacity-0 group-hover:opacity-100 transition-opacity"
                                            onClick={() => {
                                                if (confirm("Are you sure you want to delete this pattern?")) {
                                                    deleteMutation.mutate(p.id);
                                                }
                                            }}
                                            disabled={deleteMutation.isPending}
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            <div className="mt-6 flex items-center justify-between text-xs text-muted-foreground">
                <div className="flex items-center gap-1">
                    <ShieldAlert className="h-3 w-3" />
                    <span>Active content filtering enabled</span>
                </div>
                <span>Showing {filteredPatterns.length} patterns</span>
            </div>
        </div>
    );
}
