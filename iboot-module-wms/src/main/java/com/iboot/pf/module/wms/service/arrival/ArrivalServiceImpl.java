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

    @Override
    public Long createArrival(ArrivalSaveReqVO createReqVO) {
        // 插入
        ArrivalDO arrival = BeanUtils.toBean(createReqVO, ArrivalDO.class);
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
    public PageResult<ArrivalLineDO> getArrivalLinePage(PageParam pageReqVO, Long arrivalId) {
        return arrivalLineMapper.selectPage(pageReqVO, arrivalId);
    }

    @Override
    public Long createArrivalLine(ArrivalLineDO arrivalLine) {
        arrivalLine.clean(); // 清理掉创建、更新时间等相关属性值
        arrivalLineMapper.insert(arrivalLine);
        return arrivalLine.getId();
    }

    @Override
    public void updateArrivalLine(ArrivalLineDO arrivalLine) {
        // 校验存在
        validateArrivalLineExists(arrivalLine.getId());
        // 更新
        arrivalLine.clean(); // 解决更新情况下：updateTime 不更新
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
    public ArrivalLineDO getArrivalLine(Long id) {
        return arrivalLineMapper.selectById(id);
    }

    private void validateArrivalLineExists(Long id) {
        if (arrivalLineMapper.selectById(id) == null) {
            throw exception(ARRIVAL_LINE_NOT_EXISTS);
        }
    }

    private void deleteArrivalLineByArrivalId(Long arrivalId) {
        arrivalLineMapper.deleteByArrivalId(arrivalId);
    }

	private void deleteArrivalLineByArrivalIds(List<Long> arrivalIds) {
        arrivalLineMapper.deleteByArrivalIds(arrivalIds);
	}

}