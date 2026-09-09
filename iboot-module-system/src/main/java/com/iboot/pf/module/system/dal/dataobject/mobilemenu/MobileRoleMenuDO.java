package com.iboot.pf.module.system.dal.dataobject.mobilemenu;

import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import com.iboot.pf.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 移动端角色和菜单关联 DO
 *
 * @author iboot
 */
@TableName("system_mobile_role_menu")
@TenantIgnore // 无 tenant_id 字段，忽略多租户
@Data
@EqualsAndHashCode(callSuper = true)
public class MobileRoleMenuDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 角色ID
     */
    private Long roleId;
    /**
     * 菜单ID
     */
    private Long menuId;

}
