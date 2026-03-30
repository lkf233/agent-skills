# Docker Compose 快速部署

## 1. 准备环境

- 安装 Docker 与 Docker Compose
- 在项目根目录执行命令

可先在 Linux 服务器检查：

```bash
docker --version
docker compose version
```

## 2. 从 Git 仓库拉取代码（Linux 服务器）

首次部署：

```bash
git clone <你的仓库地址>.git
cd agent-chat
```

后续更新：

```bash
cd agent-chat
git pull
```

## 3. 配置环境变量

复制模板并按实际服务器信息修改：

```bash
cp .env.docker.example .env.docker
```

至少需要修改以下配置：

- `POSTGRES_PASSWORD`
- `RAG_EMBEDDING_API_KEY`
- `NEXT_PUBLIC_API_BASE_URL`（改为服务器可访问地址，例如 `http://<你的服务器IP>:8080/api` 或域名地址）

## 4. 启动服务

```bash
docker compose --env-file .env.docker up -d --build
```

启动后包含三个服务：

- `postgres`：PostgreSQL + pgvector
- `backend`：Spring Boot 后端（端口默认 `8080`）
- `frontend`：Next.js 前端（端口默认 `3000`）

## 5. 查看状态

```bash
docker compose ps
docker compose logs -f backend
```

## 6. 访问地址

- 前端：`http://<你的服务器IP>:3000`
- 后端健康检查：`http://<你的服务器IP>:8080/actuator/health`

## 7. 停止与重启

```bash
docker compose down
docker compose --env-file .env.docker up -d
```

## 8. 数据持久化

Compose 已内置两个持久化卷：

- `postgres_data`：数据库数据
- `backend_storage`：知识库上传文件
