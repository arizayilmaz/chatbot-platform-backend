# Chatbot Platform — Frontend

A production-quality React frontend for the Chatbot Platform, featuring a modern dark-mode aesthetic, conversation management, and administrative content filtering.

## 🚀 Key Features

- **Modern Chat Interface**: Local session persistence, optimistic UI updates, and real-time response rendering.
- **Content Filtering**: Admin dashboard to manage banned keywords and regex patterns with severity levels.
- **Secure Auth**: JWT-based authentication with role-based access control (RBAC).
- **Premium UI**: Built with TailwindCSS v4, shadcn/ui, and Lucide icons for a high-end feel.
- **Robust State**: Powered by TanStack Query for efficient server state management and caching.

## 📋 Prerequisites

- **Node.js**: v18+ 
- **Docker**: For running backend services (Postgres, RabbitMQ, Ollama)

## 🛠️ Getting Started

### 1. Start Backend Services

Follow these steps in the project root:

```bash
# Start Postgres, RabbitMQ, and Ollama
docker-compose up -d

# Pull the LLM model (if not already present)
# Default is llama3.2:3b
docker exec -it chatbot-ollama ollama pull llama3.2:3b

# Run the Spring Boot API
# Ensure you have a .env file based on .env.example
./mvnw spring-boot:run
```

*The backend runs on `http://localhost:8080`.*

### 2. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

*The frontend runs on `http://localhost:5173`. Calls to `/api/*` are automatically proxied to the backend.*

## 🔑 Demo Credentials

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@local.dev` | `admin123` |
| **User** | `user@local.dev` | `user123` |

## 🌐 Deployment Configuration

### CORS
The backend allowed origins can be configured via the `app.cors.allowed-origins` property or `CORS_ALLOWED_ORIGINS` environment variable (comma-separated).

### Frontend API URL
For production deployments where the frontend and backend are on different domains, set `VITE_API_BASE_URL` in your frontend environment (e.g., `.env.production`).

## 🧪 Troubleshooting

- **CORS Errors**: Ensure `VITE_API_BASE_URL` is correct and the backend origin is allowed.
- **Login Failures**: If your database was seeded with old `@local` credentials, they may fail validation. Use the new `@local.dev` credentials provided by Flyway migration V5.
- **Chat Not Responding**: Ensure Ollama is running and the model is pulled.

---
*Created as part of the Chatbot Platform portfolio project.*
