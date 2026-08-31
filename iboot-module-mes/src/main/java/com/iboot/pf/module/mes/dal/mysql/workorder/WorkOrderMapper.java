package com.iboot.pf.module.mes.dal.mysql.workorder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderRespVO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * MES-作业工单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WorkOrderMapper extends BaseMapperX<WorkOrderDO> {

    default PageResult<WorkOrderRespVO> selectPage(WorkOrderPageReqVO reqVO) {
        IPage<WorkOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    WorkOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<WorkOrderRespVO> selectJoinPage(IPage<WorkOrderRespVO> page, @Param("req") WorkOrderPageReqVO reqVO);

    /** 按作业工单号查询，唯一索引 uk_mes_work_order_no_deleted 的应用层前置校验。 */
    default WorkOrderDO selectByWorkOrderNo(String workOrderNo) {
        return selectOne(WorkOrderDO::getWorkOrderNo, workOrderNo);
    }

    /** 统计某生产工单下的作业工单数，生产工单删除前校验占用。 */
    default Long selectCountByProdOrderId(Long prodOrderId) {
        return selectCount(WorkOrderDO::getProdOrderId, prodOrderId);
    }

    default List<WorkOrderDO> selectListByProdOrderIds(Collection<Long> prodOrderIds) {
        return selectList(WorkOrderDO::getProdOrderId, prodOrderIds);
    }

}
