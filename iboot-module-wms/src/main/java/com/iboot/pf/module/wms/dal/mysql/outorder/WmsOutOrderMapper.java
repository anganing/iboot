package com.iboot.pf.module.wms.dal.mysql.outorder;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Collection;
import java.util.List;

/** 出库单 Mapper。 */
@Mapper
public interface WmsOutOrderMapper extends BaseMapperX<WmsOutOrderDO> {

    /** 按幂等键查出库单，创建命令判重使用。 */
    default WmsOutOrderDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsOutOrderDO::getIdempotencyKey, idempotencyKey);
    }

    default WmsOutOrderDO selectByOutNo(String outNo) {
        return selectOne(WmsOutOrderDO::getOutNo, outNo);
    }

    /**
     * 按出库单号集合批量查出库单，跨模块门面按单号反查用。
     *
     * <p>{@code out_no} 上有唯一索引 {@code uk_wms_out_order_no_deleted}，走索引一次 IN 查询；
     * 空集合由 {@code BaseMapperX#selectList(SFunction, Collection)} 直接返回空列表，不会拼出非法 SQL。
     */
    default List<WmsOutOrderDO> selectListByOutNos(Collection<String> outNos) {
        return selectList(WmsOutOrderDO::getOutNo, outNos);
    }

    default PageResult<OutOrderRespVO> selectPage(OutOrderPageReqVO reqVO) {
        IPage<OutOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    OutOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<OutOrderRespVO> selectJoinPage(IPage<OutOrderRespVO> page, @Param("req") OutOrderPageReqVO reqVO);
}
