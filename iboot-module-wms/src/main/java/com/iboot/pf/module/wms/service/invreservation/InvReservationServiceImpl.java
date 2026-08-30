package com.iboot.pf.module.wms.service.invreservation;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.invreservation.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invreservation.InvReservationDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.invreservation.InvReservationMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
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
    private InvReservationMapper invReservationMapper;

    @Override
    public Long createInvReservation(InvReservationSaveReqVO createReqVO) {
        // 插入
        InvReservationDO invReservation = BeanUtils.toBean(createReqVO, InvReservationDO.class);
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
        InvReservationDO updateObj = BeanUtils.toBean(updateReqVO, InvReservationDO.class);
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
        return invReservationMapper.selectJoinById(id);
    }

    @Override
    public PageResult<InvReservationRespVO> getInvReservationPage(InvReservationPageReqVO pageReqVO) {
        return invReservationMapper.selectPage(pageReqVO);
    }

}