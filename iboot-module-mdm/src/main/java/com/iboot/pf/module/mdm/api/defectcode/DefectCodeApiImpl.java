package com.iboot.pf.module.mdm.api.defectcode;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeDO;
import com.iboot.pf.module.mdm.service.defectcode.DefectCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缺陷代码 API 实现类
 *
 * @author iboot
 */
@Service
public class DefectCodeApiImpl implements DefectCodeApi {

    @Resource
    private DefectCodeService defectCodeService;

    @Override
    @DataPermission(enable = false)
    public List<DefectCodeRespDTO> getDefectCodeList() {
        List<DefectCodeDO> all = defectCodeService.getDefectCodeList();
        return BeanUtils.toBean(all, DefectCodeRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<DefectCodeRespDTO> getDefectCodeTree() {
        List<DefectCodeDO> all = defectCodeService.getDefectCodeList();
        List<DefectCodeRespDTO> enabled = new ArrayList<>(all.size());
        for (DefectCodeDO defectCode : all) {
            if (CommonStatusEnum.isEnable(defectCode.getStatus())) {
                enabled.add(BeanUtils.toBean(defectCode, DefectCodeRespDTO.class));
            }
        }
        return buildTree(enabled);
    }

    /**
     * 将扁平缺陷分类列表按 parentId 组装为树。
     */
    private List<DefectCodeRespDTO> buildTree(List<DefectCodeRespDTO> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, DefectCodeRespDTO> byId = new HashMap<>(list.size() * 2);
        for (DefectCodeRespDTO dto : list) {
            dto.setChildren(new ArrayList<>());
            byId.put(dto.getId(), dto);
        }
        List<DefectCodeRespDTO> roots = new ArrayList<>();
        for (DefectCodeRespDTO dto : list) {
            Long parentId = dto.getParentId();
            DefectCodeRespDTO parent = parentId == null || parentId == 0L ? null : byId.get(parentId);
            if (parent != null && !parent.getId().equals(dto.getId())) {
                parent.getChildren().add(dto);
            } else {
                roots.add(dto);
            }
        }
        return roots;
    }

}
