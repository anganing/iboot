package com.iboot.pf.module.wms.service.snapshot;

import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.batchmgr.BatchDO;
import com.iboot.pf.module.wms.dal.dataobject.containermgr.ContainerDO;
import com.iboot.pf.module.wms.dal.dataobject.loc.LocDO;
import com.iboot.pf.module.wms.dal.mysql.batchmgr.BatchMapper;
import com.iboot.pf.module.wms.dal.mysql.containermgr.ContainerMapper;
import com.iboot.pf.module.wms.dal.mysql.loc.LocMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.BATCH_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.CONTAINER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LINE_MAT_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LINE_UOM_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOC_NOT_EXISTS;

/**
 * 单据明细的快照补全服务。
 *
 * <p>单据明细上的物料编码、单位编码、库位编码、批次号都是快照：写入时从主数据取一次落库，
 * 之后列表查询不再回查 mdm，主数据改名也不影响历史单据。
 *
 * <p>跨模块取数一律走 mdm 的 Api 门面，不允许 Mapper 里 JOIN mdm 的表。
 */
@Service
public class WmsSnapshotService {

    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;
    @Resource
    private LocMapper locMapper;
    @Resource
    private BatchMapper batchMapper;
    @Resource
    private ContainerMapper containerMapper;

    /** 取物料快照，不存在直接报错，避免脏外键落库。 */
    public MaterialRespDTO requireMaterial(Long matId) {
        MaterialRespDTO material = matId == null ? null : materialApi.getMaterial(matId);
        if (material == null) {
            throw exception(LINE_MAT_NOT_EXISTS);
        }
        return material;
    }

    /** 取计量单位编码快照。 */
    public String requireUomCode(Long uomId) {
        UomRespDTO uom = uomId == null ? null
                : uomApi.getUomMap(Collections.singletonList(uomId)).get(uomId);
        if (uom == null) {
            throw exception(LINE_UOM_NOT_EXISTS);
        }
        return uom.getCode();
    }

    /** 取库位编码快照，库位编号为空时返回 null（部分明细的库位允许后补）。 */
    public String locCodeOrNull(Long locId) {
        if (locId == null || locId == 0L) {
            return null;
        }
        LocDO loc = locMapper.selectById(locId);
        if (loc == null) {
            throw exception(LOC_NOT_EXISTS);
        }
        return loc.getCode();
    }

    /** 取批次号快照，批次编号为空时返回 null（过账前允许未定批次）。 */
    public String lotNoOrNull(Long batchId) {
        if (batchId == null || batchId == 0L) {
            return null;
        }
        BatchDO batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw exception(BATCH_NOT_EXISTS);
        }
        return batch.getLotNo();
    }

    /** 校验容器存在，容器编号为空表示散件。 */
    public void validateContainer(Long containerId) {
        if (containerId == null || containerId == 0L) {
            return;
        }
        ContainerDO container = containerMapper.selectById(containerId);
        if (container == null) {
            throw exception(CONTAINER_NOT_EXISTS);
        }
    }

}
