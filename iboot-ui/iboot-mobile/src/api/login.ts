import type { IAuthLoginRes, IIbootLoginRes, IProfileDetail, IUpdateInfo, IUpdatePassword, IUserInfoRes } from './types/login'
import { http } from '@/http/http'

/**
 * 登录表单
 */
export interface ILoginForm {
  username: string
  password: string
}

/**
 * 登录成功跳转
 */
function handleLoginSuccess() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  }
  else {
    uni.reLaunch({ url: '/pages/index/index' })
  }
}

/**
 * 用户登录
 * @param loginForm 登录表单
 * iboot 后端返回的 expiresTime 是绝对时间戳（毫秒），模板 token store 期望 expiresIn 时长（秒），此处做映射
 */
export async function login(loginForm: ILoginForm): Promise<IAuthLoginRes> {
  const res = await http.post<IIbootLoginRes>('/system/auth/login', loginForm)
  return {
    accessToken: res.accessToken,
    refreshToken: res.refreshToken,
    accessExpiresIn: Math.max(0, Math.floor((res.expiresTime - Date.now()) / 1000)),
    refreshExpiresIn: 0,
  }
}

/**
 * 获取用户信息（个人中心），映射为移动端 user store 结构
 * roles/posts 映射为名字数组，供角色过滤等 string 消费方直接使用
 */
export async function getUserInfo(): Promise<IUserInfoRes> {
  const res = await http.get<IProfileDetail>('/system/user/profile/get')
  return {
    userId: res.id,
    username: res.username,
    nickname: res.nickname,
    avatar: res.avatar,
    mobile: res.mobile,
    email: res.email,
    sex: res.sex,
    deptName: res.dept?.name || '',
    posts: res.posts?.map(p => p.name),
    roles: res.roles?.map(r => r.name),
  }
}

/**
 * 退出登录
 */
export function logout() {
  return http.post<void>('/system/auth/logout')
}

/**
 * 刷新 token
 * @param refreshToken 刷新 token，iboot 后端要求以 query 参数传递
 * 与 login() 一致，把 expiresTime 绝对时间戳映射为 accessExpiresIn 时长（秒）
 */
export async function refreshToken(refreshToken: string): Promise<IAuthLoginRes> {
  const res = await http.post<IIbootLoginRes>('/system/auth/refresh-token', undefined, { refreshToken })
  return {
    accessToken: res.accessToken,
    refreshToken: res.refreshToken,
    accessExpiresIn: Math.max(0, Math.floor((res.expiresTime - Date.now()) / 1000)),
    refreshExpiresIn: 0,
  }
}

/**
 * 修改个人资料
 */
export function updateInfo(data: IUpdateInfo) {
  return http.put<void>('/system/user/profile/update', data)
}

/**
 * 修改密码
 */
export function updateUserPassword(data: IUpdatePassword) {
  return http.put<void>('/system/user/profile/update-password', data)
}

export { handleLoginSuccess }
