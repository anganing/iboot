package com.iboot.pf.module.mes.dal.mysql.materialreqline;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES-领退补料申请明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MaterialReqLineMapper extends BaseMapperX<MaterialReqLineDO> {

    default PageResult<MaterialReqLineDO> selectPage(PageParam reqVO, Long reqId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialReqLineDO>()
            .eq(MaterialReqLineDO::getReqId, reqId)
            .orderByDesc(MaterialReqLineDO::getId));
    }

    default int deleteByReqId(Long reqId) {
        return delete(MaterialReqLineDO::getReqId, reqId);
    }

	default int deleteByReqIds(List<Long> reqIds) {
	    return deleteBatch(MaterialReqLineDO::getReqId, reqIds);
	}

}