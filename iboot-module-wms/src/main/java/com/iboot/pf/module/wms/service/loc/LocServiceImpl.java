package com.iboot.pf.module.wms.service.loc;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.loc.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.loc.LocDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.loc.LocMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-库位 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class LocServiceImpl implements LocService {

    @Resource
    private LocMapper locMapper;

    @Override
    public Long createLoc(LocSaveReqVO createReqVO) {
        // 插入
        LocDO loc = BeanUtils.toBean(createReqVO, LocDO.class);
        locMapper.insert(loc);

        // 返回
        return loc.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public LocImportRespVO importLocList(List<LocImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return LocImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        LocImportRespVO respVO = LocImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createLoc(BeanUtils.toBean(importItem, LocSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateLoc(LocSaveReqVO updateReqVO) {
        // 校验存在
        validateLocExists(updateReqVO.getId());
        // 更新
        LocDO updateObj = BeanUtils.toBean(updateReqVO, LocDO.class);
        locMapper.updateById(updateObj);
    }

    @Override
    public void deleteLoc(Long id) {
        // 校验存在
        validateLocExists(id);
        // 删除
        locMapper.deleteById(id);
    }

    @Override
        public void deleteLocListByIds(List<Long> ids) {
        // 删除
        locMapper.deleteByIds(ids);
        }


    private void validateLocExists(Long id) {
        if (locMapper.selectById(id) == null) {
            throw exception(LOC_NOT_EXISTS);
        }
    }

    @Override
    public LocRespVO getLoc(Long id) {
        return locMapper.selectJoinById(id);
    }

    @Override
    public PageResult<LocRespVO> getLocPage(LocPageReqVO pageReqVO) {
        return locMapper.selectPage(pageReqVO);
    }

}