package com.iboot.pf.module.mdm.dal.mysql.matalt;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.matalt.MatAltDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.matalt.vo.*;

/**
 * 替代料 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MatAltMapper extends BaseMapperX<MatAltDO> {

    default PageResult<MatAltRespVO> selectPage(MatAltPageReqVO reqVO) {
        IPage<MatAltRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    MatAltRespVO selectJoinById(@Param("id") Long id);

    IPage<MatAltRespVO> selectJoinPage(IPage<MatAltRespVO> page, @Param("req") MatAltPageReqVO reqVO);

}
