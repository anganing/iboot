package com.iboot.pf.module.mes.dal.mysql.prodorder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderRespVO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MES-生产工单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProdOrderMapper extends BaseMapperX<ProdOrderDO> {

    default PageResult<ProdOrderRespVO> selectPage(ProdOrderPageReqVO reqVO) {
        IPage<ProdOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProdOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<ProdOrderRespVO> selectJoinPage(IPage<ProdOrderRespVO> page, @Param("req") ProdOrderPageReqVO reqVO);

    /** 按工单号查询，唯一索引 uk_mes_prod_order_no_deleted 的应用层前置校验。 */
    default ProdOrderDO selectByOrderNo(String orderNo) {
        return selectOne(ProdOrderDO::getOrderNo, orderNo);
    }

    /** 统计某生产需求已生成的工单数，需求删除前校验占用。 */
    default Long selectCountByDemandId(Long demandId) {
        return selectCount(ProdOrderDO::getDemandId, demandId);
    }

    default List<ProdOrderDO> selectListByPlantId(Long plantId) {
        return selectList(new LambdaQueryWrapperX<ProdOrderDO>()
                .eqIfPresent(ProdOrderDO::getPlantId, plantId)
                .orderByDesc(ProdOrderDO::getId));
    }

}
