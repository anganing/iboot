package com.iboot.pf.module.mdm.controller.admin.uom;

import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.*;
import com.iboot.pf.module.mdm.convert.uom.UomConvert;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.service.uom.UomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 计量单位") @RestController @RequestMapping("/mdm/uom") @Validated
public class UomController {
    @Resource private UomService uomService;
    @PostMapping("/create") @PreAuthorize("@ss.hasPermission('mdm:uom:create')") public CommonResult<Long> create(@Valid @RequestBody UomSaveReqVO reqVO) { return success(uomService.create(reqVO)); }
    @PutMapping("/update") @PreAuthorize("@ss.hasPermission('mdm:uom:update')") public CommonResult<Boolean> update(@Valid @RequestBody UomSaveReqVO reqVO) { uomService.update(reqVO); return success(true); }
    @DeleteMapping("/delete") @PreAuthorize("@ss.hasPermission('mdm:uom:delete')") public CommonResult<Boolean> delete(@RequestParam Long id) { uomService.delete(id); return success(true); }
    @GetMapping("/get") @PreAuthorize("@ss.hasPermission('mdm:uom:query')") public CommonResult<UomRespVO> get(@RequestParam Long id) { return success(UomConvert.INSTANCE.convert(uomService.get(id))); }
    @GetMapping("/page") @PreAuthorize("@ss.hasPermission('mdm:uom:query')") public CommonResult<PageResult<UomRespVO>> page(@Valid UomPageReqVO reqVO) { PageResult<UomDO> page = uomService.getPage(reqVO); return success(UomConvert.INSTANCE.convertPage(page)); }
}
