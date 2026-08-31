package com.iboot.pf.module.aps.dal.dataobject.shortage;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * APS-排程缺料清单 DO。
 *
 * <p>排程算完后按物料汇总的缺口：需求数量 - 可用数量 = 短缺数量。
 * 物料编码、名称、单位编码是写入时的快照。
 */
@TableName("aps_shortage")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsShortageDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程版本编号。 */
    private Long versionId;

    /** 生产工单编号，0 表示汇总缺料。 */
    private Long prodOrderId;

    /** 工序任务编号，0 表示非工序级缺料。 */
    private Long opTaskId;

    /** 项目编号，0 表示无项目。 */
    private Long projectId;

    /** 项目任务编号，0 表示无项目任务。 */
    private Long projectTaskId;

    /** 短缺物料编号。 */
    private Long matId;

    /** 物料编码快照。 */
    private String matCode;

    /** 物料名称快照。 */
    private String matName;

    /** 需求数量。 */
    private BigDecimal reqQty;

    /** 可用数量，由库存投影汇总。 */
    private BigDecimal availQty;

    /** 短缺数量。 */
    private BigDecimal shortQty;

    /** 计量单位编号。 */
    private Long uomId;

    /** 计量单位编码快照。 */
    private String uomCode;

    /** 需求到位时间。 */
    private LocalDateTime needDate;

    /** 预计供给时间。 */
    private LocalDateTime expectedSupplyDate;

    /** 状态，取值见 {@link com.iboot.pf.module.aps.enums.ApsShortageStatusEnum}。 */
    private String status;

    /** 备注。 */
    private String remark;

}
