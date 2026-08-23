package com.iboot.pf.module.mdm.dal.mysql.matcat;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.matcat.vo.*;

/**
 * 物料分类 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MatCatMapper extends BaseMapperX<MatCatDO> {

    default List<MatCatDO> selectList(MatCatListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MatCatDO>()
                .eqIfPresent(MatCatDO::getParentId, reqVO.getParentId())
                .eqIfPresent(MatCatDO::getCode, reqVO.getCode())
                .likeIfPresent(MatCatDO::getName, reqVO.getName())
                .eqIfPresent(MatCatDO::getStatus, reqVO.getStatus())
                .eqIfPresent(MatCatDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(MatCatDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MatCatDO::getId));
    }

	default MatCatDO selectByParentIdAndName(Long parentId, String name) {
	    return selectOne(MatCatDO::getParentId, parentId, MatCatDO::getName, name);
	}

    default Long selectCountByParentId(Long parentId) {
        return selectCount(MatCatDO::getParentId, parentId);
    }

}