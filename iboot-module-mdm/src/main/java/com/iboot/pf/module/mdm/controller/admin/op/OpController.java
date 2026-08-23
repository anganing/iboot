package com.iboot.pf.module.mdm.controller.admin.op;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

import com.iboot.pf.framework.excel.core.util.ExcelUtils;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.*;

import com.iboot.pf.module.mdm.controller.admin.op.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.service.op.OpService;

@Tag(name = "管理后台 - 标准工序")
@RestController
@RequestMapping("/mdm/op")
@Validated
public class OpController {

    @Resource
    private OpService opService;

    @PostMapping("/create")
    @Operation(summary = "创建标准工序")
    @PreAuthorize("@ss.hasPermission('mdm:op:create')")
    public CommonResult<Long> createOp(@Valid @RequestBody OpSaveReqVO createReqVO) {
        return success(opService.createOp(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新标准工序")
    @PreAuthorize("@ss.hasPermission('mdm:op:update')")
    public CommonResult<Boolean> updateOp(@Valid @RequestBody OpSaveReqVO updateReqVO) {
        opService.updateOp(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除标准工序")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:op:delete')")
    public CommonResult<Boolean> deleteOp(@RequestParam("id") Long id) {
        opService.deleteOp(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除标准工序")
                @PreAuthorize("@ss.hasPermission('mdm:op:delete')")
    public CommonResult<Boolean> deleteOpList(@RequestParam("ids") List<Long> ids) {
        opService.deleteOpListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得标准工序")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:op:query')")
    public CommonResult<OpRespVO> getOp(@RequestParam("id") Long id) {
        OpDO op = opService.getOp(id);
        return success(BeanUtils.toBean(op, OpRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得标准工序分页")
    @PreAuthorize("@ss.hasPermission('mdm:op:query')")
    public CommonResult<PageResult<OpRespVO>> getOpPage(@Valid OpPageReqVO pageReqVO) {
        PageResult<OpDO> pageResult = opService.getOpPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, OpRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出标准工序 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:op:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOpExcel(@Valid OpPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OpDO> list = opService.getOpPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "标准工序.xls", "数据", OpRespVO.class,
                        BeanUtils.toBean(list, OpRespVO.class));
    }

}