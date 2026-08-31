package com.iboot.pf.module.mes.dal.dataobject.rework;

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

/**
 * MES-返工任务 DO
 *
 * @author iboot
 */
@TableName("mes_rework")
@KeySequence("mes_rework_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReworkDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 返工任务编号，由 infra 编码规则 MES_REWORK_NO 分配
     */
    private String reworkNo;
    /**
     * 来源质检任务编号，0 表示手工发起
     */
    private Long sourceQcTaskId;
    /**
     * 生产工单编号，由工序任务反查带出
     */
    private Long prodOrderId;
    /**
     * 作业工单编号，由工序任务反查带出
     */
    private Long workOrderId;
    /**
     * 返工起始工序任务编号
     */
    private Long opTaskId;
    /**
     * 返工数量
     */
    private BigDecimal qty;
    /**
     * 返工工艺路线编号，来自 mdm_route
     */
    private Long routeId;
    /**
     * 返工工艺路线版本快照
     */
    private String routeVersion;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesReworkStatusEnum}
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}
