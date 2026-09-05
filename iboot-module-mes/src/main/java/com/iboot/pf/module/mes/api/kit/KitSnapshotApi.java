package com.iboot.pf.module.mes.api.kit;

import com.iboot.pf.module.mes.api.kit.dto.KitCheckReqDTO;
import com.iboot.pf.module.mes.api.kit.dto.KitCheckRespDTO;
import com.iboot.pf.module.mes.api.kit.dto.KitSnapshotRespDTO;

import java.util.List;

/**
 * 齐套快照 API 门面。
 *
 * <p>快照只记录检查那一刻的库存口径，不占用库存：占用发生在领料提交的预留阶段。
 */
public interface KitSnapshotApi {

    /**
     * 执行齐套检查并写快照。
     *
     * @param reqDTO 检查命令
     * @return 整体结论与明细行
     */
    KitCheckRespDTO checkKitSnapshot(KitCheckReqDTO reqDTO);

    /**
     * 查询某张生产工单的齐套快照行。
     *
     * @param prodOrderId 生产工单编号
     * @return 快照行列表
     */
    List<KitSnapshotRespDTO> getKitSnapshotList(Long prodOrderId);

}
