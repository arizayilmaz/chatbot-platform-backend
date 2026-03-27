import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/lib/auth";

export function ProtectedRoute() {
    const { isAuthenticated, isLoading } = useAuth();

    if (isLoading) return <div className="flex h-screen items-center justify-center">Loading...</div>;

    return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
}

export function AdminRoute() {
    const { isAuthenticated, isAdmin, isLoading } = useAuth();

    if (isLoading) return <div className="flex h-screen items-center justify-center">Loading...</div>;

    if (!isAuthenticated) return <Navigate to="/login" replace />;

    return isAdmin ? <Outlet /> : <Navigate to="/chat" replace />;
}
