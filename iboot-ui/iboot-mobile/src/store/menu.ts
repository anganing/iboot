import type { MobileMenuNode } from '@/api/menu'
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMyMobileMenu } from '@/api/menu'

/**
 * 移动端菜单 store
 * 菜单与权限是登录会话内的服务端派生数据，不持久化：
 * 持久化会让冷启动直接用旧菜单且 loaded=true 跳过请求，权限变更后展示过期数据
 */
export const useMenuStore = defineStore(
  'menu',
  () => {
    const menus = ref<MobileMenuNode[]>([])
    const permissions = ref<string[]>([])
    const loaded = ref(false)

    /**
     * 拉取当前用户菜单，已加载时跳过，force 为 true 时强制刷新
     */
    async function fetchMenu(force = false) {
      if (loaded.value && !force) {
        return
      }
      const res = await getMyMobileMenu()
      menus.value = res.menus
      permissions.value = res.permissions
      loaded.value = true
    }

    /** 判断当前用户是否拥有某权限标识 */
    function hasPerm(code: string) {
      return permissions.value.includes(code)
    }

    return {
      menus,
      permissions,
      loaded,
      fetchMenu,
      hasPerm,
    }
  },
)
