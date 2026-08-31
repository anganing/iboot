package com.iboot.pf.module.wms.dal.mysql.waveline;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.controller.admin.wave.vo.WaveLinePageReqVO;
import com.iboot.pf.module.wms.controller.admin.wave.vo.WaveLineRespVO;
import com.iboot.pf.module.wms.dal.dataobject.waveline.WaveLineDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * WMS-波次明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WaveLineMapper extends BaseMapperX<WaveLineDO> {

    default PageResult<WaveLineDO> selectPage(PageParam reqVO, Long waveId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WaveLineDO>()
            .eq(WaveLineDO::getWaveId, waveId)
            .orderByAsc(WaveLineDO::getLineNo));
    }

    /** 按波次 + 行号查询，唯一索引 uk_wms_wave_line_no_deleted 的应用层前置校验。 */
    default WaveLineDO selectByWaveIdAndLineNo(Long waveId, Integer lineNo) {
        return selectOne(WaveLineDO::getWaveId, waveId, WaveLineDO::getLineNo, lineNo);
    }

    /** 按波次 + 出库明细查询，唯一索引 uk_wms_wave_line_order_deleted 的应用层前置校验。 */
    default WaveLineDO selectByWaveIdAndOutOrderLineId(Long waveId, Long outOrderLineId) {
        return selectOne(WaveLineDO::getWaveId, waveId, WaveLineDO::getOutOrderLineId, outOrderLineId);
    }

    /** 分页查询明细，JOIN 出出库单号、出库行号与物料信息。 */
    default PageResult<WaveLineRespVO> selectJoinPage(WaveLinePageReqVO reqVO) {
        IPage<WaveLineRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    WaveLineRespVO selectJoinById(@Param("id") Long id);

    IPage<WaveLineRespVO> selectJoinPage(IPage<WaveLineRespVO> page,
                                        @Param("req") WaveLinePageReqVO reqVO);

    default int deleteByWaveId(Long waveId) {
        return delete(WaveLineDO::getWaveId, waveId);
    }

	default int deleteByWaveIds(List<Long> waveIds) {
	    return deleteBatch(WaveLineDO::getWaveId, waveIds);
	}

}
