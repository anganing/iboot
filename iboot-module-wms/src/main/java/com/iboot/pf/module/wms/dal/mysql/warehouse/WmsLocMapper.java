package com.iboot.pf.module.wms.dal.mysql.warehouse;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.loc.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 库位 Mapper。 */
@Mapper
public interface WmsLocMapper extends BaseMapperX<WmsLocDO> {

    default PageResult<LocRespVO> selectPage(LocPageReqVO reqVO) {
        IPage<LocRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    LocRespVO selectJoinById(@Param("id") Long id);

    IPage<LocRespVO> selectJoinPage(IPage<LocRespVO> page, @Param("req") LocPageReqVO reqVO);

    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default WmsLocDO selectByWhIdAndCode(Long whId, String code) {
        return selectOne(WmsLocDO::getWhId, whId, WmsLocDO::getCode, code);
    }
}
