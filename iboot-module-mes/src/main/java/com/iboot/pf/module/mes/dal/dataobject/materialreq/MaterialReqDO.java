package com.iboot.pf.module.mes.dal.dataobject.materialreq;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-领退补料申请 DO
 *
 * @author iboot
 */
@TableName("mes_material_req")
@KeySequence("mes_material_req_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * 领退补料申请单号
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
     * 作业工单编号，0表示工单级申请
     */
    private Long workOrderId;
    /**
     * 工序任务编号，0表示非工序级申请
     */
    private Long opTaskId;
    /**
     * 申请类型：ISSUE、RETURN、SUPPLEMENT
     */
    private String reqType;
    /**
     * 库存组织编号
     */
    private Long invOrgId;
    /**
     * 目标仓库或来源仓库编号
     */
    private Long whId;
    /**
     * 项目编号，0表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0表示无项目任务
     */
    private Long projectTaskId;
    /**
     * WMS生成的出入库单号
     */
    private String wmsBizNo;
    /**
     * 状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED
     */
    private String status;
    /**
     * 创建幂等键
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;


}