package com.iboot.pf.module.wms.dal.mysql.count;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 盘点单 Mapper。 */
@Mapper
public interface WmsCountOrderMapper extends BaseMapperX<WmsCountOrderDO> {

    default WmsCountOrderDO selectByCountNo(String countNo) {
        return selectOne(WmsCountOrderDO::getCountNo, countNo);
    }

    /** 按幂等键查盘点单，创建命令判重使用。 */
    default WmsCountOrderDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsCountOrderDO::getIdempotencyKey, idempotencyKey);
    }

    default PageResult<CountOrderRespVO> selectPage(CountOrderPageReqVO reqVO) {
        IPage<CountOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    CountOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<CountOrderRespVO> selectJoinPage(IPage<CountOrderRespVO> page, @Param("req") CountOrderPageReqVO reqVO);
}
