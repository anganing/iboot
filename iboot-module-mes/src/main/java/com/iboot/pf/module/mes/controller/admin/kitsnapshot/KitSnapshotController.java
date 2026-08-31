package com.iboot.pf.module.mes.controller.admin.kitsnapshot;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotPageReqVO;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotRespVO;
import com.iboot.pf.module.mes.service.kitsnapshot.KitSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * 齐套检查快照管理端接口。
 *
 * <p>账表只读，只提供 get / page / export-excel：快照由齐套检查动作写入，
 * 人工改动会让齐套判断失去证据意义。
 */
@Tag(name = "管理后台 - MES-齐套检查快照")
@RestController
@RequestMapping("/mes/kit-snapshot")
@Validated
public class KitSnapshotController {

    @Resource
    private KitSnapshotService kitSnapshotService;

    @GetMapping("/get")
    @Operation(summary = "获得齐套检查快照")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:query')")
    public CommonResult<KitSnapshotRespVO> getKitSnapshot(@RequestParam("id") Long id) {
        return success(kitSnapshotService.getKitSnapshot(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得齐套检查快照分页")
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:query')")
    public CommonResult<PageResult<KitSnapshotRespVO>> getKitSnapshotPage(
            @Valid KitSnapshotPageReqVO pageReqVO) {
        return success(kitSnapshotService.getKitSnapshotPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出齐套检查快照 Excel")
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportKitSnapshotExcel(@Valid KitSnapshotPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<KitSnapshotRespVO> list = kitSnapshotService.getKitSnapshotPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-齐套检查快照.xls", "数据", KitSnapshotRespVO.class, list);
    }

}
