# wms & mes 等需要创建的 BPM 流程模型

- 版本：v1.0.0
- 日期：2026-09-11
- 定位：阶段 3 BPM 接入需要创建的流程模型规格表。按当前指示**先不创建、不测试**，本文档作为后续建模时的唯一输入；建模动作只在 BPM 管理界面进行，禁止手工写 `ACT_*` 表。
- 上游文档：`附-BPM流程定义清单.md`（T3-02 产出）、`dev-docs/BPM模块详细设计与接入规范.md`
- 代码事实核对：本文所有 PROCESS_KEY / businessKey / 变量 / 组件路径均已于 2026-09-11 从实际代码逐处核实，与 `附-BPM流程定义清单.md` 一致。

## 统一配置

| 项 | 取值 | 依据 |
| --- | --- | --- |
| 模型类型 | `SIMPLE`（简单流程设计器） | BPM 规范：业务接入优先选 SIMPLE |
| 表单类型 | `CUSTOM`（业务表单） | 红线：业务数据存业务自己的表，禁止 NORMAL 埋流程变量 |
| businessKey | 一律传业务主键 id 的字符串（`String.valueOf(id)`） | BPM 规范红线 4 |
| 发起方式 | 只能由业务单据的领域动作发起，不提供独立发起页面 | 五个接入点的 `submit*` 动作 |
| 实例状态回写 | `approval_status` 复用 `BpmProcessInstanceStatusEnum`，监听器继承 `BpmProcessInstanceStatusEventListener`，`onEvent` 不抛异常 | BPM 规范红线 6/7 |

## 五个模型总表

| # | 模型名称 | PROCESS_KEY | 业务载体 | businessKey | 发起动作（领域入口） |
| --- | --- | --- | --- | --- | --- |
| 1 | 让步接收 | `mes-concession` | `mes_disposition`（disposition_type=CONCESSION） | 处置单 id | `MesDispositionDomainServiceImpl.submitDisposition` |
| 2 | 报废处置 | `mes-scrap` | `mes_disposition`（disposition_type=SCRAP） | 处置单 id | 同上（同一入口按类型分流） |
| 3 | 盘点差异调整 | `wms-count-diff` | `wms_count_order` | 盘点单 id | `WmsCountOrderServiceImpl.submitCountDiffApproval`（整单审批，非行级） |
| 4 | 超发领料 | `mes-supplement` | `mes_material_req`（补料 / 超 BOM 领料） | 领料申请单 id | `MesMaterialReqDomainServiceImpl.submitMaterialReq`（需审批分支） |
| 5 | 工序跳序 | `mes-op-skip` | `mes_op_skip_record` | 跳序记录 id | `MesOpSkipDomainServiceImpl.skipOpTask`（APPROVAL_REQUIRED 分支） |

## 模型 ↔ 组件对应明细

### 1. 让步接收（mes-concession）

| 项 | 值 |
| --- | --- |
| PROCESS_KEY 常量 | `MesDispositionDomainServiceImpl.PROCESS_KEY_CONCESSION` |
| businessKey | `String.valueOf(disposition.getId())` |
| 流程变量 | 无额外变量 |
| 审批链 | 质量主管 → 技术负责人（两级） |
| 只读详情组件（BPM 详情页动态加载，接收 `id: string`） | `apps/web-ele/src/views/mes/disposition/disposition-process-detail.vue` |
| 业务单据页面（发起入口所在页） | `/mes/quality/disposition` |
| 后端监听器 | `mes/service/disposition/listener/MesConcessionStatusListener.java` |
| 状态回写方法 | `updateConcessionApprovalStatus`；APPROVE → 置 APPROVED 并执行 `executeDisposition`（ConcessionHandler 质量状态过账）；REJECT/CANCEL → 回 DRAFT 记 approval_reason |

### 2. 报废处置（mes-scrap）

| 项 | 值 |
| --- | --- |
| PROCESS_KEY 常量 | `MesDispositionDomainServiceImpl.PROCESS_KEY_SCRAP` |
| businessKey | `String.valueOf(disposition.getId())`（与让步共用一张单据、共用四个审批列，同一单据同时只走一个流程） |
| 流程变量 | `qty`（处置数量，字符串）。**金额分档变量未实现**：物料标准成本字段在主数据门面与质检快照中不存在，服务端只能塞 `qty`，建模时条件网关先按数量分档（见"发布前置"） |
| 审批链 | 数量条件网关：小额 → 班组长；大额 → 生产经理 |
| 只读详情组件 | `apps/web-ele/src/views/mes/disposition/disposition-process-detail.vue`（与让步接收共用） |
| 业务单据页面 | `/mes/quality/disposition` |
| 后端监听器 | `mes/service/disposition/listener/MesScrapStatusListener.java` |
| 状态回写方法 | `updateScrapApprovalStatus`；APPROVE → `executeDisposition`（ScrapHandler 报废出库过账）；三层幂等同让步范式 |

### 3. 盘点差异调整（wms-count-diff）

| 项 | 值 |
| --- | --- |
| PROCESS_KEY 常量 | `WmsCountOrderServiceImpl.PROCESS_KEY_COUNT_DIFF` |
| businessKey | `String.valueOf(countOrderId)` |
| 流程变量 | 无额外变量 |
| 审批链 | 仓储主管 → 财务（两级） |
| 只读详情组件 | `apps/web-ele/src/views/wms/countorder/count-order-process-detail.vue` |
| 业务单据页面 | `/wms/operation/count-order` |
| 后端监听器 | `wms/service/count/listener/WmsCountDiffStatusListener.java` |
| 状态回写方法 | `updateCountDiffApprovalStatus`；APPROVE → **逐行** `confirmCountDiff`（幂等键 `WMS:{countNo}:COUNT_DIFF_POST:{lineNo}`）；REJECT → 单据回 COUNTING 要求重盘 |

### 4. 超发领料（mes-supplement）

| 项 | 值 |
| --- | --- |
| PROCESS_KEY 常量 | `MesMaterialReqDomainServiceImpl.PROCESS_KEY_SUPPLEMENT` |
| businessKey | `String.valueOf(req.getId())` |
| 流程变量 | 无额外变量（审批触发条件在服务端判定：补料 SUPPLEMENT、或 ISSUE 超 BOM 累计领用量即触发） |
| 审批链 | 车间主管（一级） |
| 只读详情组件 | `apps/web-ele/src/views/mes/materialreq/material-req-process-detail.vue` |
| 业务单据页面 | `/mes/material/material-req` |
| 后端监听器 | `mes/service/materialreq/listener/MesSupplementStatusListener.java` |
| 状态回写方法 | `updateSupplementApprovalStatus`；发起前已做预留（占住料），APPROVE → 允许 `confirmMaterialReq` 确认出库；REJECT/CANCEL → 释放预留并置 CANCELLED |

### 5. 工序跳序（mes-op-skip）

| 项 | 值 |
| --- | --- |
| PROCESS_KEY 常量 | `MesOpSkipDomainServiceImpl.PROCESS_KEY_OP_SKIP` |
| businessKey | `String.valueOf(record.getId())` |
| 流程变量 | 无额外变量 |
| 审批链 | 工艺工程师 → 质量主管（两级） |
| 只读详情组件 | `apps/web-ele/src/views/mes/opskiprecord/op-skip-record-process-detail.vue` |
| 业务单据页面 | `/mes/logistics/op-skip-record` |
| 后端监听器 | `mes/service/opskiprecord/listener/MesOpSkipStatusListener.java` |
| 状态回写方法 | `updateSkipApprovalStatus`；APPROVE → 记录置 EFFECTIVE/APPROVED，工序任务推 SKIPPED 写 SKIP 流水并推进后序 PENDING→READY；REJECT → 记录置 REVOKED/REJECTED，工序任务保持原状。审批期间工序任务状态不变 |

## 发布前置阻断项（建模前必须先解决）

1. **审批岗位角色不存在**：截至 2026-09-11，`system_role` 仅 7 个系统角色，无质量主管、技术负责人、班组长、生产经理、仓储主管、财务、车间主管、工艺工程师任何审批岗位。候选人策略不能配空，需业务方先拍板岗位与人员映射（复用现有角色或新建角色）。
2. **报废分档口径未定**：小额/大额阈值未定；物料标准成本取数通道未建（T3-04 遗留）。当前服务端只传 `qty`，建模时先按数量分档，金额分档留待标准成本门面补齐后升级流程定义版本。

## 建模与发布核对清单（恢复执行时用）

- [ ] 五个模型在 BPM 管理界面创建，模型类型 SIMPLE、表单类型 CUSTOM
- [ ] 每个模型的业务表单路由指向本文"只读详情组件"对应路径
- [ ] 审批链按本文配置，候选人策略指向已创建/映射的岗位角色
- [ ] `mes-scrap` 配置数量条件网关，变量引用 `qty`
- [ ] 逐个发布，一个 key 仅保留一个启用版本
- [ ] 发布后回填 `附-BPM流程定义清单.md` 的模型 ID、定义 ID、发布人、发布时间
- [ ] 补跑被 T3-02 阻断的运行态验证（审批期间事实未动、重复回调幂等、T3-08 仅 bpm 权限角色打开详情）
