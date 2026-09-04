package com.iboot.pf.module.wms.dal.mysql.transfer;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.transfer.WmsTransferOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 库存转移调拨单 Mapper。 */
@Mapper
public interface WmsTransferOrderMapper extends BaseMapperX<WmsTransferOrderDO> {

    /** 按幂等键查转移单，创建命令判重使用。 */
    default WmsTransferOrderDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsTransferOrderDO::getIdempotencyKey, idempotencyKey);
    }

    default WmsTransferOrderDO selectByTransferNo(String transferNo) {
        return selectOne(WmsTransferOrderDO::getTransferNo, transferNo);
    }

    default PageResult<TransferOrderRespVO> selectPage(TransferOrderPageReqVO reqVO) {
        IPage<TransferOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    TransferOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<TransferOrderRespVO> selectJoinPage(IPage<TransferOrderRespVO> page, @Param("req") TransferOrderPageReqVO reqVO);
}
