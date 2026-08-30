package com.iboot.pf.module.mes.api.defect;

import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import java.util.Collection;
import java.util.List;

/** 缺陷记录只读 API 门面。 */
public interface DefectApi {
    MesRespDTO getDefect(Long id);
    List<MesRespDTO> getDefectList(Collection<Long> ids);
    List<MesRespDTO> getDefectListByQualityTaskIds(Collection<Long> qualityTaskIds);
}
