package com.iboot.pf.module.mes.dal.mysql.wiptxn;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.*;

/**
 * MES-在制流转流水 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WipTxnMapper extends BaseMapperX<WipTxnDO> {

    default PageResult<WipTxnRespVO> selectPage(WipTxnPageReqVO reqVO) {
        IPage<WipTxnRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    WipTxnRespVO selectJoinById(@Param("id") Long id);

    IPage<WipTxnRespVO> selectJoinPage(IPage<WipTxnRespVO> page, @Param("req") WipTxnPageReqVO reqVO);

}