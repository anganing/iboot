package com.iboot.pf.module.mes.api.defect;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.defect.dto.DefectRespDTO;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectDO;
import com.iboot.pf.module.mes.dal.mysql.defect.DefectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/** 缺陷 API 实现。 */
@Service
@DataPermission(enable = false)
public class DefectApiImpl implements DefectApi {

    @Resource
    private DefectMapper defectMapper;

    @Override
    public DefectRespDTO getDefect(Long id) {
        return BeanUtils.toBean(id == null ? null : defectMapper.selectById(id), DefectRespDTO.class);
    }

    @Override
    public List<DefectRespDTO> getDefectList(Collection<Long> ids) {
        return ids == null || ids.isEmpty() ? List.of()
                : BeanUtils.toBean(defectMapper.selectByIds(ids), DefectRespDTO.class);
    }

    @Override
    public List<DefectRespDTO> getDefectListByQualityTaskIds(Collection<Long> qualityTaskIds) {
        if (qualityTaskIds == null || qualityTaskIds.isEmpty()) {
            return List.of();
        }
        List<DefectDO> defects = defectMapper.selectList(DefectDO::getTaskId, qualityTaskIds);
        return BeanUtils.toBean(defects, DefectRespDTO.class);
    }

}
