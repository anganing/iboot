package com.iboot.pf.module.mes.api.report;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.mes.api.report.dto.ProdReportReqDTO;
import com.iboot.pf.module.mes.api.report.dto.ProdReportRespDTO;
import com.iboot.pf.module.mes.api.report.dto.WipTxnRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.service.prodreport.MesProdReportDomainService;
import com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 生产报工 API 实现。报工事实写入 MES，在库事实由 WMS 维护。
 *
 * <p>门面只做转换与转调：报工转调 {@link MesProdReportDomainService}（数量只能由报工推进链改，
 * 门面自己写工序任务的完成数就等于开了第二条写入路径），流水查询直接读账表。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：报工与在制流水是车间现场事实，
 * 与调用方的登录用户所在部门无关。
 */
@Service
@Validated
@DataPermission(enable = false)
public class ProdReportApiImpl implements ProdReportApi {

    @Resource
    private MesProdReportDomainService prodReportDomainService;
    @Resource
    private ProdReportMapper prodReportMapper;
    @Resource
    private WipTxnMapper wipTxnMapper;

    @Override
    public ProdReportRespDTO reportProd(ProdReportReqDTO reqDTO) {
        // 领域方法返回的是报工记录编号，门面契约要的是整条记录事实：按编号回读一次。
        // 幂等命中时返回的是首次那条记录的编号，因此重复下发拿回的也是同一条记录，语义正确
        Long reportId = prodReportDomainService.submitReport(BeanUtils.toBean(reqDTO, MesSubmitReportBO.class));
        return BeanUtils.toBean(prodReportMapper.selectById(reportId), ProdReportRespDTO.class);
    }

    @Override
    public List<WipTxnRespDTO> getWipTxnList(Collection<Long> prodOrderIds) {
        if (CollUtil.isEmpty(prodOrderIds)) {
            return List.of();
        }
        // 一次 IN 查询取回全部工单的流水，不逐个工单查
        List<WipTxnDO> txnList = wipTxnMapper.selectList(new LambdaQueryWrapperX<WipTxnDO>()
                .in(WipTxnDO::getProdOrderId, prodOrderIds)
                .orderByAsc(WipTxnDO::getId));
        return BeanUtils.toBean(txnList, WipTxnRespDTO.class);
    }

}
