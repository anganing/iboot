package com.iboot.pf.module.mdm.dal.mysql.changeover;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.changeover.vo.ResChangeoverPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.changeover.vo.ResChangeoverRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.changeover.ResChangeoverDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 资源换型矩阵 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ResChangeoverMapper extends BaseMapperX<ResChangeoverDO> {

    default PageResult<ResChangeoverRespVO> selectJoinPage(ResChangeoverPageReqVO reqVO) {
        IPage<ResChangeoverRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ResChangeoverRespVO selectJoinById(@Param("id") Long id);

    IPage<ResChangeoverRespVO> selectJoinPage(IPage<ResChangeoverRespVO> page, @Param("req") ResChangeoverPageReqVO reqVO);

    default ResChangeoverDO selectByResIdAndGroups(Long resId, String fromGroup, String toGroup) {
        return selectOne(new LambdaQueryWrapperX<ResChangeoverDO>()
                .eq(ResChangeoverDO::getResId, resId)
                .eq(ResChangeoverDO::getFromGroup, fromGroup)
                .eq(ResChangeoverDO::getToGroup, toGroup));
    }

}
