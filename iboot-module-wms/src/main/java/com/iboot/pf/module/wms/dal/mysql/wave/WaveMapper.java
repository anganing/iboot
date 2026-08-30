package com.iboot.pf.module.wms.dal.mysql.wave;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.wave.WaveDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.wave.vo.*;

/**
 * WMS-出库波次 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WaveMapper extends BaseMapperX<WaveDO> {

    default PageResult<WaveRespVO> selectPage(WavePageReqVO reqVO) {
        IPage<WaveRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    WaveRespVO selectJoinById(@Param("id") Long id);

    IPage<WaveRespVO> selectJoinPage(IPage<WaveRespVO> page, @Param("req") WavePageReqVO reqVO);

}