package com.iboot.pf.module.aps.service.shortage;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.shortage.dto.RefreshShortageReqDTO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageHandleReqVO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortagePageReqVO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageRespVO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ApsShortageDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsInventoryProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.shortage.ApsShortageMapper;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.enums.ApsShortageStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private ApsVersionService versionService;
    @Resource
    private ApsRelationFiller relationFiller;
    @Resource
    private ApsCommandService commandService;

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
            Map<Long, BigDecimal> availableMap = sumAvailableByMat(version.getScenarioId());
            List<ApsShortageDO> rows = shortageMapper.selectListByVersionId(reqDTO.getVersionId());
            if (CollUtil.isEmpty(rows)) {
                return;
            }
            rows.forEach(row -> recalculate(row, availableMap.getOrDefault(row.getMatId(), ApsConstants.ZERO_QTY)));
            shortageMapper.updateBatch(rows);
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

}
