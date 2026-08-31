package com.iboot.pf.module.aps.dal.dataobject.projection;

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

import java.time.LocalDateTime;

/**
 * APS-MDM 物料投影 DO。
 *
 * <p>场景冻结的一份物料主数据副本（uk_aps_projection_mat_deleted：场景 + 来源物料）。
 * 投影只由“刷新投影”整体重建，是求解输入的只读快照，不接受人工新增或修改。
 */
@TableName("aps_projection_mat")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsMaterialProjectionDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程场景编号。 */
    private Long scenarioId;

    /** MDM 物料编号。 */
    private Long sourceMatId;

    /** 物料编码快照。 */
    private String matCode;

    /** 物料名称快照。 */
    private String matName;

    /** 物料规格快照。 */
    private String matSpec;

    /** 物料类型快照，取值见字典 mdm_mat_type。 */
    private String matType;

    /** 基准计量单位编号。 */
    private Long baseUomId;

    /** 基准计量单位编码快照。 */
    private String baseUomCode;

    /** 是否批次管控。 */
    private Boolean batchFlag;

    /** 当前有效 BOM 编号，0 表示无。 */
    private Long bomId;

    /** 当前有效 BOM 版本。 */
    private String bomVersion;

    /** 当前有效工艺路线编号，0 表示无。 */
    private Long routeId;

    /** 当前有效工艺路线版本。 */
    private String routeVersion;

    /** 来源数据更新时间。 */
    private LocalDateTime sourceUpdateTime;

    /** 投影快照时间。 */
    private LocalDateTime snapshotTime;

    /** 备注。 */
    private String remark;

}
