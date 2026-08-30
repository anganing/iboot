package com.iboot.pf.module.mes.dal.mysql.qcitem;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.*;

/**
 * MES-检验项目 Mapper
 *
 * @author iboot
 */
@Mapper
public interface QcItemMapper extends BaseMapperX<QcItemDO> {

    default PageResult<QcItemRespVO> selectPage(QcItemPageReqVO reqVO) {
        IPage<QcItemRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    QcItemRespVO selectJoinById(@Param("id") Long id);

    IPage<QcItemRespVO> selectJoinPage(IPage<QcItemRespVO> page, @Param("req") QcItemPageReqVO reqVO);

}