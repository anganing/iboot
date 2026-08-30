package com.iboot.pf.module.mes.dal.mysql.materialreq;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.*;

/**
 * MES-领退补料申请 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MaterialReqMapper extends BaseMapperX<MaterialReqDO> {

    default PageResult<MaterialReqRespVO> selectPage(MaterialReqPageReqVO reqVO) {
        IPage<MaterialReqRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    MaterialReqRespVO selectJoinById(@Param("id") Long id);

    IPage<MaterialReqRespVO> selectJoinPage(IPage<MaterialReqRespVO> page, @Param("req") MaterialReqPageReqVO reqVO);

}