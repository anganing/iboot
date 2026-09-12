package com.iboot.pf.module.wms.api.batch;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.batch.dto.BatchRespDTO;
import com.iboot.pf.module.wms.api.batch.dto.RegisterBatchReqDTO;
import com.iboot.pf.module.wms.api.batch.dto.RegisterBatchRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.service.batch.WmsBatchService;
import com.iboot.pf.module.wms.service.batch.bo.RegisterBatchBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMultiMap;

/**
 * 批次 API 实现类。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：跨模块调用天生是系统身份
 * 而不是用户身份，与调用方登录用户的数据范围无关。打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 */
@Service
@Validated
@DataPermission(enable = false)
public class BatchApiImpl implements BatchApi {

    @Resource
    private WmsBatchService batchService;

    @Override
    public BatchRespDTO getBatch(Long id) {
        return BeanUtils.toBean(batchService.getBatch(id), BatchRespDTO.class);
    }

    @Override
    public List<BatchRespDTO> getBatchList(Collection<Long> ids) {
        return BeanUtils.toBean(batchService.getBatchList(ids), BatchRespDTO.class);
    }

    @Override
    public Map<Long, BatchRespDTO> getBatchMap(Collection<Long> ids) {
        return convertMap(BeanUtils.toBean(batchService.getBatchList(ids), BatchRespDTO.class), BatchRespDTO::getId);
    }

    @Override
    public Map<String, List<BatchRespDTO>> getBatchMapByLotNos(Collection<String> lotNos) {
        if (CollUtil.isEmpty(lotNos)) {
            return Map.of();
        }
        List<BatchRespDTO> batches = BeanUtils.toBean(batchService.getBatchListByLotNos(lotNos), BatchRespDTO.class);
        // 批次号只在物料内唯一，同一批次号可命中多条，值用列表，不能收敛成单条
        return convertMultiMap(batches, BatchRespDTO::getLotNo);
    }

    @Override
    public RegisterBatchRespDTO registerBatch(RegisterBatchReqDTO reqDTO) {
        RegisterBatchBO bo = BeanUtils.toBean(reqDTO, RegisterBatchBO.class);
        WmsBatchDO batch = batchService.registerBatch(bo);
        RegisterBatchRespDTO respDTO = new RegisterBatchRespDTO();
        respDTO.setId(batch.getId());
        respDTO.setLotNo(batch.getLotNo());
        respDTO.setSnNo(batch.getSnNo());
        return respDTO;
    }

}
