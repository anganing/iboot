package com.iboot.pf.module.wms.dal.mysql.countorder;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.countorder.CountOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.countorder.vo.*;

/**
 * WMS-盘点单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface CountOrderMapper extends BaseMapperX<CountOrderDO> {

    default PageResult<CountOrderRespVO> selectPage(CountOrderPageReqVO reqVO) {
        IPage<CountOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    CountOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<CountOrderRespVO> selectJoinPage(IPage<CountOrderRespVO> page, @Param("req") CountOrderPageReqVO reqVO);


    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default CountOrderDO selectByCountNo(String countNo) {
        return selectOne(CountOrderDO::getCountNo, countNo);
    }

}