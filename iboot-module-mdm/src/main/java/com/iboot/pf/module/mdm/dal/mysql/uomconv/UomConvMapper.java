package com.iboot.pf.module.mdm.dal.mysql.uomconv;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.uomconv.UomConvDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.uomconv.vo.*;

/**
 * 单位换算 Mapper
 *
 * @author iboot
 */
@Mapper
public interface UomConvMapper extends BaseMapperX<UomConvDO> {

    default PageResult<UomConvRespVO> selectPage(UomConvPageReqVO reqVO) {
        IPage<UomConvRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    UomConvRespVO selectJoinById(@Param("id") Long id);

    IPage<UomConvRespVO> selectJoinPage(IPage<UomConvRespVO> page, @Param("req") UomConvPageReqVO reqVO);

}
