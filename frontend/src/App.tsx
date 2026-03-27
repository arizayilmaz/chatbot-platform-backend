import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/lib/query-client";
import { AuthProvider } from "@/lib/auth";
import { Toaster } from "@/components/ui/sonner";
import { Layout } from "@/components/layout";
import { ProtectedRoute, AdminRoute } from "@/components/protected-route";

// Pages
import LoginPage from "@/pages/login";
import RegisterPage from "@/pages/register";
import ChatPage from "@/pages/chat";
import AdminPage from "@/pages/admin";

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Layout>
            <Routes>
              {/* Public Routes */}
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />

              {/* Protected Protected Routes */}
              <Route element={<ProtectedRoute />}>
                <Route path="/chat" element={<ChatPage />} />
                <Route path="/" element={<Navigate to="/chat" replace />} />
              </Route>

              {/* Admin Only Routes */}
              <Route element={<AdminRoute />}>
                <Route path="/admin" element={<AdminPage />} />
              </Route>

              {/* Fallback */}
              <Route path="*" element={<Navigate to="/chat" replace />} />
            </Routes>
          </Layout>
          <Toaster position="top-right" richColors theme="dark" />
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
