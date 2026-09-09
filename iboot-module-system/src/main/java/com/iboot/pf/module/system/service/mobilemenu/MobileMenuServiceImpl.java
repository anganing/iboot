package com.iboot.pf.module.system.service.mobilemenu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuAssignReqVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuListReqVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuRespVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobileMenuSaveReqVO;
import com.iboot.pf.module.system.controller.admin.mobilemenu.vo.MobilePermissionInfoRespVO;
import com.iboot.pf.module.system.dal.dataobject.mobilemenu.MobileMenuDO;
import com.iboot.pf.module.system.dal.dataobject.mobilemenu.MobileRoleMenuDO;
import com.iboot.pf.module.system.dal.mysql.mobilemenu.MobileMenuMapper;
import com.iboot.pf.module.system.dal.mysql.mobilemenu.MobileRoleMenuMapper;
import com.iboot.pf.module.system.enums.permission.MenuTypeEnum;
import com.iboot.pf.module.system.service.permission.PermissionService;
import com.iboot.pf.module.system.service.permission.RoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertSet;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.filterList;
import static com.iboot.pf.module.system.dal.dataobject.mobilemenu.MobileMenuDO.ID_ROOT;
import static com.iboot.pf.module.system.enums.ErrorCodeConstants.*;

/**
 * 移动端菜单 Service 实现类
 *
 * @author iboot
 */
@Service
@Slf4j
public class MobileMenuServiceImpl implements MobileMenuService {

    /**
     * 菜单树的最大深度，超过该深度的层级直接截断
     *
     * 移动端菜单的合法结构为：分组(1) -> 入口/分组(2) -> 按钮/入口(3)
     */
    private static final int MAX_TREE_DEPTH = 3;

    @Resource
    private MobileMenuMapper mobileMenuMapper;
    @Resource
    private MobileRoleMenuMapper mobileRoleMenuMapper;
    @Resource
    private PermissionService permissionService;
    @Resource
    private RoleService roleService;

    @Override
    public Long createMenu(MobileMenuSaveReqVO createReqVO) {
        // 校验父菜单存在
        validateParentMenu(createReqVO.getParentId(), null);
        // 插入数据库
        MobileMenuDO menu = BeanUtils.toBean(createReqVO, MobileMenuDO.class);
        mobileMenuMapper.insert(menu);
        // 返回
        return menu.getId();
    }

    @Override
    public void updateMenu(MobileMenuSaveReqVO updateReqVO) {
        // 校验更新的菜单是否存在
        if (mobileMenuMapper.selectById(updateReqVO.getId()) == null) {
            throw exception(MOBILE_MENU_NOT_EXISTS);
        }
        // 校验父菜单存在
        validateParentMenu(updateReqVO.getParentId(), updateReqVO.getId());

        // 更新到数据库
        MobileMenuDO updateObj = BeanUtils.toBean(updateReqVO, MobileMenuDO.class);
        mobileMenuMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        // 校验是否还有子菜单
        if (mobileMenuMapper.selectCountByParentId(id) > 0) {
            throw exception(MOBILE_MENU_EXISTS_CHILDREN);
        }
        // 校验删除的菜单是否存在
        if (mobileMenuMapper.selectById(id) == null) {
            throw exception(MOBILE_MENU_NOT_EXISTS);
        }
        // 标记删除
        mobileMenuMapper.deleteById(id);
        // 删除授予给角色的关联
        mobileRoleMenuMapper.deleteListByMenuId(id);
    }

    @Override
    public MobileMenuDO getMenu(Long id) {
        return mobileMenuMapper.selectById(id);
    }

    @Override
    public List<MobileMenuDO> getMenuList(MobileMenuListReqVO reqVO) {
        return mobileMenuMapper.selectList(reqVO);
    }

    @Override
    public List<MobileMenuDO> getSimpleMenuList() {
        return mobileMenuMapper.selectList();
    }

    @Override
    public void validateParentMenu(Long parentId, Long childId) {
        if (parentId == null || ID_ROOT.equals(parentId)) {
            return;
        }
        // 不能设置自己为父菜单
        if (parentId.equals(childId)) {
            throw exception(MOBILE_MENU_PARENT_ERROR);
        }
        MobileMenuDO parent = mobileMenuMapper.selectById(parentId);
        // 父菜单不存在
        if (parent == null) {
            throw exception(MOBILE_MENU_PARENT_NOT_EXISTS);
        }
        // 父菜单必须是分组类型
        if (!MenuTypeEnum.DIR.getType().equals(parent.getType())) {
            throw exception(MOBILE_MENU_PARENT_NOT_DIR);
        }
        // 不能设置自己的子菜单为父菜单（会形成环），沿着父链向上查找
        Long ancestorId = parent.getParentId();
        int depth = 0;
        while (ancestorId != null && !ID_ROOT.equals(ancestorId) && depth < MAX_TREE_DEPTH) {
            if (ancestorId.equals(childId)) {
                throw exception(MOBILE_MENU_PARENT_ERROR);
            }
            MobileMenuDO ancestor = mobileMenuMapper.selectById(ancestorId);
            if (ancestor == null) {
                break; // 父链断裂，说明数据已不完整，无需继续向上
            }
            ancestorId = ancestor.getParentId();
            depth++;
        }
    }

    @Override
    public List<MobileMenuRespVO> buildMenuTree(List<MobileMenuDO> menus, Long parentId) {
        return buildMenuTree(menus, parentId, new HashSet<>(), 1);
    }

    private List<MobileMenuRespVO> buildMenuTree(List<MobileMenuDO> menus, Long parentId,
                                                 Set<Long> visited, int depth) {
        // 深度截断，防止脏数据形成过深的树
        if (depth > MAX_TREE_DEPTH) {
            return Collections.emptyList();
        }
        // 找出当前层级的子菜单，按 sort 升序
        List<MobileMenuDO> children = filterList(menus, menu -> ObjUtil.equal(menu.getParentId(), parentId));
        children.sort(Comparator.comparing(MobileMenuDO::getSort));
        // 递归组装子树。visited 记录当前递归路径上已处理的菜单，防止环形引用导致死循环
        List<MobileMenuRespVO> tree = new ArrayList<>();
        for (MobileMenuDO child : children) {
            if (!visited.add(child.getId())) {
                continue;
            }
            MobileMenuRespVO node = BeanUtils.toBean(child, MobileMenuRespVO.class);
            node.setChildren(buildMenuTree(menus, child.getId(), visited, depth + 1));
            tree.add(node);
            visited.remove(child.getId());
        }
        return tree;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenu(MobileMenuAssignReqVO reqVO) {
        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = convertSet(mobileRoleMenuMapper.selectListByRoleId(reqVO.getRoleId()),
                MobileRoleMenuDO::getMenuId);
        // 计算新增和删除的菜单编号
        Set<Long> menuIdList = CollUtil.emptyIfNull(reqVO.getMenuIds());
        Collection<Long> createMenuIds = CollUtil.subtract(menuIdList, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIdList);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (CollUtil.isNotEmpty(createMenuIds)) {
            mobileRoleMenuMapper.insertBatch(convertList(createMenuIds, menuId -> {
                MobileRoleMenuDO entity = new MobileRoleMenuDO();
                entity.setRoleId(reqVO.getRoleId());
                entity.setMenuId(menuId);
                return entity;
            }));
        }
        if (CollUtil.isNotEmpty(deleteMenuIds)) {
            mobileRoleMenuMapper.deleteListByRoleIdAndMenuIds(reqVO.getRoleId(), deleteMenuIds);
        }
    }

    @Override
    public Set<Long> getRoleMenuList(Long roleId) {
        return convertSet(mobileRoleMenuMapper.selectListByRoleId(roleId), MobileRoleMenuDO::getMenuId);
    }

    @Override
    public MobilePermissionInfoRespVO getMyMobileMenuTree(Long userId) {
        // 1. 获得用户拥有的角色编号
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(userId);
        // 2. 获得用户可见的菜单列表
        List<MobileMenuDO> menus;
        if (roleService.hasAnySuperAdmin(roleIds)) {
            // 超管：全量查询开启状态的菜单
            menus = mobileMenuMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        } else if (CollUtil.isEmpty(roleIds)) {
            menus = Collections.emptyList();
        } else {
            // 非超管：按角色查询关联的菜单
            Set<Long> menuIds = convertSet(mobileRoleMenuMapper.selectListByRoleId(roleIds),
                    MobileRoleMenuDO::getMenuId);
            menus = CollUtil.isEmpty(menuIds) ? Collections.emptyList() : mobileMenuMapper.selectByIds(menuIds);
        }
        // 过滤掉停用的菜单
        menus = filterList(menus, menu -> CommonStatusEnum.ENABLE.getStatus().equals(menu.getStatus()));
        // 3. 组装返回：权限标识来自全部类型（含按钮），菜单树只包含分组和入口
        MobilePermissionInfoRespVO respVO = new MobilePermissionInfoRespVO();
        respVO.setPermissions(convertSet(menus, MobileMenuDO::getPermission,
                menu -> StrUtil.isNotBlank(menu.getPermission())));
        respVO.setMenus(buildMenuTree(filterList(menus, menu ->
                        MenuTypeEnum.DIR.getType().equals(menu.getType())
                                || MenuTypeEnum.MENU.getType().equals(menu.getType())),
                ID_ROOT));
        return respVO;
    }

}
