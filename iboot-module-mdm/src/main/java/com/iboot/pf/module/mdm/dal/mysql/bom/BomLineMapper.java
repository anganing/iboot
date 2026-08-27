package com.iboot.pf.module.mdm.dal.mysql.bomline;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.BomLineRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 物料清单明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface BomLineMapper extends BaseMapperX<BomLineDO> {

    default PageResult<BomLineRespVO> selectPage(PageParam reqVO, Long bomId) {
        IPage<BomLineRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, bomId);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    BomLineRespVO selectJoinById(@Param("id") Long id);

    IPage<BomLineRespVO> selectJoinPage(IPage<BomLineRespVO> page, @Param("bomId") Long bomId);

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
