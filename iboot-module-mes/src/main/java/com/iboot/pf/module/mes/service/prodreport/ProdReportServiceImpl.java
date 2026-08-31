package com.iboot.pf.module.mes.service.prodreport;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportRespVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesProdReportStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_QTY_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_TIME_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/**
 * MES-生产报工记录 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProdReportServiceImpl implements ProdReportService {

    @Resource
    private ProdReportMapper prodReportMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createProdReport(ProdReportSaveReqVO createReqVO) {
        ProdReportDO prodReport = BeanUtils.toBean(createReqVO, ProdReportDO.class);
        prodReport.setId(null);
        prodReport.setReportNo(codeService.generate(MesCodeRuleConstants.PROD_REPORT, prodReport.getReportNo()));
        prepare(prodReport);
        validateReportNoUnique(prodReport.getReportNo(), null);
        validateIdempotencyKeyUnique(prodReport.getIdempotencyKey(), null);
        prodReportMapper.insert(prodReport);
        return prodReport.getId();
    }

    @Override
    public void updateProdReport(ProdReportSaveReqVO updateReqVO) {
        ProdReportDO existed = validateProdReportExists(updateReqVO.getId());
        // 已确认的报工数量已推进到工序任务，不允许再改
        if (MesProdReportStatusEnum.isConfirmed(existed.getStatus())) {
            throw exception(PROD_REPORT_STATUS_INVALID);
        }
        ProdReportDO updateObj = BeanUtils.toBean(updateReqVO, ProdReportDO.class);
        updateObj.setReportNo(existed.getReportNo());
        prepare(updateObj);
        validateIdempotencyKeyUnique(updateObj.getIdempotencyKey(), updateReqVO.getId());
        prodReportMapper.updateById(updateObj);
    }

    @Override
    public void deleteProdReport(Long id) {
        ProdReportDO existed = validateProdReportExists(id);
        if (MesProdReportStatusEnum.isConfirmed(existed.getStatus())) {
            throw exception(PROD_REPORT_STATUS_INVALID);
        }
        prodReportMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProdReportListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            ProdReportDO existed = validateProdReportExists(id);
            if (MesProdReportStatusEnum.isConfirmed(existed.getStatus())) {
                throw exception(PROD_REPORT_STATUS_INVALID);
            }
        }
        prodReportMapper.deleteByIds(ids);
    }

    private ProdReportDO validateProdReportExists(Long id) {
        ProdReportDO prodReport = id == null ? null : prodReportMapper.selectById(id);
        if (prodReport == null) {
            throw exception(PROD_REPORT_NOT_EXISTS);
        }
        return prodReport;
    }

    /**
     * 落库前的公共处理：反查单据链、校验外键、校验数量与时间、补默认值。
     */
    private void prepare(ProdReportDO prodReport) {
        inheritFromOpTask(prodReport);
        snapshotService.validateUser(prodReport.getWorkerId());
        snapshotService.validateRes(prodReport.getResId());
        snapshotService.validateShift(prodReport.getShiftId());
        prodReport.setResId(prodReport.getResId() == null ? MesConstants.NONE_ID : prodReport.getResId());
        prodReport.setShiftId(prodReport.getShiftId() == null ? MesConstants.NONE_ID : prodReport.getShiftId());
        validateQty(prodReport);
        validateTime(prodReport);
        if (prodReport.getReportTime() == null) {
            prodReport.setReportTime(LocalDateTime.now());
        }
    }

    /** 生产工单与作业工单编号从工序任务反查，前端只提交工序任务。 */
    private void inheritFromOpTask(ProdReportDO prodReport) {
        OpTaskDO opTask = opTaskMapper.selectById(prodReport.getOpTaskId());
        if (opTask == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        WorkOrderDO workOrder = workOrderMapper.selectById(opTask.getWorkOrderId());
        if (workOrder == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
        prodReport.setWorkOrderId(workOrder.getId());
        prodReport.setProdOrderId(workOrder.getProdOrderId());
    }

    /** 合格、报废、返工三者不能全为零，否则这条报工没有意义。 */
    private void validateQty(ProdReportDO prodReport) {
        BigDecimal total = prodReport.getQtyGood()
                .add(prodReport.getQtyScrap())
                .add(prodReport.getQtyRework());
        if (total.signum() <= 0) {
            throw exception(PROD_REPORT_QTY_EMPTY);
        }
    }

    /** 作业结束时间不能早于开始时间。 */
    private void validateTime(ProdReportDO prodReport) {
        if (prodReport.getEndTime().isBefore(prodReport.getStartTime())) {
            throw exception(PROD_REPORT_TIME_INVALID);
        }
    }

    /** 报工单号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。 */
    private void validateReportNoUnique(String reportNo, Long excludeId) {
        ProdReportDO existed = prodReportMapper.selectByReportNo(reportNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(PROD_REPORT_NO_DUPLICATE);
    }

    /** 幂等键唯一：扫码端重复提交同一次报工时直接拒绝。 */
    private void validateIdempotencyKeyUnique(String idempotencyKey, Long excludeId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        ProdReportDO existed = prodReportMapper.selectByIdempotencyKey(idempotencyKey);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(IDEMPOTENCY_KEY_DUPLICATE);
    }

    @Override
    public ProdReportRespVO getProdReport(Long id) {
        ProdReportRespVO respVO = prodReportMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<ProdReportRespVO> getProdReportPage(ProdReportPageReqVO pageReqVO) {
        PageResult<ProdReportRespVO> pageResult = prodReportMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 跨模块可读字段补全，放在 Service 以便 Excel 导出复用。 */
    private void fillRelationNames(List<ProdReportRespVO> list) {
        relationFiller.fillUserNickname(list, ProdReportRespVO::getWorkerId, ProdReportRespVO::setWorkerName);
        relationFiller.fillResName(list, ProdReportRespVO::getResId, ProdReportRespVO::setResName);
        relationFiller.fillShiftName(list, ProdReportRespVO::getShiftId, ProdReportRespVO::setShiftName);
    }

}
