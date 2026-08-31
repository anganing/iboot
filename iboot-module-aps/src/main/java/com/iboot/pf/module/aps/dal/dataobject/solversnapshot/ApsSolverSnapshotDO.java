package com.iboot.pf.module.aps.dal.dataobject.solversnapshot;

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
 * APS-求解快照 DO。
 *
 * <p>求解过程的证据链：输入、结果、约束命中、异常各存一份 JSON，校验值用于去重
 * （uk_aps_solver_snapshot_deleted）。只读留痕，不允许人工改写。
 */
@TableName("aps_solver_snapshot")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsSolverSnapshotDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程版本编号。 */
    private Long versionId;

    /** 快照类型，取值见 {@link com.iboot.pf.module.aps.enums.ApsSnapshotTypeEnum}。 */
    private String snapshotType;

    /** 求解输入、结果或约束命中快照，JSON。 */
    private String snapshotJson;

    /** 快照内容校验值。 */
    private String checksum;

    /** 快照生成时间。 */
    private LocalDateTime snapshotTime;

    /** 备注。 */
    private String remark;

}
