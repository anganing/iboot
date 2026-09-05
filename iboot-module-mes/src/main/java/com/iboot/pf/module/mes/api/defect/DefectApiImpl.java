package com.iboot.pf.module.mes.api.defect;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.defect.dto.DefectRespDTO;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/** 缺陷 API 实现。 */
@Service
public class DefectApiImpl implements DefectApi {

    @Override
    @DataPermission(enable = false)
    public DefectRespDTO getDefect(Long id) {
        throw new UnsupportedOperationException("缺陷记录查询尚未接入领域 Service");
    }

    @Override
    @DataPermission(enable = false)
    public List<DefectRespDTO> getDefectList(Collection<Long> ids) {
        throw new UnsupportedOperationException("缺陷记录查询尚未接入领域 Service");
    }

    @Override
    @DataPermission(enable = false)
    public List<DefectRespDTO> getDefectListByQualityTaskIds(Collection<Long> qualityTaskIds) {
        throw new UnsupportedOperationException("缺陷记录查询尚未接入领域 Service");
    }

}
