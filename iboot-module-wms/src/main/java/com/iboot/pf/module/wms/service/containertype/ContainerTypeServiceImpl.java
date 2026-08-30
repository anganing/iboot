package com.iboot.pf.module.wms.service.containertype;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.containertype.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.containertype.ContainerTypeDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.containertype.ContainerTypeMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-容器类型 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ContainerTypeServiceImpl implements ContainerTypeService {

    @Resource
    private ContainerTypeMapper containerTypeMapper;

    @Override
    public Long createContainerType(ContainerTypeSaveReqVO createReqVO) {
        // 插入
        ContainerTypeDO containerType = BeanUtils.toBean(createReqVO, ContainerTypeDO.class);
        containerTypeMapper.insert(containerType);

        // 返回
        return containerType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ContainerTypeImportRespVO importContainerTypeList(List<ContainerTypeImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ContainerTypeImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ContainerTypeImportRespVO respVO = ContainerTypeImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createContainerType(BeanUtils.toBean(importItem, ContainerTypeSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateContainerType(ContainerTypeSaveReqVO updateReqVO) {
        // 校验存在
        validateContainerTypeExists(updateReqVO.getId());
        // 更新
        ContainerTypeDO updateObj = BeanUtils.toBean(updateReqVO, ContainerTypeDO.class);
        containerTypeMapper.updateById(updateObj);
    }

    @Override
    public void deleteContainerType(Long id) {
        // 校验存在
        validateContainerTypeExists(id);
        // 删除
        containerTypeMapper.deleteById(id);
    }

    @Override
        public void deleteContainerTypeListByIds(List<Long> ids) {
        // 删除
        containerTypeMapper.deleteByIds(ids);
        }


    private void validateContainerTypeExists(Long id) {
        if (containerTypeMapper.selectById(id) == null) {
            throw exception(CONTAINER_TYPE_NOT_EXISTS);
        }
    }

    @Override
    public ContainerTypeRespVO getContainerType(Long id) {
        return containerTypeMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ContainerTypeRespVO> getContainerTypePage(ContainerTypePageReqVO pageReqVO) {
        return containerTypeMapper.selectPage(pageReqVO);
    }

}