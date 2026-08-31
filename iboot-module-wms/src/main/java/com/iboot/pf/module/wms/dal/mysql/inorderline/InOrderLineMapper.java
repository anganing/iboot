package com.iboot.pf.module.wms.dal.mysql.inorderline;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.InOrderLinePageReqVO;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.InOrderLineRespVO;
import com.iboot.pf.module.wms.dal.dataobject.inorderline.InOrderLineDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * WMS-入库单明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface InOrderLineMapper extends BaseMapperX<InOrderLineDO> {

    default PageResult<InOrderLineDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InOrderLineDO>()
            .eq(InOrderLineDO::getOrderId, orderId)
            .orderByAsc(InOrderLineDO::getLineNo));
    }

    /** 按入库单 + 行号查询，唯一索引 uk_wms_in_order_line_no_deleted 的应用层前置校验。 */
    default InOrderLineDO selectByOrderIdAndLineNo(Long orderId, Integer lineNo) {
        return selectOne(InOrderLineDO::getOrderId, orderId, InOrderLineDO::getLineNo, lineNo);
    }

    /** 分页查询明细，JOIN 出库位、容器等可读字段，前端不再二次拼接。 */
    default PageResult<InOrderLineRespVO> selectJoinPage(InOrderLinePageReqVO reqVO) {
        IPage<InOrderLineRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    InOrderLineRespVO selectJoinById(@Param("id") Long id);

    IPage<InOrderLineRespVO> selectJoinPage(IPage<InOrderLineRespVO> page,
                                           @Param("req") InOrderLinePageReqVO reqVO);

    default int deleteByOrderId(Long orderId) {
        return delete(InOrderLineDO::getOrderId, orderId);
    }

	default int deleteByOrderIds(List<Long> orderIds) {
	    return deleteBatch(InOrderLineDO::getOrderId, orderIds);
	}

}
