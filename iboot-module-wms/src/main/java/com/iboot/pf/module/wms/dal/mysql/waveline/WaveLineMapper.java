package com.iboot.pf.module.wms.dal.mysql.waveline;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.waveline.WaveLineDO;
import org.apache.ibatis.annotations.Mapper;

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
            .orderByDesc(WaveLineDO::getId));
    }

    default int deleteByWaveId(Long waveId) {
        return delete(WaveLineDO::getWaveId, waveId);
    }

	default int deleteByWaveIds(List<Long> waveIds) {
	    return deleteBatch(WaveLineDO::getWaveId, waveIds);
	}

}