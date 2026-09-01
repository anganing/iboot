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

import java.util.Collection;
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

    /**
     * 按资源编号集合批量查询资源能力。
     *
     * <p>一次 IN 查询取回全部，供 APS 资源投影落能力快照；不允许调用方按资源逐条查造成 N+1。
     */
    default List<ResCapabilityDO> selectListByResIds(Collection<Long> resIds) {
        if (resIds == null || resIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<ResCapabilityDO>()
                .in(ResCapabilityDO::getResId, resIds)
                .orderByAsc(ResCapabilityDO::getResId)
                .orderByDesc(ResCapabilityDO::getPreferredFlag));
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
