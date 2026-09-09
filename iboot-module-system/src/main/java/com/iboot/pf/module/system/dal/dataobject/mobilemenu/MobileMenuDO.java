package com.iboot.pf.module.system.dal.dataobject.mobilemenu;

import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import com.iboot.pf.framework.tenant.core.aop.TenantIgnore;
import com.iboot.pf.module.system.enums.permission.MenuTypeEnum;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 移动端菜单 DO
 *
 * 与 {@link com.iboot.pf.module.system.dal.dataobject.permission.MenuDO} 同构，但独立于 PC 菜单体系，
 * 供移动端（uni-app）展示菜单与鉴权使用
 *
 * @author iboot
 */
@TableName("system_mobile_menu")
@TenantIgnore // 无 tenant_id 字段，忽略多租户
@Data
@EqualsAndHashCode(callSuper = true)
public class MobileMenuDO extends BaseDO {

    /**
     * 菜单编号 - 根节点
     */
    public static final Long ID_ROOT = 0L;

    /**
     * 菜单编号
     */
    @TableId
    private Long id;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 权限标识
     *
     * 一般格式为：${系统}:${模块}:${操作}
     */
    private String permission;
    /**
     * 菜单类型
     *
     * 枚举 {@link MenuTypeEnum}：1 分组、2 入口、3 按钮
     */
    private Integer type;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 父菜单ID
     */
    private Long parentId;
    /**
     * 路由地址
     *
     * uni-app 页面路径，例如 /pages/notify/index
     */
    private String path;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 是否可见
     */
    private Boolean visible;
    /**
     * 备注
     */
    private String remark;

}
