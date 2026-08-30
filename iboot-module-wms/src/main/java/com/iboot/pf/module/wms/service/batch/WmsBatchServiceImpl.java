package com.iboot.pf.module.wms.service.batch;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.dal.mysql.batch.WmsBatchMapper;
import com.iboot.pf.module.wms.service.batch.bo.RegisterBatchBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.BATCH_LOT_NO_BLANK;

/** 批次 Service 实现。 */
@Service
public class WmsBatchServiceImpl implements WmsBatchService {

    @Resource
    private WmsBatchMapper wmsBatchMapper;
    @Resource
    private MaterialApi materialApi;

    @Override
    public WmsBatchDO getBatch(Long id) {
        return wmsBatchMapper.selectById(id);
    }

    @Override
    public List<WmsBatchDO> getBatchList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return wmsBatchMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, WmsBatchDO> getBatchMap(Collection<Long> ids) {
        return convertMap(getBatchList(ids), WmsBatchDO::getId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsBatchDO registerBatch(RegisterBatchBO bo) {
        MaterialRespDTO material = materialApi.getMaterial(bo.getMatId());
        if (material == null) {
            materialApi.validateMaterialList(List.of(bo.getMatId()));
        }
        return registerBatch(bo, material);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsBatchDO registerBatch(RegisterBatchBO bo, MaterialRespDTO material) {
        if (bo.getLotNo() == null || bo.getLotNo().isEmpty()) {
            throw exception(BATCH_LOT_NO_BLANK);
        }
        WmsBatchDO batch = wmsBatchMapper.selectByMatAndLot(bo.getMatId(), bo.getLotNo(), bo.getSnNo());
        if (batch != null) {
            return batch;
        }
        if (material != null) {
            bo.setMatCode(material.getCode());
            bo.setMatName(material.getName());
        }
        batch = BeanUtils.toBean(bo, WmsBatchDO.class);
        batch.setId(null);
        batch.setStatus(0);
        wmsBatchMapper.insert(batch);
        return batch;
    }

}
