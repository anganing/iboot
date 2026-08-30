package com.iboot.pf.module.mes.api.kit;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.KitReq;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import com.iboot.pf.module.mes.service.MesApiService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.List;

/** 齐套快照 API 实现。库存可用量由 WMS InventoryApi 提供。 */
@Service @Validated
public class KitSnapshotApiImpl implements KitSnapshotApi {
    @Resource private MesApiService service;
    @Override public MesRespDTO checkKitSnapshot(KitReq reqDTO) { return service.checkKit(reqDTO); }
    @Override @DataPermission(enable = false) public List<MesRespDTO> getKitSnapshotList(Long prodOrderId) { return service.listBy("kit", "prodOrderId", List.of(prodOrderId)); }
}
