package com.iboot.pf.module.mes.service.materialreq;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLinePageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineSaveReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqPageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LINE_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_LINE_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_RETURN_BATCH_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MATERIAL_REQ_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;

/**
 * MES-领退补料申请 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class MaterialReqServiceImpl implements MaterialReqService {

    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createMaterialReq(MaterialReqSaveReqVO createReqVO) {
        MaterialReqDO materialReq = BeanUtils.toBean(createReqVO, MaterialReqDO.class);
        materialReq.setId(null);
        materialReq.setReqNo(codeService.generate(MesCodeRuleConstants.MATERIAL_REQ, materialReq.getReqNo()));
        prepare(materialReq);
        validateReqNoUnique(materialReq.getReqNo(), null);
        validateIdempotencyKeyUnique(materialReq.getIdempotencyKey(), null);
        materialReqMapper.insert(materialReq);
        return materialReq.getId();
    }

    @Override
    public void updateMaterialReq(MaterialReqSaveReqVO updateReqVO) {
        MaterialReqDO existed = validateMaterialReqExists(updateReqVO.getId());
        // 已交给 WMS 的申请不允许再改，两侧数量口径必须一致
        if (MesMaterialReqStatusEnum.isHandedToWms(existed.getStatus())) {
            throw exception(MATERIAL_REQ_STATUS_INVALID);
        }
        MaterialReqDO updateObj = BeanUtils.toBean(updateReqVO, MaterialReqDO.class);
        updateObj.setReqNo(existed.getReqNo());
        // WMS 单号由 WMS 回写，不由表单维护
        updateObj.setWmsBizNo(existed.getWmsBizNo());
        prepare(updateObj);
        validateIdempotencyKeyUnique(updateObj.getIdempotencyKey(), updateReqVO.getId());
        materialReqMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaterialReq(Long id) {
        MaterialReqDO existed = validateMaterialReqExists(id);
        if (MesMaterialReqStatusEnum.isHandedToWms(existed.getStatus())) {
            throw exception(MATERIAL_REQ_STATUS_INVALID);
        }
        materialReqMapper.deleteById(id);
        // 主单删除必须级联清理明细，否则留下孤儿数据
        materialReqLineMapper.deleteByReqId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaterialReqListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            MaterialReqDO existed = validateMaterialReqExists(id);
            if (MesMaterialReqStatusEnum.isHandedToWms(existed.getStatus())) {
                throw exception(MATERIAL_REQ_STATUS_INVALID);
            }
        }
        materialReqMapper.deleteByIds(ids);
        materialReqLineMapper.deleteByReqIds(ids);
    }

    private MaterialReqDO validateMaterialReqExists(Long id) {
        MaterialReqDO materialReq = id == null ? null : materialReqMapper.selectById(id);
        if (materialReq == null) {
            throw exception(MATERIAL_REQ_NOT_EXISTS);
        }
        return materialReq;
    }

    /** 落库前的公共处理：从生产工单继承单号与项目维度，校验仓库与库存组织，可选外键归零。 */
    private void prepare(MaterialReqDO materialReq) {
        ProdOrderDO prodOrder = prodOrderMapper.selectById(materialReq.getProdOrderId());
        if (prodOrder == null) {
            throw exception(PROD_ORDER_NOT_EXISTS);
        }
        materialReq.setProdOrderNo(prodOrder.getOrderNo());
        materialReq.setProjectId(prodOrder.getProjectId());
        materialReq.setProjectTaskId(prodOrder.getProjectTaskId());
        materialReq.setWorkOrderId(materialReq.getWorkOrderId() == null
                ? MesConstants.NONE_ID : materialReq.getWorkOrderId());
        materialReq.setOpTaskId(materialReq.getOpTaskId() == null
                ? MesConstants.NONE_ID : materialReq.getOpTaskId());
    }

    /** 申请单号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。 */
    private void validateReqNoUnique(String reqNo, Long excludeId) {
        MaterialReqDO existed = materialReqMapper.selectByReqNo(reqNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(MATERIAL_REQ_NO_DUPLICATE);
    }

    /** 幂等键唯一。 */
    private void validateIdempotencyKeyUnique(String idempotencyKey, Long excludeId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        MaterialReqDO existed = materialReqMapper.selectByIdempotencyKey(idempotencyKey);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(IDEMPOTENCY_KEY_DUPLICATE);
    }

    @Override
    public MaterialReqRespVO getMaterialReq(Long id) {
        MaterialReqRespVO respVO = materialReqMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<MaterialReqRespVO> getMaterialReqPage(MaterialReqPageReqVO pageReqVO) {
        PageResult<MaterialReqRespVO> pageResult = materialReqMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 跨模块可读字段补全，放在 Service 以便 Excel 导出复用。 */
    private void fillRelationNames(List<MaterialReqRespVO> list) {
        relationFiller.fillWhName(list, MaterialReqRespVO::getWhId, MaterialReqRespVO::setWhName);
        relationFiller.fillProjectName(list, MaterialReqRespVO::getProjectId, MaterialReqRespVO::setProjectName);
    }

    // ==================== 子表（领退补料申请明细） ====================

    @Override
    public PageResult<MaterialReqLineRespVO> getMaterialReqLinePage(MaterialReqLinePageReqVO pageReqVO) {
        return materialReqLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public Long createMaterialReqLine(MaterialReqLineSaveReqVO createReqVO) {
        MaterialReqDO materialReq = validateMaterialReqExists(createReqVO.getReqId());
        validateLineEditable(materialReq);
        MaterialReqLineDO line = BeanUtils.toBean(createReqVO, MaterialReqLineDO.class);
        line.setId(null);
        validateLineNoUnique(line.getReqId(), line.getLineNo(), null);
        fillLineSnapshot(materialReq, line);
        // WMS 已处理数量由 WMS 回写，创建时归零
        line.setProcessedQty(MesConstants.ZERO_QTY);
        materialReqLineMapper.insert(line);
        return line.getId();
    }

    @Override
    public void updateMaterialReqLine(MaterialReqLineSaveReqVO updateReqVO) {
        MaterialReqLineDO existed = validateMaterialReqLineExists(updateReqVO.getId());
        MaterialReqDO materialReq = validateMaterialReqExists(updateReqVO.getReqId());
        validateLineEditable(materialReq);
        MaterialReqLineDO line = BeanUtils.toBean(updateReqVO, MaterialReqLineDO.class);
        // 改自己不能撞自己，所以排除自身编号
        validateLineNoUnique(line.getReqId(), line.getLineNo(), updateReqVO.getId());
        fillLineSnapshot(materialReq, line);
        line.setProcessedQty(existed.getProcessedQty());
        materialReqLineMapper.updateById(line);
    }

    @Override
    public void deleteMaterialReqLine(Long id) {
        MaterialReqLineDO existed = validateMaterialReqLineExists(id);
        validateLineEditable(validateMaterialReqExists(existed.getReqId()));
        materialReqLineMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaterialReqLineListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            MaterialReqLineDO existed = validateMaterialReqLineExists(id);
            validateLineEditable(validateMaterialReqExists(existed.getReqId()));
        }
        materialReqLineMapper.deleteByIds(ids);
    }

    @Override
    public MaterialReqLineRespVO getMaterialReqLine(Long id) {
        return materialReqLineMapper.selectJoinById(id);
    }

    private MaterialReqLineDO validateMaterialReqLineExists(Long id) {
        MaterialReqLineDO line = id == null ? null : materialReqLineMapper.selectById(id);
        if (line == null) {
            throw exception(MATERIAL_REQ_LINE_NOT_EXISTS);
        }
        return line;
    }

    /** 交给 WMS 之后明细不能再动，否则两侧数量对不上。 */
    private void validateLineEditable(MaterialReqDO materialReq) {
        if (MesMaterialReqStatusEnum.isHandedToWms(materialReq.getStatus())) {
            throw exception(MATERIAL_REQ_STATUS_INVALID);
        }
    }

    /** 明细行号唯一：唯一索引是 (req_id, line_no, deleted)。 */
    private void validateLineNoUnique(Long reqId, Integer lineNo, Long excludeId) {
        MaterialReqLineDO existed = materialReqLineMapper.selectByReqIdAndLineNo(reqId, lineNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(MATERIAL_REQ_LINE_NO_DUPLICATE);
    }

    /**
     * 明细上的物料、单位、批次快照统一由快照服务补全。
     *
     * <p>退料必须指定批次：退回去的库存要落到原批次上，否则批次台账对不上。
     */
    private void fillLineSnapshot(MaterialReqDO materialReq, MaterialReqLineDO line) {
        MaterialRespDTO material = snapshotService.requireMaterial(line.getMatId());
        line.setMatCode(material.getCode());
        line.setMatName(material.getName());
        line.setMatSpec(material.getSpec());
        line.setUomCode(snapshotService.requireUomCode(line.getUomId()));
        line.setRouteOpMatId(line.getRouteOpMatId() == null ? MesConstants.NONE_ID : line.getRouteOpMatId());
        line.setBatchId(line.getBatchId() == null ? MesConstants.NONE_ID : line.getBatchId());
        if (MesMaterialReqTypeEnum.isReturn(materialReq.getReqType())
                && MesConstants.NONE_ID.equals(line.getBatchId())) {
            throw exception(MATERIAL_REQ_RETURN_BATCH_REQUIRED);
        }
        line.setLotNo(snapshotService.lotNoOrNull(line.getBatchId()));
    }

}
