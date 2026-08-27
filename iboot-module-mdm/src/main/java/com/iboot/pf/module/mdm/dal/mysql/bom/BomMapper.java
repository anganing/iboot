package com.iboot.pf.module.mdm.dal.mysql.bom;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.bom.BomDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.*;

/**
 * 物料清单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface BomMapper extends BaseMapperX<BomDO> {

    default PageResult<BomRespVO> selectPage(BomPageReqVO reqVO) {
        IPage<BomRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    BomRespVO selectJoinById(@Param("id") Long id);

    IPage<BomRespVO> selectJoinPage(IPage<BomRespVO> page, @Param("req") BomPageReqVO reqVO);

}
