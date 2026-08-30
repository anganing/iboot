package com.iboot.pf.module.wms.service.zone;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.zone.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.zone.ZoneDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.zone.ZoneMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-库区 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ZoneServiceImpl implements ZoneService {

    @Resource
    private ZoneMapper zoneMapper;

    @Override
    public Long createZone(ZoneSaveReqVO createReqVO) {
        // 插入
        ZoneDO zone = BeanUtils.toBean(createReqVO, ZoneDO.class);
        zoneMapper.insert(zone);

        // 返回
        return zone.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ZoneImportRespVO importZoneList(List<ZoneImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ZoneImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ZoneImportRespVO respVO = ZoneImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createZone(BeanUtils.toBean(importItem, ZoneSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateZone(ZoneSaveReqVO updateReqVO) {
        // 校验存在
        validateZoneExists(updateReqVO.getId());
        // 更新
        ZoneDO updateObj = BeanUtils.toBean(updateReqVO, ZoneDO.class);
        zoneMapper.updateById(updateObj);
    }

    @Override
    public void deleteZone(Long id) {
        // 校验存在
        validateZoneExists(id);
        // 删除
        zoneMapper.deleteById(id);
    }

    @Override
        public void deleteZoneListByIds(List<Long> ids) {
        // 删除
        zoneMapper.deleteByIds(ids);
        }


    private void validateZoneExists(Long id) {
        if (zoneMapper.selectById(id) == null) {
            throw exception(ZONE_NOT_EXISTS);
        }
    }

    @Override
    public ZoneRespVO getZone(Long id) {
        return zoneMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ZoneRespVO> getZonePage(ZonePageReqVO pageReqVO) {
        return zoneMapper.selectPage(pageReqVO);
    }

}