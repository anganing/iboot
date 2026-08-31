package com.iboot.pf.module.mes.controller.admin.qcitem;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemRespVO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemSaveReqVO;
import com.iboot.pf.module.mes.service.qcitem.QcItemService;
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
 * 检验项目管理端接口。
 *
 * <p>检验项是检验方案的子表，按 planId 维护，不提供 Excel 导入。
 */
@Tag(name = "管理后台 - MES-检验项目")
@RestController
@RequestMapping("/mes/qc-item")
@Validated
public class QcItemController {

    @Resource
    private QcItemService qcItemService;

    @PostMapping("/create")
    @Operation(summary = "创建检验项目")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:create')")
    public CommonResult<Long> createQcItem(@Valid @RequestBody QcItemSaveReqVO createReqVO) {
        return success(qcItemService.createQcItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新检验项目")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:update')")
    public CommonResult<Boolean> updateQcItem(@Valid @RequestBody QcItemSaveReqVO updateReqVO) {
        qcItemService.updateQcItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除检验项目")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:delete')")
    public CommonResult<Boolean> deleteQcItem(@RequestParam("id") Long id) {
        qcItemService.deleteQcItem(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除检验项目")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-item:delete')")
    public CommonResult<Boolean> deleteQcItemList(@RequestParam("ids") List<Long> ids) {
        qcItemService.deleteQcItemListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得检验项目")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:query')")
    public CommonResult<QcItemRespVO> getQcItem(@RequestParam("id") Long id) {
        return success(qcItemService.getQcItem(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得检验项目分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:query')")
    public CommonResult<PageResult<QcItemRespVO>> getQcItemPage(@Valid QcItemPageReqVO pageReqVO) {
        return success(qcItemService.getQcItemPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出检验项目 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcItemExcel(@Valid QcItemPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcItemRespVO> list = qcItemService.getQcItemPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-检验项目.xls", "数据", QcItemRespVO.class, list);
    }

}
