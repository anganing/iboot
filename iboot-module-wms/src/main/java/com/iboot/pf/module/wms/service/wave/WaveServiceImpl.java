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

import com.iboot.pf.module.wms.dal.mysql.wave.WaveMapper;
import com.iboot.pf.module.wms.dal.mysql.waveline.WaveLineMapper;

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
    private WaveMapper waveMapper;
    @Resource
    private WaveLineMapper waveLineMapper;

    @Override
    public Long createWave(WaveSaveReqVO createReqVO) {
        // 插入
        WaveDO wave = BeanUtils.toBean(createReqVO, WaveDO.class);
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
    public PageResult<WaveLineDO> getWaveLinePage(PageParam pageReqVO, Long waveId) {
        return waveLineMapper.selectPage(pageReqVO, waveId);
    }

    @Override
    public Long createWaveLine(WaveLineDO waveLine) {
        waveLine.clean(); // 清理掉创建、更新时间等相关属性值
        waveLineMapper.insert(waveLine);
        return waveLine.getId();
    }

    @Override
    public void updateWaveLine(WaveLineDO waveLine) {
        // 校验存在
        validateWaveLineExists(waveLine.getId());
        // 更新
        waveLine.clean(); // 解决更新情况下：updateTime 不更新
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
    public WaveLineDO getWaveLine(Long id) {
        return waveLineMapper.selectById(id);
    }

    private void validateWaveLineExists(Long id) {
        if (waveLineMapper.selectById(id) == null) {
            throw exception(WAVE_LINE_NOT_EXISTS);
        }
    }

    private void deleteWaveLineByWaveId(Long waveId) {
        waveLineMapper.deleteByWaveId(waveId);
    }

	private void deleteWaveLineByWaveIds(List<Long> waveIds) {
        waveLineMapper.deleteByWaveIds(waveIds);
	}

}