# 通用 CRUD 开发流程

本文用于 iboot 新增后台管理功能。目标是让一次功能开发的数据库、后端、web-ele 前端、菜单权限和验证保持同一套结构，不再重复摸索项目约定。

## 1. 开始前确认

1. 明确功能所属模块、业务实体、字段、唯一约束、状态值、是否需要租户隔离和业务调用 API。
2. 先查找同模块相似功能，优先沿用其 Controller、Service、Mapper、Vue 页面和权限命名方式。
3. 读取将要修改的文件最新内容；不要覆盖用户已有改动。
4. 本项目数据库只使用 DBX：连接名为 `芋道-dev`，数据库为 `ruoyi-vue-pro-20260817`。

## 2. 数据库操作

### 2.1 原则

- 使用 DBX 直接执行 DDL、DML 和查询，不向 `sql/` 目录新增或记录本次生产数据库脚本。
- 建表前先用 DBX 查询现有表、字段和索引，避免字段重复、命名冲突或错误外键方向。
- 建表后立即用 DBX 描述表结构，并查询 `information_schema.statistics` 确认主键和唯一索引。
- 菜单、按钮权限和角色授权也是数据库变更，统一通过 DBX 写入并核验。

### 2.2 表设计清单

1. 表名使用 `<模块>_<业务>`，字段使用下划线命名。
2. 业务主键为 `id`；需要逻辑删除时，唯一索引要包含 `deleted`。
3. 普通后台实体沿用审计字段：`creator`、`create_time`、`updater`、`update_time`、`deleted`。
4. 高并发的可变业务状态拆表保存，例如编码规则与流水状态分开；用数据库唯一索引保证业务不变量。
5. 明确哪些字段是查询条件、唯一条件和排序字段，并为实际访问路径建索引。

### 2.3 菜单与权限

1. 在 `system_menu` 创建菜单，`component` 必须与 web-ele 页面路径一致，例如 `infra/codeRule/index` 对应 `src/views/infra/codeRule/index.vue`。
2. 创建查询、新增、修改、删除按钮权限，命名为 `<模块>:<业务>:query|create|update|delete`。
3. 为当前需要使用该功能的角色写入菜单授权。
4. 没有页面时菜单保持隐藏；页面完成后通过 DBX 设置为可见，并回查菜单路径、组件名、可见状态和权限。

## 3. 后端开发

### 3.1 目录与职责

以 infra 的 `CodeRule` 为例，业务名替换为实际功能名：

```text
iboot-module-<module>/src/main/java/com/iboot/pf/module/<module>/
├── api/<feature>/                 # 跨模块业务调用接口和实现
├── controller/admin/<feature>/    # 管理端 Controller 与 VO
│   └── vo/
├── convert/<feature>/             # MapStruct 转换器
├── dal/dataobject/<feature>/      # DO，与数据表对应
├── dal/mysql/<feature>/           # Mapper 与必要的原子 SQL
├── enums/<feature>/               # 枚举；错误码放 ErrorCodeConstants
└── service/<feature>/             # Service 接口与实现
```

- `DO`：仅描述表字段、MyBatis-Plus 注解、必要的枚举说明。
- `Mapper`：单表查询使用 `BaseMapperX` 默认方法；需要锁、原子更新、数据库方言能力时写清晰的专用 SQL。
- `SaveReqVO`：创建和修改共用，使用 Jakarta Validation、`@InEnum`、长度和数值范围约束。
- `PageReqVO`：继承项目现有分页请求基类，放置筛选条件。
- `RespVO`：仅暴露管理端实际需要字段。
- `Convert`：使用 MapStruct，禁止 Controller 手工逐字段复制。
- `Service`：负责存在性、唯一性、业务状态和跨字段校验；Controller 不写业务判断。
- `Controller`：接口固定为 `/create`、`/update`、`/delete`、`/get`、`/page`，使用 `@PreAuthorize` 绑定按钮权限。
- `Api`：只有其他业务模块真正需要调用时才增加，暴露最小业务方法，不暴露 Mapper 或 DO。

### 3.1.1 代码生成器直出代码的必改项

生成器省事，但直出的代码有几处一定要改，否则线上就是坑：

| 直出问题 | 必须改成 |
| --- | --- |
| `@Schema(description = "状态：DRAFT、DONE")` 把枚举串塞进字段说明 | 描述只写字段名，取值靠字典与枚举约束 |
| `example = "你猜"` / `example = "19938"` 这种随机示例 | 中文示例删掉，编号类示例统一 `1`，枚举类示例写真实枚举码 |
| `SaveReqVO` 只有 `@NotNull`，没有取值与长度约束 | 枚举字段加 `@InEnum`，字符串加 `@Size`，数量加 `@DecimalMin` |
| `RespVO` 里枚举字段直接导出原始值 | 加 `@ExcelProperty(converter = DictConvert.class)` 与 `@DictFormat(字典常量)` |
| Mapper XML 的 `create_time = #{req.createTime}` | 改成 `BETWEEN #{req.createTime[0]} AND #{req.createTime[1]}` |
| 每个字段都生成 `=` 精确匹配 | 名称类字段用 `LIKE`，`remark` 这类不做查询条件的直接删掉 |
| 表的唯一索引没有应用层校验 | Mapper 加 `selectByXxx`，Service 的 create/update 都校验，update 传 `excludeId` |
| 业务编码要用户手填 | 接入 infra 编码规则：为空时按规则分配，常量取值必须与 `infra_code_rule.rule_code` 完全一致 |
| 主表删除不检查下级占用 | 有下级实体（仓库-库区-库位这类）时先校验占用，单删与批量删都要校验 |
| 单测只有 `randomPojo` 加 `assertPojoEquals` | 改成覆盖编码分配、唯一性、外键校验、级联删除的业务用例；带 `@InEnum` 的字段 podam 造不出值，随机断言必然错位 |
| 明细行的进度数量（已完成/已过账等）出现在表单里 | 创建时归零，由业务动作推进 |
| 子表 Controller 直接收发 DO | 补 `XxxLineSaveReqVO` / `XxxLineRespVO` / `XxxLinePageReqVO` |

### 3.2 实施顺序

1. 新增错误码和领域枚举，错误码使用该模块未占用的编号段。
2. 新增 DO 与 Mapper，Mapper 查询需自动过滤逻辑删除数据。
3. 新增 VO、Convert、Service 接口及实现。
4. 在 Service 中先校验存在性、唯一性和规则合法性，再执行写操作。
5. 有并发风险时由数据库负责最终正确性：唯一索引、行锁或原子更新必须和业务事务一起设计，不能依赖内存计数器。
6. 新增 Controller，并逐个匹配前端权限。
7. 确有跨模块需求时新增 `Api` 和 `ApiImpl`。

### 3.3 在线更新与并发场景

- 规则类配置不能长期缓存。每次业务调用从数据库读取规则，在线修改提交后下一次调用即可生效。
- 更新配置和分配业务状态存在先后关系时，使用同一业务行锁或等价的事务边界定义顺序。
- 原子分配值优先使用 MySQL 的唯一键 upsert 或单条原子 update；应用层的“先查再写”不能单独保证并发唯一性。
- 业务表上仍应有最终的唯一约束，生成服务只是第一道保证。

## 4. web-ele 前端开发

> 本节只列 CRUD 流程中的前端步骤。页面结构、列表/表单/工具栏形态、控件选型、主子表 Tab 规范和交付自查清单以 [`后台UI开发规范.md`](后台UI开发规范.md) 为准，与本节冲突时以该文为准。

### 4.1 固定文件结构

页面使用 Vue 3 `script setup`、TypeScript，按现有页面拆为四个文件：

```text
iboot-ui/iboot-ui-admin-vben/apps/web-ele/src/
├── api/<module>/<feature>/index.ts
└── views/<module>/<feature>/
    ├── data.ts
    ├── index.vue
    └── modules/form.vue
```

### 4.2 API 层

`api/<module>/<feature>/index.ts`：

1. 使用 `requestClient`，定义 `namespace` 和实体 TypeScript 接口。
2. 分页接口返回 `PageResult<T>`，请求参数使用 `PageParam`。
3. CRUD 分别对应后端 `/page`、`/get`、`/create`、`/update`、`/delete`。
4. URL 参数用模板字符串；路径参数可能包含特殊字符时使用 `encodeURIComponent`。

### 4.3 data.ts

1. `useFormSchema()`：使用 `VbenFormSchema[]` 描述创建、编辑表单。
2. `useGridFormSchema()`：只放真实的列表筛选条件。
3. `useGridColumns()`：使用 `VxeTableGridOptions['columns']` 定义列表字段和操作列。
4. 状态、枚举优先复用项目字典 `DICT_TYPE`；没有字典时，在本文件集中定义静态 `options`，不要在模板重复硬编码。
5. 布尔值用 `Switch`，数值用 `InputNumber` 并限定项目允许范围，选项集合用 `Select` 或 `RadioGroup`。

### 4.4 index.vue

1. 使用 `Page`、`useVbenVxeGrid`、`TableAction` 和 `useVbenModal`，参考 `views/infra/config/index.vue`。
2. 查询方法把 `pageNo`、`pageSize` 与表单值合并传给 API。
3. 创建、编辑操作通过 `formModalApi.setData(...).open()` 打开表单；表单成功后调用 `gridApi.query()` 刷新。
4. 每个操作都设置对应 `auth` 权限；删除通过 `popConfirm` 二次确认。
5. 预览、详情等不改变数据的操作放在行操作中，调用后端专用接口，不调用生成或写入接口代替预览。

### 4.5 modules/form.vue

1. 使用 `useVbenForm` 和 `useVbenModal`，表单提交前调用 `formApi.validate()`。
2. 根据是否有 `id` 选择 create 或 update；编辑时调用 get 接口获得完整数据后再 `setValues`。
3. 弹窗关闭和新建打开时调用 `resetForm()`，避免上一次数据残留。
4. 提交期间用 `modalApi.lock()` / `unlock()` 防止重复提交。

## 5. 验证与交付

### 5.1 后端

1. 新增 `BaseDbUnitTest` 测试，覆盖正常 CRUD、核心业务规则、异常规则、并发或唯一性风险。
2. 编译优先使用 IDEA MCP 的构建诊断。
3. 多模块测试使用：

```powershell
mvn -pl <module> -am test
```

不要只运行 `mvn -pl <module> test`：本机未安装同版本 SNAPSHOT 依赖时，它会尝试从远程仓库下载本项目内部模块并失败。

### 5.2 前端

1. 执行：

```powershell
pnpm typecheck
pnpm build
```

2. 检查菜单组件路径和 Vue 文件路径大小写完全一致。
3. 启动已有 web-ele 开发服务或执行 `pnpm dev` 后，验证列表、筛选、创建、编辑、删除、权限隐藏和专用操作。

### 5.3 数据库与交付检查

1. 用 DBX 回查表、字段、索引、菜单、权限和角色授权。
2. 不向生产 SQL 脚本目录添加本次 DDL/DML。
3. 交付说明列出：功能入口、API、权限、数据库对象、验证命令和未覆盖风险。
