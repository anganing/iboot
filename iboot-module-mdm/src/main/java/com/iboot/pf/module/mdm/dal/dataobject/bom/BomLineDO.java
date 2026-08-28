package com.iboot.pf.module.mdm.dal.dataobject.bomline;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 物料清单明细 DO
 *
 * @author iboot
 */
@TableName("mdm_bom_line")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomLineDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * BOM编号
     */
    private Long bomId;
    /**
     * 行号
     */
    private Integer lineNo;
    /**
     * 物料编号
     */
    private Long matId;
    /**
     * 物料编码快照
     */
    private String matCode;
    /**
     * 物料名称快照
     */
    private String matName;
    /**
     * 数量
     */
    private BigDecimal qty;
    /**
     * 计量单位编号
     */
    private Long uomId;
    /**
     * 备注
     */
    private String remark;

}
