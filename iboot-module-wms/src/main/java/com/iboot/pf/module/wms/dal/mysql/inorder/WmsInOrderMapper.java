package com.iboot.pf.module.wms.dal.mysql.inorder;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 入库单 Mapper。 */
@Mapper
public interface WmsInOrderMapper extends BaseMapperX<WmsInOrderDO> {

    /** 按幂等键查入库单，创建命令判重使用。 */
    default WmsInOrderDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsInOrderDO::getIdempotencyKey, idempotencyKey);
    }

    default WmsInOrderDO selectByInNo(String inNo) {
        return selectOne(WmsInOrderDO::getInNo, inNo);
    }

    default PageResult<InOrderRespVO> selectPage(InOrderPageReqVO reqVO) {
        IPage<InOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    InOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<InOrderRespVO> selectJoinPage(IPage<InOrderRespVO> page, @Param("req") InOrderPageReqVO reqVO);
}
