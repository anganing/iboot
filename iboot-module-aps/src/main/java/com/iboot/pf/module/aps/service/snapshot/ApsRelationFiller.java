package com.iboot.pf.module.aps.service.snapshot;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.org.OrgApi;
import com.iboot.pf.module.mdm.api.org.dto.InvOrgRespDTO;
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
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mes.api.order.ProdOrderApi;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderRespDTO;
import com.iboot.pf.module.system.api.user.AdminUserApi;
import com.iboot.pf.module.system.api.user.dto.AdminUserRespDTO;
import com.iboot.pf.module.wms.api.warehouse.WarehouseApi;
import com.iboot.pf.module.wms.api.warehouse.dto.LocRespDTO;
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
 * <p>模块内的关联字段（场景编码、版本号）在 Mapper 里 JOIN 出来；工厂、车间、工作中心、项目、资源、
 * 班次、物料、单位、用户这些落在 mdm 与 system 的数据不能跨库 JOIN，统一在这里按
 * “收集编号 -> 一次批量查询 -> 回填”三步补全，保证一批记录只打一次对方模块的接口，不出现 N+1。
 *
 * <p>补全必须发生在 Service 层：Excel 导出复用 Service 的分页结果，写在 Controller 里会丢字段。
 */
@Service
public class ApsRelationFiller {

    @Resource
    private OrgApi orgApi;
    @Resource
    private ProjectApi projectApi;
    @Resource
    private ResourceApi resourceApi;
    @Resource
    private ShiftApi shiftApi;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private WarehouseApi warehouseApi;
    @Resource
    private ProdOrderApi prodOrderApi;

    /** 补全工厂名称。 */
    public <T> void fillPlantName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> orgApi.getPlantMap(ids), PlantRespDTO::getName);
    }

    /** 补全库存组织名称。 */
    public <T> void fillInvOrgName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> orgApi.getInvOrgMap(ids), InvOrgRespDTO::getName);
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

    /** 补全物料名称。 */
    public <T> void fillMatName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> materialApi.getMaterialMap(ids), MaterialRespDTO::getName);
    }

    /** 补全物料规格。 */
    public <T> void fillMatSpec(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> specSetter) {
        fill(list, idGetter, specSetter, ids -> materialApi.getMaterialMap(ids), MaterialRespDTO::getSpec);
    }

    /** 补全计量单位编码。 */
    public <T> void fillUomCode(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> codeSetter) {
        fill(list, idGetter, codeSetter, ids -> uomApi.getUomMap(ids), UomRespDTO::getCode);
    }

    /** 补全用户昵称，用于发布人、锁定人这类字段。 */
    public <T> void fillUserNickname(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> adminUserApi.getUserMap(ids), AdminUserRespDTO::getNickname);
    }

    /** 补全仓库名称。 */
    public <T> void fillWhName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> warehouseApi.getWhMap(ids), WhRespDTO::getName);
    }

    /** 补全库位编码。 */
    public <T> void fillLocName(List<T> list, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        fill(list, idGetter, nameSetter, ids -> warehouseApi.getLocMap(ids), LocRespDTO::getCode);
    }

    /** 补全生产工单号。 */
    public <T> void fillProdOrderNo(List<T> list, Function<T, Long> idGetter,
                                    BiConsumer<T, String> noSetter) {
        fill(list, idGetter, noSetter,
                ids -> prodOrderApi.getProdOrderList(ids).stream()
                        .collect(java.util.stream.Collectors.toMap(ProdOrderRespDTO::getId, item -> item)),
                ProdOrderRespDTO::getOrderNo);
    }

    /**
     * 收集编号、一次批量查询、逐条回填。
     *
     * <p>编号为空或为占位 0 的记录直接跳过，不参与批量查询。
     */
    private <T, D> void fill(List<T> list,
                             Function<T, Long> idGetter,
                             BiConsumer<T, String> valueSetter,
                             Function<Collection<Long>, Map<Long, D>> mapLoader,
                             Function<D, String> valueExtractor) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (T item : list) {
            Long id = idGetter.apply(item);
            if (id != null && !ApsConstants.NONE_ID.equals(id)) {
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
                valueSetter.accept(item, valueExtractor.apply(dto));
            }
        }
    }

}
