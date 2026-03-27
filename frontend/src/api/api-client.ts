import type { ApiError } from "./types";

export class ApiException extends Error {
    constructor(public error: ApiError) {
        super(error.message);
        this.name = "ApiException";
    }
}

export function getErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof ApiException) {
        return error.error.message;
    }

    if (error instanceof Error && error.message) {
        return error.message;
    }

    return fallback;
}

export async function apiFetch<T>(
    endpoint: string,
    options: RequestInit = {}
): Promise<T> {
    const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "";
    const token = localStorage.getItem("cb_token");

    const headers = new Headers(options.headers);
    if (token && !endpoint.includes("/api/auth/")) {
        headers.set("Authorization", `Bearer ${token}`);
    }

    if (!(options.body instanceof FormData)) {
        headers.set("Content-Type", "application/json");
    }

    const response = await fetch(`${baseUrl}${endpoint}`, {
        ...options,
        headers,
    });

    if (response.status === 204) {
        return {} as T;
    }

    if (!response.ok) {
        let errorData: ApiError;
        try {
            errorData = await response.json();
        } catch {
            errorData = {
                timestamp: new Date().toISOString(),
                path: endpoint,
                code: "UNKNOWN_ERROR",
                message: `Request failed with status ${response.status}`,
            };
        }

        if (response.status === 401 && !endpoint.includes("/api/auth/login")) {
            localStorage.removeItem("cb_token");
            window.location.href = "/login";
        }

        throw new ApiException(errorData);
    }

    return response.json();
}
