package com.iboot.pf.module.wms.dal.mysql.loc;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.loc.LocDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.loc.vo.*;

/**
 * WMS-库位 Mapper
 *
 * @author iboot
 */
@Mapper
public interface LocMapper extends BaseMapperX<LocDO> {

    default PageResult<LocRespVO> selectPage(LocPageReqVO reqVO) {
        IPage<LocRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    LocRespVO selectJoinById(@Param("id") Long id);

    IPage<LocRespVO> selectJoinPage(IPage<LocRespVO> page, @Param("req") LocPageReqVO reqVO);

}