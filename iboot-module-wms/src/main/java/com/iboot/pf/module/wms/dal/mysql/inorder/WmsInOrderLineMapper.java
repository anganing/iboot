package com.iboot.pf.module.wms.dal.mysql.inorder;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.InOrderLineRespVO;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.InOrderLinePageReqVO;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 入库单明细 Mapper。 */
@Mapper
public interface WmsInOrderLineMapper extends BaseMapperX<WmsInOrderLineDO> {

    default List<WmsInOrderLineDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<WmsInOrderLineDO>()
                .eq(WmsInOrderLineDO::getOrderId, orderId)
                .orderByAsc(WmsInOrderLineDO::getLineNo));
    }

    default List<WmsInOrderLineDO> selectListBySourceLineId(Long sourceLineId) {
        return selectList(WmsInOrderLineDO::getSourceLineId, sourceLineId);
    }

    default PageResult<WmsInOrderLineDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInOrderLineDO>()
            .eq(WmsInOrderLineDO::getOrderId, orderId)
            .orderByAsc(WmsInOrderLineDO::getLineNo));
    }

    /** 按入库单 + 行号查询，唯一索引 uk_wms_in_order_line_no_deleted 的应用层前置校验。 */
    default WmsInOrderLineDO selectByOrderIdAndLineNo(Long orderId, Integer lineNo) {
        return selectOne(WmsInOrderLineDO::getOrderId, orderId, WmsInOrderLineDO::getLineNo, lineNo);
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
        return delete(WmsInOrderLineDO::getOrderId, orderId);
    }

	default int deleteByOrderIds(List<Long> orderIds) {
	    return deleteBatch(WmsInOrderLineDO::getOrderId, orderIds);
	}
}
