# 通用规则

1. 始终以简体中文输出计划和回复。
2. 每次回复都叫我老大。
3. 不要假设我清楚自己想要什么，动机或目标不清晰时，停下来讨论。
4. 我发出的指令目标清晰但方案不是最佳，直接告诉我并建议更好的办法。
5. 遇到问题追根因，不打补丁。每个决策都要能回答“为什么”。
6. 不要过度考虑边界，当前项目中不可能出现的边界情况不需要处理。

# 仓库指南

iboot 是基于 Java 17 / Spring Boot 3 的多模块后端项目，前端包含 Vue 3 应用。

## 项目说明与当前开发约定

- 当前 UI 后台二次开发项目：`iboot-ui/iboot-ui-admin-vben` 的 `iboot-ui/iboot-ui-admin-vben/apps/web-ele`。
- 数据库连接一律使用 dbx  MPC操作，连接名称为 `芋道-dev`，数据库为 `ruoyi-vue-pro-20260817`。
- **后端代码检索与静态校验优先走 idea-mcp**（IntelliJ IDEA MCP Server，本地 HTTP 端点 `http://127.0.0.1:64342/stream`，共 67 个工具），日常只用这三个：`search_symbol`——按标识符片段语义查类/方法/字段，跨模块定位 `com.iboot.pf.module.*` 下的符号比全文 grep 准，不受注释与字符串干扰；`analyze_calls`——出 Call Hierarchy 树，梳理 `XxxApi`/`XxxApiImpl` 门面调用链、改动前确认影响面都用它；`get_file_problems`——跑 IDEA inspections，改完 Java 不等 `mvn compile` 就能拿到类型错误与告警。前提：IDEA 必须开着，它是本地 HTTP 服务，IDEA 退出后工具直接超时失败（不是配置坏了）；端口 `64342` 是插件默认值，在 IDEA 的 MCP 设置里改过或开了多个实例会顺延变化，连不上先去设置页核实际端口；多窗口场景显式传 `projectPath`（本项目 `D:\iboot-pro\iboot`），否则可能作用到别的工程上。
- **idea-mcp 的数据库类工具（`execute_sql_query`、`preview_table_data`、`introspect_schema` 等 14 个）一律禁用。** 它们走的是 IDEA 里配的数据源，与 dbx 的 `芋道-dev` 是两套独立通道，混用必然出现“改了 IDEA 连接而 dbx 不知情”的割裂。数据库操作只有 dbx 一个出口。
- **idea-mcp 的重构能力只有 `rename_refactoring` 一个**：符号级重命名，会同步更新全项目引用（含 import、注入点、MyBatis XML 里可识别的 `type`/`resultType`），参数 `pathInProject` + `symbolName` + `newName`。它按名字定位而非行列坐标，同一文件内存在重载方法（`convert(A a)` / `convert(B b)`）时命中哪个不确定，这种情况回到 IDEA 手动 Shift+F6。Extract Method、Inline、Move Class、Change Signature、Safe Delete 一概没有，`execute_tool` 只是同一批工具的命令行转发器、不是隐藏后门，别指望从它绕出重构动作。改方法签名这类高频需求的替代流程：`analyze_calls` 拉全量调用点 → `apply_patch` 批量改 → `get_file_problems` 验证无编译错误——这是文本级改动而非重构，靠 inspections 兜底。
- 新增后台 CRUD 功能前，先阅读并遵循 [`dev-docs/通用CRUD开发流程.md`](dev-docs/通用CRUD开发流程.md)，其中定义了 DBX  MCP数据库操作、后端分层、web-ele 页面结构、菜单权限和验证流程。
- 写任何 web-ele 页面（含主子表 Tab、抽屉内的子表）前，先阅读并遵循 [`dev-docs/后台UI开发规范.md`](dev-docs/后台UI开发规范.md)，其中定义了页面文件结构、列表/表单/工具栏形态、控件选型、主子表 Tab 规范、子表后端接口对齐要求和交付自查清单。不可协商的红线：列表一律用 `useVbenVxeGrid`，禁止手写 `el-table`；新增/编辑一律用 `useVbenModal` + `useVbenForm` 独立弹窗，禁止行内输入框加“添加”按钮的即时追加式交互；子表必须和主表页面同构，具备新增、修改、删除、批量删除完整能力；后端接口不齐就补后端（`get`/`update`/`delete-list`），不许在前端把交互降级凑合。
- 复杂主子表（主表 + 多个子表/详情 Tab）一律采用“独立抽屉 + ColPage 左右分栏”形态，参照 `apps/web-ele/src/views/mdm/route/modules/route-config.vue` 与 `apps/web-ele/src/views/mdm/bom/modules/bom-line-drawer.vue`：列表页只留主表和一个“明细/配置”入口按钮，禁止在列表页下方内嵌 `ElTabs` 平铺子表；抽屉左侧放主表，右侧顶部用 `ElDescriptions` 展示当前选中主记录的可读摘要（编码/名称/关联对象/标识），下方 `ElTabs` 挂各子表，`ElTabPane` 设 `class="h-full"`。
- 分栏与表格的宽度/高度约束链必须完整，否则表格会按列宽总和把面板撑宽（工艺路线 1006px 事故的根因）：抽屉 `content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"`；内容区容器一律 `flex h-full min-h-0 flex-col overflow-hidden`；`el-tabs__content` 与 `el-tab-pane` flex 化并显式加 `min-width: 0; min-height: 0`——flex item 的 automatic minimum size 会按 vxe 表格列宽总和撑开面板，必须放行收缩；表格高度用 `height: 'auto'`，列总和超出容器宽时交给 vxe 自带横向滚动。禁止用 `:deep(.vxe-grid){width:100% !important}` 这类通配覆盖 vxe 内部布局的补丁“修”宽度。
- 新增后台 CRUD 模块时优先使用 infra 代码生成器生成代码；生成前在“字段管理”里人工维护外键列的“关联表名”和“显示字段”，由生成器产出 XML JOIN 查询和列表序号/关联展示列。
- 查询关联数据时，**同模块内**（例如 `mdm_*` 表之间）优先在 Mapper 层使用 SQL `JOIN` 一次查询并返回可读字段；不允许默认先查主表再在应用层查询外键数据拼接。
- **跨模块禁止跨库 `JOIN`**（例如 mdm 的 SQL 里 `JOIN system_users`）。跨模块取数一律在 Service 层调用对方模块的 `XxxApi` 门面批量补全（`AdminUserApi.getUserMap(ids)` 这类批量方法，一次 SQL，不允许 N+1），DO 和 Mapper 不得跨模块暴露。原因是跨库 JOIN 会把两个模块焊死在同一个数据库上，将来微服务拆分时无处下手。只有数据源不同、确实无法 JOIN，或关联数据已有专用聚合接口时才允许例外，并必须在代码评审中说明原因。注意补全逻辑要写在 Service 而非 Controller，否则 Excel 导出复用 Service 分页结果时会丢字段。
- 跨模块 `api` 门面的实现类（`XxxApiImpl`）**一律关闭数据权限**，且必须覆盖门面的每一个方法：统一在类上打 `@DataPermission(enable = false)`，不要逐方法加。原因是门面是给别的模块调的，调用方的登录用户与被调方的数据范围毫无关系——MES 领料要占 WMS 的库存，占的是仓库里客观存在的料，不是“当前登录人所在部门的料”，跨模块调用天生是系统身份而不是用户身份。机制上这样写是成立的：`@DataPermission` 的 `@Target` 含 `ElementType.TYPE`，`DataPermissionAnnotationAdvisor` 的切点是类级与方法级的并集，`DataPermissionAnnotationInterceptor.findAnnotation` 先取方法上的、没有再取类上的，所以类级注解兜住全部方法，个别确实要按用户过滤的方法在自己身上覆盖即可；注解经 `DataPermissionContextHolder` 栈生效，门面方法内整条下游 Service 调用链一并继承禁用状态。反面教材是门面层现状：mdm 逐方法加满、wms 只有 `InventoryApiImpl` 全加而 `OutOrderApiImpl`/`InOrderApiImpl`/`CountOrderApiImpl`/`TransferOrderApiImpl` 一个没有、mes 与 aps 只加一半、system 与 bpm 与 infra 基本没加，有人加有人不加、加的人也没加全，毫无规律可循。**现在漏了不报错，只是因为目前只有 system 模块给 `system_users` / `system_dept` 配了部门数据权限规则，业务表一张都没配，`DeptDataPermissionRule` 对未注册的表直接返回 null——这是“暂时无害”，不是“不需要加”。** 哪天真给业务表配了规则（多仓库按部门隔离这类需求），漏掉的那一半会集中爆，而且爆在跨模块调用的深处，是最难定位的一类故障。
- 表单中的外键字段（`xxxId`）一律用选择器让用户从主数据中选取，禁止用 `InputNumber`/`Input` 把主键编号暴露给用户手填：平铺主数据用 `RelationSelect`（关联配置统一沉淀在 `views/mdm/relation-config.ts` 复用），树形主数据用 `ApiTreeSelect`。列表展示关联数据时显示编码/名称等可读字段，不显示原始 ID；代码/枚举值列必须用字典或 formatter 映射成中文，不允许直接展示 `CRITICAL` 这类原始值。
- 业务记录需要关联文档、图片、视频、图纸等附件时，先阅读并遵循 [`dev-docs/文档附件开发规范.md`](dev-docs/文档附件开发规范.md)。其中定义了附件子表结构模板、`file_type` 后端判定、去签名地址落库与读取重签名、附件全量替换与级联清理、前端上传字段组件与 `FileViewer` 预览方式。要点：业务上会出现第二个文件的一律建独立附件子表，不要在主表内联 `file_*` 列；文件名取自 `File.name`，不从 URL 反解。
- 改动 APS 求解相关代码（约束、求解域、排程能力）前，先阅读并遵循 [`dev-docs/APS求解器开发规范.md`](dev-docs/APS求解器开发规范.md)。其中定义了 Timefold 版本与许可红线、投影输入约定、求解域建模、约束与权重、求解编排与快照、测试要求。不可协商的红线：Timefold 锁定 1.34.0（2.x 要求 Java 21 且 score analysis 收费），严禁出现 `moveThreadCount`、分区搜索、近邻选择、多阶段移动、约束剖析这些 Community 版会抛 `EnterpriseLicenseException` 的配置项；求解输入只读四张投影表，不许回查主数据；时间推导用声明式影子变量（`@ShadowVariable(supplierName)` + `@ShadowSources`），不要手写 `ListVariableListener`；约束名只在 `ApsConstraintNames` 里定义，它是策略权重与命中快照的对齐锚点。参考项目 `业务建模/参考项目/jvs-aps` 用的是 OptaPlanner 9.44，写法不可直接照抄。
- 改动 `iboot-module-bpm`，或给任何业务模块接入审批流之前，先阅读并遵循 [`dev-docs/BPM模块详细设计与接入规范.md`](dev-docs/BPM模块详细设计与接入规范.md)。其中定义了 Flowable 8 版本事实、`ACT_*` 与 `bpm_*` 两套表的分工、`model_type`/`form_type` 两个建模维度、实例与任务状态机、`api` 门面与状态事件、业务接入四步动作、审批人策略、触发器与 HTTP 通知、前端业务表单挂载、交付自查清单，并附架构图、部署链路图、状态机图和端到端时序图。已落地的生产参照是 HRM 员工请假（`HrmAttendanceLeaveServiceImpl` + `HrmAttendanceLeaveStatusListener`），模块内置的 OA 请假只是流程配置演示，缺幂等保护，不要照抄。不可协商的红线：业务模块只能走 `com.iboot.pf.module.bpm.api` 三个出口（`BpmProcessInstanceApi`、`BpmProcessTaskApi`、`BpmProcessInstanceStatusEventListener`），禁止注入 bpm 内部 Service，bpm 也不许反向依赖任何业务模块；业务数据一律 `form_type = CUSTOM` 存在业务自己的表，禁止用 `NORMAL` 埋进流程变量（不可逆，只能重做）；业务表固定加 `approval_status`（复用 `BpmProcessInstanceStatusEnum`，不自建枚举）/ `process_instance_id`（唯一索引）/ `approval_time` / `approval_reason`，`businessKey` 传业务主键、发起流程与落单据同事务；终态回写必须三层幂等（终态判定 + 实例编号与状态校验 + 带 `WHERE approval_status = RUNNING` 的条件 UPDATE），且 `onEvent` 里不许抛异常（它跑在 Flowable 事务内，抛了会回滚整个流程状态变更）；业务详情 `get` 接口的权限要放行 `bpm:process-instance:query` 和 `bpm:task:query`，否则审批人打开详情 403；`ACT_*` 表结构由引擎自建，任何情况下不许手工 DDL。

## 项目结构与模块组织

- `iboot-dependencies` 管理所有 Maven 模块的依赖版本。
- `iboot-framework` 包含共享的 Spring Boot Starter 和框架扩展。
- `iboot-server` 是 Spring Boot 启动入口（`IbootServerApplication`），也是可运行的应用。
- `iboot-module-*` 是业务模块，每个模块都包含 `src/main/java` 和 `src/test/java`。
- `iboot-ui/iboot-ui-admin-vben` 是管理后台使用Element-UI-plus。
- `sql/` 存放数据库脚本；`script/` 包含 Docker、Jenkins 和 Shell 辅助脚本。

## 构建、测试与开发命令

- 数据库操作统一使用 dbx 操作，连接名是 芋道-dev，数据库是 ruoyi-vue-pro-20260817。不用记录DDL及DML。

- `mvn clean package -DskipTests`：构建所有后端模块。
- `mvn test`：运行全项目 JUnit 5 测试。
- `mvn -pl <module> -am test`：构建并测试指定模块及其依赖。
- 配置 MySQL 和 Redis 后，从 IDE 运行 `com.iboot.pf.server.IbootServerApplication`。
- 在 `iboot-ui/iboot-ui-admin-vben` 中，使用 `pnpm install`、`pnpm dev`、`pnpm build:prod` 和 `pnpm lint`。

## 编码风格与命名约定

- 后端遵循《阿里巴巴 Java 开发手册》；适用场景使用 Lombok 和 MapStruct。
- 业务代码包名格式为 `com.iboot.pf.module.<module>.<feature>`。
- 类名使用 `XxxController`、`XxxService`、`XxxServiceImpl`，测试类使用 `XxxTest`。
- 前端使用 Vue 3、TypeScript、ESLint、Prettier 和 Stylelint，缩进 2 个空格、使用单引号、加分号。
- 前端页面结构、组件选型与交互形态见 [`dev-docs/后台UI开发规范.md`](dev-docs/后台UI开发规范.md)，不要凭个人习惯另起一套。

## 测试规范

- 后端测试使用 JUnit 5 和 Mockito；数据库或服务测试继承 `BaseDbUnitTest`。
- 测试代码放在 `src/test/java` 下，包结构与生产代码保持一致。
- 提交前运行 `mvn test` 或 `mvn -pl <module> -am test`。

## 提交规范

- 使用带模块作用域的 Conventional Commits，例如 `feat(system): add user export`、`fix(bpm): correct process import`。
- **提交信息一律用中文书写**，type 和 scope 仍用英文小写：

  ```
  feat(mdm): 班组列表新增班组长筛选
  fix(mdm): 修正人员技能失效日期早于生效日期未拦截的问题
  refactor(mdm): 消除跨模块 system_users 的 JOIN，改走 AdminUserApi 门面
  docs: 补充跨模块取数规范
  ```

  中文 subject 不会被 commitlint 拦截：`internal/lint-configs/commitlint-config/index.mjs` 里 `subject-case` 已置为 `[0]`（关闭），且没有任何规则限制 subject 的字符集，唯一约束是 `header-max-length: 108` 个字符（`type-enum` 限定的 13 个 type 也仍用英文）。

- 注意校验范围：commitlint 由 `iboot-ui/iboot-ui-admin-vben/lefthook.yml` 的 `commit-msg` 钩子触发，**只在前端子项目内提交时生效**，后端 Java 代码的提交不经过它，但仍需遵守同一套格式。
- 仓库根目录的 `lefthook.yml` 是 `pnpm install` 误生成的示例模板，已被 `.gitignore` 忽略，不要提交；生效的钩子配置只有前端子项目那一份。
