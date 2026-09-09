package com.iboot.pf.module.system.controller.admin.mobilemenu;

import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.security.core.util.SecurityFrameworkUtils;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuAssignReqVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuListReqVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuRespVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuSaveReqVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuSimpleRespVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobilePermissionInfoRespVO;
import com.iboot.pf.module.system.dal.dataobject.mobilemenu.MobileMenuDO;
import com.iboot.pf.module.system.service.mobilemenu.MobileMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 移动端菜单")
@RestController
@RequestMapping("/system/mobile-menu")
@Validated
public class MobileMenuController {

    @Resource
    private MobileMenuService mobileMenuService;

    @PostMapping("/create")
    @Operation(summary = "创建移动端菜单")
    @PreAuthorize("@ss.hasPermission('system:mobile-menu:create')")
    public CommonResult<Long> createMenu(@Valid @RequestBody MobileMenuSaveReqVO createReqVO) {
        Long menuId = mobileMenuService.createMenu(createReqVO);
        return success(menuId);
    }

    @PutMapping("/update")
    @Operation(summary = "修改移动端菜单")
    @PreAuthorize("@ss.hasPermission('system:mobile-menu:update')")
    public CommonResult<Boolean> updateMenu(@Valid @RequestBody MobileMenuSaveReqVO updateReqVO) {
        mobileMenuService.updateMenu(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除移动端菜单")
    @Parameter(name = "id", description = "菜单编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('system:mobile-menu:delete')")
    public CommonResult<Boolean> deleteMenu(@RequestParam("id") Long id) {
        mobileMenuService.deleteMenu(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取移动端菜单信息")
    @Parameter(name = "id", description = "菜单编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('system:mobile-menu:query')")
    public CommonResult<MobileMenuRespVO> getMenu(@RequestParam("id") Long id) {
        MobileMenuDO menu = mobileMenuService.getMenu(id);
        return success(BeanUtils.toBean(menu, MobileMenuRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获取移动端菜单列表", description = "用于【移动端菜单管理】界面，支持 name、status 可选过滤")
    @PreAuthorize("@ss.hasPermission('system:mobile-menu:query')")
    public CommonResult<List<MobileMenuRespVO>> getMenuList(MobileMenuListReqVO reqVO) {
        List<MobileMenuDO> list = mobileMenuService.getMenuList(reqVO);
        list.sort(Comparator.comparing(MobileMenuDO::getSort));
        return success(BeanUtils.toBean(list, MobileMenuRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取移动端菜单精简信息列表",
            description = "用于【角色分配移动端菜单】功能的选项，登录即可访问")
    public CommonResult<List<MobileMenuSimpleRespVO>> getSimpleMenuList() {
        List<MobileMenuDO> list = mobileMenuService.getSimpleMenuList();
        list.sort(Comparator.comparing(MobileMenuDO::getSort));
        return success(BeanUtils.toBean(list, MobileMenuSimpleRespVO.class));
    }

    @GetMapping("/my")
    @Operation(summary = "获得自己的移动端菜单树和权限标识列表",
            description = "移动端登录后调用，返回可见的分组与入口菜单树，以及按钮的权限标识集合")
    public CommonResult<MobilePermissionInfoRespVO> getMyMobileMenuTree() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(mobileMenuService.getMyMobileMenuTree(userId));
    }

    @GetMapping("/list-role-menus")
    @Operation(summary = "获得角色拥有的移动端菜单编号列表")
    @Parameter(name = "roleId", description = "角色编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('system:mobile-menu:assign')")
    public CommonResult<Set<Long>> getRoleMenuList(@RequestParam("roleId") Long roleId) {
        return success(mobileMenuService.getRoleMenuList(roleId));
    }

    @PostMapping("/assign-role-menu")
    @Operation(summary = "赋予角色移动端菜单（全量替换）")
    @PreAuthorize("@ss.hasPermission('system:mobile-menu:assign')")
    public CommonResult<Boolean> assignRoleMenu(@Validated @RequestBody MobileMenuAssignReqVO reqVO) {
        mobileMenuService.assignRoleMenu(reqVO);
        return success(true);
    }

}
