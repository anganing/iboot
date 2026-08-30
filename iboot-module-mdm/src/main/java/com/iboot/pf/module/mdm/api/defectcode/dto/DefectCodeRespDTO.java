package com.iboot.pf.module.mdm.api.defectcode.dto;

import lombok.Data;

import java.util.List;

/**
 * 缺陷代码 Response DTO
 *
 * @author iboot
 */
@Data
public class DefectCodeRespDTO {

    /** 缺陷编号 */
    private Long id;

    /** 缺陷编码 */
    private String code;

    /** 缺陷名称 */
    private String name;

    /** 缺陷等级：CRITICAL-致命，MAJOR-严重，MINOR-轻微 */
    private String level;

    /** 缺陷分类树父级编号，0-顶级 */
    private Long parentId;

    /** 状态：0-启用，1-停用 */
    private Integer status;

    /** 子缺陷分类列表 */
    private List<DefectCodeRespDTO> children;

}
