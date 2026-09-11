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
| `mes-concession` | 让步接收 | `mes_disposition`，`CONCESSION` | `/mes/quality/disposition` | `/mes/disposition/disposition-process-detail` | 质量主管 → 技术负责人 | `6cc76604-adec-11f1-a2d1-f8b54d9d43a2` | `mes-concession:1:842436aa-adf3-11f1-a2d1-f8b54d9d43a2` | 已发布 2026-09-11 23:14（admin） |
| `mes-scrap` | 报废处置 | `mes_disposition`，`SCRAP` | `/mes/quality/disposition` | `/mes/disposition/disposition-process-detail` | 数量条件网关：小额班组长；大额生产经理 | `37530617-adf0-11f1-a2d1-f8b54d9d43a2` | `mes-scrap:1:f4d7a4a0-adf2-11f1-a2d1-f8b54d9d43a2` | 已发布 2026-09-11 23:10（admin） |
| `wms-count-diff` | 盘点差异调整 | `wms_count_order` | `/wms/operation/count-order` | `/wms/countorder/count-order-process-detail` | 仓储主管 → 财务 | `86018a78-adf0-11f1-a2d1-f8b54d9d43a2` | `wms-count-diff:2:158fd82a-adf4-11f1-a2d1-f8b54d9d43a2` | 已发布 2026-09-11 23:18（admin），v2 为当前版本 |
| `mes-supplement` | 超发领料 | `mes_material_req` | `/mes/material/material-req` | `/mes/materialreq/material-req-process-detail` | 车间主管 | `c3a2abc9-adf0-11f1-a2d1-f8b54d9d43a2` | `mes-supplement:1:a8031b00-adf3-11f1-a2d1-f8b54d9d43a2` | 已发布 2026-09-11 23:15（admin） |
| `mes-op-skip` | 工序跳序 | `mes_op_skip_record` | `/mes/logistics/op-skip-record` | `/mes/opskiprecord/op-skip-record-process-detail` | 工艺工程师 → 质量主管 | `fc026f0a-adf0-11f1-a2d1-f8b54d9d43a2` | `mes-op-skip:1:d6529306-adf3-11f1-a2d1-f8b54d9d43a2` | 已发布 2026-09-11 23:17（admin） |

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

## 落地记录（2026-09-12 回填）

- **角色阻断项已解除**：dbx 建 8 个审批岗位角色（`system_role` type=2，ID 164–171）：质量主管 `mes_quality_manager`(164)、技术负责人 `mes_tech_leader`(165)、班组长 `mes_team_leader`(166)、生产经理 `mes_production_manager`(167)、仓储主管 `wms_warehouse_leader`(168)、财务 `finance`(169)、车间主管 `mes_workshop_leader`(170)、工艺工程师 `mes_process_engineer`(171)。角色与人员的映射待业务方在"系统管理-角色管理"维护。
- 五个模型于 2026-09-11 晚在 BPM 管理界面创建并发布，全部 SIMPLE + CUSTOM，表单路径与上表一致。
- **与规格的差异一：`wms-count-diff` 当前链是"仓储主管"单级**（v1/v2 均只配仓储主管一个节点），规格要求的"仓储主管 → 财务"两级未配齐；财务角色（169）已建好，补节点后重新发布即可。
- **与规格的差异二：`mes-scrap` 数量分档阈值取 `${qty < 100}`**（小额班组长，其它情况生产经理），100 是占位值，业务方拍板后改表达式重新发布。
- 金额分档仍按发布前阻断项执行：服务端只传 `qty`，物料标准成本取数通道（T3-04 遗留）补齐后再升级流程定义。
- 待办：补跑被 T3-02 阻断的运行态验证（审批期间事实未动、重复回调幂等、T3-08 仅 bpm 权限角色打开详情）。
