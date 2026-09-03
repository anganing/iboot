# MDM 模块 API 门面开发文档

- 版本：v1.0.0
- 日期：2026-08-30
- 状态：待实施
- 模块：`iboot-module-mdm`
- 定位：对 WMS / MES / APS 提供只读主数据门面

---

## 1. 范围

### 1.1 本轮要做

1. 新增 `com.iboot.pf.module.mdm.api` 包。
2. 按 13 个子域拆分 API 接口、DTO 与实现。
3. 复用现有 MDM Service，必要时补充只读 Service 方法。
4. 为每个 API 编写单元测试。

### 1.2 本轮不做

1. 不新增 admin CRUD。
2. 不新增 HTTP Controller。
3. 不暴露 Mapper、DO、Controller VO。
4. 不做写操作 API；MDM 对外是只读主数据门面。
5. 不直接依赖 WMS / MES / APS。

## 2. Maven 依赖

`iboot-module-mdm` 当前已依赖 system 与 infra，无需新增依赖。

禁止新增以下依赖：

```xml
iboot-module-wms
iboot-module-mes
iboot-module-aps
```

## 3. 包结构

```text
com.iboot.pf.module.mdm.api
├── package-info.java
├── material
│   ├── MaterialApi.java
│   ├── MaterialApiImpl.java
│   └── dto
├── matcategory
│   ├── MatCategoryApi.java
│   ├── MatCategoryApiImpl.java
│   └── dto
├── uom
│   ├── UomApi.java
│   ├── UomApiImpl.java
│   └── dto
├── bom
│   ├── BomApi.java
│   ├── BomApiImpl.java
│   └── dto
├── route
│   ├── RouteApi.java
│   ├── RouteApiImpl.java
│   └── dto
├── resource
│   ├── ResourceApi.java
│   ├── ResourceApiImpl.java
│   └── dto
├── org
│   ├── OrgApi.java
│   ├── OrgApiImpl.java
│   └── dto
├── partner
│   ├── PartnerApi.java
│   ├── PartnerApiImpl.java
│   └── dto
├── project
│   ├── ProjectApi.java
│   ├── ProjectApiImpl.java
│   └── dto
├── team
│   ├── TeamApi.java
│   ├── TeamApiImpl.java
│   └── dto
├── skill
│   ├── SkillApi.java
│   ├── SkillApiImpl.java
│   └── dto
├── tool
│   ├── ToolApi.java
│   ├── ToolApiImpl.java
│   └── dto
└── defectcode
    ├── DefectCodeApi.java
    ├── DefectCodeApiImpl.java
    └── dto
```

## 4. API 契约清单

| API | 核心方法 | 主要消费者 | 说明 |
| --- | --- | --- | --- |
| `MaterialApi` | `getMaterial`、`getMaterialList`、`getMaterialMap`、`getMaterialListByCodes`、`validateMaterialList` | WMS、MES、APS | 物料主档与计划、储运属性 |
| `MatCategoryApi` | `getMatCategoryTree`、`getMatCategoryList` | MES、APS | 物料分类树 |
| `UomApi` | `getUomList`、`getUomMap`、`getUomConvList` | WMS、MES、APS | 单位与换算 |
| `BomApi` | `getEffectiveBom`、`getBomDetail`、`getBomLineList` | MES、APS | BOM 版本与明细 |
| `RouteApi` | `getEffectiveRoute`、`getRouteDetail`、`getRouteOpList`、`getRouteOpMatList`、`getRouteOpEsopList` | MES、APS | 工艺路线、工序、用料、ESOP |
| `ResourceApi` | `getResourceList`、`getResourceCalendarList`、`getResourceCapabilityList`、`getChangeoverList` | MES、APS | 资源、日历、能力、换型 |
| `OrgApi` | `getInvOrgList`、`getPlantList`、`getWorkshopList`、`getWcList` | WMS、MES、APS | 组织与工作中心 |
| `PartnerApi` | `getSupplierList`、`getSupplierMap`、`getCustomerList`、`getCustomerMap` | WMS、MES | 供应商与客户 |
| `ProjectApi` | `getProjectList`、`getProjectTaskList` | WMS、MES、APS | 项目与项目任务 |
| `TeamApi` | `getTeamListByWorkshopIds`、`getTeamMemberList` | MES | 班组与成员 |
| `SkillApi` | `getSkillList`、`getOpSkillReqList`、`getMemberSkillList` | MES | 技能与资质 |
| `ToolApi` | `getToolList`、`getRouteOpToolList` | MES、APS | 工装与工序工装 |
| `DefectCodeApi` | `getDefectCodeList`、`getDefectCodeTree` | MES | 缺陷代码与分类 |

## 5. 关键接口设计

### 5.1 MaterialApi

```java
public interface MaterialApi {

    MaterialRespDTO getMaterial(Long id);

    List<MaterialRespDTO> getMaterialList(Collection<Long> ids);

    Map<Long, MaterialRespDTO> getMaterialMap(Collection<Long> ids);

    List<MaterialRespDTO> getMaterialListByCodes(Collection<String> codes);

    void validateMaterialList(Collection<Long> ids);
}
```

`MaterialRespDTO` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `Long` | 物料编号 |
| `catId` | `Long` | 物料分类编号 |
| `code` | `String` | 物料编码 |
| `name` | `String` | 物料名称 |
| `spec` | `String` | 规格型号 |
| `drawingNo` | `String` | 图号 |
| `matType` | `String` | 物料类型 |
| `baseUomId` | `Long` | 基准计量单位编号 |
| `batchFlag` | `Boolean` | 是否批次管控 |
| `snFlag` | `Boolean` | 是否 SN 管控 |
| `shelfLifeDays` | `Integer` | 保质期天数 |
| `netWeight` | `BigDecimal` | 净重 |
| `grossWeight` | `BigDecimal` | 毛重 |
| `weightUomId` | `Long` | 重量单位编号 |
| `length` | `BigDecimal` | 长，毫米 |
| `width` | `BigDecimal` | 宽，毫米 |
| `height` | `BigDecimal` | 高，毫米 |
| `volume` | `BigDecimal` | 体积，立方毫米 |
| `planStrategy` | `String` | `MTO` 按单 / `MTS` 备库 |
| `leadTimeDays` | `Integer` | 提前期 |
| `safetyStock` | `BigDecimal` | 安全库存 |
| `minOrderQty` | `BigDecimal` | 最小订货量 |
| `orderMultiple` | `BigDecimal` | 订货倍数 |
| `defaultWhId` | `Long` | 默认仓库编号 |
| `defaultLocId` | `Long` | 默认库位编号 |
| `packQty` | `BigDecimal` | 标准包装量 |
| `abcClass` | `String` | ABC 分类 |
| `changeoverGroup` | `String` | 换型分组 |
| `status` | `Integer` | `0` 启用 / `1` 停用 |

### 5.2 BomApi

```java
public interface BomApi {

    BomRespDTO getBom(Long id);

    BomRespDTO getEffectiveBom(Long matId, LocalDate date);

    BomDetailRespDTO getBomDetail(Long id);

    List<BomLineRespDTO> getBomLineList(Long bomId);
}
```

生效规则：

1. 只返回启用状态。
2. `effectiveDate <= date <= expiryDate`。
3. 同一物料存在多个生效版本时，取 `effectiveDate` 最新的一条。
4. 无生效版本返回 `null`，不抛异常。

`BomLineRespDTO` 字段包含：`id`、`bomId`、`lineNo`、`matId`、`matCode`、`matName`、`qty`、`scrapRate`、`issueMethod`、`routeOpSeq`、`positionNo`、`effectiveDate`、`expiryDate`、`uomId`、`remark`。

### 5.3 RouteApi

```java
public interface RouteApi {

    RouteRespDTO getRoute(Long id);

    RouteRespDTO getEffectiveRoute(Long matId, LocalDate date);

    RouteDetailRespDTO getRouteDetail(Long id);

    List<RouteOpRespDTO> getRouteOpList(Long routeId);

    List<RouteOpMatRespDTO> getRouteOpMatList(Collection<Long> routeOpIds);

    List<RouteOpEsopRespDTO> getRouteOpEsopList(Collection<Long> routeOpIds);
}
```

要求：

1. `getRouteOpList` 按 `seq` 升序。
2. `getRouteOpMatList` 支持一次传入多个工序 ID。
3. `getRouteOpEsopList` 返回作业步骤和附件，附件 `fileUrl` 必须是预签名地址。
4. ESOP 预签名复用 `FileApi.presignGetUrl`，不返回原始对象地址。

### 5.4 ResourceApi

```java
public interface ResourceApi {

    ResourceRespDTO getResource(Long id);

    List<ResourceRespDTO> getResourceList(Collection<Long> ids);

    Map<Long, ResourceRespDTO> getResourceMap(Collection<Long> ids);

    List<ResourceCalendarRespDTO> getResourceCalendarList(Collection<Long> resIds,
                                                         LocalDate startDate,
                                                         LocalDate endDate);

    List<ResourceCapabilityRespDTO> getResourceCapabilityList(Collection<Long> opIds);

    List<ResChangeoverRespDTO> getChangeoverList(Collection<Long> resIds,
                                                 Collection<String> resGroups);
}
```

规则：

1. 日历查询区间为闭区间，返回结果按 `resId + workDate + shiftId` 排序。
2. 能力查询只返回启用记录，按 `opId + preferredFlag desc + resId` 排序。
3. 换型矩阵可按 `resId` 或 `resGroup` 查询，`resId = 0` 表示按资源组生效。

### 5.5 其他 API

以下 API 以批量查询为主，字段与现有 DO 对齐，但不暴露审计字段：

| API | DTO | 核心字段 |
| --- | --- | --- |
| `MatCategoryApi` | `MatCategoryRespDTO` | `id`、`parentId`、`code`、`name`、`status` |
| `UomApi` | `UomRespDTO`、`UomConvRespDTO` | 单位编号、编码、名称、换算系数 |
| `OrgApi` | `InvOrgRespDTO`、`PlantRespDTO`、`WorkshopRespDTO`、`WcRespDTO` | 组织编号、编码、名称、状态 |
| `PartnerApi` | `SupplierRespDTO`、`CustomerRespDTO` | 编号、编码、名称、状态 |
| `ProjectApi` | `ProjectRespDTO`、`ProjectTaskRespDTO` | 项目编号、编码、名称、状态 |
| `TeamApi` | `TeamRespDTO`、`TeamMemberRespDTO` | 班组、车间、成员、用户编号 |
| `SkillApi` | `SkillRespDTO`、`OpSkillReqRespDTO`、`MemberSkillRespDTO` | 技能、等级、有效期 |
| `ToolApi` | `ToolRespDTO`、`RouteOpToolRespDTO` | 工装、寿命、需求量、是否必备 |
| `DefectCodeApi` | `DefectCodeRespDTO` | 编码、名称、等级、父级、状态 |

## 6. 实现要点

1. API 实现类统一使用 `@Service` 和 `@Resource` 注入 Service。
2. 查询方法加 `@DataPermission(enable = false)`。
3. `getMap` 可在接口中提供 default 方法，参考 `AdminUserApi#getUserMap`。
4. DTO 转换优先使用 `BeanUtils.toBean`。
5. 所有批量方法内部一次 SQL 查询，不做循环单查。
6. `validateXxxList` 只校验存在且启用，不返回数据。
7. ESOP 附件返回预签名地址时，保留 `fileName`、`fileType`、`fileId`。
8. `getEffectiveBom` 与 `getEffectiveRoute` 的日期规则必须一致。

## 7. 测试计划

测试类放在：

```text
iboot-module-mdm/src/test/java/com/iboot/pf/module/mdm/api
```

必须覆盖：

| 用例 | 验证点 |
| --- | --- |
| 单查不存在 | 返回 `null` |
| 批量查空集合 | 返回空集合 |
| `getMap` | 键为 ID，缺失 ID 不产生键 |
| `validateXxxList` | 缺失或停用抛 `ErrorCode` |
| 生效 BOM / Route | 日期区间、状态、多版本取最新 |
| `getRouteOpMatList` | 一次查询多个工序 |
| ESOP 预签名 | 调用 `FileApi.presignGetUrl` |
| 资源能力 | 只返回启用记录 |

验证命令：

```bash
mvn -pl iboot-module-mdm -am test
```

## 8. 交付清单

- [ ] `api` 包与 `package-info.java`
- [ ] 13 个 API 接口全部落地
- [ ] DTO 无 DO / VO 泄漏
- [ ] 所有批量查询无 N+1
- [ ] ESOP 附件返回预签名地址
- [ ] 生效 BOM / Route 规则正确
- [ ] 单元测试通过
- [ ] 未新增 WMS / MES / APS 依赖

