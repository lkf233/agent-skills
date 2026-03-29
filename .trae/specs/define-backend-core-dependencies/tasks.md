# Tasks

- [x] Task 1: 建立后端依赖分层清单
  - [x] SubTask 1.1: 列出框架层依赖（Spring Boot、Web、Validation、Security、Actuator）
  - [x] SubTask 1.2: 列出数据层依赖（MyBatis、PostgreSQL、pgvector 方案）
  - [x] SubTask 1.3: 列出 AI 层依赖（LangChain4j 核心、模型接入、记忆与工具调用）
  - [x] SubTask 1.4: 列出工程层依赖（日志、测试、构建、代码质量）

- [x] Task 2: 定义依赖职责边界与禁用规则
  - [x] SubTask 2.1: 为每个核心依赖补充“使用场景/禁止场景”
  - [x] SubTask 2.2: 定义同类能力唯一方案（如 ORM、HTTP 客户端、JSON 库）
  - [x] SubTask 2.3: 定义替代策略与技术债标记方式

- [x] Task 3: 制定版本治理与升级策略
  - [x] SubTask 3.1: 定义 BOM/父依赖统一版本策略
  - [x] SubTask 3.2: 定义升级窗口、回滚策略与兼容验证清单
  - [x] SubTask 3.3: 定义“最小可运行依赖集”与“扩展依赖集”边界

- [x] Task 4: 建立安全与合规校验基线
  - [x] SubTask 4.1: 定义漏洞扫描与许可证检查要求
  - [x] SubTask 4.2: 定义高危依赖处置优先级和时限
  - [x] SubTask 4.3: 定义例外审批和风险记录模板

- [x] Task 5: 对齐项目能力并输出落地清单
  - [x] SubTask 5.1: 映射认证、会话、记忆、知识库、MCP 到依赖项
  - [x] SubTask 5.2: 给出后端第一阶段必选依赖与可选依赖
  - [x] SubTask 5.3: 形成 `pom.xml` 变更草案清单（不实施代码）

# Task Dependencies
- Task 2 depends on Task 1
- Task 3 depends on Task 1
- Task 4 depends on Task 1
- Task 5 depends on Task 2, Task 3, Task 4
