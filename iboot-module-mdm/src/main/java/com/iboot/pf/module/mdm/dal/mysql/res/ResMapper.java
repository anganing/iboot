package com.iboot.pf.module.mdm.dal.mysql.res;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.res.vo.*;

/**
 * 生产资源 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ResMapper extends BaseMapperX<ResDO> {

    default PageResult<ResRespVO> selectPage(ResPageReqVO reqVO) {
        IPage<ResRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ResRespVO selectJoinById(@Param("id") Long id);

    IPage<ResRespVO> selectJoinPage(IPage<ResRespVO> page, @Param("req") ResPageReqVO reqVO);

}
