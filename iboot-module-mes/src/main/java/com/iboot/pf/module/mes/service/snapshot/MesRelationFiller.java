package com.iboot.pf.module.mes.service.snapshot;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.op.OpApi;
import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;
import com.iboot.pf.module.mdm.api.org.OrgApi;
import com.iboot.pf.module.mdm.api.org.dto.PlantRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WcRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WorkshopRespDTO;
import com.iboot.pf.module.mdm.api.project.ProjectApi;
import com.iboot.pf.module.mdm.api.project.dto.ProjectRespDTO;
import com.iboot.pf.module.mdm.api.project.dto.ProjectTaskRespDTO;
import com.iboot.pf.module.mdm.api.resource.ResourceApi;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import com.iboot.pf.module.mdm.api.shift.ShiftApi;
import com.iboot.pf.module.mdm.api.shift.dto.ShiftRespDTO;
import com.iboot.pf.module.mdm.api.team.TeamApi;
import com.iboot.pf.module.mdm.api.team.dto.TeamRespDTO;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.system.api.user.AdminUserApi;
import com.iboot.pf.module.system.api.user.dto.AdminUserRespDTO;
import com.iboot.pf.module.wms.api.warehouse.WarehouseApi;
import com.iboot.pf.module.wms.api.warehouse.dto.WhRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 列表与详情的跨模块可读字段补全器。
 *
 * <p>模块内的关联字段在 Mapper 里 JOIN 出来；工厂、车间、工作中心、项目、资源、班次、班组、用户
 * 这些落在 mdm 与 system 的数据不能跨库 JOIN，统一在这里按“收集编号 -> 一次批量查询 -> 回填”三步补全，
 * 保证一批记录只打一次对方模块的接口，不出现 N+1。
 *
 * <p>补全必须发生在 Service 层：Excel 导出复用 Service 的分页结果，写在 Controller 里会丢字段。
 */
@Service
public class MesRelationFiller {

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
    private MaterialApi materialApi;
    @Resource
    private OpApi opApi;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private WarehouseApi warehouseApi;

    /** 补全工厂名称。 */
    public <T> void fillPlantName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> orgApi.getPlantMap(ids), PlantRespDTO::getName);
    }

    /** 补全车间名称。 */
    public <T> void fillWorkshopName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> orgApi.getWorkshopMap(ids), WorkshopRespDTO::getName);
    }

    /** 补全工作中心名称。 */
    public <T> void fillWcName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> orgApi.getWcMap(ids), WcRespDTO::getName);
    }

    /** 补全项目名称。 */
    public <T> void fillProjectName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> projectApi.getProjectMap(ids), ProjectRespDTO::getName);
    }

    /** 补全项目任务名称。 */
    public <T> void fillProjectTaskName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> projectApi.getProjectTaskMap(ids), ProjectTaskRespDTO::getName);
    }

    /** 补全生产资源名称。 */
    public <T> void fillResName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> resourceApi.getResourceMap(ids), ResourceRespDTO::getName);
    }

    /** 补全班次名称。 */
    public <T> void fillShiftName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> shiftApi.getShiftMap(ids), ShiftRespDTO::getName);
    }

    /** 补全班组名称。 */
    public <T> void fillTeamName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> teamApi.getTeamMap(ids), TeamRespDTO::getName);
    }

    /** 补全用户昵称，用于操作人、审批人、检验员这类字段。 */
    public <T> void fillUserNickname(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> adminUserApi.getUserMap(ids), AdminUserRespDTO::getNickname);
    }

    /** 补全物料名称，用于替代料这类没有落快照的物料字段。 */
    public <T> void fillMaterialName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> materialApi.getMaterialMap(ids), MaterialRespDTO::getName);
    }

    /** 补全标准工序名称。 */
    public <T> void fillOpName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> opApi.getOpMap(ids), OpRespDTO::getName);
    }

    /** 补全仓库名称，仓库主数据在 wms 模块，同样只能走门面。 */
    public <T> void fillWhName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> warehouseApi.getWhMap(ids), WhRespDTO::getName);
    }

    /**
     * 收集编号、一次批量查询、逐条回填。
     *
     * <p>编号为空或为占位 0 的记录直接跳过，不参与批量查询。
     */
    private <T, D> void fill(List<T> list,
                             Function<T, Long> idGetter,
                             BiConsumer<T, String> nameSetter,
                             Function<Collection<Long>, Map<Long, D>> mapLoader,
                             Function<D, String> nameExtractor) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (T item : list) {
            Long id = idGetter.apply(item);
            if (id != null && !MesConstants.NONE_ID.equals(id)) {
                ids.add(id);
            }
        }
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, D> map = mapLoader.apply(ids);
        for (T item : list) {
            D dto = map.get(idGetter.apply(item));
            if (dto != null) {
                nameSetter.accept(item, nameExtractor.apply(dto));
            }
        }
    }

}
