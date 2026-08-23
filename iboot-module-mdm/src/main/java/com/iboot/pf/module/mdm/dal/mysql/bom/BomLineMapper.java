package com.iboot.pf.module.mdm.dal.mysql.bomline;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料清单明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface BomLineMapper extends BaseMapperX<BomLineDO> {

    default PageResult<BomLineDO> selectPage(PageParam reqVO, Long bomId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BomLineDO>()
            .eq(BomLineDO::getBomId, bomId)
            .orderByDesc(BomLineDO::getId));
    }

    default int deleteByBomId(Long bomId) {
        return delete(BomLineDO::getBomId, bomId);
    }

    default long selectCountByBomId(Long bomId) {
        return selectCount(BomLineDO::getBomId, bomId);
    }

	default int deleteByBomIds(List<Long> bomIds) {
	    return deleteBatch(BomLineDO::getBomId, bomIds);
	}

}
