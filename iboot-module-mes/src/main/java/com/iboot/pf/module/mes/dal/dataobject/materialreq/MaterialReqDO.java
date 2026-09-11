package com.iboot.pf.module.mes.dal.dataobject.materialreq;

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

import java.time.LocalDateTime;

/**
 * MES-领退补料申请 DO
 *
 * @author iboot
 */
@TableName("mes_material_req")
@KeySequence("mes_material_req_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialReqDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 领退补料申请单号，由 infra 编码规则 MES_MATERIAL_REQ_NO 分配
     */
    private String reqNo;
    /**
     * 生产工单编号
     */
    private Long prodOrderId;
    /**
     * 生产工单号快照
     */
    private String prodOrderNo;
    /**
     * 作业工单编号，0 表示工单级申请
     */
    private Long workOrderId;
    /**
     * 工序任务编号，0 表示非工序级申请
     */
    private Long opTaskId;
    /**
     * 申请类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum}
     */
    private String reqType;
    /**
     * 库存组织编号，来自 mdm_inv_org
     */
    private Long invOrgId;
    /**
     * 目标仓库或来源仓库编号，来自 wms_wh
     */
    private Long whId;
    /**
     * 项目编号，0 表示无项目，从生产工单继承
     */
    private Long projectId;
    /**
     * 项目任务编号，0 表示无项目任务，从生产工单继承
     */
    private Long projectTaskId;
    /**
     * WMS 生成的出入库单号，由 WMS 回写
     */
    private String wmsBizNo;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum}
     */
    private String status;
    /**
     * 创建幂等键，唯一索引防止重复申请
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;

    /** 审批状态，取值见 BpmProcessInstanceStatusEnum。 */
    private Integer approvalStatus;
    /** 流程实例编号。 */
    private String processInstanceId;
    /** 审批完成时间。 */
    private LocalDateTime approvalTime;
    /** 审批意见或驳回原因。 */
    private String approvalReason;

}
