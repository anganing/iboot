package com.iboot.pf.module.wms.dal.mysql.zone;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsZoneDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.zone.vo.*;

/**
 * WMS-库区 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ZoneMapper extends BaseMapperX<WmsZoneDO> {

    default PageResult<ZoneRespVO> selectPage(ZonePageReqVO reqVO) {
        IPage<ZoneRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ZoneRespVO selectJoinById(@Param("id") Long id);

    IPage<ZoneRespVO> selectJoinPage(IPage<ZoneRespVO> page, @Param("req") ZonePageReqVO reqVO);


    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default WmsZoneDO selectByWhIdAndCode(Long whId, String code) {
        return selectOne(WmsZoneDO::getWhId, whId, WmsZoneDO::getCode, code);
    }

}