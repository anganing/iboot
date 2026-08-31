package com.iboot.pf.module.mes.controller.admin.defect;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectPageReqVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectRespVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectSaveReqVO;
import com.iboot.pf.module.mes.service.defect.DefectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * 缺陷记录管理端接口。
 *
 * <p>缺陷来自检验现场，附件随主记录一起提交，不提供 Excel 导入。
 */
@Tag(name = "管理后台 - MES-缺陷记录")
@RestController
@RequestMapping("/mes/defect")
@Validated
public class DefectController {

    @Resource
    private DefectService defectService;

    @PostMapping("/create")
    @Operation(summary = "创建缺陷记录")
    @PreAuthorize("@ss.hasPermission('mes:defect:create')")
    public CommonResult<Long> createDefect(@Valid @RequestBody DefectSaveReqVO createReqVO) {
        return success(defectService.createDefect(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新缺陷记录")
    @PreAuthorize("@ss.hasPermission('mes:defect:update')")
    public CommonResult<Boolean> updateDefect(@Valid @RequestBody DefectSaveReqVO updateReqVO) {
        defectService.updateDefect(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除缺陷记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:defect:delete')")
    public CommonResult<Boolean> deleteDefect(@RequestParam("id") Long id) {
        defectService.deleteDefect(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除缺陷记录")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:defect:delete')")
    public CommonResult<Boolean> deleteDefectList(@RequestParam("ids") List<Long> ids) {
        defectService.deleteDefectListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得缺陷记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:defect:query')")
    public CommonResult<DefectRespVO> getDefect(@RequestParam("id") Long id) {
        return success(defectService.getDefect(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得缺陷记录分页")
    @PreAuthorize("@ss.hasPermission('mes:defect:query')")
    public CommonResult<PageResult<DefectRespVO>> getDefectPage(@Valid DefectPageReqVO pageReqVO) {
        return success(defectService.getDefectPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出缺陷记录 Excel")
    @PreAuthorize("@ss.hasPermission('mes:defect:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDefectExcel(@Valid DefectPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DefectRespVO> list = defectService.getDefectPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-缺陷记录.xls", "数据", DefectRespVO.class, list);
    }

}
