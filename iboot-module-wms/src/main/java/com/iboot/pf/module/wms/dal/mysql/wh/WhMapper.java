package com.iboot.pf.module.wms.dal.mysql.wh;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.wh.WhDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.wh.vo.*;

/**
 * WMS-仓库 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WhMapper extends BaseMapperX<WhDO> {

    default PageResult<WhRespVO> selectPage(WhPageReqVO reqVO) {
        IPage<WhRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    WhRespVO selectJoinById(@Param("id") Long id);

    IPage<WhRespVO> selectJoinPage(IPage<WhRespVO> page, @Param("req") WhPageReqVO reqVO);

}