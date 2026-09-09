package com.iboot.pf.module.system.dal.mysql.mobilemenu;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.system.dal.dataobject.mobilemenu.MobileRoleMenuDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface MobileRoleMenuMapper extends BaseMapperX<MobileRoleMenuDO> {

    default List<MobileRoleMenuDO> selectListByRoleId(Long roleId) {
        return selectList(MobileRoleMenuDO::getRoleId, roleId);
    }

    default List<MobileRoleMenuDO> selectListByRoleId(Collection<Long> roleIds) {
        return selectList(MobileRoleMenuDO::getRoleId, roleIds);
    }

    default List<MobileRoleMenuDO> selectListByMenuId(Long menuId) {
        return selectList(MobileRoleMenuDO::getMenuId, menuId);
    }

    /**
     * 物理删除角色的移动端菜单关联
     *
     * 不沿用 RoleMenuMapper 逻辑删除惯例的原因：本表存在 UNIQUE KEY uk_role_menu(role_id, menu_id)，
     * 且唯一键未包含 deleted 列，逻辑删除会残留墓碑行，导致取消授权后重新分配同一菜单时违反唯一约束。
     * 写法参照 OAuth2AccessTokenMapper、ApsTaskMapper 的 @Delete 纯 SQL 惯例
     */
    @Delete("DELETE FROM system_mobile_role_menu WHERE role_id = #{roleId}")
    int physicalDeleteByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM system_mobile_role_menu WHERE menu_id = #{menuId}")
    int physicalDeleteByMenuId(@Param("menuId") Long menuId);

    @Delete("DELETE FROM system_mobile_role_menu WHERE role_id = #{roleId} AND menu_id = #{menuId}")
    int physicalDeleteByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    default void deleteListByRoleIdAndMenuIds(Long roleId, Collection<Long> menuIds) {
        menuIds.forEach(menuId -> physicalDeleteByRoleIdAndMenuId(roleId, menuId));
    }

    default void deleteListByMenuId(Long menuId) {
        physicalDeleteByMenuId(menuId);
    }

    default void deleteListByRoleId(Long roleId) {
        physicalDeleteByRoleId(roleId);
    }

}
