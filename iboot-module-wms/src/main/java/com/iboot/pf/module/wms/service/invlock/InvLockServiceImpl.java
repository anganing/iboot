package com.iboot.pf.module.wms.service.invlock;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.invlock.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.lock.WmsInvLockDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import com.iboot.pf.module.wms.dal.mysql.invlock.InvLockMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertSet;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.framework.common.util.collection.MapUtils.findAndThen;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-库存冻结记录 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class InvLockServiceImpl implements InvLockService {

    @Resource
    private WmsCodeService codeService;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private InvLockMapper invLockMapper;

    @Override
    public Long createInvLock(InvLockSaveReqVO createReqVO) {
        // 插入
        WmsInvLockDO invLock = BeanUtils.toBean(createReqVO, WmsInvLockDO.class);
        invLock.setId(null);
        invLock.setLockNo(codeService.generate(WmsCodeRuleConstants.LOCK, invLock.getLockNo()));
        validateInvLockUnique(invLock, null);
        invLockMapper.insert(invLock);

        // 返回
        return invLock.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public InvLockImportRespVO importInvLockList(List<InvLockImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return InvLockImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        InvLockImportRespVO respVO = InvLockImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createInvLock(BeanUtils.toBean(importItem, InvLockSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateInvLock(InvLockSaveReqVO updateReqVO) {
        // 校验存在
        validateInvLockExists(updateReqVO.getId());
        // 更新
        WmsInvLockDO updateObj = BeanUtils.toBean(updateReqVO, WmsInvLockDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateInvLockUnique(updateObj, updateReqVO.getId());
        invLockMapper.updateById(updateObj);
    }

    @Override
    public void deleteInvLock(Long id) {
        // 校验存在
        validateInvLockExists(id);
        // 删除
        invLockMapper.deleteById(id);
    }

    @Override
        public void deleteInvLockListByIds(List<Long> ids) {
        // 删除
        invLockMapper.deleteByIds(ids);
        }


    private void validateInvLockExists(Long id) {
        if (invLockMapper.selectById(id) == null) {
            throw exception(INV_LOCK_NOT_EXISTS);
        }
    }

    @Override
    public InvLockRespVO getInvLock(Long id) {
        InvLockRespVO invLock = invLockMapper.selectJoinById(id);
        fillMaterialName(invLock == null ? Collections.emptyList() : Collections.singletonList(invLock));
        return invLock;
    }

    @Override
    public PageResult<InvLockRespVO> getInvLockPage(InvLockPageReqVO pageReqVO) {
        PageResult<InvLockRespVO> pageResult = invLockMapper.selectPage(pageReqVO);
        fillMaterialName(pageResult.getList());
        return pageResult;
    }

    /**
     * 批量补全物料编码与名称。物料属于 mdm，只能走门面批量取，不能在 SQL 里跨库 JOIN；
     * 补全放在 Service 而不是 Controller，Excel 导出复用同一份结果才不会丢字段。
     */
    private void fillMaterialName(List<InvLockRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<Long, MaterialRespDTO> materialMap =
                materialApi.getMaterialMap(convertSet(list, InvLockRespVO::getMatId));
        list.forEach(vo -> findAndThen(materialMap, vo.getMatId(), material -> {
            vo.setMatCode(material.getCode());
            vo.setMatName(material.getName());
        }));
    }


    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateInvLockUnique(WmsInvLockDO invLock, Long excludeId) {
        WmsInvLockDO existed = invLockMapper.selectByLockNo(invLock.getLockNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(INV_LOCK_NO_DUPLICATE);
    }

}
