package com.iboot.pf.module.mes.service.disposition.handler;

import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_BALANCE_NOT_EXISTS;

/** 质量处置统一按质检任务定位实际库存维度。 */
@Component
public class DispositionInventoryResolver {

    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private InventoryApi inventoryApi;

    public ResolvedInventory resolve(DispositionDO disposition) {
        QcTaskDO qcTask = qcTaskMapper.selectById(disposition.getTaskId());
        if (qcTask == null) {
            throw exception(QC_TASK_NOT_EXISTS);
        }
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setMatId(qcTask.getMatId());
        query.setBatchId(qcTask.getBatchId());
        query.setQualifiedOnly(false);
        InventoryBalanceRespDTO balance = inventoryApi.getInventoryBalanceList(query).stream()
                .filter(item -> item.getQtyOnhand() != null
                        && item.getQtyOnhand().compareTo(disposition.getQty()) >= 0)
                .findFirst().orElseThrow(() -> exception(INV_BALANCE_NOT_EXISTS));
        return new ResolvedInventory(qcTask, balance);
    }

    public record ResolvedInventory(QcTaskDO qcTask, InventoryBalanceRespDTO balance) {
    }
}
