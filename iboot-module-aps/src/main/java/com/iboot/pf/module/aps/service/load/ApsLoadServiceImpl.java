package com.iboot.pf.module.aps.service.load;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.aps.controller.admin.load.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.load.ApsLoadDO;
import com.iboot.pf.module.aps.dal.mysql.load.ApsLoadMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.LOAD_NOT_EXISTS;

/**
 * 资源负荷 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ApsLoadServiceImpl implements ApsLoadService {

    @Resource
    private ApsLoadMapper loadMapper;

    @Override
    public Long createLoad(LoadSaveReqVO createReqVO) {
        // 插入
        ApsLoadDO load = BeanUtils.toBean(createReqVO, ApsLoadDO.class);
        loadMapper.insert(load);

        // 返回
        return load.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public LoadImportRespVO importLoadList(List<LoadImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return LoadImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        LoadImportRespVO respVO = LoadImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createLoad(BeanUtils.toBean(importItem, LoadSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateLoad(LoadSaveReqVO updateReqVO) {
        // 校验存在
        validateLoadExists(updateReqVO.getId());
        // 更新
        ApsLoadDO updateObj = BeanUtils.toBean(updateReqVO, ApsLoadDO.class);
        loadMapper.updateById(updateObj);
    }

    @Override
    public void deleteLoad(Long id) {
        // 校验存在
        validateLoadExists(id);
        // 删除
        loadMapper.deleteById(id);
    }

    @Override
        public void deleteLoadListByIds(List<Long> ids) {
        // 删除
        loadMapper.deleteByIds(ids);
        }


    private void validateLoadExists(Long id) {
        if (loadMapper.selectById(id) == null) {
            throw exception(LOAD_NOT_EXISTS);
        }
    }

    @Override
    public LoadRespVO getLoad(Long id) {
        return loadMapper.selectJoinById(id);
    }

    @Override
    public PageResult<LoadRespVO> getLoadPage(LoadPageReqVO pageReqVO) {
        return loadMapper.selectPage(pageReqVO);
    }

    @Override
    public List<LoadRespVO> getLoadListByVersionId(Long versionId) {
        return loadMapper.selectListByVersionId(versionId);
    }

    @Override
    public void refreshLoad(com.iboot.pf.module.aps.api.load.dto.RefreshLoadReqDTO r) {
        // TODO: 实现刷新负荷逻辑
    }

}
