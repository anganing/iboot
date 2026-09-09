package com.iboot.pf.module.mdm.api.defectcode;

import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缺陷代码 API 接口
 *
 * <p>对 MES 提供缺陷代码与分类门面。
 *
 * @author iboot
 */
public interface DefectCodeApi {

    /**
     * 获得全部缺陷代码
     *
     * @return 缺陷代码列表；无数据返回空列表
     */
    List<DefectCodeRespDTO> getDefectCodeList();

    /**
     * 获得缺陷代码树（只含启用节点）
     *
     * @return 缺陷代码树；无数据返回空列表
     */
    List<DefectCodeRespDTO> getDefectCodeTree();

    /**
     * 按编号批量查询缺陷代码 Map，供批量补全
     *
     * <p>MES 缺陷记录落编码、名称、等级快照时用它取值。
     *
     * @param ids 缺陷代码编号集合
     * @return 缺陷编号 -> 缺陷代码；缺失编号不产生键
     */
    Map<Long, DefectCodeRespDTO> getDefectCodeMap(Collection<Long> ids);

    /**
     * 查询指定物料分类与工序下可选的缺陷代码（仅启用）
     *
     * <p>按 {@code mdm_defect_code_mat_cat} 与 {@code mdm_defect_code_op} 两张适用范围表过滤：
     * 有适用物料分类记录的代码仅在该分类下可选，有适用工序记录的代码仅在该工序下可选；
     * 两张关联表都没有记录的代码视为通用码，任何场景可选。调用方没有某个维度的上下文
     * （如 IQC / OQC 检验没有工序）时传 {@code null}，该维度受限的代码一律不可选。
     *
     * @param matCatId 物料分类编号，可空
     * @param opId     工序编号，可空
     * @return 可选缺陷代码列表；无数据返回空列表
     */
    List<DefectCodeRespDTO> getAvailableDefectCodeList(Long matCatId, Long opId);

}
