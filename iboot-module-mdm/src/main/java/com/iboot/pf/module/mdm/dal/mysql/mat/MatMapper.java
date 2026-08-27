package com.iboot.pf.module.mdm.dal.mysql.mat;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.*;

/**
 * 物料主档 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MatMapper extends BaseMapperX<MatDO> {

    default PageResult<MatRespVO> selectPage(MatPageReqVO reqVO) {
        IPage<MatRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    MatRespVO selectJoinById(@Param("id") Long id);

    IPage<MatRespVO> selectJoinPage(IPage<MatRespVO> page, @Param("req") MatPageReqVO reqVO);

}
