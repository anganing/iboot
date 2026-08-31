package com.iboot.pf.module.aps.dal.dataobject.taskdep;

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

/**
 * APS-排程任务依赖 DO。
 *
 * <p>约束求解器的先后关系边：前置任务、后置任务、依赖类型、间隔分钟数四者联合唯一。
 */
@TableName("aps_task_dep")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsTaskDepDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程版本编号。 */
    private Long versionId;

    /** 前置排程任务编号。 */
    private Long fromTaskId;

    /** 后置排程任务编号。 */
    private Long toTaskId;

    /** 依赖类型，取值见 {@link com.iboot.pf.module.aps.enums.ApsTaskDepTypeEnum}。 */
    private String depType;

    /** 依赖间隔分钟数。 */
    private Integer lagMinutes;

    /** 备注。 */
    private String remark;

}
