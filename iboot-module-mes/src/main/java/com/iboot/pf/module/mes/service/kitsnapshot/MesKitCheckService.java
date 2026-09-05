package com.iboot.pf.module.mes.service.kitsnapshot;

import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckBO;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckResultBO;

/**
 * 齐套检查领域动作。
 *
 * <p>与 codegen 的 {@link KitSnapshotService} 并存：codegen Service 只查快照，本服务负责产生快照。
 * 快照表对外是只读账表，唯一写入路径在这里。
 *
 * <p>快照是**检查时点的快照，不占库存**。齐套通过不代表料被留住了，别的工单照样能领走；
 * 真正的占用发生在领料提交时的预留动作（见 {@code MesMaterialReqDomainService#submitMaterialReq}）。
 * 快照的价值在于事后复盘"当时判齐套时账上有多少"，以及 APS 缺料分析的对账基准。
 */
public interface MesKitCheckService {

    /**
     * 执行齐套检查：展开需求、比对库存、逐行写快照、汇总结论、回写齐套状态。
     *
     * <p>全部齐套时把生产工单从 {@code RELEASED} 推到 {@code READY}，车间由此知道可以开工。
     *
     * @param bo 检查参数，幂等键格式 {@code UI:{工单号}:KIT_CHECK:{检查编号}}
     * @return 整体结论与落库的快照行
     */
    MesKitCheckResultBO checkKit(MesKitCheckBO bo);

}
