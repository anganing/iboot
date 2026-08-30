package com.iboot.pf.module.wms.dal.mysql.batchmgr;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.batchmgr.BatchDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.*;

/**
 * WMS-批次主档 Mapper
 *
 * @author iboot
 */
@Mapper
public interface BatchMapper extends BaseMapperX<BatchDO> {

    default PageResult<BatchRespVO> selectPage(BatchPageReqVO reqVO) {
        IPage<BatchRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    BatchRespVO selectJoinById(@Param("id") Long id);

    IPage<BatchRespVO> selectJoinPage(IPage<BatchRespVO> page, @Param("req") BatchPageReqVO reqVO);

}