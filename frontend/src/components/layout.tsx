import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "@/lib/auth";
import { Button } from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";
import { User, LogOut, Shield, MessageSquare, Bot } from "lucide-react";

export function Layout({ children }: { children: React.ReactNode }) {
    const { user, logout, isAdmin, isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    if (!isAuthenticated) return <>{children}</>;

    const navLinks = [
        { label: "Chat", path: "/chat", icon: MessageSquare },
        { label: "Admin", path: "/admin", icon: Shield, adminOnly: true },
    ];

    return (
        <div className="flex min-h-screen flex-col bg-background text-foreground">
            <header className="sticky top-0 z-40 border-b bg-background/95 backdrop-blur">
                <div className="container flex h-16 items-center justify-between px-4 sm:px-8">
                    <div className="flex items-center gap-6">
                        <Link to="/chat" className="flex items-center gap-2 font-bold text-xl tracking-tight">
                            <Bot className="h-6 w-6 text-primary" />
                            <span>Chatbot Platform</span>
                        </Link>
                        <nav className="hidden md:flex items-center gap-4">
                            {navLinks.map((link) => {
                                if (link.adminOnly && !isAdmin) return null;
                                const active = location.pathname === link.path;
                                return (
                                    <Link
                                        key={link.path}
                                        to={link.path}
                                        className={`text-sm font-medium transition-colors hover:text-primary ${active ? "text-primary" : "text-muted-foreground"
                                            }`}
                                    >
                                        {link.label}
                                    </Link>
                                );
                            })}
                        </nav>
                    </div>

                    <div className="flex items-center gap-4">
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="icon" className="relative h-9 w-9 rounded-full border">
                                    <User className="h-5 w-5" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-56">
                                <DropdownMenuLabel className="font-normal">
                                    <div className="flex flex-col space-y-1">
                                        <p className="text-sm font-medium leading-none">{user?.id}</p>
                                        <p className="text-xs leading-none text-muted-foreground">
                                            {user?.role} Role
                                        </p>
                                    </div>
                                </DropdownMenuLabel>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={() => navigate("/chat")}>
                                    <MessageSquare className="mr-2 h-4 w-4" />
                                    <span>Chat</span>
                                </DropdownMenuItem>
                                {isAdmin && (
                                    <DropdownMenuItem onClick={() => navigate("/admin")}>
                                        <Shield className="mr-2 h-4 w-4" />
                                        <span>Admin Panel</span>
                                    </DropdownMenuItem>
                                )}
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={logout} className="text-destructive focus:text-destructive">
                                    <LogOut className="mr-2 h-4 w-4" />
                                    <span>Log out</span>
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>
            </header>
            <main className="flex-1 overflow-hidden">
                {children}
            </main>
        </div>
    );
}
