package com.iboot.pf.module.mes.dal.dataobject.defect;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-缺陷记录 DO
 *
 * @author iboot
 */
@TableName("mes_defect")
@KeySequence("mes_defect_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 质检任务编号
     */
    private Long taskId;
    /**
     * 关联检验结果编号
     */
    private Long resultId;
    /**
     * 缺陷编码
     */
    private String defectCode;
    /**
     * 缺陷名称
     */
    private String defectName;
    /**
     * 缺陷等级：CRITICAL、MAJOR、MINOR
     */
    private String defectLevel;
    /**
     * 缺陷数量
     */
    private BigDecimal qty;
    /**
     * 缺陷描述
     */
    private String description;
    /**
     * 缺陷位置
     */
    private String location;
    /**
     * 缺陷附件地址列表
     */
    private String attachmentUrls;
    /**
     * 备注
     */
    private String remark;


}