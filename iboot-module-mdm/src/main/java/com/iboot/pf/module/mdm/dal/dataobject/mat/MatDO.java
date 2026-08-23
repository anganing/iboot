package com.iboot.pf.module.mdm.dal.dataobject.mat;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 物料主档 DO
 *
 * @author iboot
 */
@TableName("mdm_mat")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 物料分类编号
     */
    private Long catId;
    /**
     * 编码
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 规格型号
     */
    private String spec;
    /**
     * 图号
     */
    private String drawingNo;
    /**
     * 物料类型
     */
    private String matType;
    /**
     * 基准计量单位编号
     */
    private Long baseUomId;
    /**
     * 是否批次管控
     */
    private Boolean batchFlag;
    /**
     * 是否SN管控
     */
    private Boolean snFlag;
    /**
     * 保质期（天）
     */
    private Integer shelfLifeDays;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;


}