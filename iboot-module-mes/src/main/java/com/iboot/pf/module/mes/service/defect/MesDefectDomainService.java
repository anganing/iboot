package com.iboot.pf.module.mes.service.defect;

import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;
import com.iboot.pf.module.mes.service.defect.bo.MesCreateDefectBO;

import java.util.List;

/**
 * 缺陷录入领域动作。
 *
 * <p>缺陷记录的落库走 codegen 的 {@link DefectService}（含附件子表），本接口只承担
 * 两件领域语义的事：缺陷码的可选范围解析与缺陷录入（按当前物料与工序过滤后的校验落库）。
 *
 * @author iboot
 */
public interface MesDefectDomainService {

    /**
     * 查询当前检验场景可选的缺陷码。
     *
     * <p>按当前物料分类与工序过滤：适用范围表里限定物料分类的缺陷码仅在该分类下可选，
     * 限定工序的仅在该工序下可选；两张关联表都没有记录的缺陷码视为通用码，任何场景可选。
     *
     * @param matId 物料编号，取质检任务的物料快照
     * @param opId  工序编号，可空；工序上下文不明确（IQC / OQC）时传 null
     * @return 可选缺陷码列表，仅含启用代码
     */
    List<DefectCodeRespDTO> getAvailableDefectCodes(Long matId, Long opId);

    /**
     * 录入缺陷记录。
     *
     * <p>校验缺陷码在当前物料分类与工序的可选范围内，快照缺陷编码、名称、等级后写
     * {@code mes_defect}。
     *
     * @param bo 缺陷录入参数
     * @return 缺陷记录编号
     */
    Long createDefect(MesCreateDefectBO bo);

}
