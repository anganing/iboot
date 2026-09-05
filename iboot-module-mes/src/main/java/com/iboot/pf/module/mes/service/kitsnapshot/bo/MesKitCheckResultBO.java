package com.iboot.pf.module.mes.service.kitsnapshot.bo;

import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 齐套检查结果：整体结论 + 落库的快照行。
 *
 * <p>汇总规则留在服务端（全齐 {@code FULL_KIT}、有齐有缺 {@code PARTIAL_KIT}、全缺 {@code SHORTAGE}），
 * 不让调用方按行自己拼结论——两个调用方拼法不同，同一份快照就有两种说法。
 */
@Data
public class MesKitCheckResultBO {

    /** 齐套检查编号，一次检查一个，走编码规则生成。 */
    private String checkNo;

    /** 检查范围，取 {@link com.iboot.pf.module.mes.enums.MesKitCheckScopeEnum} 的枚举名。 */
    private String checkScope;

    /** 生产工单编号。 */
    private Long prodOrderId;

    /** 作业工单编号，0 表示整单检查。 */
    private Long workOrderId;

    /** 工序任务编号，0 表示非工序检查。 */
    private Long opTaskId;

    /** 整体齐套结论，取 {@link com.iboot.pf.module.mes.enums.MesKitStatusEnum} 的枚举名。 */
    private String result;

    /** 检查时间，与快照行上的 {@code check_time} 同值。 */
    private LocalDateTime checkTime;

    /** 落库的快照行，按行号升序。无需求行时为空列表。 */
    private List<KitSnapshotDO> lines;

}
