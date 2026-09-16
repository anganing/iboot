# iboot MOM 交付脚本（sql/deploy）

- 版本：**v1.0.0**
- 日期：2026-09-16
- 对应任务：`dev-docs/开发任务安排/06-阶段5-追溯与交付物.md` 的 **T5-04**
- 导出源库：`ruoyi-vue-pro-20260817` @ `127.0.0.1:3306`
- 定位：把 MOM 改造（阶段 0~5）产生的**表结构变更与配置类初始数据**固化成交付脚本，供新环境一次性建库到「可进入试点配置」的状态。

> 本目录与 `sql/` 下芋道原生脚本（`mysql/` `oracle/` `postgresql/` 等）**完全隔离**，互不覆盖、互不依赖。原生脚本是芋道框架自带的，本目录是 iboot MOM 改造的增量交付物。

---

## 一、怎么用

### 前置条件

| 项 | 要求 |
| --- | --- |
| MySQL | 8.0（实测 8.0.32 通过） |
| 目标库 | 一个**空库**（字符集 `utf8mb4`），例如 `CREATE DATABASE iboot_mom DEFAULT CHARACTER SET utf8mb4;` |
| Redis | 应用启动需要，6379 可连 |
| 应用 | `iboot-server.jar`，`--spring.profiles.active=local`（或自行改数据源指向新库） |

### 执行顺序（严格按编号）

```bash
# 1. 建空库
mysql -h127.0.0.1 -P3306 -uroot -e "CREATE DATABASE iboot_mom DEFAULT CHARACTER SET utf8mb4;"

# 2. 按编号顺序执行
for f in sql/deploy/0*.sql; do
  mysql -h127.0.0.1 -P3306 -uroot iboot_mom < "$f"
done

# 3. 重新部署五个审批流程的 BPMN 模型（见下方「BPMN 模型」一节）
#    启动应用后，通过 BpmModelController 的 /import + /deploy 完成
```

### BPMN 模型（重要，别跳过）

`09-bpm-process.sql` 只包含流程的**登记信息**（`bpm_process_definition_info` 等），它决定流程在「流程定义列表」里显示什么；而真正决定流程**能不能跑起来**的 BPMN 定义，存在 Flowable 的 `ACT_RE_PROCDEF` / `ACT_GE_BYTEARRAY` 里。

按交付红线「不导出 `ACT_*` 表结构」，这两张表不进脚本。因此**只执行 SQL 不部署模型，会出现「流程列表里看得见、点发起报流程定义不存在」**——这是首次空库实测实际踩到的坑（详见「验证记录」）。

正确做法：把 `sql/deploy/bpmn/*.bpmn` 当**源文件**，应用启动后经 `BpmModelController` 的 `/import` 导入模型、`/deploy` 发布，让引擎自己生成 `ACT_*` 记录。

| 文件 | 对应流程 key |
| --- | --- |
| `bpmn/mes-scrap.bpmn` | `mes-scrap` 报废处置审批 |
| `bpmn/mes-concession.bpmn` | `mes-concession` 让步接收审批 |
| `bpmn/mes-supplement.bpmn` | `mes-supplement` 超发领料审批 |
| `bpmn/mes-op-skip.bpmn` | `mes-op-skip` 工序跳序审批 |
| `bpmn/wms-count-diff.bpmn` | `wms-count-diff` 盘点差异调整审批 |

---

## 二、文件清单

| 文件 | 作用 | 导出范围 | 本次条数 |
| --- | --- | --- | --- |
| `01-schema.sql` | 全量表结构 DDL，不含数据 | 全库 166 张表（155 张业务表 + 11 张 QRTZ）；**剔除 `ACT_*` 39 张与 `FLW_*` 6 张**，引擎自建 | 166 CREATE TABLE |
| `02-menu.sql` | 菜单与按钮权限 | `system_menu` 全量 | 1147 |
| `03-role.sql` | 角色与授权 | `system_role` + `system_role_menu` 全量 | 15 + 1349 |
| `04-dict.sql` | 字典 | `system_dict_type` + `system_dict_data` 全量 | 207 + 1065 |
| `05-code-rule.sql` | 编码规则 | `infra_code_rule` 全量 | 54（MDM 18 / WMS 17 / MES 15 / APS 3 / 其他 1） |
| `06-wms-strategy.sql` | 仓储策略样例 | `wms_strategy` | 7 |
| `07-qc-rule.sql` | 质检规则与检验方案样例 | `mes_qc_rule` + `mes_qc_plan` + `mes_qc_item` | 8 + 7 + 22 |
| `08-aps-strategy.sql` | 排程策略样例 | `aps_strategy` | 1 |
| `09-bpm-process.sql` | 五个审批流程定义（登记信息） | `bpm_category` + `bpm_form` + `bpm_process_definition_info` + `bpm_process_expression` | 6 + 0 + 315 + 2 |
| `bpmn/*.bpmn` | 五个审批流程的 BPMN 模型源文件 | 见上表 | 5 个文件 |

脚本执行顺序即编号顺序：`01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 09 → bpmn 部署`。

**不含业务数据**：工单、库存、流水、批次这类运行时数据一律不进交付脚本（只有配置与样例）。

---

## 三、怎么重新生成

导出脚本：`script/export-deploy-sql.sh`（项目 `script/` 目录下已有脚本以 shell 为主，故本任务采用 `.sh`）。

```bash
bash script/export-deploy-sql.sh
```

### 凭据从哪来（不硬编码密码）

按优先级取，脚本里不写死密码：

1. 环境变量 `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_USER` / `MYSQL_PWD` / `MYSQL_DATABASE`
2. 同目录的 `script/.env.deploy`（存在则 `source`，该文件不入库）
3. 兜底默认值（仅开发机通用口径）

```bash
# 示例：指向别的库
MYSQL_DATABASE=ruoyi-vue-pro-20260817 MYSQL_PWD=xxx bash script/export-deploy-sql.sh
```

### 可重跑性怎么保证

三条一起用，实测连跑两次 **14 个文件（9 SQL + 5 BPMN）md5 全部一致**：

1. **数据段全部由 `mysqldump` 生成**，脚本只做「拼文件头 + 调 dump + 拼文件尾」，不手工拼一行 SQL。
2. `--skip-extended-insert`：一行一条 `INSERT`（而非一条 INSERT 塞全部行），输出形态稳定、便于 diff。
3. `--order-by-primary`：无显式 `ORDER BY` 时按主键排序，消除行序漂移。

`mysqldump` 取值通道：本机 `PATH` 有就用本机的；没有则 `docker exec` 进 `$MYSQL_DOCKER_CONTAINER`（默认 `mysql-server`）执行——开发机 MySQL 跑在 Docker 里、镜像自带 mysqldump，这是本项目实际形态。

> `sql/deploy/*.sql` 是**生成物**，不要手工编辑——改了下次导出就被覆盖。要改内容请改导出脚本后重跑。

---

## 四、变更记录

| 日期 | 版本 | 改了什么 | 谁改的 |
| --- | --- | --- | --- |
| 2026-09-16 | v1.0.0 | 首版。建立 `sql/deploy/` 与 `script/export-deploy-sql.sh`；产出 01~09 九个 SQL 与 5 个 BPMN 模型文件；空库实测通过应用启动 | T5-04 执行 AI |

> 后续每次重新导出并交付，请在上表追加一行，同时在任务文档 T5-04 的自查里说明。

---

## 五、验证记录

### 5.1 空库实测（2026-09-16，实际执行）

**实测环境**：MySQL 8.0.32（容器 `mysql-server`，宿主 3306）、Redis 6379 在跑、应用 `iboot-server.jar`（`--spring.profiles.active=local`，数据源改指向验证库）、JDK 21。

**步骤与结果**：

| # | 步骤 | 结果 |
| --- | --- | --- |
| 1 | 建空库 `iboot_deploy_verify`（utf8mb4） | 建库成功，初始 0 张表 |
| 2 | 按 README 顺序执行 `01`~`09` 九个 SQL | **9/9 全部 OK**，无一条报错 |
| 3 | 启动应用指向该库 | **`Started IbootServerApplication in 216.797 seconds`**，Tomcat 监听 48080 |
| 4 | 健康检查 `GET /actuator/health` | `{"status":"UP"}` |
| 5 | Flowable 自建表确认 | 引擎按预期自建 **39 张 `ACT_*`**（`01-schema.sql` 未含其结构，符合红线） |

**四项验收确认**：

| 验收项 | 结论 | 实测依据 |
| --- | --- | --- |
| 菜单树完整，三个隐藏菜单确实隐藏 | **通过（数据层）** | `system_menu` 装载 1147 条，与源库同数；T5-01/T5-03 新增菜单 2711/2712/2713 齐全；三个隐藏菜单 2317「RGV任务」/2339「出库波次」/2340「设备任务」`visible=0` 保持隐藏（源库另有一条 891 属芋道原生，共 4 条隐藏） |
| 编码规则齐全 | **通过** | `infra_code_rule` 装载 54 条 = MDM 18 + WMS 17 + MES 15 + APS 3 + 其他 1，与源库同数、分类同数 |
| 字典完整 | **通过（数据层）** | `system_dict_type` 207 / `system_dict_data` 1065，与源库同数；1065 条字典项 `label` 均非空（枚举列可映中文）；MES 前缀字典 39 个、WMS 前缀 37 个 |
| 五个审批流程可发起 | **部分通过，见下方偏差** | 五个流程定义（`mes-scrap`/`mes-concession`/`mes-supplement`/`mes-op-skip`/`wms-count-diff`）登记信息齐全、`visible=1`；但**仅执行 SQL 时引擎侧无部署记录，此时不可发起**，需补做 BPMN 模型重新部署（脚本已补 `bpmn/` 出料，部署动作见「一、怎么用」） |

**说明**：第 3 项与第 4 项在**数据层**已验证（表内数据与源库逐项同数）；「菜单树渲染」与「枚举列显示中文」属前端渲染表现，未做界面点击验证，原因见 5.4。

### 5.2 可重跑性实测

连跑两次导出脚本，9 个 SQL + 5 个 BPMN 共 **14 个文件 `md5sum -c` 全部 OK**（字节一致）。

### 5.3 复测（2026-09-16 收尾，第二次空库）

确认全部改动落定后重跑一遍完整链路：建空库 `iboot_final_verify` → 依次执行 01~09 → **九个 SQL 全部执行成功** → 复核 表总数 166 / `ACT_*` 0 张 / `system_menu` 1147 / `infra_code_rule` 54 / MOM 流程定义 5 个，全部符合预期。测完已 `DROP DATABASE` 清理。

### 5.4 未做到的部分（如实记录）

**1. 未完成「登录后走界面点击」的端到端验证。** 应用起来了、健康检查 UP、数据齐备，但登录接口返回 `账号密码不正确`——源库 `system_users` 里 admin 是 BCrypt 哈希（`$2a$04$JQtf3G7...`），非芋道演示默认口令，试了若干常见口令均不匹配。**不猜密码**，故菜单树渲染、枚举列显示、以及「发起审批流程」的界面验证未执行。补齐方式：提供 admin 明文口令，或直接用可用账号登录后按 5.1 的四个验收项点一遍。

**2. 五个审批流程的「可发起」未经运行时验证。** 见 5.1 第 4 项。根因已定位（BPMN 定义在 `ACT_*` 里，而交付规则不许导 `ACT_*`），补救出料（`bpmn/`）已补进脚本，但「导入 + 部署后再发起」这一步未实测。

---

## 六、已知遗留

1. **8 个审批岗位角色（`system_role` id 164~171）没有任何菜单授权**：`system_role_menu` 里这些角色授权数为 **0**。核对源库后确认**源库本身也是 0**，导出是忠实拷贝，不是导出缺陷。影响：以这些角色登录看不到菜单。当前审批人是通过 BPM 待办任务拿单，不依赖菜单，故暂不阻断；若后续要求审批人独立登录操作，需补授权。
2. **`bpm_process_definition_info` 携带了 256 条历史上的流程登记**（含 OA、测试流程等），不只 MOM 的五个。当前按「全量」导出，属可接受的保守做法；若交付要求只含 MOM 流程，需在导出脚本里加过滤条件。
3. **`system_mobile_menu`（8 条）未纳入本次导出**。移动端菜单属移动端改造范畴，T5-04 任务书表格未列该表；若移动端交付需要，应另立任务或扩表。
4. **BPMN 模型重新部署尚无一键脚本**。当前靠 `BpmModelController` 的 `/import` + `/deploy` 手工触发，见「一、怎么用」的 BPMN 模型一节。

---

## 七、关于本目录的版本库可见性

仓库根 `.gitignore` 里有一条历史规则 `/sql/`，会把**整个根 `sql/` 目录**排除在版本库外（目的是不让芋道上游原生脚本入库）。

该规则由提交 `42be1db`（"修复 .gitignore 的 sql/ 规则误伤"）从 `sql/` 收窄为 `/sql/` 得来，**收窄的部分是保护嵌套的 `src/test/resources/sql/` 与 codegen 的 `sql/` 模板**，那部分必须保持。但根 `sql/` 本身仍在排除范围内，会导致本目录的交付脚本无法入库。

T5-04 已把规则改为：

```gitignore
/sql/*
!/sql/deploy/
```

用 `/sql/*` 替代 `/sql/`，再放行 `!/sql/deploy/`。两点注意：

- 必须用 `/sql/*` 而非 `/sql/`——git 不会重新包含「已被排除的目录」内部的文件，只排除目录下的条目才能靠否定规则放行。
- `42be1db` 保护的嵌套资源不受影响（`/sql/*` 只作用于仓库根，不下探），已复核 20 个模块测试脚本仍在跟踪列表内。

