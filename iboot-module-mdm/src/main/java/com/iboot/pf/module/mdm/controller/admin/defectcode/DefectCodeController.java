package com.iboot.pf.module.mdm.controller.admin.defectcode;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mdm.controller.admin.defectcode.vo.*;
import com.iboot.pf.module.mdm.service.defectcode.DefectCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 缺陷代码")
@RestController
@RequestMapping("/mdm/defect-code")
@Validated
public class DefectCodeController {

    @Resource
    private DefectCodeService defectCodeService;

    @PostMapping("/create")
    @Operation(summary = "创建缺陷代码")
    @PreAuthorize("@ss.hasPermission('mdm:defect-code:create')")
    public CommonResult<Long> createDefectCode(@Valid @RequestBody DefectCodeSaveReqVO createReqVO) {
        return success(defectCodeService.createDefectCode(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新缺陷代码")
    @PreAuthorize("@ss.hasPermission('mdm:defect-code:update')")
    public CommonResult<Boolean> updateDefectCode(@Valid @RequestBody DefectCodeSaveReqVO updateReqVO) {
        defectCodeService.updateDefectCode(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除缺陷代码")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:defect-code:delete')")
    public CommonResult<Boolean> deleteDefectCode(@RequestParam("id") Long id) {
        defectCodeService.deleteDefectCode(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除缺陷代码")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:defect-code:delete')")
    public CommonResult<Boolean> deleteDefectCodeList(@RequestParam("ids") List<Long> ids) {
        defectCodeService.deleteDefectCodeListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得缺陷代码")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:defect-code:query')")
    public CommonResult<DefectCodeRespVO> getDefectCode(@RequestParam("id") Long id) {
        return success(defectCodeService.getDefectCode(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得缺陷代码分页")
    @PreAuthorize("@ss.hasPermission('mdm:defect-code:query')")
    public CommonResult<PageResult<DefectCodeRespVO>> getDefectCodePage(@Valid DefectCodePageReqVO pageReqVO) {
        return success(defectCodeService.getDefectCodePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出缺陷代码 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:defect-code:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDefectCodeExcel(@Valid DefectCodePageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DefectCodeRespVO> list = defectCodeService.getDefectCodePage(pageReqVO).getList();
        ExcelUtils.write(response, "缺陷代码.xls", "数据", DefectCodeRespVO.class, list);
    }

}
