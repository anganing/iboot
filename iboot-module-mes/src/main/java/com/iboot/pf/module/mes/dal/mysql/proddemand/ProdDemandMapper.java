package com.iboot.pf.module.mes.dal.mysql.proddemand;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandPageReqVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandRespVO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES-生产需求 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProdDemandMapper extends BaseMapperX<ProdDemandDO> {

    default PageResult<ProdDemandRespVO> selectPage(ProdDemandPageReqVO reqVO) {
        IPage<ProdDemandRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProdDemandRespVO selectJoinById(@Param("id") Long id);

    IPage<ProdDemandRespVO> selectJoinPage(IPage<ProdDemandRespVO> page, @Param("req") ProdDemandPageReqVO reqVO);

    /** 按需求编号查询，唯一索引 uk_mes_prod_demand_no_deleted 的应用层前置校验。 */
    default ProdDemandDO selectByDemandNo(String demandNo) {
        return selectOne(ProdDemandDO::getDemandNo, demandNo);
    }

    /** 按幂等键查询，唯一索引 uk_mes_prod_demand_idem_deleted 的应用层前置校验。 */
    default ProdDemandDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(ProdDemandDO::getIdempotencyKey, idempotencyKey);
    }

}
