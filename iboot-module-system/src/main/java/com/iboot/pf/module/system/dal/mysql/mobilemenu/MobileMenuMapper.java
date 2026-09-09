package com.iboot.pf.module.system.dal.mysql.mobilemenu;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuListReqVO;
import com.iboot.pf.module.system.dal.dataobject.mobilemenu.MobileMenuDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MobileMenuMapper extends BaseMapperX<MobileMenuDO> {

    default List<MobileMenuDO> selectListByStatus(Integer status) {
        return selectList(MobileMenuDO::getStatus, status);
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(MobileMenuDO::getParentId, parentId);
    }

    default List<MobileMenuDO> selectList(MobileMenuListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MobileMenuDO>()
                .likeIfPresent(MobileMenuDO::getName, reqVO.getName())
                .eqIfPresent(MobileMenuDO::getStatus, reqVO.getStatus()));
    }

}
