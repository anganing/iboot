package com.iboot.pf.module.wms.dal.mysql.outorder;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

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

    default PageResult<OutOrderRespVO> selectPage(OutOrderPageReqVO reqVO) {
        IPage<OutOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    OutOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<OutOrderRespVO> selectJoinPage(IPage<OutOrderRespVO> page, @Param("req") OutOrderPageReqVO reqVO);
}
