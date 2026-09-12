# Agent 作业规范（UI 规范改造 2026-09-12）

所有 agent 先读这三份，再开工：

1. `D:\iboot-pro\iboot\dev-docs\后台UI开发规范.md` —— 第 3.3/3.4/3.5/6.0 节是本轮新规则
2. `D:\iboot-pro\iboot\dev-docs\UI规范改造-20260912\任务清单.md` —— 命中页面与实测数据
3. `D:\iboot-pro\iboot\dev-docs\UI规范改造-20260912\后端支撑核查.md` —— 接口与菜单事实

## 已建好的公共设施（禁止重复实现）

- `apps/web-ele/src/utils/codeLinkColumn.ts`：`codeLinkColumn()` 展开进编码列，`isCodeLinkColumn(column)` 用于双击分流。
- `apps/web-ele/src/styles/index.scss`：`.cell-code-link` 下划线+手型（vxe 列 `className` 落在 `<td>` 上，必须全局，已在 `bootstrap.ts` 引入）。

## 操作列档位（任务 3）

位数 = 固定按钮数 + 「更多」按钮算 1 位。

| 位数 | width |
| --- | --- |
| 1 | 100 |
| 2 | 160 |
| 3 | 220 |
| 4 | 280 |
| 5 | 340 |

固定动作 > 4 个时，低频动作（取消/关闭/恢复/导出/打印/历史）挪进 `dropDownActions`。180/240/260 这类中间值一律改掉。
不可点的按钮保持展示并 `disabled` + `tooltip` 说明原因，不许隐藏。

## 只读详情抽屉（任务 4）

- 文件：`views/<module>/<feature>/modules/<feature>-detail.vue`（kebab-case）。
- 形态：`useVbenDrawer` + `ElDescriptions`，`defineProps<{ id: string }>()`，`watch(() => props.id, load, { immediate: true })`，打开时 `get(id)` 拉全量。
- 参照：`views/wms/countorder/count-order-process-detail.vue`（它是嵌入审批详情的组件，你要做的是独立抽屉，容器换成 `useVbenDrawer`）。
- 字段用完整 RespVO；字典 `DictTag`，时间 `formatDate`，布尔 `DICT_TYPE.INFRA_BOOLEAN_STRING`，不显示裸 ID 和原始枚举值。
- `get` 返回 null（行已被删除）时展示空态，不抛错。
- 列表页：`const [DetailDrawer, detailDrawerApi] = useVbenDrawer({ connectedComponent: Detail, destroyOnClose: true })`，模板里 `<DetailDrawer />`。
- 双击入口：`@cell-dblclick="handleCellDblclick"`，函数内 `if (!isCodeLinkColumn(column)) return;` 再 open。
- **宽表若操作列已有「明细/详情」类按钮，让它打开同一个 detail 抽屉**，不要两个抽屉并存。

## 硬约束

- 只改分配给你的文件清单，一字不差。共享 data.ts 的所有权见各 agent 任务书。
- 禁止 `el-table` 替换 `useVbenVxeGrid`；禁止改后端 Java；禁止改 `system_menu`（数据库由主线程统一处理）。
- 禁止通配覆盖 vxe 内部布局（`:deep(.vxe-grid){...!important}`）。
- 不新增依赖，不动 `pnpm-lock.yaml`。
- 简体中文注释，解释"为什么"而非复述代码。
- 不确定的取舍写进 `dev-docs/UI规范改造-20260912/待决问题.md`（追加，不覆盖别人的条目，用小节标题区分模块），不要自行扩大范围。

## 自查（每个 agent 必须跑）

**不要跑 `vue-tsc`**（全量、吃 8G 堆，多 agent 并发会打爆内存，主线程最后统一验收）。
你只做两件事：

1. 语法自检（只解析不解析依赖，每个改过的 `.ts`）：
   ```bash
   cd /d/iboot-pro/iboot/iboot-ui/iboot-ui-admin-vben/apps/web-ele
   pnpm exec esbuild --loader:.ts=ts src/views/<module>/<feature>/data.ts --outfile=NUL
   ```
   `.vue` 文件 esbuild 不解析，靠你自查：模板标签闭合、import 路径正确、
   `codeLinkColumn()` / `isCodeLinkColumn()` 用法与样板一致。
2. 对照规范自查：操作列 width 落在档位、详情抽屉回查 `get`、`get` 失败走空态、枚举列走字典。

最后回报：改了哪些文件、每页按钮位数与档位、哪些没动及原因。

