package com.iboot.pf.module.mdm.dal.mysql.mat;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.*;

/**
 * 物料主档 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MatMapper extends BaseMapperX<MatDO> {

    default PageResult<MatDO> selectPage(MatPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MatDO>()
                .eqIfPresent(MatDO::getCatId, reqVO.getCatId())
                .eqIfPresent(MatDO::getCode, reqVO.getCode())
                .likeIfPresent(MatDO::getName, reqVO.getName())
                .eqIfPresent(MatDO::getSpec, reqVO.getSpec())
                .eqIfPresent(MatDO::getDrawingNo, reqVO.getDrawingNo())
                .eqIfPresent(MatDO::getMatType, reqVO.getMatType())
                .eqIfPresent(MatDO::getBaseUomId, reqVO.getBaseUomId())
                .eqIfPresent(MatDO::getBatchFlag, reqVO.getBatchFlag())
                .eqIfPresent(MatDO::getSnFlag, reqVO.getSnFlag())
                .eqIfPresent(MatDO::getShelfLifeDays, reqVO.getShelfLifeDays())
                .eqIfPresent(MatDO::getStatus, reqVO.getStatus())
                .eqIfPresent(MatDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(MatDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MatDO::getId));
    }

}