package com.iboot.pf.module.wms.dal.mysql.invtxn;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.invtxn.InvTxnDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.invtxn.vo.*;

/**
 * WMS-库存流水 Mapper
 *
 * @author iboot
 */
@Mapper
public interface InvTxnMapper extends BaseMapperX<InvTxnDO> {

    default PageResult<InvTxnRespVO> selectPage(InvTxnPageReqVO reqVO) {
        IPage<InvTxnRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    InvTxnRespVO selectJoinById(@Param("id") Long id);

    IPage<InvTxnRespVO> selectJoinPage(IPage<InvTxnRespVO> page, @Param("req") InvTxnPageReqVO reqVO);

}