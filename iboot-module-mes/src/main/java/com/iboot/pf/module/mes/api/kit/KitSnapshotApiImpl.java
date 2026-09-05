package com.iboot.pf.module.mes.api.kit;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.kit.dto.KitCheckReqDTO;
import com.iboot.pf.module.mes.api.kit.dto.KitCheckRespDTO;
import com.iboot.pf.module.mes.api.kit.dto.KitSnapshotRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** 齐套快照 API 实现。库存可用量由 WMS InventoryApi 提供。 */
@Service
@Validated
public class KitSnapshotApiImpl implements KitSnapshotApi {

    @Override
    public KitCheckRespDTO checkKitSnapshot(KitCheckReqDTO reqDTO) {
        throw new UnsupportedOperationException("齐套检查命令尚未接入领域规则");
    }

    @Override
    @DataPermission(enable = false)
    public List<KitSnapshotRespDTO> getKitSnapshotList(Long prodOrderId) {
        throw new UnsupportedOperationException("齐套快照查询尚未接入领域 Service");
    }

}
