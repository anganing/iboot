package com.iboot.pf.module.mes.service.opskiprecord;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.opskiprecord.OpSkipRecordDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.opskiprecord.OpSkipRecordMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-工序跳过记录 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class OpSkipRecordServiceImpl implements OpSkipRecordService {

    @Resource
    private OpSkipRecordMapper opSkipRecordMapper;

    @Override
    public Long createOpSkipRecord(OpSkipRecordSaveReqVO createReqVO) {
        // 插入
        OpSkipRecordDO opSkipRecord = BeanUtils.toBean(createReqVO, OpSkipRecordDO.class);
        opSkipRecordMapper.insert(opSkipRecord);

        // 返回
        return opSkipRecord.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public OpSkipRecordImportRespVO importOpSkipRecordList(List<OpSkipRecordImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return OpSkipRecordImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        OpSkipRecordImportRespVO respVO = OpSkipRecordImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createOpSkipRecord(BeanUtils.toBean(importItem, OpSkipRecordSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateOpSkipRecord(OpSkipRecordSaveReqVO updateReqVO) {
        // 校验存在
        validateOpSkipRecordExists(updateReqVO.getId());
        // 更新
        OpSkipRecordDO updateObj = BeanUtils.toBean(updateReqVO, OpSkipRecordDO.class);
        opSkipRecordMapper.updateById(updateObj);
    }

    @Override
    public void deleteOpSkipRecord(Long id) {
        // 校验存在
        validateOpSkipRecordExists(id);
        // 删除
        opSkipRecordMapper.deleteById(id);
    }

    @Override
        public void deleteOpSkipRecordListByIds(List<Long> ids) {
        // 删除
        opSkipRecordMapper.deleteByIds(ids);
        }


    private void validateOpSkipRecordExists(Long id) {
        if (opSkipRecordMapper.selectById(id) == null) {
            throw exception(OP_SKIP_RECORD_NOT_EXISTS);
        }
    }

    @Override
    public OpSkipRecordRespVO getOpSkipRecord(Long id) {
        return opSkipRecordMapper.selectJoinById(id);
    }

    @Override
    public PageResult<OpSkipRecordRespVO> getOpSkipRecordPage(OpSkipRecordPageReqVO pageReqVO) {
        return opSkipRecordMapper.selectPage(pageReqVO);
    }

}