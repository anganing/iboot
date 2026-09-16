package com.iboot.pf.server.integration.fixture;

import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandSaveReqVO;
import com.iboot.pf.module.mes.enums.MesDemandSourceTypeEnum;
import com.iboot.pf.module.mes.enums.MesDemandStatusEnum;
import com.iboot.pf.module.mes.service.proddemand.ProdDemandService;
import com.iboot.pf.server.integration.PilotLine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MES 数据工厂（T6-01）。
 *
 * 覆盖试点产线的生产需求：物料取成品，数量取 {@link PilotLine#DEMAND_QTY}，
 * 交期给未来某个固定时刻，保证 APS / 工单用例拿到稳定的输入。
 *
 * @author iboot
 */
@Component
public class MesFixture {

    /** 需求交期基准：固定值而非 now()，避免用例结果随执行时间漂移。 */
    private static final LocalDateTime DUE_DATE = LocalDateTime.of(2026, 12, 31, 18, 0);

    @Resource
    private ProdDemandService prodDemandService;

    /**
     * 建一条生产需求。
     *
     * @param line 试点产线句柄（用其 plantId / fgMatId / uomId）
     * @return 生产需求编号
     */
    public Long createProdDemand(PilotLine line) {
        ProdDemandSaveReqVO demand = new ProdDemandSaveReqVO();
        // demandNo 留空由 MES 编码规则（MES_DEMAND_NO）分配
        demand.setSourceType(MesDemandSourceTypeEnum.MANUAL.name());
        demand.setSourceNo("T6-01-SMOKE");
        demand.setPlantId(line.plantId());
        demand.setProjectId(0L);     // 0 表示无项目
        demand.setProjectTaskId(0L); // 0 表示无项目任务
        demand.setMatId(line.fgMatId());
        demand.setQty(PilotLine.DEMAND_QTY);
        demand.setUomId(line.uomId());
        demand.setDueDate(DUE_DATE);
        demand.setPriority(0);
        demand.setStatus(MesDemandStatusEnum.DRAFT.name());
        demand.setIdempotencyKey("INTEGRATION:T6-01-SMOKE:PROD_DEMAND");
        return prodDemandService.createProdDemand(demand);
    }

}
