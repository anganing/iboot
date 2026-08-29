package com.iboot.pf.module.mdm.dal.dataobject.defectcode;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 缺陷代码适用物料分类 DO：无关联行表示该缺陷不限物料分类。
 *
 * @author iboot
 */
@TableName("mdm_defect_code_mat_cat")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectCodeMatCatDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 缺陷代码编号
     */
    private Long defectCodeId;
    /**
     * 适用物料分类编号
     */
    private Long matCatId;

}
