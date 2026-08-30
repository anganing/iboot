package com.iboot.pf.module.aps.service.shortage;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ShortageDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.shortage.ShortageMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * APS-排程缺料清单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ShortageServiceImpl implements ShortageService {

    @Resource
    private ShortageMapper shortageMapper;

    @Override
    public Long createShortage(ShortageSaveReqVO createReqVO) {
        // 插入
        ShortageDO shortage = BeanUtils.toBean(createReqVO, ShortageDO.class);
        shortageMapper.insert(shortage);

        // 返回
        return shortage.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ShortageImportRespVO importShortageList(List<ShortageImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ShortageImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ShortageImportRespVO respVO = ShortageImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createShortage(BeanUtils.toBean(importItem, ShortageSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateShortage(ShortageSaveReqVO updateReqVO) {
        // 校验存在
        validateShortageExists(updateReqVO.getId());
        // 更新
        ShortageDO updateObj = BeanUtils.toBean(updateReqVO, ShortageDO.class);
        shortageMapper.updateById(updateObj);
    }

    @Override
    public void deleteShortage(Long id) {
        // 校验存在
        validateShortageExists(id);
        // 删除
        shortageMapper.deleteById(id);
    }

    @Override
        public void deleteShortageListByIds(List<Long> ids) {
        // 删除
        shortageMapper.deleteByIds(ids);
        }


    private void validateShortageExists(Long id) {
        if (shortageMapper.selectById(id) == null) {
            throw exception(SHORTAGE_NOT_EXISTS);
        }
    }

    @Override
    public ShortageRespVO getShortage(Long id) {
        return shortageMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ShortageRespVO> getShortagePage(ShortagePageReqVO pageReqVO) {
        return shortageMapper.selectPage(pageReqVO);
    }

}