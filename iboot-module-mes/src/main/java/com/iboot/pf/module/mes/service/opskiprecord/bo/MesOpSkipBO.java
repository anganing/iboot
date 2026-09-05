package com.iboot.pf.module.mes.service.opskiprecord.bo;

import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskActionBO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 跳序参数。
 *
 * <p>在 {@link MesOpTaskActionBO} 的基础上多两个跳序专属字段。为什么不把它们加到公共动作参数上：
 * 暂停、恢复、开工三个动作永远用不到"跳序原因类型"与"影响说明"，加上去只会得到两个恒为空的字段，
 * 而空字段最终会让人搞不清哪个动作该填什么。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesOpSkipBO extends MesOpTaskActionBO {

    /**
     * 跳序原因类型，必填，取值见 {@code MesSkipReasonTypeEnum}。
     *
     * <p>跳序原因本身走继承来的 {@code reason} 字段：它落 {@code mes_op_skip_record.skip_reason}，
     * 同时也是在制流水的 remark，两处是同一句话。
     */
    private String skipReasonType;

    /**
     * 对后续工序、质检及排程的影响说明。
     *
     * <p>{@code APPROVAL_REQUIRED} 策略下必填——审批人要凭它判断能不能跳，只给一句"设备故障"
     * 没人敢批。{@code ALLOW_WITH_REASON} 下可空。
     */
    private String effectRemark;

}
