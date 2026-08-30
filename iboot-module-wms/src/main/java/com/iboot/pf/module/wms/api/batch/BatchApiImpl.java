package com.iboot.pf.module.wms.api.batch;

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

/**
 * 批次 API 实现类。
 *
 * <p>查询方法关闭数据权限，避免内部跨模块调用被当前用户数据权限过滤。
 */
@Service
@Validated
public class BatchApiImpl implements BatchApi {

    @Resource
    private WmsBatchService batchService;

    @Override
    @DataPermission(enable = false)
    public BatchRespDTO getBatch(Long id) {
        return BeanUtils.toBean(batchService.getBatch(id), BatchRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<BatchRespDTO> getBatchList(Collection<Long> ids) {
        return BeanUtils.toBean(batchService.getBatchList(ids), BatchRespDTO.class);
    }

    @Override
    public Map<Long, BatchRespDTO> getBatchMap(Collection<Long> ids) {
        return convertMap(BeanUtils.toBean(batchService.getBatchList(ids), BatchRespDTO.class), BatchRespDTO::getId);
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
