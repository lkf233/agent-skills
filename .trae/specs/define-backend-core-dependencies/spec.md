# 后端核心依赖梳理 Spec

## Why
当前项目准备先进行后端开发，但尚未形成统一、可执行的依赖基线。需要明确 Spring Boot、LangChain4j 及配套组件的职责边界、选型理由与版本策略，避免后续模块重复建设和技术分叉。

## What Changes
- 新增后端核心依赖清单规范，覆盖框架层、AI 层、数据层、安全层、可观测层与工程层。
- 明确每个依赖的使用场景、禁止场景与替代策略。
- 新增依赖版本管理规则（BOM、锁版本、升级窗口、兼容验证）。
- 新增“最小可运行依赖集”与“按能力扩展依赖集”两级基线。
- 新增依赖安全治理流程（漏洞扫描、许可证检查、升级优先级）。
- **BREAKING**：后续后端实现必须遵循该依赖基线，不允许自由引入同类框架。

## Impact
- Affected specs: 用户认证、会话管理、记忆管理、知识库、MCP 工具调用、规划执行、可观测能力。
- Affected code: `backend/pom.xml`、`backend/pom-parent.xml`（如采用）、`backend/src/main/resources/application*.yml`、数据库迁移脚本、CI 构建流程。

## ADDED Requirements

### Requirement: 后端核心依赖分层清单
系统 SHALL 提供一份分层依赖清单，并明确各依赖在项目中的唯一职责。

#### Scenario: 输出框架层依赖
- **WHEN** 团队查看后端依赖规范
- **THEN** 可看到 Spring Boot Web、Validation、Security、Actuator、MyBatis、PostgreSQL 驱动等基础依赖及职责说明

#### Scenario: 输出 AI 与 Agent 层依赖
- **WHEN** 团队查看 AI 能力依赖
- **THEN** 可看到 LangChain4j 核心、模型接入、向量存储接入、工具调用与记忆相关组件的用途边界

### Requirement: 依赖版本治理规则
系统 SHALL 定义统一的版本治理规则，避免依赖冲突和不可控升级。

#### Scenario: 版本统一
- **WHEN** 新增后端模块
- **THEN** 必须继承统一 BOM/父依赖版本，不得在子模块随意覆盖核心依赖版本

#### Scenario: 升级验证
- **WHEN** 升级 Spring Boot 或 LangChain4j
- **THEN** 必须执行兼容性验证（启动、核心接口、数据库连接、SSE 对话、MCP 调用）

### Requirement: 最小可运行依赖集
系统 SHALL 定义后端第一阶段开发所需的最小依赖集合。

#### Scenario: 首批后端接口开发
- **WHEN** 开发登录注册、会话管理、知识库入库与基础工具调用
- **THEN** 仅引入最小依赖集即可完成，不依赖前端工程

### Requirement: 扩展依赖引入门禁
系统 SHALL 对扩展依赖（如缓存、消息队列、异步任务编排）设置准入规则。

#### Scenario: 引入新中间件
- **WHEN** 团队拟引入 Redis、MQ 或工作流引擎
- **THEN** 必须给出业务必要性、替代方案比较、运维影响和回滚方案

### Requirement: 依赖安全与合规
系统 SHALL 建立依赖安全扫描和合规检查要求。

#### Scenario: 依赖漏洞处理
- **WHEN** 扫描发现高危漏洞依赖
- **THEN** 必须在约定周期内升级或提供隔离缓解措施，并记录风险接受结论

## MODIFIED Requirements

### Requirement: MCP 工具依赖策略
原仅强调内置 MCP 适配。现修改为：后端依赖策略需同时支持“内置工具调用能力”与“用户配置工具调用能力”，并在依赖清单中明确协议支持、HTTP 客户端、序列化与校验组件的选型统一。

## REMOVED Requirements

### Requirement: 无版本治理的自由引入依赖
**Reason**: 自由引入会导致冲突、重复能力和维护成本上升。  
**Migration**: 已引入但不在清单内的依赖需要逐项评估，保留/替换/移除并形成迁移记录。
