package com.iboot.pf.module.wms.service.containermgr;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.containermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.containermgr.ContainerDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import com.iboot.pf.module.wms.dal.mysql.containermgr.ContainerMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-容器主档 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ContainerServiceImpl implements ContainerService {

    @Resource
    private WmsCodeService codeService;
    @Resource
    private ContainerMapper containerMapper;

    @Override
    public Long createContainer(ContainerSaveReqVO createReqVO) {
        // 插入
        ContainerDO container = BeanUtils.toBean(createReqVO, ContainerDO.class);
        container.setId(null);
        container.setContainerNo(codeService.generate(WmsCodeRuleConstants.CONTAINER, container.getContainerNo()));
        validateContainerUnique(container, null);
        containerMapper.insert(container);

        // 返回
        return container.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ContainerImportRespVO importContainerList(List<ContainerImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ContainerImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ContainerImportRespVO respVO = ContainerImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createContainer(BeanUtils.toBean(importItem, ContainerSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateContainer(ContainerSaveReqVO updateReqVO) {
        // 校验存在
        validateContainerExists(updateReqVO.getId());
        // 更新
        ContainerDO updateObj = BeanUtils.toBean(updateReqVO, ContainerDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateContainerUnique(updateObj, updateReqVO.getId());
        containerMapper.updateById(updateObj);
    }

    @Override
    public void deleteContainer(Long id) {
        // 校验存在
        validateContainerExists(id);
        // 删除
        containerMapper.deleteById(id);
    }

    @Override
        public void deleteContainerListByIds(List<Long> ids) {
        // 删除
        containerMapper.deleteByIds(ids);
        }


    private void validateContainerExists(Long id) {
        if (containerMapper.selectById(id) == null) {
            throw exception(CONTAINER_NOT_EXISTS);
        }
    }

    @Override
    public ContainerRespVO getContainer(Long id) {
        return containerMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ContainerRespVO> getContainerPage(ContainerPageReqVO pageReqVO) {
        return containerMapper.selectPage(pageReqVO);
    }


    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateContainerUnique(ContainerDO container, Long excludeId) {
        ContainerDO existed = containerMapper.selectByContainerNo(container.getContainerNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(CONTAINER_NO_DUPLICATE);
    }

}
