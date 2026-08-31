package com.iboot.pf.module.mes.service.snapshot;

import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.org.OrgApi;
import com.iboot.pf.module.mdm.api.org.dto.PlantRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WcRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.WorkshopRespDTO;
import com.iboot.pf.module.mdm.api.project.ProjectApi;
import com.iboot.pf.module.mdm.api.project.dto.ProjectRespDTO;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mes.enums.MesConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MAT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PLANT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.UOM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WC_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORKSHOP_NOT_EXISTS;

/**
 * Excel 导入时的编码到编号解析器。
 *
 * <p>导入模板给用户填的是编码，落库需要编号。解析不到就抛出对应的“不存在”错误码，
 * 由导入流程记成该行的失败原因，不静默落一个脏外键。
 */
@Service
public class MesImportResolver {

    @Resource
    private MaterialApi materialApi;
    @Resource
    private UomApi uomApi;
    @Resource
    private OrgApi orgApi;
    @Resource
    private ProjectApi projectApi;

    /** 物料编码 -> 物料编号。 */
    public Long requireMatId(String matCode) {
        MaterialRespDTO material = blank(matCode) ? null
                : CollectionUtils.getFirst(materialApi.getMaterialListByCodes(Collections.singletonList(matCode)));
        if (material == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        return material.getId();
    }

    /** 计量单位编码 -> 计量单位编号。 */
    public Long requireUomId(String uomCode) {
        if (blank(uomCode)) {
            throw exception(UOM_NOT_EXISTS);
        }
        Map<String, UomRespDTO> byCode = CollectionUtils.convertMap(uomApi.getUomList(), UomRespDTO::getCode);
        UomRespDTO uom = byCode.get(uomCode);
        if (uom == null) {
            throw exception(UOM_NOT_EXISTS);
        }
        return uom.getId();
    }

    /** 工厂编码 -> 工厂编号。 */
    public Long requirePlantId(String plantCode) {
        if (blank(plantCode)) {
            throw exception(PLANT_NOT_EXISTS);
        }
        Map<String, PlantRespDTO> byCode = CollectionUtils.convertMap(orgApi.getPlantList(), PlantRespDTO::getCode);
        PlantRespDTO plant = byCode.get(plantCode);
        if (plant == null) {
            throw exception(PLANT_NOT_EXISTS);
        }
        return plant.getId();
    }

    /** 车间编码 -> 车间编号。 */
    public Long requireWorkshopId(String workshopCode) {
        if (blank(workshopCode)) {
            throw exception(WORKSHOP_NOT_EXISTS);
        }
        Map<String, WorkshopRespDTO> byCode =
                CollectionUtils.convertMap(orgApi.getWorkshopList(), WorkshopRespDTO::getCode);
        WorkshopRespDTO workshop = byCode.get(workshopCode);
        if (workshop == null) {
            throw exception(WORKSHOP_NOT_EXISTS);
        }
        return workshop.getId();
    }

    /** 工作中心编码 -> 工作中心编号。 */
    public Long requireWcId(String wcCode) {
        if (blank(wcCode)) {
            throw exception(WC_NOT_EXISTS);
        }
        Map<String, WcRespDTO> byCode = CollectionUtils.convertMap(orgApi.getWcList(), WcRespDTO::getCode);
        WcRespDTO wc = byCode.get(wcCode);
        if (wc == null) {
            throw exception(WC_NOT_EXISTS);
        }
        return wc.getId();
    }

    /** 项目编码 -> 项目编号；留空表示无项目，返回占位 0。 */
    public Long resolveProjectId(String projectCode) {
        if (blank(projectCode)) {
            return MesConstants.NONE_ID;
        }
        Map<String, ProjectRespDTO> byCode =
                CollectionUtils.convertMap(projectApi.getProjectList(), ProjectRespDTO::getCode);
        ProjectRespDTO project = byCode.get(projectCode);
        if (project == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
        return project.getId();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

}
