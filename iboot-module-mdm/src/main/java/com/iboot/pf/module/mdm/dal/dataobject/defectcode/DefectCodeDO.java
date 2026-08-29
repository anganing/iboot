package com.iboot.pf.module.mdm.dal.dataobject.defectcode;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 缺陷代码 DO：缺陷帕累托分析的主数据来源。
 *
 * @author iboot
 */
@TableName("mdm_defect_code")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectCodeDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 缺陷编码
     */
    private String code;
    /**
     * 缺陷名称
     */
    private String name;
    /**
     * 缺陷等级：CRITICAL-致命，MAJOR-严重，MINOR-轻微
     */
    private String level;
    /**
     * 缺陷分类树父级编号，0-顶级
     */
    private Long parentId;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
