# MDM 审核反馈修复方案

## 摘要

按 6 条审核反馈修复本轮 MDM 实施成果，同时清掉审核报告已发现的 P0 构建错误与 P1 功能缺口。核心是两处重新设计：ESOP 从「单文件 + doc_type 混合枚举」重构为「作业步骤 + 多附件子表」，缺陷代码适用范围从单值改为多对多关联表。

---

## 一、ESOP 重新设计

### 现存问题（根因）

1. 一条记录只有一组 `file_id`/`file_url`/`file_name`，**只能挂一个文件**；实际一道工序的作业步骤往往需要图 + 视频 + 图纸并存。
2. `doc_type` 把「内容形态」（TEXT/PARAM）和「文件格式」（IMAGE/VIDEO/PDF/DRAWING）混在一个枚举里，导致前端必须做 `isFileDoc` 分支，切换类型还要清空已填内容。
3. `fileName` 由 `extractFileName(fileUrl)` 从 URL 反解，依赖 URL 末段格式，原始文件名不可靠。
4. 「文件类型必须有文件」的校验只在前端 `ElMessage.warning`，绕过前端直调接口可写入空壳记录。
5. 无唯一索引，同一工序可写入多条相同 `seq`。

### 表结构改造

改造 `mdm_route_op_esop`（语义变为「作业步骤」）：

- 删除 `doc_type`、`file_id`、`file_url`、`file_name`
- 新增 `param_json` text（工艺参数结构化 JSON，与 `content` 作业要点文本分离）
- 保留 `id`、`route_op_id`、`seq`、`title`、`content`、`required_confirm_flag`、`status` + 审计字段
- 补唯一索引 `uk_mdm_route_op_esop_route_op_seq_deleted (route_op_id, seq, deleted)`

新增 `mdm_route_op_esop_file`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `esop_id` | bigint | 作业步骤编号 |
| `seq` | int | 附件展示顺序 |
| `file_type` | varchar(32) | `IMAGE`/`VIDEO`/`PDF`/`DRAWING`/`OFFICE`/`OTHER`，由后端按文件名扩展名判定 |
| `file_name` | varchar(255) | 原始文件名，由上传组件直接回传 |
| `file_url` | varchar(512) | 文件地址 |
| `file_id` | bigint | infra 文件编号 |

索引 `idx_mdm_route_op_esop_file_esop_id (esop_id)`。存量 2 条记录均为纯文本无附件，不需要数据迁移。

字典：删除 `mdm_esop_doc_type`，新增 `mdm_esop_file_type`（IMAGE 图片 / VIDEO 视频 / PDF PDF文件 / DRAWING 图纸 / OFFICE Office文档 / OTHER 其他）。

### 后端改造

- `RouteOpEsopDO` 同步字段增删；新增 `RouteOpEsopFileDO` + `RouteOpEsopFileMapper`
- `RouteOpEsopSaveReqVO`：移除 `docType` 及其 `@Pattern`，新增 `paramJson`、`List<RouteOpEsopFileSaveReqVO> files`（子项 `fileName`/`fileUrl` 必填，`fileId` 可空）
- `RouteOpEsopRespVO`：新增 `List<RouteOpEsopFileRespVO> files`、`fileCount`
- `RouteOpEsopServiceImpl`：
  - `file_type` 由后端按 `fileName` 扩展名判定，前端不传，判定逻辑收口在 `EsopFileTypeEnum`
  - 附件 `seq` 按入参数组下标生成
  - create/update 全量替换子表行（先按 `esop_id` 删再批量插），delete 级联删子表
  - 新增校验：`content`、`paramJson`、`files` 三者不可同时为空（`ROUTE_OP_ESOP_CONTENT_REQUIRED`）；同一工序 `seq` 重复拒绝（`ROUTE_OP_ESOP_SEQ_DUPLICATE`）。校验从前端下沉到 Service
  - 版本冻结锁 `validateRouteEditableByRouteOpId` 逻辑不变，附件写入同样受保护
- `RouteOpEsopMapper.xml`：`selectJoinById` 用 `resultMap` + `<collection>` 一次 JOIN 出附件列表；`selectJoinPage` 用子查询带出 `file_count`；分页条件移除 `docType`，改按 `title` 模糊查
- `RouteServiceImpl.copyRoute`：复制 ESOP 后一并复制附件子行，`esop_id` 重指向新记录

### 前端改造

- 重写 `route-op-esop-file-field.vue`：基于 `ElUpload` + `useUpload().httpRequest` 自建受控组件，`v-model` 绑定 `{ fileName, fileUrl, fileId }[]`。上传成功回调直接取 `file.name`，**不再从 URL 反解文件名**。支持多文件（上限 10 个 / 单个 50MB）、逐个删除、上下调序
- 重写 `route-op-esop-form.vue`：去掉 `docType` 选择器与 `isFileDoc` 分支，字段固定为 顺序 / 标题 / 作业要点 / 工艺参数 JSON / 附件 / 要求确认阅读 / 状态
- `route-config.vue` ESOP 子表格列改为：顺序、标题、附件数、要求确认阅读、状态、操作
- `route-op-esop-preview.vue` 改为接收附件数组：左侧附件列表切换、右侧 `FileViewer` 渲染当前选中文件
- `api/mdm/routeopesop/index.ts` 类型同步

---

## 二、缺陷代码适用范围改多对多

一种缺陷只能绑一道工序、一个物料分类不符合实际（如「外观划伤」适用于打磨、喷涂、总装多道工序）。改为多对多，无关联行即表示不限。

- 新增 `mdm_defect_code_op`（`defect_code_id`、`op_id` + 审计），唯一索引 `(defect_code_id, op_id, deleted)`
- 新增 `mdm_defect_code_mat_cat`（`defect_code_id`、`mat_cat_id` + 审计），唯一索引 `(defect_code_id, mat_cat_id, deleted)`
- 删除 `mdm_defect_code.op_id`、`mat_cat_id`；存量 6 条数据的原单值关系迁移为各一行关联记录
- `DefectCodeSaveReqVO` 新增 `List<Long> opIds`、`List<Long> matCatIds`；`DefectCodeRespVO` 移除 `opId`/`opCode`/`opName`/`matCatId`/`matCatName`，改为 `opIds`/`matCatIds` + `opNames`/`matCatNames`
- `DefectCodeMapper.xml`：`selectJoinPage` 用 `LEFT JOIN` + `GROUP_CONCAT` 一次查出工序编码与分类名称拼接串；`opId`/`matCatId` 查询条件改为 `EXISTS` 子查询
- `DefectCodeServiceImpl`：create/update 全量替换两组关联行，delete 级联清理；校验引用的工序、物料分类存在（新增错误码 `1_062_023_005`、`1_062_023_006`）
- 前端 `defectcode/data.ts`：两个 `RelationSelect` 改为 `multiple: true`、字段名改 `opIds`/`matCatIds`；列表列改展示 `opNames`/`matCatNames`；搜索表单保留单值过滤

---

## 三、去掉 alt_group

替代料是独立的物料替代关系主数据，不是 BOM 行上的一个自由文本分组字段，当前项目不做，直接删除：

- dbx 删除 `mdm_bom_line.alt_group` 列
- 删除 `BomLineDO.altGroup`、`BomLineRespVO.altGroup`
- 同步删除 `dev-docs/MDM基础数据补齐方案.md` 中 BOM 行字段表的替代组条目

---

## 四、布尔列改用系统字典

`infra_boolean_string` 字典（是/否，value 为字符串 `'true'`/`'false'`）与 `CellDict` 渲染器的 `.toString()` 取值方式天然匹配，直接用：

- 列表列加 `cellRender: { name: 'CellDict', props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING } }`
- 表单与搜索表单的 `MDM_BOOLEAN_OPTIONS` 换成 `getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean')`
- 统一范围：物料 `batchFlag`/`snFlag`、资源能力 `preferredFlag`、工序 `keyFlag`/`faiFlag`、工序用料 `mustIssueBeforeStart`、ESOP `requiredConfirmFlag`
- 全部替换完成后删除 `views/mdm/constants.ts` 的 `MDM_BOOLEAN_OPTIONS`

---

## 五、P0 修复

1. **vue-tsc 类型错误**：`calendar/data.ts`、`calendar/modules/rule.vue`、`defectcode/data.ts`、`rescapability/data.ts` 的 `formatter: ({ cell })` 改为 `({ cellValue })`；删除 `rule.vue` 中未使用的 `columns` 常量及其 import
2. **3 个 API 类型定义整段复制自 defectcode**：`api/mdm/team`、`api/mdm/skill`、`api/mdm/tool` 的 interface 字段与注释全部错误，按各自 `data.ts` 实际使用的字段重写
3. **缺陷等级硬编码**：`defectcode/data.ts` 的 `LEVEL_OPTIONS` 与 `formatLevel()` 删除，改用 `getDictOptions('mdm_defect_level','string')` + `CellDict`
4. **eslint import 顺序错误**：`npx eslint --fix` 一次修复

---

## 六、P1 缺口补齐

1. **4 张子表补前端入口**：
   - `mdm_team_member` → 班组列表行操作「成员管理」弹窗
   - `mdm_op_skill_req`、`mdm_member_skill` → 技能列表行操作「工序要求」「人员技能」弹窗
   - `mdm_route_op_tool` → `route-config.vue` 与 ESOP 同层新增「工装」Tab

   弹窗结构沿用 `calendar/modules/rule.vue` 的行内表单 + 明细表格模式；权限沿用各父模块，无需新增菜单
2. **`/mdm/route/copy` 接入**：route 列表加「复制新版本」按钮 + 版本号/生效日期录入弹窗，权限 `mdm:route:create`
3. **BOM 明细 6 个新字段补前端**：`scrapRate`、`issueMethod`、`routeOpSeq`、`positionNo`、`effectiveDate`、`expiryDate`，`issueMethod` 用 `mdm_issue_method` 字典
4. 资源表单补 `calendarId`/`plantId`/`workshopId`，物料表单补 `defaultWhId`/`defaultLocId`

---

## 测试计划

**新增测试**

- `DefectCodeServiceImplTest`：适用工序/物料分类关联行全量替换、更新时旧关联被清理、删除时级联清理、无关联行表示不限、引用不存在的工序被拒
- `RouteOpEsopServiceImplTest` 扩展：多附件保存与全量替换、`file_type` 按扩展名正确判定、`content`/`paramJson`/`files` 同时为空被拒、同工序 `seq` 重复被拒、`copyRoute` 复制步骤时附件一并复制且 `esop_id` 重指向

**验证命令**

- `mvn -pl iboot-module-mdm -am test`
- `apps/web-ele` 下 `npx vue-tsc --noEmit -p tsconfig.json`
- `npx eslint --no-warn-ignored src/views/mdm src/api/mdm src/components/file-viewer`

---

## 假设与默认决策

- **ESOP 不加独立主档**：步骤继续挂 `mdm_route_op`，沿用已实现的 `validateRouteEditable` 版本冻结锁。代价是同一工序在不同产品路线下的指导书各自维护一份，本期接受
- **`content` 与 `param_json` 分离**：原 `doc_type=PARAM` 用 `content` 存 JSON 的做法取消，作业要点文本与工艺参数各占一个字段
- **`file_type` 后端判定**：由 `fileName` 扩展名在 Service 层统一判定，前端不传，避免前后端两处映射表漂移
- **附件上限 10 个 / 单个 50MB**：工艺指导书场景足够，不做可配置
- **缺陷代码关联展示用 `GROUP_CONCAT`**：本项目规模下不会触及默认长度上限
- **`mdm_esop_doc_type` 字典直接删除**：字段已移除，无残留引用，不做停用过渡
