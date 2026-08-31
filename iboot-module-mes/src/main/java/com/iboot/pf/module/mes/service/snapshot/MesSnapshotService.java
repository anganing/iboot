package com.iboot.pf.module.mes.service.snapshot;

import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.defectcode.DefectCodeApi;
import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.op.OpApi;
import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;
import com.iboot.pf.module.mdm.api.org.OrgApi;
import com.iboot.pf.module.mdm.api.project.ProjectApi;
import com.iboot.pf.module.mdm.api.resource.ResourceApi;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteRespDTO;
import com.iboot.pf.module.mdm.api.shift.ShiftApi;
import com.iboot.pf.module.mdm.api.team.TeamApi;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.system.api.user.AdminUserApi;
import com.iboot.pf.module.wms.api.batch.BatchApi;
import com.iboot.pf.module.wms.api.batch.dto.BatchRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.time.LocalDate;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.BATCH_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.BOM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DEFECT_CODE_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MAT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PLANT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROJECT_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.RES_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.ROUTE_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.ROUTE_OP_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.SHIFT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.TEAM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.UOM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WC_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORKSHOP_NOT_EXISTS;

/**
 * MES 单据的外键校验与快照补全服务。
 *
 * <p>单据上的物料编码、物料名称、计量单位编码、工序编码这些字段都是快照：写入时从主数据取一次落库，
 * 之后列表查询不再回查 mdm，主数据改名也不影响历史单据。
 *
 * <p>跨模块取数一律走对方模块的 Api 门面（mdm、wms、system），不允许 Mapper 里 JOIN 别的模块的表。
 * 跨库 JOIN 会把两个模块焊死在同一个数据库上，将来微服务拆分无处下手。
 */
@Service
public class MesSnapshotService {

    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;
    @Resource
    private BomApi bomApi;
    @Resource
    private RouteApi routeApi;
    @Resource
    private OpApi opApi;
    @Resource
    private OrgApi orgApi;
    @Resource
    private ProjectApi projectApi;
    @Resource
    private ResourceApi resourceApi;
    @Resource
    private ShiftApi shiftApi;
    @Resource
    private TeamApi teamApi;
    @Resource
    private DefectCodeApi defectCodeApi;
    @Resource
    private BatchApi batchApi;
    @Resource
    private AdminUserApi adminUserApi;

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
        UomRespDTO uom = uomId == null ? null
                : uomApi.getUomMap(Collections.singletonList(uomId)).get(uomId);
        if (uom == null) {
            throw exception(UOM_NOT_EXISTS);
        }
        return uom.getCode();
    }

    /** 取 BOM 版本快照，工单锁定 BOM 时用。 */
    public String requireBomVersion(Long bomId) {
        BomRespDTO bom = bomId == null ? null : bomApi.getBom(bomId);
        if (bom == null) {
            throw exception(BOM_NOT_EXISTS);
        }
        return bom.getVersion();
    }

    /** 取物料在指定日期的生效 BOM，导入建单时自动锁定版本。 */
    public BomRespDTO requireEffectiveBom(Long matId, LocalDate date) {
        BomRespDTO bom = matId == null ? null : bomApi.getEffectiveBom(matId, date);
        if (bom == null) {
            throw exception(BOM_NOT_EXISTS);
        }
        return bom;
    }

    /** 取物料在指定日期的生效工艺路线，导入建单时自动锁定版本。 */
    public RouteRespDTO requireEffectiveRoute(Long matId, LocalDate date) {
        RouteRespDTO route = matId == null ? null : routeApi.getEffectiveRoute(matId, date);
        if (route == null) {
            throw exception(ROUTE_NOT_EXISTS);
        }
        return route;
    }

    /** 取工艺路线版本快照，工单锁定路线时用。 */
    public String requireRouteVersion(Long routeId) {
        RouteRespDTO route = routeId == null ? null : routeApi.getRoute(routeId);
        if (route == null) {
            throw exception(ROUTE_NOT_EXISTS);
        }
        return route.getVersion();
    }

    /** 取路线工序，工序任务锁定 routeOpId 时用来带出工序顺序与并行组。 */
    public RouteOpRespDTO requireRouteOp(Long routeOpId) {
        RouteOpRespDTO routeOp = routeOpId == null ? null
                : routeApi.getRouteOpMap(Collections.singletonList(routeOpId)).get(routeOpId);
        if (routeOp == null) {
            throw exception(ROUTE_OP_NOT_EXISTS);
        }
        return routeOp;
    }

    /** 取标准工序快照，工序任务落工序编码与名称时用。 */
    public OpRespDTO requireOp(Long opId) {
        OpRespDTO op = opId == null ? null : opApi.getOp(opId);
        if (op == null) {
            throw exception(OP_NOT_EXISTS);
        }
        return op;
    }

    /** 校验工厂存在。 */
    public void validatePlant(Long plantId) {
        if (plantId == null || orgApi.getPlantMap(Collections.singletonList(plantId)).get(plantId) == null) {
            throw exception(PLANT_NOT_EXISTS);
        }
    }

    /** 校验车间存在。 */
    public void validateWorkshop(Long workshopId) {
        if (workshopId == null
                || orgApi.getWorkshopMap(Collections.singletonList(workshopId)).get(workshopId) == null) {
            throw exception(WORKSHOP_NOT_EXISTS);
        }
    }

    /** 校验工作中心存在。 */
    public void validateWc(Long wcId) {
        if (wcId == null || orgApi.getWcMap(Collections.singletonList(wcId)).get(wcId) == null) {
            throw exception(WC_NOT_EXISTS);
        }
    }

    /** 校验生产资源存在，0 表示未分配资源。 */
    public void validateRes(Long resId) {
        if (isNone(resId)) {
            return;
        }
        if (resourceApi.getResource(resId) == null) {
            throw exception(RES_NOT_EXISTS);
        }
    }

    /** 校验班次存在，0 表示不区分班次。 */
    public void validateShift(Long shiftId) {
        if (isNone(shiftId)) {
            return;
        }
        if (shiftApi.getShiftMap(Collections.singletonList(shiftId)).get(shiftId) == null) {
            throw exception(SHIFT_NOT_EXISTS);
        }
    }

    /** 校验班组存在，0 表示未指定班组。 */
    public void validateTeam(Long teamId) {
        if (isNone(teamId)) {
            return;
        }
        if (teamApi.getTeamMap(Collections.singletonList(teamId)).get(teamId) == null) {
            throw exception(TEAM_NOT_EXISTS);
        }
    }

    /** 校验用户存在且可用，0 表示未指定人员。 */
    public void validateUser(Long userId) {
        if (isNone(userId)) {
            return;
        }
        adminUserApi.validateUser(userId);
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

    /** 取批次号快照，批次编号为空或 0 时返回 null（未定批次时允许后补）。 */
    public String lotNoOrNull(Long batchId) {
        if (isNone(batchId)) {
            return null;
        }
        BatchRespDTO batch = batchApi.getBatch(batchId);
        if (batch == null) {
            throw exception(BATCH_NOT_EXISTS);
        }
        return batch.getLotNo();
    }

    /** 取缺陷代码，缺陷记录落编码、名称、等级快照时用。 */
    public DefectCodeRespDTO requireDefectCode(Long defectCodeId) {
        DefectCodeRespDTO defectCode = defectCodeId == null ? null
                : defectCodeApi.getDefectCodeMap(Collections.singletonList(defectCodeId)).get(defectCodeId);
        if (defectCode == null) {
            throw exception(DEFECT_CODE_NOT_EXISTS);
        }
        return defectCode;
    }

    /** 可选外键：null 与 0 都表示“无”。 */
    private boolean isNone(Long id) {
        return id == null || MesConstants.NONE_ID.equals(id);
    }

}
