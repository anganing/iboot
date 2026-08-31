package com.iboot.pf.module.wms.dal.mysql.invlock;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.invlock.InvLockDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.invlock.vo.*;

/**
 * WMS-库存冻结记录 Mapper
 *
 * @author iboot
 */
@Mapper
public interface InvLockMapper extends BaseMapperX<InvLockDO> {

    default PageResult<InvLockRespVO> selectPage(InvLockPageReqVO reqVO) {
        IPage<InvLockRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    InvLockRespVO selectJoinById(@Param("id") Long id);

    IPage<InvLockRespVO> selectJoinPage(IPage<InvLockRespVO> page, @Param("req") InvLockPageReqVO reqVO);


    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default InvLockDO selectByLockNo(String lockNo) {
        return selectOne(InvLockDO::getLockNo, lockNo);
    }

}