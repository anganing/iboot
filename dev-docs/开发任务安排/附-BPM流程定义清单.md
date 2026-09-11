# BPM 流程定义清单

本文是 T3-02 的建模输入与发布核对表。流程定义必须在 BPM 管理界面创建并发布；禁止手工写入 `ACT_*` 表。

## 统一配置

- 模型类型：`SIMPLE`。
- 表单类型：`CUSTOM`。
- 发起页面只负责进入相应的业务单据页面；流程实例只能由业务单据的领域动作发起，`businessKey` 固定为业务主键字符串。
- 查看页面均为只读 Vue 组件，接收 `id: string`，由 BPM 流程详情动态加载。
- 流程定义发布后，补回本表的模型 ID、流程定义 ID、发布人及发布时间；一个 key 仅保留一个启用版本。

| 流程 key | 名称 | 业务载体 | 发起页面 | 只读表单路径 | 审批链 | 模型 ID | 定义 ID | 发布状态 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `mes-concession` | 让步接收 | `mes_disposition`，`CONCESSION` | `/mes/quality/disposition` | `/mes/disposition/disposition-process-detail` | 质量主管 → 技术负责人 | 待创建 | 待发布 | 待发布 |
| `mes-scrap` | 报废处置 | `mes_disposition`，`SCRAP` | `/mes/quality/disposition` | `/mes/disposition/disposition-process-detail` | 数量条件网关：小额班组长；大额生产经理 | 待创建 | 待发布 | 待发布 |
| `wms-count-diff` | 盘点差异调整 | `wms_count_order` | `/wms/operation/count-order` | `/wms/countorder/count-order-process-detail` | 仓储主管 → 财务 | 待创建 | 待发布 | 待发布 |
| `mes-supplement` | 超发领料 | `mes_material_req` | `/mes/material/material-req` | `/mes/materialreq/material-req-process-detail` | 车间主管 | 待创建 | 待发布 | 待发布 |
| `mes-op-skip` | 工序跳序 | `mes_op_skip_record` | `/mes/logistics/op-skip-record` | `/mes/opskiprecord/op-skip-record-process-detail` | 工艺工程师 → 质量主管 | 待创建 | 待发布 | 待发布 |

## 节点与变量

| 流程 key | 节点顺序 | 流程变量 |
| --- | --- | --- |
| `mes-concession` | 发起人 → 质量主管 → 技术负责人 → 结束 | 无额外变量 |
| `mes-scrap` | 发起人 → 数量条件网关 → 班组长/生产经理 → 结束 | `qty`；金额分档需在服务端补齐物料标准成本后才可启用金额条件 |
| `wms-count-diff` | 发起人 → 仓储主管 → 财务 → 结束 | 无额外变量 |
| `mes-supplement` | 发起人 → 车间主管 → 结束 | 无额外变量 |
| `mes-op-skip` | 发起人 → 工艺工程师 → 质量主管 → 结束 | 无额外变量 |

## 发布前阻断项

截至 2026-09-11，`system_role` 中没有质量、技术、班组、生产、仓储、财务、车间或工艺对应角色。候选人策略不能配置为空；必须先明确并创建/映射这些岗位角色，才可发布上述流程。

`mes-scrap` 的分档阈值与物料标准成本来源尚未定义。当前业务代码仅传递 `qty`，因此发布时只能先采用数量分档；若需按金额分档，必须先补充标准成本的跨模块批量取数与流程变量。
