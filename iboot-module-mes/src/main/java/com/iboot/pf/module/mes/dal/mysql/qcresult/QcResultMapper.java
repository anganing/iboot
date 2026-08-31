package com.iboot.pf.module.mes.dal.mysql.qcresult;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultRespVO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MES-检验结果 Mapper
 *
 * @author iboot
 */
@Mapper
public interface QcResultMapper extends BaseMapperX<QcResultDO> {

    default PageResult<QcResultRespVO> selectPage(QcResultPageReqVO reqVO) {
        IPage<QcResultRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    QcResultRespVO selectJoinById(@Param("id") Long id);

    IPage<QcResultRespVO> selectJoinPage(IPage<QcResultRespVO> page, @Param("req") QcResultPageReqVO reqVO);

    /** 按任务 + 检验项 + 样本序号查询，唯一索引 uk_mes_qc_result_sample_deleted 的应用层前置校验。 */
    default QcResultDO selectBySample(Long taskId, Long itemId, Integer sampleNo) {
        return selectOne(QcResultDO::getTaskId, taskId, QcResultDO::getItemId, itemId,
                QcResultDO::getSampleNo, sampleNo);
    }

    /** 统计某质检任务下的检验结果数，任务删除前校验占用。 */
    default Long selectCountByTaskId(Long taskId) {
        return selectCount(QcResultDO::getTaskId, taskId);
    }

    default List<QcResultDO> selectListByTaskId(Long taskId) {
        return selectList(QcResultDO::getTaskId, taskId);
    }

}
