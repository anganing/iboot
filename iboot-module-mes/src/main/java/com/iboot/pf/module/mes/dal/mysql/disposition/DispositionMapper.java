package com.iboot.pf.module.mes.dal.mysql.disposition;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.*;

/**
 * MES-不合格品处置单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface DispositionMapper extends BaseMapperX<DispositionDO> {

    default PageResult<DispositionRespVO> selectPage(DispositionPageReqVO reqVO) {
        IPage<DispositionRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    DispositionRespVO selectJoinById(@Param("id") Long id);

    IPage<DispositionRespVO> selectJoinPage(IPage<DispositionRespVO> page, @Param("req") DispositionPageReqVO reqVO);

}