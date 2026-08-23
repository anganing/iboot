package com.iboot.pf.module.mdm.controller.admin.invorg;

import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.invorg.vo.InvOrgPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.invorg.vo.InvOrgRespVO;
import com.iboot.pf.module.mdm.controller.admin.invorg.vo.InvOrgSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.invorg.InvOrgDO;
import com.iboot.pf.module.mdm.service.invorg.InvOrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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

import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 库存组织")
@RestController
@RequestMapping("/mdm/inv-org")
@Validated
public class InvOrgController {

    @Resource
    private InvOrgService invOrgService;

    @PostMapping("/create")
    @Operation(summary = "创建库存组织")
    @PreAuthorize("@ss.hasPermission('mdm:inv-org:create')")
    public CommonResult<Long> createInvOrg(@Valid @RequestBody InvOrgSaveReqVO createReqVO) {
        return success(invOrgService.createInvOrg(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存组织")
    @PreAuthorize("@ss.hasPermission('mdm:inv-org:update')")
    public CommonResult<Boolean> updateInvOrg(@Valid @RequestBody InvOrgSaveReqVO updateReqVO) {
        invOrgService.updateInvOrg(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存组织")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:inv-org:delete')")
    public CommonResult<Boolean> deleteInvOrg(@RequestParam("id") Long id) {
        invOrgService.deleteInvOrg(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存组织")
    @PreAuthorize("@ss.hasPermission('mdm:inv-org:query')")
    public CommonResult<InvOrgRespVO> getInvOrg(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(invOrgService.getInvOrg(id), InvOrgRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存组织分页")
    @PreAuthorize("@ss.hasPermission('mdm:inv-org:query')")
    public CommonResult<PageResult<InvOrgRespVO>> getInvOrgPage(@Valid InvOrgPageReqVO pageReqVO) {
        PageResult<InvOrgDO> pageResult = invOrgService.getInvOrgPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, InvOrgRespVO.class));
    }
}
