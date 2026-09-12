# 后台 UI 开发规范

本文约束 `iboot-ui/iboot-ui-admin-vben/apps/web-ele` 下所有后台页面的结构、组件选型和交互形态。目标是任何一个新页面、任何一个主子表 Tab，看起来和用起来都跟已有页面一致，不出现“这块是另一个人临时手写的”。

已落地的参考实现：

- 标准单表页面：`views/mdm/tool/`（`index.vue` + `data.ts` + `modules/form.vue`）
- 主子表抽屉 + 多 Tab 子表：`views/mdm/route/modules/route-config.vue`
- 主子表页内 Tab 子表：`views/mdm/bom/`（`index.vue` + `modules/bom-line-list.vue`）

新功能一律沿用，不要各自发明。

## 1. 最高优先级红线

这几条是返工成本最高的，违反了基本等于重写：

1. **任何数据列表都必须用 `useVbenVxeGrid`。** 禁止手写 `el-table` 渲染业务列表。
2. **任何新增/编辑都必须是 `useVbenModal` + `useVbenForm` 的独立弹窗表单。** 禁止在页面里放一排行内输入框加“添加”按钮的即时追加式交互。
3. **子表功能必须和主表页面同构。** 子表也要有工具栏“新增/批量删除”与行操作“修改/删除”，不能因为“它只是个附属面板”就降级。
4. **后端接口不齐就补后端，不要在前端凑合交互。** 子表只有 create/delete/page 时，正确做法是补 get/update/delete-list，而不是把 UI 退化成“选一个加一个、只能删不能改”。
5. **表单里的外键一律用选择器**，列表里的关联字段一律显示可读字段，枚举一律映射中文。详见第 5、6 节。
6. **页面布局一律“列表页 + 抽屉”，禁止页面级“左列表 + 右详情”分栏。** 两级结构（列表 + 选中后的详情/作业）必须点击行打开抽屉承载右侧内容，详见第 6.0 节。

> 反面案例：工艺路线的“工装”Tab 曾用 `el-form inline` 加 `el-table` 手写，只能一次选一个工装点“添加工装”，加完不能改，也没有批量删除，因为后端当时只有 create/delete/page。这是规范缺失导致的，不是需求特殊。

## 2. 文件结构

### 2.1 单表页面

```text
apps/web-ele/src/
├── api/<module>/<feature>/index.ts        # namespace + 接口
└── views/<module>/<feature>/
    ├── data.ts                            # useFormSchema / useGridFormSchema / useGridColumns
    ├── index.vue                          # Page + Grid + TableAction + FormModal
    └── modules/form.vue                   # 新增/编辑弹窗
```

### 2.2 主子表页面

子表的 schema 和列放在主表的 `data.ts` 里（用 `use<Child>FormSchema` / `use<Child>GridColumns` 命名），字段较多时单独拆一个 `<feature>-<child>-data.ts`：

```text
views/<module>/<feature>/
├── data.ts
├── <feature>-<child>-data.ts              # 子表字段多时才拆
├── index.vue
└── modules/
    ├── form.vue                           # 主表表单
    ├── <feature>-<child>-form.vue         # 子表表单弹窗
    └── <feature>-<child>-list.vue         # 子表列表（页内 Tab 形态时才需要独立文件）
```

命名统一用 kebab-case，前缀与业务对应，例如 `route-op-tool-form.vue`、`route-op-tool-data.ts`。

**不允许出现 `xxx-panel.vue` 这种自创形态。** 子表要么是 `-list.vue`（独立列表组件），要么直接在父组件里用 `useVbenVxeGrid` 定义一个 Grid，两者都必须配 `-form.vue`。

## 3. 列表规范

### 3.1 Grid 配置

```ts
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },   // 有筛选条件时才加
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',                               // 主列表用 auto；子表用固定值如 280px
    keepSource: true,
    proxyConfig: { ajax: { query: async ({ page }, formValues) => { /* ... */ } } },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<Xxx>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
```

- 主列表 `height: 'auto'`，外层用 `<Page auto-content-height>`。
- 子表 Grid 高度用固定值（现有约定 `280px`，页内 Tab 大列表可用 `600px`），并 `pagerConfig: { enabled: false }` 配合后端一次取足（`pageSize: 100`）；子表数据量确实会超百条时才开分页。
- 子表工具栏保留 `custom` / `refresh` / `zoom`，关闭 `search`。

### 3.2 列定义

`useGridColumns()` 返回 `VxeTableGridOptions['columns']`，固定顺序：

1. `{ type: 'checkbox', width: 40 }`，支持批量删除时必须有
2. `{ type: 'seq', title: '序号', width: 60 }`
3. 业务列
4. `{ field: 'createTime', title: '创建时间', minWidth: 160, formatter: 'formatDateTime' }`
5. `{ title: '操作', width: 150~280, fixed: 'right', slots: { default: 'actions' } }`

列内容渲染硬约束：

| 数据类型 | 渲染方式 |
| --- | --- |
| 字典/枚举值 | `cellRender: { name: 'CellDict', props: { type: DICT_TYPE.XXX } }`，无 `DICT_TYPE` 常量时传字典编码字符串 |
| 布尔值 | `CellDict` 配 `DICT_TYPE.INFRA_BOOLEAN_STRING` |
| 关联外键 | 显示后端 JOIN 出的 `xxxCode` / `xxxName`，**不显示 `xxxId`** |
| 时间 | `formatter: 'formatDateTime'` |

禁止直接把 `CRITICAL`、`0`、`true` 这类原始值渲染到单元格里。

### 3.3 工具栏与行操作

工具栏统一 `#toolbar-tools` 插槽配 `TableAction`，动作顺序为新增、导出、批量删除；批量删除用 `disabled: isEmpty(checkedIds)` 控制。

行操作统一 `#actions` 插槽配 `TableAction`，`link: true`：

- 顺序固定：业务动作（配置、复制、上移、下移、预览等）、修改、删除。
- 删除必须 `popConfirm` 二次确认，标题里带上业务可读标识（编码/名称），不要只给 `id`。
- 批量删除用 `confirm(...)`，级联影响要在确认文案里说明，例如“并级联删除 N 条用料”。
- 每个 action 都要有 `auth`。
- 只读状态（如已发布的工艺路线）通过 `disabled: !isEditable` 统一压制写操作，不要靠隐藏按钮。

## 4. 表单弹窗规范

```ts
const formLayout = getMdmFormLayout(1);            // 1 / 2 / 3 列，来自 views/mdm/form-layout.ts

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 80,                                // 由最长的中文标签决定，常用 80/110
  },
  wrapperClass: formLayout.wrapperClass,
  layout: 'horizontal',
  schema: useXxxFormSchema(),                      // schema 放 data.ts，不内联在 vue 文件
  showDefaultActions: false,
});
```

硬要求：

1. 弹窗宽度用 `getMdmFormLayout(columns)` 返回的 `modalClass`，不要自己写死 `w-[860px]`。
2. 打开时先 `resetForm()`，关闭时清 `formData`，避免上次数据残留。
3. 编辑必须先调 `get` 接口拿完整数据再 `setValues`，不能直接用列表行数据（列表 RespVO 通常是裁剪过的）。
4. 提交期间 `modalApi.lock()` / `unlock()` 防重复提交；提交成功 `emit('success')`，由父级刷新表格。
5. 标题用 `$t('ui.actionTitle.create'|'edit', ['业务名'])`，或直接“新增X”/“编辑X”，同一模块内保持一致。
6. 子表表单的父键（`routeOpId`、`bomId` 等）由父组件 `setData({ 父键 })` 传入，提交时从 `formData` 回填，**不作为表单可见字段**。
7. **嵌套在弹窗内的子弹窗（明细行表单等）禁止手写 `z-index`，一律走 `z-popup` 基准。** `useVbenModal` 的内容与遮罩默认层级都是 `z-popup = var(--popup-z-index) = 2000`（`packages/@core/base/design/src/design-tokens/default.css`）；同为 2000 时子弹窗由 `DialogPortal` 后挂载到 `body` 末尾，DOM 靠后自然叠在父弹窗之上，不需要额外提层。手写一个低于 2000 的值（历史上是 `:z-index="1200"`）会把子弹窗压进父遮罩之下：点“添加明细”界面纹丝不动，而鼠标命中的是父遮罩，`closeOnClickModal` 默认 true，随手一点连整张单据都被关掉——现象是“按钮没反应”，根因在层级，不在事件。要写也只能写高于 2000 的值，但那是在给下次改基准埋雷，正确做法是不写。

## 5. 表单控件选型

| 字段类型 | 组件 | 说明 |
| --- | --- | --- |
| 外键（平铺主数据） | `markRaw(RelationSelect)` 配 `componentProps: XXX_RELATION_PROPS` | 关联配置统一沉淀在 `views/mdm/relation-config.ts` 复用，不在页面里重复写 columns/queryFields |
| 外键（树形主数据） | `ApiTreeSelect` | 物料分类、部门、菜单等 |
| 字典/固定枚举 | `Select` / `RadioGroup` 配 `getDictOptions(...)` | 选项不超过 3 个且互斥用 `RadioGroup`，其余用 `Select` |
| 布尔 | `RadioGroup` 配 `getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean')` | 表单里优先 `RadioGroup`（是/否语义明确），空间紧张时可用 `Switch` |
| 数值 | `InputNumber` | 必须给 `min`/`precision`，`class: '!w-full'`，`controlsPosition: 'right'` |
| 文本 | `Input` / `Textarea` | 给 `maxLength` 和 `placeholder` |
| 附件 | 见 [`文档附件开发规范.md`](文档附件开发规范.md) | 不要自己写 upload |

**绝对禁止**：用 `InputNumber` / `Input` 承载 `xxxId` 让用户手填主键编号。

选项来源优先级：项目字典 `DICT_TYPE`、模块字典编码字符串、`views/mdm/constants.ts` 里的静态 options。不允许在模板或 schema 里硬编码中文选项数组。

## 6. 主子表与 Tab 形态

### 6.0 页面布局：列表页 + 抽屉，禁止页面级左右分栏

页面级“左列表 + 右详情”的 `ColPage` 分栏形态已废弃，**存量 7 个作业页是反面教材**（`mes/workbench/inspect`、`mes/workbench/disposition`、`mes/workbench/report`、`mes/workbench/issue`、`wms/workbench/receive`、`wms/workbench/pick`、`wms/workbench/count`）。改造方式：列表占满整页，点击行打开 `useVbenDrawer`（`class="w-full lg:w-4/5"`），把原右侧的 `ElDescriptions` 摘要 + 作业表格 + 动作按钮整体搬进抽屉，`selectedXxx` 选中逻辑与接口调用不变，只挪承载容器。

规则与理由：

- **判定标准：只有两级（列表 → 单条记录的详情/作业）的页面，一律列表 + 抽屉。** 抽屉是本规范已验证的形态（工艺路线、BOM 配置都在用），列表全宽可展示更多列，抽屉内空间与分栏右侧相当。
- **例外一：左侧是过滤树而非记录列表的不算两级。** 如派工页左侧的“车间/工作中心”树是筛选维度，右侧才是列表，这类“树 + 工作区”形态保留。
- **例外二：抽屉内部的 `ColPage` 左右分栏照常使用。** `route-config.vue`、`bom-line-drawer.vue` 是参照实现，规范约束的是页面级布局，不是抽屉内布局。
- 选中态不再依赖左侧列表常驻，抽屉打开即“已选中”，关闭即回到纯列表，`cell-click` 联动逻辑随之取消。

### 6.1 两种形态

| 场景 | 形态 |
| --- | --- |
| 子表是主记录的配置明细，编辑频繁、层级深（工序对用料/ESOP/工装） | 主列表行操作“配置”打开 `useVbenDrawer`（`class="w-full lg:w-4/5"`），抽屉内上方主子表 Grid，下方 `ElTabs` 挂各子表 Grid |
| 子表结构简单、和主记录一对一浏览（BOM 对 BOM 行） | 主页面内 `ElTabs`，每个 Tab 一个独立 `-list.vue` 组件，通过 props 接父键并 `watch` 刷新 |

### 6.2 Tab 内子表要求

每个 Tab 的子表都必须具备完整能力，不许有“残疾 Tab”：

- 独立 `useVbenVxeGrid`，`table-title` 带上当前父记录标识，例如“工序工装列表 - OP010 定子绕线”。
- 工具栏“新增 + 批量删除”，行操作“修改 + 删除”。
- 未选中父记录时新增按钮 `disabled`，点击给 `ElMessage.warning('请先选择一道工序')`。
- 切换父记录、父记录重新加载时，**所有** Tab 的 Grid 都要 `query()`，勾选态要清空。集中在一个 `syncGridData()` 里做，不要漏 Tab。

### 6.3 级联删除

删除父记录时，所有子表都要在后端一个事务里级联清理。新增子表时**必须回头检查父记录的删除路径**（单删、批量删、删主记录三处），漏一个就是脏数据。

## 7. 后端接口对齐

前端 UI 的完整度由后端接口决定，所以子表后端接口是本规范的一部分。任何被 UI 管理的子表，Controller 必须提供：

```text
POST   /<module>/<feature>/create
PUT    /<module>/<feature>/update
DELETE /<module>/<feature>/delete?id=
DELETE /<module>/<feature>/delete-list?ids=
GET    /<module>/<feature>/get?id=
GET    /<module>/<feature>/page
```

挂在父资源下的子表用“父资源/子资源/动作”路径，例如 `/mdm/tool/route-op/update`。权限沿用父资源的 `query` / `update` 即可，不必为子表单独建权限项。

`update` 的唯一性校验必须支持 `excludeId`，否则改一条记录会撞到自己。

列表接口按 [`通用CRUD开发流程.md`](通用CRUD开发流程.md) 的要求在 Mapper 层 JOIN 出 `xxxCode`/`xxxName`，前端不做二次拼接。

子表接口只收发 VO，**不允许把 DO 当请求体或响应体**。生成器直出的 `@RequestBody XxxLineDO` 必须改掉：
DO 带审计字段和数据库结构，直接暴露等于让前端能写 `creator`、`deleted`，也没法挂 `@InEnum` 校验。
子表要有自己的 `XxxLineSaveReqVO` / `XxxLineRespVO` / `XxxLinePageReqVO`，分页参数里的父键用 `@NotNull` 约束。

### 7.1 账表只读

账表（库存余额、库存流水这类由过账写入的表）**只提供查询与导出**，不给新增/修改/删除/导入：

- Controller 只留 `get` / `page` / `export-excel`，Service 只留查询方法，多余的 VO 与 Excel 模板类一起删掉。
- 前端页面同步去掉表单弹窗、行操作、勾选列，工具栏只留导出；`system_menu` 里对应的 create/update/delete/import 权限项也要删除。
- 理由：余额靠乐观锁 + 唯一索引维持账实一致，流水是过账留痕的证据链，放开人工写入等于允许篡改账面。

### 7.2 快照字段谁来填

单据明细上的 `matCode`/`matName`/`uomCode`/`locCode`/`lotNo` 是快照，由后端在写入时补全后落库：

- 前端表单只提交 `matId`/`uomId`/`locId`/`batchId` 这些外键，快照字段不出现在表单里。
- 后端在 Service 里统一走快照服务（WMS 是 `WmsSnapshotService`）取值，跨模块只能用对方模块的 `XxxApi` 门面。
- 进度类数量（已完成/已过账/已分配/已拣选/已搬运/差异）由业务动作推进，创建时归零，同样不放进表单。

## 8. 交付前自查

- [ ] 列表用 `useVbenVxeGrid`，没有手写 `el-table`
- [ ] 页面布局是“列表页 + 抽屉”，没有页面级“左列表 + 右详情”分栏（抽屉内分栏不受限）
- [ ] 新增/编辑是独立弹窗，没有行内即时追加
- [ ] 有 checkbox 列、序号列、固定右侧操作列
- [ ] 字典/布尔/枚举都映射成中文，没有原始值
- [ ] 关联字段显示 code/name，没有裸 ID
- [ ] 外键字段用 `RelationSelect` / `ApiTreeSelect`
- [ ] 关联配置复用 `relation-config.ts`，没有重复定义
- [ ] 弹窗宽度走 `getMdmFormLayout`
- [ ] 编辑先 `get` 再 `setValues`；打开先 `resetForm`
- [ ] 每个 action 有 `auth`，删除有二次确认
- [ ] 只读状态统一 `disabled`
- [ ] 所有 Tab 在父记录切换时都刷新，勾选态清空
- [ ] 子表后端六个接口齐全，`update` 唯一性校验带 `excludeId`
- [ ] 父记录删除路径级联清理了新增的子表
- [ ] `pnpm typecheck` 与 eslint 对改动文件无告警
