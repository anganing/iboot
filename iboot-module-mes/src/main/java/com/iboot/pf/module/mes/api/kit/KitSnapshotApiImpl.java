package com.iboot.pf.module.mes.api.kit;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.mes.api.kit.dto.KitCheckReqDTO;
import com.iboot.pf.module.mes.api.kit.dto.KitCheckRespDTO;
import com.iboot.pf.module.mes.api.kit.dto.KitSnapshotRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import com.iboot.pf.module.mes.dal.mysql.kitsnapshot.KitSnapshotMapper;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitCheckService;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckBO;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckResultBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 齐套快照 API 实现。库存可用量由 WMS InventoryApi 提供。
 *
 * <p>门面只做转换与转调：检查转调 {@link MesKitCheckService}（快照表对外是只读账表，唯一写入路径在那里），
 * 快照查询直接读账表。整体结论的汇总规则留在服务端，门面原样带出，不按行自己拼结论——两个调用方拼法不同，
 * 同一份快照就有两种说法。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：齐套算的是仓库里客观存在的料，
 * 与调用方的登录用户无关。
 */
@Service
@Validated
@DataPermission(enable = false)
public class KitSnapshotApiImpl implements KitSnapshotApi {

    @Resource
    private MesKitCheckService kitCheckService;
    @Resource
    private KitSnapshotMapper kitSnapshotMapper;

    @Override
    public KitCheckRespDTO checkKitSnapshot(KitCheckReqDTO reqDTO) {
        MesKitCheckResultBO result = kitCheckService.checkKit(BeanUtils.toBean(reqDTO, MesKitCheckBO.class));
        KitCheckRespDTO respDTO = BeanUtils.toBean(result, KitCheckRespDTO.class);
        // 明细行的载体不同（BO 里是落库的 KitSnapshotDO，DTO 里是 KitSnapshotRespDTO），显式转一遍：
        // 缺料量、替代料、逐行结论都在行上，丢了行等于只给一个"部分齐套"的结论而不说缺什么
        respDTO.setLines(BeanUtils.toBean(result.getLines(), KitSnapshotRespDTO.class));
        return respDTO;
    }

    @Override
    public List<KitSnapshotRespDTO> getKitSnapshotList(Long prodOrderId) {
        if (prodOrderId == null) {
            return List.of();
        }
        // 一张工单可以反复检查、留多份快照，这里按主键升序返回全部，调用方自行按 checkNo 分组
        List<KitSnapshotDO> lines = kitSnapshotMapper.selectList(new LambdaQueryWrapperX<KitSnapshotDO>()
                .eq(KitSnapshotDO::getProdOrderId, prodOrderId)
                .orderByAsc(KitSnapshotDO::getId));
        return BeanUtils.toBean(lines, KitSnapshotRespDTO.class);
    }

}
