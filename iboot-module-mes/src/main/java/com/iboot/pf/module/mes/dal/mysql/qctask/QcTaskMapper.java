package com.iboot.pf.module.mes.dal.mysql.qctask;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskRespVO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES-质检任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface QcTaskMapper extends BaseMapperX<QcTaskDO> {

    default PageResult<QcTaskRespVO> selectPage(QcTaskPageReqVO reqVO) {
        IPage<QcTaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    QcTaskRespVO selectJoinById(@Param("id") Long id);

    IPage<QcTaskRespVO> selectJoinPage(IPage<QcTaskRespVO> page, @Param("req") QcTaskPageReqVO reqVO);

    /** 按任务号查询，唯一索引 uk_mes_qc_task_no_deleted 的应用层前置校验。 */
    default QcTaskDO selectByTaskNo(String taskNo) {
        return selectOne(QcTaskDO::getTaskNo, taskNo);
    }

    /** 按幂等键查询，唯一索引 uk_mes_qc_task_idem_deleted 的应用层前置校验。 */
    default QcTaskDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(QcTaskDO::getIdempotencyKey, idempotencyKey);
    }

    /** 统计命中某质检规则的任务数，规则删除前校验占用。 */
    default Long selectCountByRuleId(Long ruleId) {
        return selectCount(QcTaskDO::getRuleId, ruleId);
    }

}
