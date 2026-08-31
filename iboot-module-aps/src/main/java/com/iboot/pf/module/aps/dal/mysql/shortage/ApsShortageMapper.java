package com.iboot.pf.module.aps.dal.mysql.shortage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortagePageReqVO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageRespVO;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ApsShortageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** APS-排程缺料清单 Mapper。 */
@Mapper
public interface ApsShortageMapper extends BaseMapperX<ApsShortageDO> {

    default PageResult<ShortageRespVO> selectPage(ShortagePageReqVO reqVO) {
        IPage<ShortageRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ShortageRespVO selectJoinById(@Param("id") Long id);

    IPage<ShortageRespVO> selectJoinPage(IPage<ShortageRespVO> page, @Param("req") ShortagePageReqVO reqVO);

    /** 按版本列出缺料，刷新缺料与跨模块查询都走它。 */
    default List<ApsShortageDO> selectListByVersionId(Long versionId) {
        return selectList(ApsShortageDO::getVersionId, versionId);
    }

}
