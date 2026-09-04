package com.iboot.pf.module.wms.dal.mysql.warehouse;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.wh.vo.*;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/** 仓库 Mapper。 */
@Mapper
public interface WmsWhMapper extends BaseMapperX<WmsWhDO> {

    default PageResult<WhRespVO> selectPage(WhPageReqVO reqVO) {
        IPage<WhRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    /**
     * 按库存组织 + 编码查询，唯一索引 uk_wms_wh_org_code_deleted 的应用层前置校验。
     */
    default WmsWhDO selectByInvOrgIdAndCode(Long invOrgId, String code) {
        return selectOne(WmsWhDO::getInvOrgId, invOrgId, WmsWhDO::getCode, code);
    }

    WhRespVO selectJoinById(@Param("id") Long id);

    IPage<WhRespVO> selectJoinPage(IPage<WhRespVO> page, @Param("req") WhPageReqVO reqVO);
}
