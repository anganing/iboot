package com.iboot.pf.module.mdm.dal.dataobject.bomline;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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
     * 损耗率
     */
    private BigDecimal scrapRate;
    /**
     * 供料方式：PICK-领料，BACKFLUSH-倒冲，PHANTOM-虚拟件，DIRECT-直送线边
     */
    private String issueMethod;
    /**
     * 用料绑定工序顺序号
     */
    private Integer routeOpSeq;
    /**
     * 位号/装配位置
     */
    private String positionNo;
    /**
     * 行级生效日期
     */
    private LocalDate effectiveDate;
    /**
     * 行级失效日期
     */
    private LocalDate expiryDate;
    /**
     * 计量单位编号
     */
    private Long uomId;
    /**
     * 备注
     */
    private String remark;

}