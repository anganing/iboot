package com.iboot.pf.module.mes.dal.dataobject.proddemand;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES-生产需求 DO
 *
 * @author iboot
 */
@TableName("mes_prod_demand")
@KeySequence("mes_prod_demand_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdDemandDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 生产需求编号，由 infra 编码规则 MES_DEMAND_NO 分配
     */
    private String demandNo;
    /**
     * 来源类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesDemandSourceTypeEnum}
     */
    private String sourceType;
    /**
     * 来源单号
     */
    private String sourceNo;
    /**
     * 工厂编号，来自 mdm_plant
     */
    private Long plantId;
    /**
     * 项目编号，0 表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0 表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 需求物料编号，来自 mdm_mat
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
     * 物料规格快照
     */
    private String matSpec;
    /**
     * 需求数量
     */
    private BigDecimal qty;
    /**
     * 计量单位编号，来自 mdm_uom
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 需求交期
     */
    private LocalDateTime dueDate;
    /**
     * 优先级，数值越大越优先
     */
    private Integer priority;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesDemandStatusEnum}
     */
    private String status;
    /**
     * 创建幂等键，唯一索引保证外部系统重复下发不产生重复需求
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;

}
