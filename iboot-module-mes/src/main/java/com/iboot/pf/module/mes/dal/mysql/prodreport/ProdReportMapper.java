package com.iboot.pf.module.mes.dal.mysql.prodreport;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportRespVO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES-生产报工记录 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ProdReportMapper extends BaseMapperX<ProdReportDO> {

    default PageResult<ProdReportRespVO> selectPage(ProdReportPageReqVO reqVO) {
        IPage<ProdReportRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProdReportRespVO selectJoinById(@Param("id") Long id);

    IPage<ProdReportRespVO> selectJoinPage(IPage<ProdReportRespVO> page, @Param("req") ProdReportPageReqVO reqVO);

    /** 按报工单号查询，唯一索引 uk_mes_prod_report_no_deleted 的应用层前置校验。 */
    default ProdReportDO selectByReportNo(String reportNo) {
        return selectOne(ProdReportDO::getReportNo, reportNo);
    }

    /** 按幂等键查询，唯一索引 uk_mes_prod_report_idem_deleted 的应用层前置校验。 */
    default ProdReportDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(ProdReportDO::getIdempotencyKey, idempotencyKey);
    }

}
