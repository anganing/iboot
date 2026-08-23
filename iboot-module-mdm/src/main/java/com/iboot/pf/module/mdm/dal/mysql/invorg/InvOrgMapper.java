package com.iboot.pf.module.mdm.dal.mysql.invorg;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.mdm.controller.admin.invorg.vo.InvOrgPageReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.invorg.InvOrgDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InvOrgMapper extends BaseMapperX<InvOrgDO> {

    default PageResult<InvOrgDO> selectPage(InvOrgPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InvOrgDO>()
                .eqIfPresent(InvOrgDO::getCode, reqVO.getCode())
                .likeIfPresent(InvOrgDO::getName, reqVO.getName())
                .eqIfPresent(InvOrgDO::getStatus, reqVO.getStatus())
                .orderByDesc(InvOrgDO::getId));
    }

    default InvOrgDO selectByCode(String code) {
        return selectOne(InvOrgDO::getCode, code);
    }
}
