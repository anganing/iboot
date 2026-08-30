package com.iboot.pf.module.mes.dal.mysql.defect;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.defect.vo.*;

/**
 * MES-缺陷记录 Mapper
 *
 * @author iboot
 */
@Mapper
public interface DefectMapper extends BaseMapperX<DefectDO> {

    default PageResult<DefectRespVO> selectPage(DefectPageReqVO reqVO) {
        IPage<DefectRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    DefectRespVO selectJoinById(@Param("id") Long id);

    IPage<DefectRespVO> selectJoinPage(IPage<DefectRespVO> page, @Param("req") DefectPageReqVO reqVO);

}