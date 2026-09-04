package com.iboot.pf.module.wms.service.invreservation;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.invreservation.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.reservation.WmsInvReservationDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import com.iboot.pf.module.wms.dal.mysql.invreservation.InvReservationMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertSet;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.framework.common.util.collection.MapUtils.findAndThen;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-库存预留 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class InvReservationServiceImpl implements InvReservationService {

    @Resource
    private WmsCodeService codeService;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private InvReservationMapper invReservationMapper;

    @Override
    public Long createInvReservation(InvReservationSaveReqVO createReqVO) {
        // 插入
        WmsInvReservationDO invReservation = BeanUtils.toBean(createReqVO, WmsInvReservationDO.class);
        invReservation.setId(null);
        invReservation.setReservationNo(codeService.generate(WmsCodeRuleConstants.RESERVATION, invReservation.getReservationNo()));
        validateInvReservationUnique(invReservation, null);
        invReservationMapper.insert(invReservation);

        // 返回
        return invReservation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public InvReservationImportRespVO importInvReservationList(List<InvReservationImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return InvReservationImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        InvReservationImportRespVO respVO = InvReservationImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createInvReservation(BeanUtils.toBean(importItem, InvReservationSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateInvReservation(InvReservationSaveReqVO updateReqVO) {
        // 校验存在
        validateInvReservationExists(updateReqVO.getId());
        // 更新
        WmsInvReservationDO updateObj = BeanUtils.toBean(updateReqVO, WmsInvReservationDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateInvReservationUnique(updateObj, updateReqVO.getId());
        invReservationMapper.updateById(updateObj);
    }

    @Override
    public void deleteInvReservation(Long id) {
        // 校验存在
        validateInvReservationExists(id);
        // 删除
        invReservationMapper.deleteById(id);
    }

    @Override
        public void deleteInvReservationListByIds(List<Long> ids) {
        // 删除
        invReservationMapper.deleteByIds(ids);
        }


    private void validateInvReservationExists(Long id) {
        if (invReservationMapper.selectById(id) == null) {
            throw exception(INV_RESERVATION_NOT_EXISTS);
        }
    }

    @Override
    public InvReservationRespVO getInvReservation(Long id) {
        InvReservationRespVO reservation = invReservationMapper.selectJoinById(id);
        fillMaterialName(reservation == null ? Collections.emptyList() : Collections.singletonList(reservation));
        return reservation;
    }

    @Override
    public PageResult<InvReservationRespVO> getInvReservationPage(InvReservationPageReqVO pageReqVO) {
        PageResult<InvReservationRespVO> pageResult = invReservationMapper.selectPage(pageReqVO);
        fillMaterialName(pageResult.getList());
        return pageResult;
    }

    /**
     * 批量补全物料编码与名称。物料属于 mdm，只能走门面批量取，不能在 SQL 里跨库 JOIN；
     * 补全放在 Service 而不是 Controller，Excel 导出复用同一份结果才不会丢字段。
     */
    private void fillMaterialName(List<InvReservationRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<Long, MaterialRespDTO> materialMap =
                materialApi.getMaterialMap(convertSet(list, InvReservationRespVO::getMatId));
        list.forEach(vo -> findAndThen(materialMap, vo.getMatId(), material -> {
            vo.setMatCode(material.getCode());
            vo.setMatName(material.getName());
        }));
    }


    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateInvReservationUnique(WmsInvReservationDO invReservation, Long excludeId) {
        WmsInvReservationDO existed = invReservationMapper.selectByReservationNo(invReservation.getReservationNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(INV_RESERVATION_NO_DUPLICATE);
    }

}
