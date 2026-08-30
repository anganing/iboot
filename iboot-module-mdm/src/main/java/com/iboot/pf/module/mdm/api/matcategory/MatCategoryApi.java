package com.iboot.pf.module.mdm.api.matcategory;

import com.iboot.pf.module.mdm.api.matcategory.dto.MatCategoryRespDTO;

import java.util.List;

/**
 * 物料分类 API 接口
 *
 * <p>对 MES / APS 提供物料分类树与列表门面。
 *
 * @author iboot
 */
public interface MatCategoryApi {

    /**
     * 获得物料分类树
     *
     * <p>仅包含启用状态，按父级嵌套组织。
     *
     * @return 顶级分类树列表；无数据返回空列表
     */
    List<MatCategoryRespDTO> getMatCategoryTree();

    /**
     * 获得全部物料分类列表（含停用，用于下拉展示）
     *
     * @return 物料分类列表；无数据返回空列表
     */
    List<MatCategoryRespDTO> getMatCategoryList();

}
