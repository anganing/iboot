package com.iboot.pf.module.aps.dal.dataobject.version;

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
 * APS-排程版本 DO。
 *
 * <p>一次求解产出一个版本，版本号在场景内连续（uk_aps_version_scenario_no_deleted）。
 * 同一场景下最多只有一个已发布版本，发布新版本时旧版本自动置为已被替换。
 */
@TableName("aps_version")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsVersionDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程场景编号。 */
    private Long scenarioId;

    /** 场景内排程版本号，从 1 开始。 */
    private Integer versionNo;

    /** 状态，取值见 {@link com.iboot.pf.module.aps.enums.ApsVersionStatusEnum}。 */
    private String status;

    /** 求解器综合得分。 */
    private String score;

    /** 硬约束得分。 */
    private Long hardScore;

    /** 软约束得分。 */
    private Long softScore;

    /** 求解开始时间。 */
    private LocalDateTime solveStartTime;

    /** 求解结束时间。 */
    private LocalDateTime solveEndTime;

    /** 发布时间。 */
    private LocalDateTime publishedTime;

    /** 发布人员编号。 */
    private Long publishedBy;

    /** 求解结果或失败信息。 */
    private String solverMessage;

    /**
     * 乐观锁版本，防止并发发布同一版本。
     *
     * <p>不用 MyBatis-Plus 的 {@code @Version}：框架没有注册 OptimisticLockerInnerInterceptor，
     * 加了注解只会让 updateById 生成拿不到参数的 SQL。和 WMS 库存余额一致，由 Service 显式带版本条件更新。
     */
    private Integer version;

    /** 备注。 */
    private String remark;

}
