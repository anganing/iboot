package com.iboot.pf.module.mdm.api.material;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.service.mat.MatService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 物料主档 API 实现类
 *
 * @author iboot
 */
@Service
@DataPermission(enable = false)
public class MaterialApiImpl implements MaterialApi {

    @Resource
    private MatService matService;

    @Override
    public MaterialRespDTO getMaterial(Long id) {
        List<MaterialRespDTO> list = getMaterialList(Collections.singleton(id));
        return CollUtil.getFirst(list);
    }

    @Override
    public List<MaterialRespDTO> getMaterialList(Collection<Long> ids) {
        List<MatDO> mats = matService.getMatList(ids);
        return BeanUtils.toBean(mats, MaterialRespDTO.class);
    }

    @Override
    public List<MaterialRespDTO> getEnabledMaterialList() {
        List<MatDO> mats = matService.getEnabledMatList();
        return BeanUtils.toBean(mats, MaterialRespDTO.class);
    }

    @Override
    public Map<Long, MaterialRespDTO> getMaterialMap(Collection<Long> ids) {
        List<MaterialRespDTO> list = getMaterialList(ids);
        return CollectionUtils.convertMap(list, MaterialRespDTO::getId);
    }

    @Override
    public List<MaterialRespDTO> getMaterialListByCodes(Collection<String> codes) {
        List<MatDO> mats = matService.getMatListByCodes(codes);
        return BeanUtils.toBean(mats, MaterialRespDTO.class);
    }

    @Override
    public void validateMaterialList(Collection<Long> ids) {
        matService.validateMatList(ids);
    }

}
