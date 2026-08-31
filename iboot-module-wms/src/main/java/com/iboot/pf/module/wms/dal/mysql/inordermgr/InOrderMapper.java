package com.iboot.pf.module.wms.dal.mysql.inordermgr;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.inordermgr.InOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.*;

/**
 * WMS-入库单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface InOrderMapper extends BaseMapperX<InOrderDO> {

    default PageResult<InOrderRespVO> selectPage(InOrderPageReqVO reqVO) {
        IPage<InOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    InOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<InOrderRespVO> selectJoinPage(IPage<InOrderRespVO> page, @Param("req") InOrderPageReqVO reqVO);


    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default InOrderDO selectByInNo(String inNo) {
        return selectOne(InOrderDO::getInNo, inNo);
    }

}