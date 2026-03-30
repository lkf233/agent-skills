# MCP工具服务接入说明

## 服务地址

- 路径：*`POST /mcp`*
- 协议：JSON-RPC 2.0
- 内容类型：`application/json`

## 已支持方法

- `initialize`
- `notifications/initialized`
- `ping`
- `tools/list`
- `tools/call`

## 已内置工具

- `echo_text`：原样返回输入文本
- `add_numbers`：计算两个数字之和
- `get_server_time`：返回当前服务端 UTC 时间

## 调用示例

### 1) 初始化

```bash
curl -X POST "http://localhost:8080/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "clientInfo": {"name": "demo-client", "version": "1.0.0"},
      "capabilities": {}
    }
  }'
```

### 2) 获取工具列表

```bash
curl -X POST "http://localhost:8080/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
```

### 3) 调用工具

```bash
curl -X POST "http://localhost:8080/mcp" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "add_numbers",
      "arguments": {"a": 12, "b": 30}
    }
  }'
```

## 返回规范

- 所有请求需包含 `jsonrpc: "2.0"`
- 通知请求（无 `id`）不返回响应体
- 工具调用结果遵循 MCP 工具返回结构：
  - `content`: 内容数组
  - `isError`: 是否为错误结果

