package com.iboot.pf.module.mes.service.rgvtask;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.rgvtask.RgvTaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.rgvtask.RgvTaskMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-RGV工位流转任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class RgvTaskServiceImpl implements RgvTaskService {

    @Resource
    private RgvTaskMapper rgvTaskMapper;

    @Override
    public Long createRgvTask(RgvTaskSaveReqVO createReqVO) {
        // 插入
        RgvTaskDO rgvTask = BeanUtils.toBean(createReqVO, RgvTaskDO.class);
        rgvTaskMapper.insert(rgvTask);

        // 返回
        return rgvTask.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public RgvTaskImportRespVO importRgvTaskList(List<RgvTaskImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return RgvTaskImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        RgvTaskImportRespVO respVO = RgvTaskImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createRgvTask(BeanUtils.toBean(importItem, RgvTaskSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateRgvTask(RgvTaskSaveReqVO updateReqVO) {
        // 校验存在
        validateRgvTaskExists(updateReqVO.getId());
        // 更新
        RgvTaskDO updateObj = BeanUtils.toBean(updateReqVO, RgvTaskDO.class);
        rgvTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteRgvTask(Long id) {
        // 校验存在
        validateRgvTaskExists(id);
        // 删除
        rgvTaskMapper.deleteById(id);
    }

    @Override
        public void deleteRgvTaskListByIds(List<Long> ids) {
        // 删除
        rgvTaskMapper.deleteByIds(ids);
        }


    private void validateRgvTaskExists(Long id) {
        if (rgvTaskMapper.selectById(id) == null) {
            throw exception(RGV_TASK_NOT_EXISTS);
        }
    }

    @Override
    public RgvTaskRespVO getRgvTask(Long id) {
        return rgvTaskMapper.selectJoinById(id);
    }

    @Override
    public PageResult<RgvTaskRespVO> getRgvTaskPage(RgvTaskPageReqVO pageReqVO) {
        return rgvTaskMapper.selectPage(pageReqVO);
    }

}