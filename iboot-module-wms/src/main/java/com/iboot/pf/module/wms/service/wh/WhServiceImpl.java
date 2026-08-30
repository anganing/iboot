package com.iboot.pf.module.wms.service.wh;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.wh.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.wh.WhDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.wh.WhMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-仓库 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class WhServiceImpl implements WhService {

    @Resource
    private WhMapper whMapper;

    @Override
    public Long createWh(WhSaveReqVO createReqVO) {
        // 插入
        WhDO wh = BeanUtils.toBean(createReqVO, WhDO.class);
        whMapper.insert(wh);

        // 返回
        return wh.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public WhImportRespVO importWhList(List<WhImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return WhImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        WhImportRespVO respVO = WhImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createWh(BeanUtils.toBean(importItem, WhSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateWh(WhSaveReqVO updateReqVO) {
        // 校验存在
        validateWhExists(updateReqVO.getId());
        // 更新
        WhDO updateObj = BeanUtils.toBean(updateReqVO, WhDO.class);
        whMapper.updateById(updateObj);
    }

    @Override
    public void deleteWh(Long id) {
        // 校验存在
        validateWhExists(id);
        // 删除
        whMapper.deleteById(id);
    }

    @Override
        public void deleteWhListByIds(List<Long> ids) {
        // 删除
        whMapper.deleteByIds(ids);
        }


    private void validateWhExists(Long id) {
        if (whMapper.selectById(id) == null) {
            throw exception(WH_NOT_EXISTS);
        }
    }

    @Override
    public WhRespVO getWh(Long id) {
        return whMapper.selectJoinById(id);
    }

    @Override
    public PageResult<WhRespVO> getWhPage(WhPageReqVO pageReqVO) {
        return whMapper.selectPage(pageReqVO);
    }

}