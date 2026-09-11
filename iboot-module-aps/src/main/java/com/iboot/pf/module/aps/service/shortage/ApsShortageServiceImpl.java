package com.iboot.pf.module.aps.service.shortage;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.shortage.dto.RefreshShortageReqDTO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageHandleReqVO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortagePageReqVO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageRespVO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ApsShortageDO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsInventoryProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsOrderProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.shortage.ApsShortageMapper;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.enums.ApsShortageStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import com.iboot.pf.module.aps.service.projection.kit.ApsMaterialKitCalculator;
import com.iboot.pf.module.aps.service.projection.kit.ApsMaterialKitCalculator.MaterialRequirementAllocation;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SHORTAGE_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_NOT_EXISTS;

/**
 * APS-排程缺料清单 Service 实现类。
 *
 * <p>可用数量只认合格库存：待检、不合格、冻结的库存不能拿来兑需求，否则算出来的缺口是假的。
 *
 * @author iboot
 */
@Service
@Validated
public class ApsShortageServiceImpl implements ApsShortageService {

    @Resource
    private ApsShortageMapper shortageMapper;
    @Resource
    private ApsInventoryProjectionMapper inventoryProjectionMapper;
    @Resource
    private ApsOrderProjectionMapper orderProjectionMapper;
    @Resource
    private ApsTaskMapper taskMapper;
    @Resource
    private ApsVersionService versionService;
    @Resource
    private ApsRelationFiller relationFiller;
    @Resource
    private ApsCommandService commandService;
    @Resource
    private ApsMaterialKitCalculator materialKitCalculator;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;

    @Override
    public ShortageRespVO getShortage(Long id) {
        ShortageRespVO respVO = id == null ? null : shortageMapper.selectJoinById(id);
        if (respVO == null) {
            throw exception(SHORTAGE_NOT_EXISTS);
        }
        fillRelationNames(Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<ShortageRespVO> getShortagePage(ShortagePageReqVO pageReqVO) {
        PageResult<ShortageRespVO> pageResult = shortageMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    @Override
    public void handleShortage(ShortageHandleReqVO handleReqVO) {
        ApsShortageDO shortage = validateShortageExists(handleReqVO.getId());
        shortageMapper.updateById(ApsShortageDO.builder()
                .id(shortage.getId())
                .expectedSupplyDate(handleReqVO.getExpectedSupplyDate())
                .remark(handleReqVO.getRemark())
                .build());
    }

    @Override
    public void ignoreShortage(Long id) {
        ApsShortageDO shortage = validateShortageExists(id);
        shortageMapper.updateById(ApsShortageDO.builder()
                .id(shortage.getId())
                .status(ApsShortageStatusEnum.IGNORED.name())
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshShortage(RefreshShortageReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.REFRESH_SHORTAGE, reqDTO, () -> {
            ApsVersionDO version = versionService.getVersionDO(reqDTO.getVersionId());
            if (version == null) {
                throw exception(VERSION_NOT_EXISTS);
            }
            List<ApsTaskDO> versionTasks = taskMapper.selectListByVersionId(reqDTO.getVersionId());
            if (CollUtil.isEmpty(versionTasks)) {
                // 兼容历史版本：它们没有落 aps_task，只能按已经存在的缺料记录重算。
                refreshLegacyShortages(reqDTO.getVersionId(), version.getScenarioId());
                return;
            }
            rebuildShortages(reqDTO.getVersionId(), version.getScenarioId(), versionTasks);
        });
    }

    @Override
    public List<ApsShortageDO> getShortageDOListByVersionId(Long versionId) {
        return shortageMapper.selectListByVersionId(versionId);
    }

    /** 按物料汇总场景内的合格可用库存。 */
    private Map<Long, BigDecimal> sumAvailableByMat(Long scenarioId) {
        return inventoryProjectionMapper
                .selectListByScenarioIdAndQualityStatus(scenarioId, WmsQualityStatusEnum.QUALIFIED.name())
                .stream()
                .collect(Collectors.groupingBy(ApsInventoryProjectionDO::getMatId,
                        Collectors.reducing(ApsConstants.ZERO_QTY,
                                ApsInventoryProjectionDO::getQtyAvailable, BigDecimal::add)));
    }

    /** 历史缺料记录的兼容重算路径。 */
    private void refreshLegacyShortages(Long versionId, Long scenarioId) {
        Map<Long, BigDecimal> availableMap = sumAvailableByMat(scenarioId);
        List<ApsShortageDO> rows = shortageMapper.selectListByVersionId(versionId);
        if (CollUtil.isEmpty(rows)) {
            return;
        }
        rows.forEach(row -> recalculate(row, availableMap.getOrDefault(row.getMatId(), ApsConstants.ZERO_QTY)));
        shortageMapper.updateBatch(rows);
    }

    /**
     * 从版本任务对应的工序投影重建缺料清单。
     *
     * <p>版本任务限定本轮计划范围，物料需求与库存扣减完全委托给投影齐套计算器，避免两个缺料入口
     * 各自展开 BOM 后形成第二套口径。
     */
    private void rebuildShortages(Long versionId, Long scenarioId, List<ApsTaskDO> versionTasks) {
        Map<Long, ApsTaskDO> taskMap = new HashMap<>();
        for (ApsTaskDO task : versionTasks) {
            taskMap.put(task.getOpTaskId(), task);
        }
        List<ApsOrderProjectionDO> projectionRows = orderProjectionMapper
                .selectListByScenario(scenarioId, null, null, null, null).stream()
                .filter(row -> taskMap.containsKey(row.getOpTaskId())).toList();
        List<MaterialRequirementAllocation> allocations = materialKitCalculator
                .calculateRequirementAllocations(scenarioId, projectionRows);
        Map<ShortageKey, ApsShortageDO> existingRows = new HashMap<>();
        for (ApsShortageDO existing : shortageMapper.selectListByVersionId(versionId)) {
            existingRows.put(new ShortageKey(existing.getOpTaskId(), existing.getMatId()), existing);
        }
        List<ApsShortageDO> rows = buildShortageRows(versionId, taskMap, allocations, existingRows);
        shortageMapper.deletePhysicalByVersionId(versionId);
        if (CollUtil.isNotEmpty(rows)) {
            shortageMapper.insertBatch(rows);
        }
    }

    /** 只持久化真实短缺；零短缺无需在清单里占一条已解决记录。 */
    private List<ApsShortageDO> buildShortageRows(Long versionId, Map<Long, ApsTaskDO> taskMap,
                                                   List<MaterialRequirementAllocation> allocations,
                                                   Map<ShortageKey, ApsShortageDO> existingRows) {
        Set<Long> matIds = new HashSet<>();
        for (MaterialRequirementAllocation allocation : allocations) {
            if (allocation.shortQty().signum() > 0) {
                matIds.add(allocation.matId());
            }
        }
        if (matIds.isEmpty()) {
            return List.of();
        }
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(matIds);
        Set<Long> uomIds = new HashSet<>();
        materialMap.values().forEach(material -> {
            if (material.getBaseUomId() != null) {
                uomIds.add(material.getBaseUomId());
            }
        });
        Map<Long, UomRespDTO> uomMap = uomIds.isEmpty() ? Map.of() : uomApi.getUomMap(uomIds);
        List<ApsShortageDO> rows = new ArrayList<>();
        for (MaterialRequirementAllocation allocation : allocations) {
            if (allocation.shortQty().signum() <= 0) {
                continue;
            }
            ApsOrderProjectionDO order = allocation.order();
            ApsTaskDO task = taskMap.get(order.getOpTaskId());
            MaterialRespDTO material = materialMap.get(allocation.matId());
            ApsShortageDO existing = existingRows.get(new ShortageKey(order.getOpTaskId(), allocation.matId()));
            Long uomId = material == null || material.getBaseUomId() == null
                    ? ApsConstants.NONE_ID : material.getBaseUomId();
            UomRespDTO uom = uomMap.get(uomId);
            rows.add(ApsShortageDO.builder()
                    .versionId(versionId)
                    .prodOrderId(order.getProdOrderId())
                    .opTaskId(order.getOpTaskId())
                    .projectId(order.getProjectId())
                    .projectTaskId(order.getProjectTaskId())
                    .matId(allocation.matId())
                    .matCode(material == null ? "MAT-" + allocation.matId() : material.getCode())
                    .matName(material == null ? "" : material.getName())
                    .reqQty(allocation.reqQty())
                    .availQty(allocation.availQty())
                    .shortQty(allocation.shortQty())
                    .uomId(uomId)
                    .uomCode(uom == null ? "" : uom.getCode())
                    .needDate(task == null ? order.getDueDate() : task.getStartTime())
                    .expectedSupplyDate(existing == null ? null : existing.getExpectedSupplyDate())
                    .remark(existing == null ? null : existing.getRemark())
                    .status(existing != null && ApsShortageStatusEnum.IGNORED.name().equals(existing.getStatus())
                            ? ApsShortageStatusEnum.IGNORED.name()
                            : ApsShortageStatusEnum.of(allocation.shortQty(), allocation.availQty()).name())
                    .build());
        }
        return rows;
    }

    /**
     * 重算单条缺料。
     *
     * <p>已忽略的记录不再参与重算：人工判断过“这条不用管”，重算会把它拉回未解决。
     */
    private void recalculate(ApsShortageDO row, BigDecimal available) {
        if (ApsShortageStatusEnum.IGNORED.name().equals(row.getStatus())) {
            return;
        }
        BigDecimal shortage = row.getReqQty().subtract(available).max(ApsConstants.ZERO_QTY);
        row.setAvailQty(available);
        row.setShortQty(shortage);
        row.setStatus(ApsShortageStatusEnum.of(shortage, available).name());
    }

    private ApsShortageDO validateShortageExists(Long id) {
        ApsShortageDO shortage = id == null ? null : shortageMapper.selectById(id);
        if (shortage == null) {
            throw exception(SHORTAGE_NOT_EXISTS);
        }
        return shortage;
    }

    /**
     * 跨模块可读字段补全。
     *
     * <p>放在 Service 而非 Controller：Excel 导出复用这里的分页结果，写在 Controller 会丢字段。
     */
    private void fillRelationNames(List<ShortageRespVO> list) {
        relationFiller.fillProdOrderNo(list, ShortageRespVO::getProdOrderId, ShortageRespVO::setProdOrderNo);
        relationFiller.fillProjectName(list, ShortageRespVO::getProjectId, ShortageRespVO::setProjectName);
        relationFiller.fillProjectTaskName(list, ShortageRespVO::getProjectTaskId,
                ShortageRespVO::setProjectTaskName);
    }

    private record ShortageKey(Long opTaskId, Long matId) {
    }

}
