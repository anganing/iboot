package com.iboot.pf.module.wms.dal.mysql.transfer;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.TransferOrderLineRespVO;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.TransferOrderLinePageReqVO;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 库存转移调拨明细 Mapper。 */
@Mapper
public interface WmsTransferOrderLineMapper extends BaseMapperX<WmsTransferOrderLineDO> {

    default List<WmsTransferOrderLineDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<WmsTransferOrderLineDO>()
                .eq(WmsTransferOrderLineDO::getOrderId, orderId)
                .orderByAsc(WmsTransferOrderLineDO::getLineNo));
    }

    default PageResult<WmsTransferOrderLineDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsTransferOrderLineDO>()
            .eq(WmsTransferOrderLineDO::getOrderId, orderId)
            .orderByAsc(WmsTransferOrderLineDO::getLineNo));
    }

    /** 按转移单 + 行号查询，唯一索引 uk_wms_transfer_line_no_deleted 的应用层前置校验。 */
    default WmsTransferOrderLineDO selectByOrderIdAndLineNo(Long orderId, Integer lineNo) {
        return selectOne(WmsTransferOrderLineDO::getOrderId, orderId, WmsTransferOrderLineDO::getLineNo, lineNo);
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
        return delete(WmsTransferOrderLineDO::getOrderId, orderId);
    }

	default int deleteByOrderIds(List<Long> orderIds) {
	    return deleteBatch(WmsTransferOrderLineDO::getOrderId, orderIds);
	}
}
