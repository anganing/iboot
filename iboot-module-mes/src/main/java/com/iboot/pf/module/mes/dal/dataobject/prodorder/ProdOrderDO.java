package com.iboot.pf.module.mes.dal.dataobject.prodorder;

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
 * MES-生产工单 DO
 *
 * @author iboot
 */
@TableName("mes_prod_order")
@KeySequence("mes_prod_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 生产工单号，由 infra 编码规则 MES_PROD_ORDER_NO 分配
     */
    private String orderNo;
    /**
     * 生产需求编号，0 表示手工建单
     */
    private Long demandId;
    /**
     * 工厂编号，来自 mdm_plant
     */
    private Long plantId;
    /**
     * 车间编号，来自 mdm_workshop
     */
    private Long workshopId;
    /**
     * 项目编号，0 表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0 表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 生产物料编号，来自 mdm_mat
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
     * 计划生产数量
     */
    private BigDecimal qty;
    /**
     * 已完成数量，由报工推进
     */
    private BigDecimal completedQty;
    /**
     * 累计报废数量，由报工推进
     */
    private BigDecimal scrapQty;
    /**
     * 计量单位编号，来自 mdm_uom
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 锁定的 BOM 编号，来自 mdm_bom
     */
    private Long bomId;
    /**
     * 锁定的 BOM 版本快照
     */
    private String bomVersion;
    /**
     * 锁定的工艺路线编号，来自 mdm_route
     */
    private Long routeId;
    /**
     * 锁定的工艺路线版本快照
     */
    private String routeVersion;
    /**
     * 工单交期
     */
    private LocalDateTime dueDate;
    /**
     * 优先级，数值越大越优先
     */
    private Integer priority;
    /**
     * 齐套状态，由齐套检查写入
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesKitStatusEnum}
     */
    private String kitStatus;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum}
     */
    private String status;
    /**
     * 下达时间，由下达动作写入
     */
    private LocalDateTime releaseTime;
    /**
     * 实际开始时间，由首道工序开工写入
     */
    private LocalDateTime actualStartTime;
    /**
     * 实际结束时间，由末道工序完工写入
     */
    private LocalDateTime actualEndTime;
    /**
     * 乐观锁版本，并发推进数量时由数据库保证不丢更新
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;

}
