import { http } from '@/http/http'

/**
 * 移动端菜单节点
 * type: 1=分组 2=入口 3=按钮；接口树中只返回分组与入口，并已按 sort 排序
 */
export interface MobileMenuNode {
  id: number
  name: string
  type: 1 | 2 | 3
  sort: number
  path: string
  icon: string
  children?: MobileMenuNode[]
}

/** /system/mobile-menu/my 返回结构 */
export interface MobileMenuResult {
  menus: MobileMenuNode[]
  permissions: string[]
}

/** 获取当前用户的移动端菜单树与权限标识 */
export async function getMyMobileMenu(): Promise<MobileMenuResult> {
  return http.get<MobileMenuResult>('/system/mobile-menu/my')
}
