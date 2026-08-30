package com.iboot.pf.module.aps.service.projection;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iboot.pf.module.aps.api.projection.dto.*;
import com.iboot.pf.module.aps.dal.dataobject.projection.*;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.mysql.projection.*;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import com.iboot.pf.module.aps.service.projection.source.OrderProjectionSource;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

@Service
public class ApsProjectionServiceImpl implements ApsProjectionService {
    @Resource private ApsScenarioMapper apsScenarioMapper;
    @Resource private ApsMaterialProjectionMapper apsMaterialProjectionMapper;
    @Resource private ApsInventoryProjectionMapper apsInventoryProjectionMapper;
    @Resource private ApsOrderProjectionMapper apsOrderProjectionMapper;
    @Resource private MaterialApi materialApi;
    @Resource private UomApi uomApi;
    @Resource private InventoryApi inventoryApi;
    @Resource private ObjectProvider<OrderProjectionSource> orderSourceProvider;
    @Resource private ApsCommandService commandService;

    @Override @Transactional(rollbackFor = Exception.class)
    public void refreshMaterialProjection(RefreshProjectionReqDTO reqDTO) {
        commandService.executeVoid("REFRESH_MATERIAL_PROJECTION", reqDTO, () -> {
            requireScenario(reqDTO.getScenarioId());
            List<MaterialRespDTO> materials = materialApi.getMaterialList(reqDTO.getMaterialIds());
            Set<Long> uomIds = new HashSet<>(); materials.forEach(item -> uomIds.add(item.getBaseUomId()));
            Map<Long, UomRespDTO> uomMap = uomApi.getUomMap(uomIds);
            LocalDateTime now = LocalDateTime.now();
            List<ApsMaterialProjectionDO> rows = materials.stream().map(item -> ApsMaterialProjectionDO.builder()
                    .scenarioId(reqDTO.getScenarioId()).sourceMatId(item.getId()).matCode(item.getCode())
                    .matName(item.getName()).matSpec(item.getSpec()).matType(item.getMatType()).baseUomId(item.getBaseUomId())
                    .baseUomCode(Optional.ofNullable(uomMap.get(item.getBaseUomId())).map(UomRespDTO::getCode).orElse(""))
                    .batchFlag(item.getBatchFlag()).bomId(0L).routeId(0L).sourceUpdateTime(now).snapshotTime(now).build()).toList();
            apsMaterialProjectionMapper.deletePhysicalByScenarioId(reqDTO.getScenarioId());
            if (CollUtil.isNotEmpty(rows)) apsMaterialProjectionMapper.insertBatch(rows);
        });
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void refreshInventoryProjection(RefreshProjectionReqDTO reqDTO) {
        commandService.executeVoid("REFRESH_INVENTORY_PROJECTION", reqDTO, () -> {
            ApsScenarioDO scenario = requireScenario(reqDTO.getScenarioId());
            InventoryQueryReqDTO query = new InventoryQueryReqDTO(); query.setPlantId(scenario.getPlantId()); query.setQualifiedOnly(false);
            LocalDateTime now = LocalDateTime.now();
            List<ApsInventoryProjectionDO> rows = inventoryApi.getInventoryBalanceList(query).stream().map(item ->
                    ApsInventoryProjectionDO.builder().scenarioId(reqDTO.getScenarioId()).sourceBalanceId(item.getId())
                            .invOrgId(zero(item.getInvOrgId())).plantId(zero(item.getPlantId())).whId(zero(item.getWhId()))
                            .locId(zero(item.getLocId())).matId(item.getMatId()).matCode(item.getMatCode())
                            .batchId(zero(item.getBatchId())).lotNo(Optional.ofNullable(item.getLotNo()).orElse(""))
                            .projectId(zero(item.getProjectId())).projectTaskId(0L).stockScope(item.getStockScope())
                            .qualityStatus(item.getQualityStatus()).qtyOnhand(item.getQtyOnhand()).qtyReserved(item.getQtyReserved())
                            .qtyFrozen(item.getQtyFrozen()).qtyAvailable(item.getQtyAvailable()).sourceUpdateTime(now).snapshotTime(now).build()).toList();
            apsInventoryProjectionMapper.deletePhysicalByScenarioId(reqDTO.getScenarioId());
            if (CollUtil.isNotEmpty(rows)) apsInventoryProjectionMapper.insertBatch(rows);
        });
    }
    @Override @Transactional(rollbackFor = Exception.class)
    public void refreshOrderProjection(RefreshProjectionReqDTO reqDTO) {
        commandService.executeVoid("REFRESH_ORDER_PROJECTION", reqDTO, () -> {
            ApsScenarioDO scenario = requireScenario(reqDTO.getScenarioId());
            OrderProjectionSource source = orderSourceProvider.getIfAvailable();
            if (source == null) throw exception(PROJECTION_SOURCE_UNAVAILABLE);
            List<ApsOrderProjectionDO> rows = source.loadOrderProjection(reqDTO.getScenarioId(), scenario.getPlantId());
            apsOrderProjectionMapper.deletePhysicalByScenarioId(reqDTO.getScenarioId());
            if (CollUtil.isNotEmpty(rows)) apsOrderProjectionMapper.insertBatch(rows);
        });
    }
    @Override public List<ApsMaterialProjectionDO> getMaterialProjectionList(MaterialProjectionQueryReqDTO q) {
        return apsMaterialProjectionMapper.selectList(new LambdaQueryWrapper<ApsMaterialProjectionDO>().eq(ApsMaterialProjectionDO::getScenarioId, q.getScenarioId())
                .eq(q.getSourceMatId() != null, ApsMaterialProjectionDO::getSourceMatId, q.getSourceMatId())
                .eq(q.getMatType() != null, ApsMaterialProjectionDO::getMatType, q.getMatType()));
    }
    @Override public List<ApsInventoryProjectionDO> getInventoryProjectionList(InventoryProjectionQueryReqDTO q) {
        return apsInventoryProjectionMapper.selectList(new LambdaQueryWrapper<ApsInventoryProjectionDO>().eq(ApsInventoryProjectionDO::getScenarioId, q.getScenarioId())
                .eq(q.getMatId() != null, ApsInventoryProjectionDO::getMatId, q.getMatId()).eq(q.getPlantId() != null, ApsInventoryProjectionDO::getPlantId, q.getPlantId())
                .eq(q.getProjectId() != null, ApsInventoryProjectionDO::getProjectId, q.getProjectId()).eq(q.getQualityStatus() != null, ApsInventoryProjectionDO::getQualityStatus, q.getQualityStatus()));
    }
    @Override public List<ApsOrderProjectionDO> getOrderProjectionList(OrderProjectionQueryReqDTO q) {
        return apsOrderProjectionMapper.selectList(new LambdaQueryWrapper<ApsOrderProjectionDO>().eq(ApsOrderProjectionDO::getScenarioId, q.getScenarioId())
                .eq(q.getSourceType() != null, ApsOrderProjectionDO::getSourceType, q.getSourceType()).eq(q.getProdOrderId() != null, ApsOrderProjectionDO::getProdOrderId, q.getProdOrderId())
                .eq(q.getMatId() != null, ApsOrderProjectionDO::getMatId, q.getMatId()).eq(q.getCurrentStatus() != null, ApsOrderProjectionDO::getCurrentStatus, q.getCurrentStatus()));
    }
    private ApsScenarioDO requireScenario(Long id) { ApsScenarioDO v = apsScenarioMapper.selectById(id); if (v == null) throw exception(SCENARIO_NOT_EXISTS); return v; }
    private Long zero(Long value) { return value == null ? 0L : value; }
}
