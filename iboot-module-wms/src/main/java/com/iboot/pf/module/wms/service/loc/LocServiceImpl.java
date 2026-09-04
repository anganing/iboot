package com.iboot.pf.module.wms.service.loc;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.loc.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import com.iboot.pf.module.wms.dal.mysql.warehouse.WmsLocMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-库位 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class LocServiceImpl implements LocService {

    @Resource
    private WmsCodeService codeService;
    @Resource
    private WmsLocMapper locMapper;

    @Override
    public Long createLoc(LocSaveReqVO createReqVO) {
        // 插入
        WmsLocDO loc = BeanUtils.toBean(createReqVO, WmsLocDO.class);
        loc.setId(null);
        loc.setCode(codeService.generate(WmsCodeRuleConstants.LOC, loc.getCode()));
        validateLocUnique(loc, null);
        locMapper.insert(loc);

        // 返回
        return loc.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public LocImportRespVO importLocList(List<LocImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return LocImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        LocImportRespVO respVO = LocImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createLoc(BeanUtils.toBean(importItem, LocSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateLoc(LocSaveReqVO updateReqVO) {
        // 校验存在
        validateLocExists(updateReqVO.getId());
        // 更新
        WmsLocDO updateObj = BeanUtils.toBean(updateReqVO, WmsLocDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateLocUnique(updateObj, updateReqVO.getId());
        locMapper.updateById(updateObj);
    }

    @Override
    public void deleteLoc(Long id) {
        // 校验存在
        validateLocExists(id);
        // 删除
        locMapper.deleteById(id);
    }

    @Override
        public void deleteLocListByIds(List<Long> ids) {
        // 删除
        locMapper.deleteByIds(ids);
        }


    private void validateLocExists(Long id) {
        if (locMapper.selectById(id) == null) {
            throw exception(LOC_NOT_EXISTS);
        }
    }

    @Override
    public LocRespVO getLoc(Long id) {
        return locMapper.selectJoinById(id);
    }

    @Override
    public PageResult<LocRespVO> getLocPage(LocPageReqVO pageReqVO) {
        return locMapper.selectPage(pageReqVO);
    }


    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateLocUnique(WmsLocDO loc, Long excludeId) {
        WmsLocDO existed = locMapper.selectByWhIdAndCode(loc.getWhId(), loc.getCode());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(LOC_CODE_DUPLICATE);
    }

}
