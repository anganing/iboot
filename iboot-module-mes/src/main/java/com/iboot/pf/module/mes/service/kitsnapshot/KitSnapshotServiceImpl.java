package com.iboot.pf.module.mes.service.kitsnapshot;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.kitsnapshot.KitSnapshotMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-齐套检查快照 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class KitSnapshotServiceImpl implements KitSnapshotService {

    @Resource
    private KitSnapshotMapper kitSnapshotMapper;

    @Override
    public Long createKitSnapshot(KitSnapshotSaveReqVO createReqVO) {
        // 插入
        KitSnapshotDO kitSnapshot = BeanUtils.toBean(createReqVO, KitSnapshotDO.class);
        kitSnapshotMapper.insert(kitSnapshot);

        // 返回
        return kitSnapshot.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public KitSnapshotImportRespVO importKitSnapshotList(List<KitSnapshotImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return KitSnapshotImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        KitSnapshotImportRespVO respVO = KitSnapshotImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createKitSnapshot(BeanUtils.toBean(importItem, KitSnapshotSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateKitSnapshot(KitSnapshotSaveReqVO updateReqVO) {
        // 校验存在
        validateKitSnapshotExists(updateReqVO.getId());
        // 更新
        KitSnapshotDO updateObj = BeanUtils.toBean(updateReqVO, KitSnapshotDO.class);
        kitSnapshotMapper.updateById(updateObj);
    }

    @Override
    public void deleteKitSnapshot(Long id) {
        // 校验存在
        validateKitSnapshotExists(id);
        // 删除
        kitSnapshotMapper.deleteById(id);
    }

    @Override
        public void deleteKitSnapshotListByIds(List<Long> ids) {
        // 删除
        kitSnapshotMapper.deleteByIds(ids);
        }


    private void validateKitSnapshotExists(Long id) {
        if (kitSnapshotMapper.selectById(id) == null) {
            throw exception(KIT_SNAPSHOT_NOT_EXISTS);
        }
    }

    @Override
    public KitSnapshotRespVO getKitSnapshot(Long id) {
        return kitSnapshotMapper.selectJoinById(id);
    }

    @Override
    public PageResult<KitSnapshotRespVO> getKitSnapshotPage(KitSnapshotPageReqVO pageReqVO) {
        return kitSnapshotMapper.selectPage(pageReqVO);
    }

}