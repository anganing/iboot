package com.iboot.pf.module.mes.service.qcitem;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-检验项目 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class QcItemServiceImpl implements QcItemService {

    @Resource
    private QcItemMapper qcItemMapper;

    @Override
    public Long createQcItem(QcItemSaveReqVO createReqVO) {
        // 插入
        QcItemDO qcItem = BeanUtils.toBean(createReqVO, QcItemDO.class);
        qcItemMapper.insert(qcItem);

        // 返回
        return qcItem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public QcItemImportRespVO importQcItemList(List<QcItemImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return QcItemImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        QcItemImportRespVO respVO = QcItemImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createQcItem(BeanUtils.toBean(importItem, QcItemSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateQcItem(QcItemSaveReqVO updateReqVO) {
        // 校验存在
        validateQcItemExists(updateReqVO.getId());
        // 更新
        QcItemDO updateObj = BeanUtils.toBean(updateReqVO, QcItemDO.class);
        qcItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteQcItem(Long id) {
        // 校验存在
        validateQcItemExists(id);
        // 删除
        qcItemMapper.deleteById(id);
    }

    @Override
        public void deleteQcItemListByIds(List<Long> ids) {
        // 删除
        qcItemMapper.deleteByIds(ids);
        }


    private void validateQcItemExists(Long id) {
        if (qcItemMapper.selectById(id) == null) {
            throw exception(QC_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public QcItemRespVO getQcItem(Long id) {
        return qcItemMapper.selectJoinById(id);
    }

    @Override
    public PageResult<QcItemRespVO> getQcItemPage(QcItemPageReqVO pageReqVO) {
        return qcItemMapper.selectPage(pageReqVO);
    }

}