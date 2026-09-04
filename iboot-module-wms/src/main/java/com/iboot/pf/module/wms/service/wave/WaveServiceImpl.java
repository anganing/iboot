package com.iboot.pf.module.wms.service.wave;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.wave.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.wave.WaveDO;
import com.iboot.pf.module.wms.dal.dataobject.waveline.WaveLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;

import com.iboot.pf.module.wms.dal.mysql.wave.WaveMapper;
import com.iboot.pf.module.wms.dal.mysql.waveline.WaveLineMapper;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderLineDO;
import com.iboot.pf.module.wms.dal.mysql.outorderline.OutOrderLineMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-出库波次 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class WaveServiceImpl implements WaveService {

    @Resource
    private WmsCodeService codeService;
    @Resource
    private WaveMapper waveMapper;
    @Resource
    private WaveLineMapper waveLineMapper;
    @Resource
    private OutOrderLineMapper outOrderLineMapper;

    @Override
    public Long createWave(WaveSaveReqVO createReqVO) {
        // 插入
        WaveDO wave = BeanUtils.toBean(createReqVO, WaveDO.class);
        wave.setId(null);
        wave.setWaveNo(codeService.generate(WmsCodeRuleConstants.WAVE, wave.getWaveNo()));
        validateWaveUnique(wave, null);
        waveMapper.insert(wave);

        // 返回
        return wave.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public WaveImportRespVO importWaveList(List<WaveImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return WaveImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        WaveImportRespVO respVO = WaveImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createWave(BeanUtils.toBean(importItem, WaveSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateWave(WaveSaveReqVO updateReqVO) {
        // 校验存在
        validateWaveExists(updateReqVO.getId());
        // 更新
        WaveDO updateObj = BeanUtils.toBean(updateReqVO, WaveDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateWaveUnique(updateObj, updateReqVO.getId());
        waveMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWave(Long id) {
        // 校验存在
        validateWaveExists(id);
        // 删除
        waveMapper.deleteById(id);

        // 删除子表
        deleteWaveLineByWaveId(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteWaveListByIds(List<Long> ids) {
        // 删除
        waveMapper.deleteByIds(ids);
    
    // 删除子表
            deleteWaveLineByWaveIds(ids);
    }


    private void validateWaveExists(Long id) {
        if (waveMapper.selectById(id) == null) {
            throw exception(WAVE_NOT_EXISTS);
        }
    }

    @Override
    public WaveRespVO getWave(Long id) {
        return waveMapper.selectJoinById(id);
    }

    @Override
    public PageResult<WaveRespVO> getWavePage(WavePageReqVO pageReqVO) {
        return waveMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-波次明细） ====================

    @Override
    public PageResult<WaveLineRespVO> getWaveLinePage(WaveLinePageReqVO pageReqVO) {
        return waveLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public Long createWaveLine(WaveLineSaveReqVO createReqVO) {
        validateWaveExists(createReqVO.getWaveId());
        validateLineNoUnique(createReqVO.getWaveId(), createReqVO.getLineNo(), null);
        validateOutOrderLineUnique(createReqVO.getWaveId(), createReqVO.getOutOrderLineId(), null);
        validateOutOrderLine(createReqVO.getOutOrderId(), createReqVO.getOutOrderLineId());
        WaveLineDO waveLine = BeanUtils.toBean(createReqVO, WaveLineDO.class);
        waveLine.setId(null);
        waveLineMapper.insert(waveLine);
        return waveLine.getId();
    }

    @Override
    public void updateWaveLine(WaveLineSaveReqVO updateReqVO) {
        // 校验存在
        validateWaveLineExists(updateReqVO.getId());
        validateLineNoUnique(updateReqVO.getWaveId(), updateReqVO.getLineNo(), updateReqVO.getId());
        validateOutOrderLineUnique(updateReqVO.getWaveId(), updateReqVO.getOutOrderLineId(), updateReqVO.getId());
        validateOutOrderLine(updateReqVO.getOutOrderId(), updateReqVO.getOutOrderLineId());
        WaveLineDO waveLine = BeanUtils.toBean(updateReqVO, WaveLineDO.class);
        waveLineMapper.updateById(waveLine);
    }

    @Override
    public void deleteWaveLine(Long id) {
        // 删除
        waveLineMapper.deleteById(id);
    }

	@Override
	public void deleteWaveLineListByIds(List<Long> ids) {
        // 删除
        waveLineMapper.deleteByIds(ids);
	}

    @Override
    public WaveLineRespVO getWaveLine(Long id) {
        return waveLineMapper.selectJoinById(id);
    }

    private void validateWaveLineExists(Long id) {
        if (waveLineMapper.selectById(id) == null) {
            throw exception(WAVE_LINE_NOT_EXISTS);
        }
    }

    /** 明细行号唯一：唯一索引是 (wave_id, line_no, deleted)。 */
    private void validateLineNoUnique(Long waveId, Integer lineNo, Long excludeId) {
        WaveLineDO existed = waveLineMapper.selectByWaveIdAndLineNo(waveId, lineNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(WAVE_LINE_NO_DUPLICATE);
    }

    /** 同一波次里一条出库明细只能加入一次：唯一索引是 (wave_id, out_order_line_id, deleted)。 */
    private void validateOutOrderLineUnique(Long waveId, Long outOrderLineId, Long excludeId) {
        WaveLineDO existed = waveLineMapper.selectByWaveIdAndOutOrderLineId(waveId, outOrderLineId);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(WAVE_LINE_ORDER_DUPLICATE);
    }

    /** 出库明细必须存在且属于所选出库单，避免波次挂到别的单据上。 */
    private void validateOutOrderLine(Long outOrderId, Long outOrderLineId) {
        WmsOutOrderLineDO line = outOrderLineMapper.selectById(outOrderLineId);
        if (line == null) {
            throw exception(OUT_ORDER_LINE_NOT_EXISTS);
        }
        if (!line.getOrderId().equals(outOrderId)) {
            throw exception(WAVE_LINE_ORDER_NOT_MATCH);
        }
    }

    private void deleteWaveLineByWaveId(Long waveId) {
        waveLineMapper.deleteByWaveId(waveId);
    }

	private void deleteWaveLineByWaveIds(List<Long> waveIds) {
        waveLineMapper.deleteByWaveIds(waveIds);
	}


    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateWaveUnique(WaveDO wave, Long excludeId) {
        WaveDO existed = waveMapper.selectByWaveNo(wave.getWaveNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(WAVE_NO_DUPLICATE);
    }

}
