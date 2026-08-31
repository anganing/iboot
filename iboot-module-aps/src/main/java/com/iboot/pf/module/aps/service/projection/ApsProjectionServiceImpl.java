package com.iboot.pf.module.aps.service.projection;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.projection.dto.InventoryProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.MaterialProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.OrderProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.RefreshProjectionReqDTO;
import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.ProjectionInvPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.ProjectionInvRespVO;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.ProjectionMatPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.ProjectionMatRespVO;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.ProjectionOrderPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.ProjectionOrderRespVO;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.ProjectionResourcePageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.ProjectionResourceRespVO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsMaterialProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsResourceProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsInventoryProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsMaterialProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsOrderProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsResourceProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.enums.ApsScenarioStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import com.iboot.pf.module.aps.service.projection.source.OrderProjectionSource;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.resource.ResourceApi;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceCalendarRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.PROJECTION_INV_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.PROJECTION_MAT_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.PROJECTION_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.PROJECTION_RESOURCE_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.PROJECTION_SOURCE_UNAVAILABLE;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_CLOSED;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_NOT_EXISTS;

/**
 * APS 数据投影服务实现类。
 *
 * <p>四张投影表都按“清空场景数据 -> 从对方模块门面重新拉取 -> 批量写入”的方式重建，
 * 期间只调用 mdm / wms / mes 的 Api 门面，不跨库 JOIN 对方模块的表。
 *
 * <p>刷新会写入场景的快照时间，作为“这份输入是什么时候冻结的”依据。
 *
 * @author iboot
 */
@Service
@Validated
public class ApsProjectionServiceImpl implements ApsProjectionService {

    @Resource
    private ApsScenarioMapper scenarioMapper;
    @Resource
    private ApsMaterialProjectionMapper materialProjectionMapper;
    @Resource
    private ApsInventoryProjectionMapper inventoryProjectionMapper;
    @Resource
    private ApsOrderProjectionMapper orderProjectionMapper;
    @Resource
    private ApsResourceProjectionMapper resourceProjectionMapper;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;
    @Resource
    private BomApi bomApi;
    @Resource
    private RouteApi routeApi;
    @Resource
    private ResourceApi resourceApi;
    @Resource
    private InventoryApi inventoryApi;
    @Resource
    private ObjectProvider<OrderProjectionSource> orderSourceProvider;
    @Resource
    private ApsRelationFiller relationFiller;
    @Resource
    private ApsCommandService commandService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshMaterialProjection(RefreshProjectionReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.REFRESH_MATERIAL_PROJECTION, reqDTO, () -> {
            requireOpenScenario(reqDTO.getScenarioId());
            LocalDateTime now = LocalDateTime.now();
            List<MaterialRespDTO> materials = materialApi.getMaterialList(reqDTO.getMaterialIds());
            Map<Long, UomRespDTO> uomMap = uomApi.getUomMap(collectBaseUomIds(materials));
            List<ApsMaterialProjectionDO> rows = new ArrayList<>(materials.size());
            for (MaterialRespDTO material : materials) {
                rows.add(toMaterialProjection(reqDTO.getScenarioId(), material, uomMap, now));
            }
            materialProjectionMapper.deletePhysicalByScenarioId(reqDTO.getScenarioId());
            if (CollUtil.isNotEmpty(rows)) {
                materialProjectionMapper.insertBatch(rows);
            }
            touchSnapshotTime(reqDTO.getScenarioId(), now);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshInventoryProjection(RefreshProjectionReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.REFRESH_INVENTORY_PROJECTION, reqDTO, () -> {
            ApsScenarioDO scenario = requireOpenScenario(reqDTO.getScenarioId());
            LocalDateTime now = LocalDateTime.now();
            InventoryQueryReqDTO query = new InventoryQueryReqDTO();
            query.setPlantId(scenario.getPlantId());
            // 待检与不合格库存也要投影：缺料汇总只认合格库存，但计划员需要看到全量库存分布
            query.setQualifiedOnly(false);
            List<ApsInventoryProjectionDO> rows = inventoryApi.getInventoryBalanceList(query).stream()
                    .map(balance -> toInventoryProjection(reqDTO.getScenarioId(), balance, now))
                    .toList();
            inventoryProjectionMapper.deletePhysicalByScenarioId(reqDTO.getScenarioId());
            if (CollUtil.isNotEmpty(rows)) {
                inventoryProjectionMapper.insertBatch(rows);
            }
            touchSnapshotTime(reqDTO.getScenarioId(), now);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshOrderProjection(RefreshProjectionReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.REFRESH_ORDER_PROJECTION, reqDTO, () -> {
            ApsScenarioDO scenario = requireOpenScenario(reqDTO.getScenarioId());
            OrderProjectionSource source = orderSourceProvider.getIfAvailable();
            if (source == null) {
                throw exception(PROJECTION_SOURCE_UNAVAILABLE);
            }
            List<ApsOrderProjectionDO> rows =
                    source.loadOrderProjection(reqDTO.getScenarioId(), scenario.getPlantId());
            orderProjectionMapper.deletePhysicalByScenarioId(reqDTO.getScenarioId());
            if (CollUtil.isNotEmpty(rows)) {
                orderProjectionMapper.insertBatch(rows);
            }
            touchSnapshotTime(reqDTO.getScenarioId(), LocalDateTime.now());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshResourceProjection(RefreshProjectionReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.REFRESH_RESOURCE_PROJECTION, reqDTO, () -> {
            ApsScenarioDO scenario = requireOpenScenario(reqDTO.getScenarioId());
            LocalDateTime now = LocalDateTime.now();
            List<ApsResourceProjectionDO> rows = buildResourceProjection(scenario, now);
            resourceProjectionMapper.deletePhysicalByScenarioId(reqDTO.getScenarioId());
            if (CollUtil.isNotEmpty(rows)) {
                resourceProjectionMapper.insertBatch(rows);
            }
            touchSnapshotTime(reqDTO.getScenarioId(), now);
        });
    }

    @Override
    public PageResult<ProjectionMatRespVO> getMaterialProjectionPage(ProjectionMatPageReqVO pageReqVO) {
        return materialProjectionMapper.selectPage(pageReqVO);
    }

    @Override
    public ProjectionMatRespVO getMaterialProjection(Long id) {
        ProjectionMatRespVO respVO = id == null ? null : materialProjectionMapper.selectJoinById(id);
        if (respVO == null) {
            throw exception(PROJECTION_MAT_NOT_EXISTS);
        }
        return respVO;
    }

    @Override
    public PageResult<ProjectionInvRespVO> getInventoryProjectionPage(ProjectionInvPageReqVO pageReqVO) {
        PageResult<ProjectionInvRespVO> pageResult = inventoryProjectionMapper.selectPage(pageReqVO);
        fillInventoryRelationNames(pageResult.getList());
        return pageResult;
    }

    @Override
    public ProjectionInvRespVO getInventoryProjection(Long id) {
        ProjectionInvRespVO respVO = id == null ? null : inventoryProjectionMapper.selectJoinById(id);
        if (respVO == null) {
            throw exception(PROJECTION_INV_NOT_EXISTS);
        }
        fillInventoryRelationNames(Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<ProjectionOrderRespVO> getOrderProjectionPage(ProjectionOrderPageReqVO pageReqVO) {
        PageResult<ProjectionOrderRespVO> pageResult = orderProjectionMapper.selectPage(pageReqVO);
        fillOrderRelationNames(pageResult.getList());
        return pageResult;
    }

    @Override
    public ProjectionOrderRespVO getOrderProjection(Long id) {
        ProjectionOrderRespVO respVO = id == null ? null : orderProjectionMapper.selectJoinById(id);
        if (respVO == null) {
            throw exception(PROJECTION_ORDER_NOT_EXISTS);
        }
        fillOrderRelationNames(Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<ProjectionResourceRespVO> getResourceProjectionPage(ProjectionResourcePageReqVO pageReqVO) {
        PageResult<ProjectionResourceRespVO> pageResult = resourceProjectionMapper.selectPage(pageReqVO);
        fillResourceRelationNames(pageResult.getList());
        return pageResult;
    }

    @Override
    public ProjectionResourceRespVO getResourceProjection(Long id) {
        ProjectionResourceRespVO respVO = id == null ? null : resourceProjectionMapper.selectJoinById(id);
        if (respVO == null) {
            throw exception(PROJECTION_RESOURCE_NOT_EXISTS);
        }
        fillResourceRelationNames(Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public List<ApsMaterialProjectionDO> getMaterialProjectionList(MaterialProjectionQueryReqDTO query) {
        return materialProjectionMapper.selectListByScenario(query.getScenarioId(), query.getSourceMatId(),
                query.getMatType());
    }

    @Override
    public List<ApsInventoryProjectionDO> getInventoryProjectionList(InventoryProjectionQueryReqDTO query) {
        return inventoryProjectionMapper.selectListByScenario(query.getScenarioId(), query.getMatId(),
                query.getPlantId(), query.getProjectId(), query.getQualityStatus());
    }

    @Override
    public List<ApsOrderProjectionDO> getOrderProjectionList(OrderProjectionQueryReqDTO query) {
        return orderProjectionMapper.selectListByScenario(query.getScenarioId(), query.getSourceType(),
                query.getProdOrderId(), query.getMatId(), query.getCurrentStatus());
    }

    /** 物料投影行：物料主档 + 基准单位编码 + 当前生效的 BOM 与工艺路线版本。 */
    private ApsMaterialProjectionDO toMaterialProjection(Long scenarioId, MaterialRespDTO material,
                                                         Map<Long, UomRespDTO> uomMap, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        BomRespDTO bom = bomApi.getEffectiveBom(material.getId(), today);
        RouteRespDTO route = routeApi.getEffectiveRoute(material.getId(), today);
        return ApsMaterialProjectionDO.builder()
                .scenarioId(scenarioId)
                .sourceMatId(material.getId())
                .matCode(material.getCode())
                .matName(material.getName())
                .matSpec(material.getSpec())
                .matType(material.getMatType())
                .baseUomId(material.getBaseUomId())
                .baseUomCode(Optional.ofNullable(uomMap.get(material.getBaseUomId()))
                        .map(UomRespDTO::getCode).orElse(""))
                .batchFlag(material.getBatchFlag())
                // 没有生效 BOM / 路线时落 0，表示“无”，不能留 NULL（库表非空）
                .bomId(bom == null ? ApsConstants.NONE_ID : bom.getId())
                .bomVersion(bom == null ? null : bom.getVersion())
                .routeId(route == null ? ApsConstants.NONE_ID : route.getId())
                .routeVersion(route == null ? null : route.getVersion())
                .sourceUpdateTime(now)
                .snapshotTime(now)
                .build();
    }

    /** 库存投影行：可选维度统一落 0，批次号缺失落空串，保证唯一索引与非空约束成立。 */
    private ApsInventoryProjectionDO toInventoryProjection(Long scenarioId, InventoryBalanceRespDTO balance,
                                                           LocalDateTime now) {
        return ApsInventoryProjectionDO.builder()
                .scenarioId(scenarioId)
                .sourceBalanceId(balance.getId())
                .invOrgId(orNone(balance.getInvOrgId()))
                .plantId(orNone(balance.getPlantId()))
                .whId(orNone(balance.getWhId()))
                .locId(orNone(balance.getLocId()))
                .matId(balance.getMatId())
                .matCode(balance.getMatCode())
                .batchId(orNone(balance.getBatchId()))
                .lotNo(Optional.ofNullable(balance.getLotNo()).orElse(""))
                .projectId(orNone(balance.getProjectId()))
                // 余额门面不返回项目任务维度，投影里统一落 0
                .projectTaskId(ApsConstants.NONE_ID)
                .stockScope(balance.getStockScope())
                .qualityStatus(balance.getQualityStatus())
                .qtyOnhand(balance.getQtyOnhand())
                .qtyReserved(balance.getQtyReserved())
                .qtyFrozen(balance.getQtyFrozen())
                .qtyAvailable(balance.getQtyAvailable())
                .sourceUpdateTime(now)
                .snapshotTime(now)
                .build();
    }

    /**
     * 资源投影行：工厂内启用资源 × 计划窗口内的日历。
     *
     * <p>日历按“资源 + 日期 + 班次”逐条落库，班次为 0 的记录由 mdm 侧的日汇总提供；
     * 负荷校验只读班次为 0 的那部分。
     */
    private List<ApsResourceProjectionDO> buildResourceProjection(ApsScenarioDO scenario, LocalDateTime now) {
        List<ResourceRespDTO> resources = resourceApi.getResourceListByPlantId(scenario.getPlantId());
        if (CollUtil.isEmpty(resources)) {
            return List.of();
        }
        Map<Long, ResourceRespDTO> resourceMap = resources.stream()
                .collect(java.util.stream.Collectors.toMap(ResourceRespDTO::getId, item -> item));
        LocalDate startDate = scenario.getPlanStartTime().toLocalDate();
        LocalDate endDate = scenario.getPlanEndTime().toLocalDate();
        List<ResourceCalendarRespDTO> calendars =
                resourceApi.getResourceCalendarList(resourceMap.keySet(), startDate, endDate);
        List<ApsResourceProjectionDO> rows = new ArrayList<>(calendars.size());
        for (ResourceCalendarRespDTO calendar : calendars) {
            ResourceRespDTO resource = resourceMap.get(calendar.getResId());
            if (resource == null) {
                continue;
            }
            rows.add(ApsResourceProjectionDO.builder()
                    .scenarioId(scenario.getId())
                    .sourceResId(resource.getId())
                    .resCode(resource.getCode())
                    .resName(resource.getName())
                    .resType(resource.getResType())
                    .plantId(orNone(resource.getPlantId()))
                    .workshopId(orNone(resource.getWorkshopId()))
                    .wcId(orNone(resource.getWcId()))
                    .workDate(calendar.getWorkDate())
                    .shiftId(orNone(calendar.getShiftId()))
                    .availableMinutes(calendar.getAvailableMinutes())
                    .sourceUpdateTime(now)
                    .snapshotTime(now)
                    .build());
        }
        return rows;
    }

    private Set<Long> collectBaseUomIds(List<MaterialRespDTO> materials) {
        Set<Long> uomIds = new LinkedHashSet<>();
        materials.forEach(item -> uomIds.add(item.getBaseUomId()));
        return uomIds;
    }

    /** 场景存在且未关闭：关闭的场景不再接受输入刷新。 */
    private ApsScenarioDO requireOpenScenario(Long scenarioId) {
        ApsScenarioDO scenario = scenarioId == null ? null : scenarioMapper.selectById(scenarioId);
        if (scenario == null) {
            throw exception(SCENARIO_NOT_EXISTS);
        }
        if (ApsScenarioStatusEnum.isClosed(scenario.getStatus())) {
            throw exception(SCENARIO_CLOSED);
        }
        return scenario;
    }

    /** 记录输入冻结时间，供“这版计划是基于什么时候的数据算的”追溯。 */
    private void touchSnapshotTime(Long scenarioId, LocalDateTime snapshotTime) {
        scenarioMapper.updateById(ApsScenarioDO.builder().id(scenarioId).snapshotTime(snapshotTime).build());
    }

    /** 可选外键统一落 0。 */
    private Long orNone(Long value) {
        return value == null ? ApsConstants.NONE_ID : value;
    }

    private void fillInventoryRelationNames(List<ProjectionInvRespVO> list) {
        relationFiller.fillPlantName(list, ProjectionInvRespVO::getPlantId, ProjectionInvRespVO::setPlantName);
        relationFiller.fillMatName(list, ProjectionInvRespVO::getMatId, ProjectionInvRespVO::setMatName);
        relationFiller.fillProjectName(list, ProjectionInvRespVO::getProjectId, ProjectionInvRespVO::setProjectName);
    }

    private void fillOrderRelationNames(List<ProjectionOrderRespVO> list) {
        relationFiller.fillPlantName(list, ProjectionOrderRespVO::getPlantId, ProjectionOrderRespVO::setPlantName);
        relationFiller.fillWorkshopName(list, ProjectionOrderRespVO::getWorkshopId,
                ProjectionOrderRespVO::setWorkshopName);
        relationFiller.fillProjectName(list, ProjectionOrderRespVO::getProjectId,
                ProjectionOrderRespVO::setProjectName);
        relationFiller.fillProjectTaskName(list, ProjectionOrderRespVO::getProjectTaskId,
                ProjectionOrderRespVO::setProjectTaskName);
        relationFiller.fillMatName(list, ProjectionOrderRespVO::getMatId, ProjectionOrderRespVO::setMatName);
        relationFiller.fillResName(list, ProjectionOrderRespVO::getResId, ProjectionOrderRespVO::setResName);
    }

    private void fillResourceRelationNames(List<ProjectionResourceRespVO> list) {
        relationFiller.fillPlantName(list, ProjectionResourceRespVO::getPlantId,
                ProjectionResourceRespVO::setPlantName);
        relationFiller.fillWorkshopName(list, ProjectionResourceRespVO::getWorkshopId,
                ProjectionResourceRespVO::setWorkshopName);
        relationFiller.fillWcName(list, ProjectionResourceRespVO::getWcId, ProjectionResourceRespVO::setWcName);
        relationFiller.fillShiftName(list, ProjectionResourceRespVO::getShiftId,
                ProjectionResourceRespVO::setShiftName);
    }

}
