package com.iboot.pf.module.mes.dal.mysql.dispatchtask;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskRespVO;
import com.iboot.pf.module.mes.dal.dataobject.dispatchtask.DispatchTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES-派工任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface DispatchTaskMapper extends BaseMapperX<DispatchTaskDO> {

    default PageResult<DispatchTaskRespVO> selectPage(DispatchTaskPageReqVO reqVO) {
        IPage<DispatchTaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    DispatchTaskRespVO selectJoinById(@Param("id") Long id);

    IPage<DispatchTaskRespVO> selectJoinPage(IPage<DispatchTaskRespVO> page, @Param("req") DispatchTaskPageReqVO reqVO);

    /** 按派工单号查询，唯一索引 uk_mes_dispatch_no_deleted 的应用层前置校验。 */
    default DispatchTaskDO selectByDispatchNo(String dispatchNo) {
        return selectOne(DispatchTaskDO::getDispatchNo, dispatchNo);
    }

    /** 统计某工序任务下的派工数，工序任务删除前校验占用。 */
    default Long selectCountByOpTaskId(Long opTaskId) {
        return selectCount(DispatchTaskDO::getOpTaskId, opTaskId);
    }

}
