package com.iboot.pf.module.aps.service.snapshot;

import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.org.OrgApi;
import com.iboot.pf.module.mdm.api.project.ProjectApi;
import com.iboot.pf.module.mdm.api.resource.ResourceApi;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import com.iboot.pf.module.mdm.api.shift.ShiftApi;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.MAT_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.PLANT_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.PROJECT_TASK_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.RES_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SHIFT_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.UOM_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.WC_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.WORKSHOP_NOT_EXISTS;

/**
 * APS 的外键校验与快照补全服务。
 *
 * <p>投影、任务、缺料、负荷上的物料编码、资源编码、单位编码这些字段都是快照：写入时从主数据取一次落库，
 * 之后查询不再回查 mdm，主数据改名也不影响历史排程结果。
 *
 * <p>跨模块取数一律走对方模块的 Api 门面，不允许 Mapper 里 JOIN 别的模块的表。
 * 跨库 JOIN 会把两个模块焊死在同一个数据库上，将来微服务拆分无处下手。
 */
@Service
public class ApsSnapshotService {

    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;
    @Resource
    private ResourceApi resourceApi;
    @Resource
    private OrgApi orgApi;
    @Resource
    private ProjectApi projectApi;
    @Resource
    private ShiftApi shiftApi;

    /** 取物料快照，不存在直接报错，避免脏外键落库。 */
    public MaterialRespDTO requireMaterial(Long matId) {
        MaterialRespDTO material = matId == null ? null : materialApi.getMaterial(matId);
        if (material == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        return material;
    }

    /** 取计量单位编码快照。 */
    public String requireUomCode(Long uomId) {
        UomRespDTO uom = uomId == null ? null : uomApi.getUomMap(Collections.singletonList(uomId)).get(uomId);
        if (uom == null) {
            throw exception(UOM_NOT_EXISTS);
        }
        return uom.getCode();
    }

    /** 取生产资源快照，任务与负荷落资源编码、名称时用。 */
    public ResourceRespDTO requireResource(Long resId) {
        ResourceRespDTO resource = resId == null ? null : resourceApi.getResource(resId);
        if (resource == null) {
            throw exception(RES_NOT_EXISTS);
        }
        return resource;
    }

    /** 校验工厂存在。 */
    public void validatePlant(Long plantId) {
        if (plantId == null || orgApi.getPlantMap(Collections.singletonList(plantId)).get(plantId) == null) {
            throw exception(PLANT_NOT_EXISTS);
        }
    }

    /** 校验车间存在，0 表示全工厂。 */
    public void validateWorkshop(Long workshopId) {
        if (isNone(workshopId)) {
            return;
        }
        if (orgApi.getWorkshopMap(Collections.singletonList(workshopId)).get(workshopId) == null) {
            throw exception(WORKSHOP_NOT_EXISTS);
        }
    }

    /** 校验工作中心存在，0 表示未指定。 */
    public void validateWc(Long wcId) {
        if (isNone(wcId)) {
            return;
        }
        if (orgApi.getWcMap(Collections.singletonList(wcId)).get(wcId) == null) {
            throw exception(WC_NOT_EXISTS);
        }
    }

    /** 校验班次存在，0 表示日汇总。 */
    public void validateShift(Long shiftId) {
        if (isNone(shiftId)) {
            return;
        }
        if (shiftApi.getShiftMap(Collections.singletonList(shiftId)).get(shiftId) == null) {
            throw exception(SHIFT_NOT_EXISTS);
        }
    }

    /** 校验项目与项目任务存在，0 表示无项目。 */
    public void validateProject(Long projectId, Long projectTaskId) {
        if (!isNone(projectId)
                && projectApi.getProjectMap(Collections.singletonList(projectId)).get(projectId) == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
        if (!isNone(projectTaskId)
                && projectApi.getProjectTaskMap(Collections.singletonList(projectTaskId)).get(projectTaskId) == null) {
            throw exception(PROJECT_TASK_NOT_EXISTS);
        }
    }

    /** 可选外键：null 与 0 都表示“无”。 */
    private boolean isNone(Long id) {
        return id == null || ApsConstants.NONE_ID.equals(id);
    }

}
