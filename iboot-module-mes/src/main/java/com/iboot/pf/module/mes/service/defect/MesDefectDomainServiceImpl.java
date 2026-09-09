package com.iboot.pf.module.mes.service.defect;

import com.iboot.pf.module.mdm.api.defectcode.DefectCodeApi;
import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.defect.DefectMapper;
import com.iboot.pf.module.mes.service.defect.bo.MesCreateDefectBO;
import com.iboot.pf.module.mes.service.qctask.QcTaskService;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DEFECT_CODE_NOT_AVAILABLE;

/**
 * 缺陷录入领域动作实现。
 *
 * <p>缺陷码过滤的职责分界：{@code mdm_defect_code_mat_cat} / {@code mdm_defect_code_op}
 * 两张适用范围表是 mdm 的主数据，MES 不许直查，过滤在 mdm 的
 * {@link DefectCodeApi#getAvailableDefectCodeList} 一次批量完成（三次批量查询，无 N+1）；
 * MES 侧只负责把业务上下文翻译成过滤维度——物料编号经 {@link MaterialApi} 解析出物料分类，
 * 与工序编号一起交给门面。
 *
 * <p>落库委托 codegen 的 {@link DefectService}：它已实现检验结果归属校验、快照补全与
 * 附件子表全量替换，领域层只在其外补"可选范围"这道门禁，不重写既有逻辑。
 *
 * @author iboot
 */
@Slf4j
@Service
public class MesDefectDomainServiceImpl implements MesDefectDomainService {

    @Resource
    private DefectCodeApi defectCodeApi;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private QcTaskService qcTaskService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private DefectService defectService;
    @Resource
    private DefectMapper defectMapper;

    @Override
    public List<DefectCodeRespDTO> getAvailableDefectCodes(Long matId, Long opId) {
        Long matCatId = null;
        if (matId != null) {
            MaterialRespDTO material = materialApi.getMaterial(matId);
            if (material != null) {
                matCatId = material.getCatId();
            }
        }
        return defectCodeApi.getAvailableDefectCodeList(matCatId, opId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDefect(MesCreateDefectBO bo) {
        // 任务必须存在：缺陷挂在质检任务上，任务快照的物料决定缺陷码过滤的物料维度
        QcTaskDO task = qcTaskService.validateQcTaskExists(bo.getTaskId());

        assertCodeAvailable(task.getMatId(), bo.getDefectCodeId(), bo.getOpId());

        DefectSaveReqVO reqVO = new DefectSaveReqVO();
        reqVO.setTaskId(bo.getTaskId());
        reqVO.setResultId(bo.getResultId());
        reqVO.setDefectCodeId(bo.getDefectCodeId());
        reqVO.setQty(bo.getQty());
        reqVO.setDescription(bo.getDescription());
        reqVO.setLocation(bo.getLocation());
        reqVO.setRemark(bo.getRemark());
        // 快照（编码/名称/等级）与检验结果归属校验由 codegen 的 createDefect 补全
        return defectService.createDefect(reqVO);
    }

    /**
     * 缺陷码必须在当前物料分类与工序的可选范围内，越界说明前端选择器绕过了过滤或主数据已变更。
     *
     * <p>可选范围查询本身就是前端选择器的同源数据，正常路径只查它一次；
     * 缺陷编码仅用于拼报错提示，留到错误路径再解析——代码不存在时由快照校验抛
     * {@code DEFECT_CODE_NOT_EXISTS}，与"存在但越界"区分开。
     */
    private void assertCodeAvailable(Long matId, Long defectCodeId, Long opId) {
        if (getAvailableDefectCodes(matId, opId).stream()
                .anyMatch(code -> Objects.equals(code.getId(), defectCodeId))) {
            return;
        }
        DefectCodeRespDTO defectCode = snapshotService.requireDefectCode(defectCodeId);
        throw exception(DEFECT_CODE_NOT_AVAILABLE, defectCode.getCode());
    }

}
