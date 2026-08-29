package com.iboot.pf.module.mdm.dal.mysql.rescapability;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilityPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilityRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.rescapability.ResCapabilityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源能力 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ResCapabilityMapper extends BaseMapperX<ResCapabilityDO> {

    default PageResult<ResCapabilityRespVO> selectJoinPage(ResCapabilityPageReqVO reqVO) {
        IPage<ResCapabilityRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ResCapabilityRespVO selectJoinById(@Param("id") Long id);

    IPage<ResCapabilityRespVO> selectJoinPage(IPage<ResCapabilityRespVO> page, @Param("req") ResCapabilityPageReqVO reqVO);

    default List<ResCapabilityDO> selectListByOpId(Long opId) {
        return selectList(new LambdaQueryWrapperX<ResCapabilityDO>()
                .eq(ResCapabilityDO::getOpId, opId)
                .orderByDesc(ResCapabilityDO::getPreferredFlag));
    }

    default List<ResCapabilityDO> selectListByResId(Long resId) {
        return selectList(ResCapabilityDO::getResId, resId);
    }

    default ResCapabilityDO selectByResIdAndOpId(Long resId, Long opId) {
        return selectOne(new LambdaQueryWrapperX<ResCapabilityDO>()
                .eq(ResCapabilityDO::getResId, resId)
                .eq(ResCapabilityDO::getOpId, opId));
    }

    default int deleteByResId(Long resId) {
        return delete(ResCapabilityDO::getResId, resId);
    }

}
