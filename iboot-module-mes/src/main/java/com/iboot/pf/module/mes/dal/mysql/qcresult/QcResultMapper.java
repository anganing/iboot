package com.iboot.pf.module.mes.dal.mysql.qcresult;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.*;

/**
 * MES-检验结果 Mapper
 *
 * @author iboot
 */
@Mapper
public interface QcResultMapper extends BaseMapperX<QcResultDO> {

    default PageResult<QcResultRespVO> selectPage(QcResultPageReqVO reqVO) {
        IPage<QcResultRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    QcResultRespVO selectJoinById(@Param("id") Long id);

    IPage<QcResultRespVO> selectJoinPage(IPage<QcResultRespVO> page, @Param("req") QcResultPageReqVO reqVO);

}