package com.iboot.pf.module.mdm.dal.mysql.uom;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.UomPageReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UomMapper extends BaseMapperX<UomDO> {

    default UomDO selectByCode(String code) {
        return selectOne(UomDO::getCode, code);
    }

    default List<UomDO> selectListByCodes(Collection<String> codes) {
        if (CollUtil.isEmpty(codes)) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<UomDO>().in(UomDO::getCode, codes));
    }

    default PageResult<UomDO> selectPage(UomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UomDO>()
                .likeIfPresent(UomDO::getCode, reqVO.getCode())
                .likeIfPresent(UomDO::getName, reqVO.getName())
                .eqIfPresent(UomDO::getStatus, reqVO.getStatus())
                .orderByDesc(UomDO::getId));
    }
}
