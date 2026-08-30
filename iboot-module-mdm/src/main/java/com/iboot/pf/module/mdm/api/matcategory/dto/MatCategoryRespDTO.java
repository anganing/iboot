package com.iboot.pf.module.mdm.api.matcategory.dto;

import lombok.Data;

import java.util.List;

/**
 * 物料分类 Response DTO
 *
 * @author iboot
 */
@Data
public class MatCategoryRespDTO {

    /** 分类编号 */
    private Long id;

    /** 父级编号，0-顶级 */
    private Long parentId;

    /** 分类编码 */
    private String code;

    /** 分类名称 */
    private String name;

    /** 状态：0-启用，1-停用 */
    private Integer status;

    /** 子分类列表 */
    private List<MatCategoryRespDTO> children;

}
