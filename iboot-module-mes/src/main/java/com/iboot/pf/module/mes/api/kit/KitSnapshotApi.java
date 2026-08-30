package com.iboot.pf.module.mes.api.kit;

import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.KitReq;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import java.util.List;

/** 齐套快照 API 门面。快照只记录结果，不占用库存。 */
public interface KitSnapshotApi {
    MesRespDTO checkKitSnapshot(KitReq reqDTO);
    List<MesRespDTO> getKitSnapshotList(Long prodOrderId);
}
