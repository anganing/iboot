package com.iboot.pf.module.wms.service.arrival;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.arrival.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.arrival.ArrivalDO;
import com.iboot.pf.module.wms.dal.dataobject.arrivalline.ArrivalLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.arrival.ArrivalMapper;
import com.iboot.pf.module.wms.dal.mysql.arrivalline.ArrivalLineMapper;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.service.snapshot.WmsSnapshotService;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-到货登记 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ArrivalServiceImpl implements ArrivalService {

    @Resource
    private ArrivalMapper arrivalMapper;
    @Resource
    private ArrivalLineMapper arrivalLineMapper;
    @Resource
    private WmsCodeService codeService;
    @Resource
    private WmsSnapshotService snapshotService;

    @Override
    public Long createArrival(ArrivalSaveReqVO createReqVO) {
        // 插入
        ArrivalDO arrival = BeanUtils.toBean(createReqVO, ArrivalDO.class);
        arrival.setId(null);
        arrival.setArrivalNo(codeService.generate(WmsCodeRuleConstants.ARRIVAL, arrival.getArrivalNo()));
        validateArrivalNoUnique(arrival.getArrivalNo(), null);
        arrivalMapper.insert(arrival);

        // 返回
        return arrival.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ArrivalImportRespVO importArrivalList(List<ArrivalImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ArrivalImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ArrivalImportRespVO respVO = ArrivalImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createArrival(BeanUtils.toBean(importItem, ArrivalSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateArrival(ArrivalSaveReqVO updateReqVO) {
        // 校验存在
        validateArrivalExists(updateReqVO.getId());
        // 改自己不能撞自己，所以排除自身编号
        validateArrivalNoUnique(updateReqVO.getArrivalNo(), updateReqVO.getId());
        // 更新
        ArrivalDO updateObj = BeanUtils.toBean(updateReqVO, ArrivalDO.class);
        arrivalMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArrival(Long id) {
        // 校验存在
        validateArrivalExists(id);
        // 删除
        arrivalMapper.deleteById(id);

        // 删除子表
        deleteArrivalLineByArrivalId(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteArrivalListByIds(List<Long> ids) {
        // 删除
        arrivalMapper.deleteByIds(ids);
    
    // 删除子表
            deleteArrivalLineByArrivalIds(ids);
    }


    private void validateArrivalExists(Long id) {
        if (arrivalMapper.selectById(id) == null) {
            throw exception(ARRIVAL_NOT_EXISTS);
        }
    }

    /** 到货单号唯一：唯一索引是 (arrival_no, deleted)，这里做前置校验给出可读提示。 */
    private void validateArrivalNoUnique(String arrivalNo, Long excludeId) {
        ArrivalDO existed = arrivalMapper.selectByArrivalNo(arrivalNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(ARRIVAL_NO_DUPLICATE);
    }

    @Override
    public ArrivalRespVO getArrival(Long id) {
        return arrivalMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ArrivalRespVO> getArrivalPage(ArrivalPageReqVO pageReqVO) {
        return arrivalMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-到货登记明细） ====================

    @Override
    public PageResult<ArrivalLineRespVO> getArrivalLinePage(ArrivalLinePageReqVO pageReqVO) {
        return arrivalLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public Long createArrivalLine(ArrivalLineSaveReqVO createReqVO) {
        validateArrivalExists(createReqVO.getArrivalId());
        validateLineNoUnique(createReqVO.getArrivalId(), createReqVO.getLineNo(), null);
        ArrivalLineDO arrivalLine = BeanUtils.toBean(createReqVO, ArrivalLineDO.class);
        arrivalLine.setId(null);
        fillSnapshot(arrivalLine);
        arrivalLineMapper.insert(arrivalLine);
        return arrivalLine.getId();
    }

    @Override
    public void updateArrivalLine(ArrivalLineSaveReqVO updateReqVO) {
        // 校验存在
        validateArrivalLineExists(updateReqVO.getId());
        validateLineNoUnique(updateReqVO.getArrivalId(), updateReqVO.getLineNo(), updateReqVO.getId());
        ArrivalLineDO arrivalLine = BeanUtils.toBean(updateReqVO, ArrivalLineDO.class);
        fillSnapshot(arrivalLine);
        arrivalLineMapper.updateById(arrivalLine);
    }

    @Override
    public void deleteArrivalLine(Long id) {
        // 删除
        arrivalLineMapper.deleteById(id);
    }

	@Override
	public void deleteArrivalLineListByIds(List<Long> ids) {
        // 删除
        arrivalLineMapper.deleteByIds(ids);
	}

    @Override
    public ArrivalLineRespVO getArrivalLine(Long id) {
        return arrivalLineMapper.selectJoinById(id);
    }

    private void validateArrivalLineExists(Long id) {
        if (arrivalLineMapper.selectById(id) == null) {
            throw exception(ARRIVAL_LINE_NOT_EXISTS);
        }
    }

    /** 明细行号唯一：唯一索引是 (arrival_id, line_no, deleted)。 */
    private void validateLineNoUnique(Long arrivalId, Integer lineNo, Long excludeId) {
        ArrivalLineDO existed = arrivalLineMapper.selectByArrivalIdAndLineNo(arrivalId, lineNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(ARRIVAL_LINE_NO_DUPLICATE);
    }

    /**
     * 物料与单位快照跨模块取数，只能走 mdm 的 Api 门面，不允许 JOIN mdm 的表。
     * 快照落库后列表查询无需再回查 mdm。
     */
    private void fillSnapshot(ArrivalLineDO line) {
        MaterialRespDTO material = snapshotService.requireMaterial(line.getMatId());
        line.setMatCode(material.getCode());
        line.setMatName(material.getName());
        line.setMatSpec(material.getSpec());
        line.setUomCode(snapshotService.requireUomCode(line.getUomId()));
    }

    private void deleteArrivalLineByArrivalId(Long arrivalId) {
        arrivalLineMapper.deleteByArrivalId(arrivalId);
    }

	private void deleteArrivalLineByArrivalIds(List<Long> arrivalIds) {
        arrivalLineMapper.deleteByArrivalIds(arrivalIds);
	}

}
