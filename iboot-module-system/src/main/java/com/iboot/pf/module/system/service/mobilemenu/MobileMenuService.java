package com.iboot.pf.module.system.service.mobilemenu;

import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuAssignReqVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuListReqVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuRespVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuSaveReqVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobilePermissionInfoRespVO;
import com.iboot.pf.module.system.dal.dataobject.mobilemenu.MobileMenuDO;

import java.util.List;
import java.util.Set;

/**
 * 移动端菜单 Service 接口
 *
 * @author iboot
 */
public interface MobileMenuService {

    /**
     * 创建移动端菜单
     *
     * @param createReqVO 创建信息
     * @return 菜单编号
     */
    Long createMenu(MobileMenuSaveReqVO createReqVO);

    /**
     * 更新移动端菜单
     *
     * @param updateReqVO 更新信息
     */
    void updateMenu(MobileMenuSaveReqVO updateReqVO);

    /**
     * 删除移动端菜单，并联动删除角色-菜单关联
     *
     * @param id 菜单编号
     */
    void deleteMenu(Long id);

    /**
     * 获得移动端菜单
     *
     * @param id 菜单编号
     * @return 菜单
     */
    MobileMenuDO getMenu(Long id);

    /**
     * 获得移动端菜单列表，支持 name、status 可选过滤
     *
     * @param reqVO 查询条件
     * @return 菜单列表
     */
    List<MobileMenuDO> getMenuList(MobileMenuListReqVO reqVO);

    /**
     * 获得移动端菜单全量列表
     *
     * @return 菜单列表
     */
    List<MobileMenuDO> getSimpleMenuList();

    /**
     * 校验父菜单是否合法。不能设置自己、自己的子菜单为父菜单，父菜单必须存在且为分组类型
     *
     * @param parentId 父菜单编号
     * @param childId 当前菜单编号
     */
    void validateParentMenu(Long parentId, Long childId);

    /**
     * 将列表结构的移动端菜单，按 parentId 递归组装成菜单树
     *
     * 使用 visited 集合防环，深度截断不超过 3 层
     *
     * @param menus 菜单列表
     * @param parentId 根菜单的父编号，一般为 {@link MobileMenuDO#ID_ROOT}
     * @return 菜单树
     */
    List<MobileMenuRespVO> buildMenuTree(List<MobileMenuDO> menus, Long parentId);

    /**
     * 赋予角色移动端菜单（全量替换）
     *
     * @param reqVO 分配信息
     */
    void assignRoleMenu(MobileMenuAssignReqVO reqVO);

    /**
     * 获得角色拥有的移动端菜单编号集合
     *
     * @param roleId 角色编号
     * @return 菜单编号集合
     */
    Set<Long> getRoleMenuList(Long roleId);

    /**
     * 获得指定用户的移动端菜单树 + 权限标识集合
     *
     * 菜单树只包含分组和入口类型；权限标识集合包含按钮类型的 permission
     *
     * @param userId 用户编号
     * @return 菜单树 + 权限标识集合
     */
    MobilePermissionInfoRespVO getMyMobileMenuTree(Long userId);

}
