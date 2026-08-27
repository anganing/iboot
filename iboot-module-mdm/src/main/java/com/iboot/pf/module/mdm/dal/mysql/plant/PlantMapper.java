package com.iboot.pf.module.mdm.dal.mysql.plant;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.plant.PlantDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.plant.vo.*;

/**
 * 工厂 Mapper
 *
 * @author iboot
 */
@Mapper
public interface PlantMapper extends BaseMapperX<PlantDO> {

    default PageResult<PlantRespVO> selectPage(PlantPageReqVO reqVO) {
        IPage<PlantRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    PlantRespVO selectJoinById(@Param("id") Long id);

    IPage<PlantRespVO> selectJoinPage(IPage<PlantRespVO> page, @Param("req") PlantPageReqVO reqVO);

}
