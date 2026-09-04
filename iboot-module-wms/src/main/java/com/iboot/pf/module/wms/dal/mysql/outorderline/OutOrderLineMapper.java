package com.iboot.pf.module.wms.dal.mysql.outorderline;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.OutOrderLinePageReqVO;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.OutOrderLineRespVO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderLineDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * WMS-出库单明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface OutOrderLineMapper extends BaseMapperX<WmsOutOrderLineDO> {

    default PageResult<WmsOutOrderLineDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsOutOrderLineDO>()
            .eq(WmsOutOrderLineDO::getOrderId, orderId)
            .orderByAsc(WmsOutOrderLineDO::getLineNo));
    }

    /** 按出库单 + 行号查询，唯一索引 uk_wms_out_order_line_no_deleted 的应用层前置校验。 */
    default WmsOutOrderLineDO selectByOrderIdAndLineNo(Long orderId, Integer lineNo) {
        return selectOne(WmsOutOrderLineDO::getOrderId, orderId, WmsOutOrderLineDO::getLineNo, lineNo);
    }

    /** 分页查询明细，JOIN 出目标库位、容器等可读字段。 */
    default PageResult<OutOrderLineRespVO> selectJoinPage(OutOrderLinePageReqVO reqVO) {
        IPage<OutOrderLineRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    OutOrderLineRespVO selectJoinById(@Param("id") Long id);

    IPage<OutOrderLineRespVO> selectJoinPage(IPage<OutOrderLineRespVO> page,
                                            @Param("req") OutOrderLinePageReqVO reqVO);

    default int deleteByOrderId(Long orderId) {
        return delete(WmsOutOrderLineDO::getOrderId, orderId);
    }

	default int deleteByOrderIds(List<Long> orderIds) {
	    return deleteBatch(WmsOutOrderLineDO::getOrderId, orderIds);
	}

}
