package com.iboot.pf.module.mes.api.defect;

import com.iboot.pf.module.mes.api.defect.dto.DefectRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 缺陷记录只读 API 门面。
 *
 * <p>缺陷是检验结果的附属事实，由检验录入产生，不接受跨模块写入。
 */
public interface DefectApi {

    /**
     * 查询单条缺陷记录。
     *
     * @param id 缺陷记录编号
     * @return 缺陷事实，不存在时返回 null
     */
    DefectRespDTO getDefect(Long id);

    /**
     * 批量查询缺陷记录。
     *
     * @param ids 缺陷记录编号集合
     * @return 缺陷事实列表
     */
    List<DefectRespDTO> getDefectList(Collection<Long> ids);

    /**
     * 按质检任务批量查询缺陷记录。
     *
     * @param qualityTaskIds 质检任务编号集合
     * @return 缺陷事实列表
     */
    List<DefectRespDTO> getDefectListByQualityTaskIds(Collection<Long> qualityTaskIds);

}
