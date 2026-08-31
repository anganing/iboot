package com.iboot.pf.module.wms.service.wave;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.wave.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.wave.WaveDO;
import com.iboot.pf.module.wms.dal.dataobject.waveline.WaveLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-出库波次 Service 接口
 *
 * @author iboot
 */
public interface WaveService {

    /**
     * 创建WMS-出库波次
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWave(@Valid WaveSaveReqVO createReqVO);

    /**
     * 导入WMS-出库波次
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    WaveImportRespVO importWaveList(List<WaveImportExcelVO> importList);

    /**
     * 更新WMS-出库波次
     *
     * @param updateReqVO 更新信息
     */
    void updateWave(@Valid WaveSaveReqVO updateReqVO);

    /**
     * 删除WMS-出库波次
     *
     * @param id 编号
     */
    void deleteWave(Long id);

    /**
    * 批量删除WMS-出库波次
    *
    * @param ids 编号
    */
    void deleteWaveListByIds(List<Long> ids);

    /**
     * 获得WMS-出库波次
     *
     * @param id 编号
     * @return WMS-出库波次
     */
    WaveRespVO getWave(Long id);

    /**
     * 获得WMS-出库波次分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-出库波次分页
     */
    PageResult<WaveRespVO> getWavePage(WavePageReqVO pageReqVO);

    // ==================== 子表（WMS-波次明细） ====================

    /**
     * 获得WMS-波次明细分页
     *
     * @param pageReqVO 分页查询
     * @param waveId 波次编号
     * @return WMS-波次明细分页
     */
    PageResult<WaveLineRespVO> getWaveLinePage(WaveLinePageReqVO pageReqVO);

    /**
     * 创建WMS-波次明细
     *
     * @param waveLine 创建信息
     * @return 编号
     */
    Long createWaveLine(@Valid WaveLineSaveReqVO createReqVO);

    /**
     * 更新WMS-波次明细
     *
     * @param waveLine 更新信息
     */
    void updateWaveLine(@Valid WaveLineSaveReqVO updateReqVO);

    /**
     * 删除WMS-波次明细
     *
     * @param id 编号
     */
    void deleteWaveLine(Long id);

    /**
    * 批量删除WMS-波次明细
    *
    * @param ids 编号
    */
    void deleteWaveLineListByIds(List<Long> ids);

	/**
	 * 获得WMS-波次明细
	 *
	 * @param id 编号
     * @return WMS-波次明细
	 */
    WaveLineRespVO getWaveLine(Long id);

}
