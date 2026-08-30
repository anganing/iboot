package com.iboot.pf.module.aps.service.version;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.version.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.version.VersionDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.version.VersionMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * APS-排程版本 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class VersionServiceImpl implements VersionService {

    @Resource
    private VersionMapper versionMapper;

    @Override
    public Long createVersion(VersionSaveReqVO createReqVO) {
        // 插入
        VersionDO version = BeanUtils.toBean(createReqVO, VersionDO.class);
        versionMapper.insert(version);

        // 返回
        return version.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public VersionImportRespVO importVersionList(List<VersionImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return VersionImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        VersionImportRespVO respVO = VersionImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createVersion(BeanUtils.toBean(importItem, VersionSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateVersion(VersionSaveReqVO updateReqVO) {
        // 校验存在
        validateVersionExists(updateReqVO.getId());
        // 更新
        VersionDO updateObj = BeanUtils.toBean(updateReqVO, VersionDO.class);
        versionMapper.updateById(updateObj);
    }

    @Override
    public void deleteVersion(Long id) {
        // 校验存在
        validateVersionExists(id);
        // 删除
        versionMapper.deleteById(id);
    }

    @Override
        public void deleteVersionListByIds(List<Long> ids) {
        // 删除
        versionMapper.deleteByIds(ids);
        }


    private void validateVersionExists(Long id) {
        if (versionMapper.selectById(id) == null) {
            throw exception(VERSION_NOT_EXISTS);
        }
    }

    @Override
    public VersionRespVO getVersion(Long id) {
        return versionMapper.selectJoinById(id);
    }

    @Override
    public PageResult<VersionRespVO> getVersionPage(VersionPageReqVO pageReqVO) {
        return versionMapper.selectPage(pageReqVO);
    }

}