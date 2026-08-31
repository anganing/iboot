package com.iboot.pf.module.wms.dal.mysql.outordermgr;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.outordermgr.OutOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;

/**
 * WMS-出库单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface OutOrderMapper extends BaseMapperX<OutOrderDO> {

    default PageResult<OutOrderRespVO> selectPage(OutOrderPageReqVO reqVO) {
        IPage<OutOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    OutOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<OutOrderRespVO> selectJoinPage(IPage<OutOrderRespVO> page, @Param("req") OutOrderPageReqVO reqVO);


    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default OutOrderDO selectByOutNo(String outNo) {
        return selectOne(OutOrderDO::getOutNo, outNo);
    }

}