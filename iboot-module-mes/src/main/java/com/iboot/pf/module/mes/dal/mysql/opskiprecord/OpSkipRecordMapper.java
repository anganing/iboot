package com.iboot.pf.module.mes.dal.mysql.opskiprecord;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.opskiprecord.OpSkipRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.*;

/**
 * MES-工序跳过记录 Mapper
 *
 * @author iboot
 */
@Mapper
public interface OpSkipRecordMapper extends BaseMapperX<OpSkipRecordDO> {

    default PageResult<OpSkipRecordRespVO> selectPage(OpSkipRecordPageReqVO reqVO) {
        IPage<OpSkipRecordRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    OpSkipRecordRespVO selectJoinById(@Param("id") Long id);

    IPage<OpSkipRecordRespVO> selectJoinPage(IPage<OpSkipRecordRespVO> page, @Param("req") OpSkipRecordPageReqVO reqVO);

}