package com.iboot.pf.module.wms.dal.mysql.transferorderline;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.TransferOrderLinePageReqVO;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.TransferOrderLineRespVO;
import com.iboot.pf.module.wms.dal.dataobject.transferorderline.TransferOrderLineDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * WMS-库存转移调拨明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface TransferOrderLineMapper extends BaseMapperX<TransferOrderLineDO> {

    default PageResult<TransferOrderLineDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TransferOrderLineDO>()
            .eq(TransferOrderLineDO::getOrderId, orderId)
            .orderByAsc(TransferOrderLineDO::getLineNo));
    }

    /** 按转移单 + 行号查询，唯一索引 uk_wms_transfer_line_no_deleted 的应用层前置校验。 */
    default TransferOrderLineDO selectByOrderIdAndLineNo(Long orderId, Integer lineNo) {
        return selectOne(TransferOrderLineDO::getOrderId, orderId, TransferOrderLineDO::getLineNo, lineNo);
    }

    /** 分页查询明细，JOIN 出来源/目标库位、容器等可读字段。 */
    default PageResult<TransferOrderLineRespVO> selectJoinPage(TransferOrderLinePageReqVO reqVO) {
        IPage<TransferOrderLineRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    TransferOrderLineRespVO selectJoinById(@Param("id") Long id);

    IPage<TransferOrderLineRespVO> selectJoinPage(IPage<TransferOrderLineRespVO> page,
                                                 @Param("req") TransferOrderLinePageReqVO reqVO);

    default int deleteByOrderId(Long orderId) {
        return delete(TransferOrderLineDO::getOrderId, orderId);
    }

	default int deleteByOrderIds(List<Long> orderIds) {
	    return deleteBatch(TransferOrderLineDO::getOrderId, orderIds);
	}

}
