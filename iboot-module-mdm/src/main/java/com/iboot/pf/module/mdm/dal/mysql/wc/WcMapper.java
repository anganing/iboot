package com.iboot.pf.module.mdm.dal.mysql.wc;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.wc.vo.*;

/**
 * 工作中心 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WcMapper extends BaseMapperX<WcDO> {

    default PageResult<WcRespVO> selectPage(WcPageReqVO reqVO) {
        IPage<WcRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    WcRespVO selectJoinById(@Param("id") Long id);

    IPage<WcRespVO> selectJoinPage(IPage<WcRespVO> page, @Param("req") WcPageReqVO reqVO);

}
