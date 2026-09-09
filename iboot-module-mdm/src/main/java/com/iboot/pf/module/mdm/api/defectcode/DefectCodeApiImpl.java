package com.iboot.pf.module.mdm.api.defectcode;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeDO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeMatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeOpDO;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeMapper;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeMatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeOpMapper;
import com.iboot.pf.module.mdm.service.defectcode.DefectCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缺陷代码 API 实现类
 *
 * @author iboot
 */
@Service
@DataPermission(enable = false)
public class DefectCodeApiImpl implements DefectCodeApi {

    @Resource
    private DefectCodeService defectCodeService;
    @Resource
    private DefectCodeMapper defectCodeMapper;
    @Resource
    private DefectCodeMatCatMapper defectCodeMatCatMapper;
    @Resource
    private DefectCodeOpMapper defectCodeOpMapper;

    @Override
    public List<DefectCodeRespDTO> getDefectCodeList() {
        List<DefectCodeDO> all = defectCodeService.getDefectCodeList();
        return BeanUtils.toBean(all, DefectCodeRespDTO.class);
    }

    @Override
    public Map<Long, DefectCodeRespDTO> getDefectCodeMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        // 一次 IN 查询，避免调用方 N+1
        List<DefectCodeDO> list = defectCodeMapper.selectByIds(ids);
        return CollectionUtils.convertMap(BeanUtils.toBean(list, DefectCodeRespDTO.class), DefectCodeRespDTO::getId);
    }

    @Override
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

    @Override
    public List<DefectCodeRespDTO> getAvailableDefectCodeList(Long matCatId, Long opId) {
        // 三次批量查询：全部启用代码 + 两张适用范围表全量，不按代码逐个查关联行
        List<DefectCodeDO> codes = defectCodeMapper.selectList();
        List<DefectCodeMatCatDO> matCatLinks = defectCodeMatCatMapper.selectList();
        List<DefectCodeOpDO> opLinks = defectCodeOpMapper.selectList();

        Set<Long> matCatLimited = new HashSet<>();
        Set<Long> matCatAllowed = new HashSet<>();
        for (DefectCodeMatCatDO link : matCatLinks) {
            matCatLimited.add(link.getDefectCodeId());
            if (matCatId != null && matCatId.equals(link.getMatCatId())) {
                matCatAllowed.add(link.getDefectCodeId());
            }
        }
        Set<Long> opLimited = new HashSet<>();
        Set<Long> opAllowed = new HashSet<>();
        for (DefectCodeOpDO link : opLinks) {
            opLimited.add(link.getDefectCodeId());
            if (opId != null && opId.equals(link.getOpId())) {
                opAllowed.add(link.getDefectCodeId());
            }
        }

        List<DefectCodeRespDTO> result = new ArrayList<>(codes.size());
        for (DefectCodeDO code : codes) {
            if (!CommonStatusEnum.isEnable(code.getStatus())) {
                continue;
            }
            // 有适用记录的代码仅在与当前上下文匹配时可选，无记录视为通用码任何场景可选
            if (matCatLimited.contains(code.getId()) && !matCatAllowed.contains(code.getId())) {
                continue;
            }
            if (opLimited.contains(code.getId()) && !opAllowed.contains(code.getId())) {
                continue;
            }
            result.add(BeanUtils.toBean(code, DefectCodeRespDTO.class));
        }
        return result;
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
