package com.iboot.pf.module.mdm.api.matcategory;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.matcategory.dto.MatCategoryRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.service.matcat.MatCatService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物料分类 API 实现类
 *
 * @author iboot
 */
@Service
public class MatCategoryApiImpl implements MatCategoryApi {

    @Resource
    private MatCatService matCatService;

    @Override
    @DataPermission(enable = false)
    public List<MatCategoryRespDTO> getMatCategoryTree() {
        List<MatCatDO> all = matCatService.getMatCatDOList();
        List<MatCategoryRespDTO> enabled = new ArrayList<>(all.size());
        for (MatCatDO matCat : all) {
            if (CommonStatusEnum.isEnable(matCat.getStatus())) {
                enabled.add(BeanUtils.toBean(matCat, MatCategoryRespDTO.class));
            }
        }
        return buildTree(enabled);
    }

    @Override
    @DataPermission(enable = false)
    public List<MatCategoryRespDTO> getMatCategoryList() {
        List<MatCatDO> all = matCatService.getMatCatDOList();
        return BeanUtils.toBean(all, MatCategoryRespDTO.class);
    }

    /**
     * 将扁平分类列表按 parentId 组装为树。
     */
    private List<MatCategoryRespDTO> buildTree(List<MatCategoryRespDTO> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, MatCategoryRespDTO> byId = new HashMap<>(list.size() * 2);
        for (MatCategoryRespDTO dto : list) {
            dto.setChildren(new ArrayList<>());
            byId.put(dto.getId(), dto);
        }
        List<MatCategoryRespDTO> roots = new ArrayList<>();
        for (MatCategoryRespDTO dto : list) {
            Long parentId = dto.getParentId();
            MatCategoryRespDTO parent = parentId == null ? null : byId.get(parentId);
            if (parent != null && !parent.getId().equals(dto.getId())) {
                parent.getChildren().add(dto);
            } else {
                roots.add(dto);
            }
        }
        return roots;
    }

}
