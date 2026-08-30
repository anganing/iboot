package com.iboot.pf.module.aps.dal.mysql.shortage;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ShortageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.*;

/**
 * APS-排程缺料清单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ShortageMapper extends BaseMapperX<ShortageDO> {

    default PageResult<ShortageRespVO> selectPage(ShortagePageReqVO reqVO) {
        IPage<ShortageRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ShortageRespVO selectJoinById(@Param("id") Long id);

    IPage<ShortageRespVO> selectJoinPage(IPage<ShortageRespVO> page, @Param("req") ShortagePageReqVO reqVO);

}