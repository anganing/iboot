package com.iboot.pf.module.mes.dal.mysql.rework;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkRespVO;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES-返工任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ReworkMapper extends BaseMapperX<ReworkDO> {

    default PageResult<ReworkRespVO> selectPage(ReworkPageReqVO reqVO) {
        IPage<ReworkRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ReworkRespVO selectJoinById(@Param("id") Long id);

    IPage<ReworkRespVO> selectJoinPage(IPage<ReworkRespVO> page, @Param("req") ReworkPageReqVO reqVO);

    /** 按返工任务编号查询，唯一索引 uk_mes_rework_no_deleted 的应用层前置校验。 */
    default ReworkDO selectByReworkNo(String reworkNo) {
        return selectOne(ReworkDO::getReworkNo, reworkNo);
    }

}
