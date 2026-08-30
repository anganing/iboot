package com.iboot.pf.module.wms.dal.mysql.arrivalline;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.arrivalline.ArrivalLineDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * WMS-到货登记明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ArrivalLineMapper extends BaseMapperX<ArrivalLineDO> {

    default PageResult<ArrivalLineDO> selectPage(PageParam reqVO, Long arrivalId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ArrivalLineDO>()
            .eq(ArrivalLineDO::getArrivalId, arrivalId)
            .orderByDesc(ArrivalLineDO::getId));
    }

    default int deleteByArrivalId(Long arrivalId) {
        return delete(ArrivalLineDO::getArrivalId, arrivalId);
    }

	default int deleteByArrivalIds(List<Long> arrivalIds) {
	    return deleteBatch(ArrivalLineDO::getArrivalId, arrivalIds);
	}

}