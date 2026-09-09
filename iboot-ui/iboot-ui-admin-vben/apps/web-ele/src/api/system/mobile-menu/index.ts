import { requestClient } from '#/api/request';

export namespace SystemMobileMenuApi {
  /** 移动端菜单信息 */
  export interface MobileMenu {
    id: number;
    name: string;
    permission: string;
    type: number; // 1分组 2入口 3按钮
    sort: number;
    parentId: number;
    path: string;
    icon: string;
    status: number; // 0开启 1停用
    visible: boolean;
    remark: string;
    children?: MobileMenu[];
  }

  /** 移动端菜单精简信息 */
  export interface SimpleMobileMenu {
    id: number;
    parentId: number;
    name: string;
    type: number;
    status: number;
  }

  /** 分配角色移动端菜单请求 */
  export interface AssignRoleMenuReqVO {
    roleId: number;
    menuIds: number[];
  }
}

/** 查询移动端菜单（精简）列表 */
export async function getSimpleMobileMenuList() {
  return requestClient.get<SystemMobileMenuApi.SimpleMobileMenu[]>(
    '/system/mobile-menu/simple-list',
  );
}

/** 查询移动端菜单列表（平铺，前端自行组树） */
export async function getMobileMenuList(params?: Record<string, any>) {
  return requestClient.get<SystemMobileMenuApi.MobileMenu[]>(
    '/system/mobile-menu/list',
    {
      params,
    },
  );
}

/** 获取移动端菜单详情 */
export async function getMobileMenu(id: number) {
  return requestClient.get<SystemMobileMenuApi.MobileMenu>(
    `/system/mobile-menu/get?id=${id}`,
  );
}

/** 新增移动端菜单 */
export async function createMobileMenu(data: SystemMobileMenuApi.MobileMenu) {
  return requestClient.post('/system/mobile-menu/create', data);
}

/** 修改移动端菜单 */
export async function updateMobileMenu(data: SystemMobileMenuApi.MobileMenu) {
  return requestClient.put('/system/mobile-menu/update', data);
}

/** 删除移动端菜单 */
export async function deleteMobileMenu(id: number) {
  return requestClient.delete(`/system/mobile-menu/delete?id=${id}`);
}

/** 查询角色已分配的移动端菜单编号集合 */
export async function getRoleMobileMenuList(roleId: number) {
  return requestClient.get<number[]>(
    `/system/mobile-menu/list-role-menus?roleId=${roleId}`,
  );
}

/** 分配角色移动端菜单 */
export async function assignRoleMobileMenu(
  data: SystemMobileMenuApi.AssignRoleMenuReqVO,
) {
  return requestClient.post('/system/mobile-menu/assign-role-menu', data);
}
