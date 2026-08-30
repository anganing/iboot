package com.iboot.pf.module.wms.dal.dataobject.count;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/** 盘点明细。 */
@TableName("wms_count_order_line")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsCountOrderLineDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 盘点单编号。 */
    private Long orderId;

    /** 行号。 */
    private Integer lineNo;

    /** 库存余额编号。 */
    private Long balanceId;

    /** 物料编号。 */
    private Long matId;

    /** 物料编码快照。 */
    private String matCode;

    /** 物料名称快照。 */
    private String matName;

    /** 批次编号。 */
    private Long batchId;

    /** 批次号快照。 */
    private String lotNo;

    /** 库位编号。 */
    private Long locId;

    /** 库位编码快照。 */
    private String locCode;

    /** 容器编号，0 表示散件。 */
    private Long containerId;

    /** 质量状态。 */
    private String qualityStatus;

    /** 项目编号，0 表示无项目。 */
    private Long projectId;

    /** 项目任务编号，0 表示无项目任务。 */
    private Long projectTaskId;

    /** 账面数量。 */
    private BigDecimal bookQty;

    /** 实盘数量。 */
    private BigDecimal actualQty;

    /** 差异数量。 */
    private BigDecimal diffQty;

    /** 差异调整入出库单号。 */
    private String adjustBizNo;

    /** 明细状态：PENDING、COUNTED、DIFF、ADJUSTED。 */
    private String status;

    /** 备注。 */
    private String remark;

}
