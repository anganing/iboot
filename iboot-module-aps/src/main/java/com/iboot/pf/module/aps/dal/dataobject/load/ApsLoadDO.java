package com.iboot.pf.module.aps.dal.dataobject.load;

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
import java.time.LocalDate;

/**
 * APS-资源负荷 DO。
 *
 * <p>按“版本 + 资源 + 日期”汇总的产能占用（uk_aps_load_res_day_deleted）：
 * 负荷分钟数来自当日排定的任务，可用分钟数来自资源日历投影，两者相减得到超负荷分钟数。
 */
@TableName("aps_load")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsLoadDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程版本编号。 */
    private Long versionId;

    /** 生产资源编号。 */
    private Long resId;

    /** 资源编码快照。 */
    private String resCode;

    /** 资源名称快照。 */
    private String resName;

    /** 负荷日期。 */
    private LocalDate workDate;

    /** 计划负荷分钟数。 */
    private Integer loadMinutes;

    /** 可用产能分钟数。 */
    private Integer capacityMinutes;

    /** 负荷率 = 负荷分钟 / 可用分钟，decimal(10,4)。 */
    private BigDecimal loadRate;

    /** 超负荷分钟数，不超时为 0。 */
    private Integer overloadMinutes;

    /** 备注。 */
    private String remark;

}
