package com.iboot.pf.module.mes.dal.mysql.rgvtask;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskRespVO;
import com.iboot.pf.module.mes.dal.dataobject.rgvtask.RgvTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES-RGV工位流转任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface RgvTaskMapper extends BaseMapperX<RgvTaskDO> {

    default PageResult<RgvTaskRespVO> selectPage(RgvTaskPageReqVO reqVO) {
        IPage<RgvTaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    RgvTaskRespVO selectJoinById(@Param("id") Long id);

    IPage<RgvTaskRespVO> selectJoinPage(IPage<RgvTaskRespVO> page, @Param("req") RgvTaskPageReqVO reqVO);

    /** 按本地任务号查询，唯一索引 uk_mes_rgv_task_no_deleted 的应用层前置校验。 */
    default RgvTaskDO selectByTaskNo(String taskNo) {
        return selectOne(RgvTaskDO::getTaskNo, taskNo);
    }

    /** 按外部系统 + 外部任务号查询，唯一索引 uk_mes_rgv_external_deleted 的应用层前置校验。 */
    default RgvTaskDO selectByExternalTaskNo(String externalSystemCode, String externalTaskNo) {
        return selectOne(RgvTaskDO::getExternalSystemCode, externalSystemCode,
                RgvTaskDO::getExternalTaskNo, externalTaskNo);
    }

    /** 按幂等键查询，唯一索引 uk_mes_rgv_task_idem_deleted 的应用层前置校验。 */
    default RgvTaskDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(RgvTaskDO::getIdempotencyKey, idempotencyKey);
    }

}
