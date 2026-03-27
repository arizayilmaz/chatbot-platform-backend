/* eslint-disable react-refresh/only-export-components */
import React, { createContext, useContext, useState } from "react";
import { jwtDecode } from "jwt-decode";
import type { JwtPayload, LoginRequest, RegisterRequest } from "@/api/types";
import { apiFetch } from "@/api/api-client";

interface AuthContextType {
    user: { id: string; role: "USER" | "ADMIN" } | null;
    isAuthenticated: boolean;
    isAdmin: boolean;
    login: (req: LoginRequest) => Promise<void>;
    register: (req: RegisterRequest) => Promise<void>;
    logout: () => void;
    isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<{ id: string; role: "USER" | "ADMIN" } | null>(() => {
        const token = localStorage.getItem("cb_token");
        if (!token) {
            return null;
        }

        try {
            const decoded = jwtDecode<JwtPayload>(token);
            return { id: decoded.sub, role: decoded.role };
        } catch {
            localStorage.removeItem("cb_token");
            return null;
        }
    });
    const [isLoading] = useState(false);

    const decodeAndSetUser = (token: string) => {
        try {
            const decoded = jwtDecode<JwtPayload>(token);
            setUser({ id: decoded.sub, role: decoded.role });
        } catch (e) {
            console.error("Failed to decode token", e);
            localStorage.removeItem("cb_token");
            setUser(null);
        }
    };
    const login = async (req: LoginRequest) => {
        const { token } = await apiFetch<{ token: string }>("/api/auth/login", {
            method: "POST",
            body: JSON.stringify(req),
        });
        localStorage.setItem("cb_token", token);
        decodeAndSetUser(token);
    };

    const register = async (req: RegisterRequest) => {
        const { token } = await apiFetch<{ token: string }>("/api/auth/register", {
            method: "POST",
            body: JSON.stringify(req),
        });
        localStorage.setItem("cb_token", token);
        decodeAndSetUser(token);
    };

    const logout = () => {
        localStorage.removeItem("cb_token");
        setUser(null);
        window.location.href = "/login";
    };

    return (
        <AuthContext.Provider
            value={{
                user,
                isAuthenticated: !!user,
                isAdmin: user?.role === "ADMIN",
                login,
                register,
                logout,
                isLoading,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
};
