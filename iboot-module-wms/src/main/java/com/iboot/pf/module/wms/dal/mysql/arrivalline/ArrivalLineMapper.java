package com.iboot.pf.module.wms.dal.mysql.arrivalline;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.controller.admin.arrival.vo.ArrivalLinePageReqVO;
import com.iboot.pf.module.wms.controller.admin.arrival.vo.ArrivalLineRespVO;
import com.iboot.pf.module.wms.dal.dataobject.arrivalline.ArrivalLineDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
            .orderByAsc(ArrivalLineDO::getLineNo));
    }

    /** 按到货单 + 行号查询，唯一索引 uk_wms_arrival_line_no_deleted 的应用层前置校验。 */
    default ArrivalLineDO selectByArrivalIdAndLineNo(Long arrivalId, Integer lineNo) {
        return selectOne(ArrivalLineDO::getArrivalId, arrivalId, ArrivalLineDO::getLineNo, lineNo);
    }

    /** 分页查询明细，返回带可读字段的 VO，前端不需要再拼接。 */
    default PageResult<ArrivalLineRespVO> selectJoinPage(ArrivalLinePageReqVO reqVO) {
        IPage<ArrivalLineRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ArrivalLineRespVO selectJoinById(@Param("id") Long id);

    IPage<ArrivalLineRespVO> selectJoinPage(IPage<ArrivalLineRespVO> page,
                                            @Param("req") ArrivalLinePageReqVO reqVO);

    default int deleteByArrivalId(Long arrivalId) {
        return delete(ArrivalLineDO::getArrivalId, arrivalId);
    }

	default int deleteByArrivalIds(List<Long> arrivalIds) {
	    return deleteBatch(ArrivalLineDO::getArrivalId, arrivalIds);
	}

}
