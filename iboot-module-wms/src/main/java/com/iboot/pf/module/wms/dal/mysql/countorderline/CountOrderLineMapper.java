package com.iboot.pf.module.wms.dal.mysql.countorderline;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.CountOrderLinePageReqVO;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.CountOrderLineRespVO;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderLineDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * WMS-盘点明细 Mapper
 *
 * @author iboot
 */
@Mapper
public interface CountOrderLineMapper extends BaseMapperX<WmsCountOrderLineDO> {

    default PageResult<WmsCountOrderLineDO> selectPage(PageParam reqVO, Long orderId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsCountOrderLineDO>()
            .eq(WmsCountOrderLineDO::getOrderId, orderId)
            .orderByAsc(WmsCountOrderLineDO::getLineNo));
    }

    /** 按盘点单 + 行号查询，唯一索引 uk_wms_count_line_no_deleted 的应用层前置校验。 */
    default WmsCountOrderLineDO selectByOrderIdAndLineNo(Long orderId, Integer lineNo) {
        return selectOne(WmsCountOrderLineDO::getOrderId, orderId, WmsCountOrderLineDO::getLineNo, lineNo);
    }

    /** 按盘点单 + 库存余额查询，唯一索引 uk_wms_count_line_balance_deleted 的应用层前置校验。 */
    default WmsCountOrderLineDO selectByOrderIdAndBalanceId(Long orderId, Long balanceId) {
        return selectOne(WmsCountOrderLineDO::getOrderId, orderId, WmsCountOrderLineDO::getBalanceId, balanceId);
    }

    /** 分页查询明细，JOIN 出容器等可读字段。 */
    default PageResult<CountOrderLineRespVO> selectJoinPage(CountOrderLinePageReqVO reqVO) {
        IPage<CountOrderLineRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    CountOrderLineRespVO selectJoinById(@Param("id") Long id);

    IPage<CountOrderLineRespVO> selectJoinPage(IPage<CountOrderLineRespVO> page,
                                              @Param("req") CountOrderLinePageReqVO reqVO);

    default int deleteByOrderId(Long orderId) {
        return delete(WmsCountOrderLineDO::getOrderId, orderId);
    }

	default int deleteByOrderIds(List<Long> orderIds) {
	    return deleteBatch(WmsCountOrderLineDO::getOrderId, orderIds);
	}

}
