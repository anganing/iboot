package com.iboot.pf.module.mdm.dal.mysql.matcat;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.matcat.vo.*;

/**
 * 物料分类 Mapper
 *
 * @author iboot
 */
@Mapper
public interface MatCatMapper extends BaseMapperX<MatCatDO> {

    default List<MatCatRespVO> selectList(MatCatListReqVO reqVO) {
        return selectJoinList(reqVO);
    }

    List<MatCatRespVO> selectJoinList(@Param("req") MatCatListReqVO reqVO);

    MatCatRespVO selectJoinById(@Param("id") Long id);

	default MatCatDO selectByParentIdAndName(Long parentId, String name) {
	    return selectOne(MatCatDO::getParentId, parentId, MatCatDO::getName, name);
	}

    default Long selectCountByParentId(Long parentId) {
        return selectCount(MatCatDO::getParentId, parentId);
    }

}
