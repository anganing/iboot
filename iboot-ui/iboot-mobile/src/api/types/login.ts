// 认证模式类型
export type AuthMode = 'single' | 'double'

// 单Token响应类型
export interface ISingleTokenRes {
  token: string
  expiresIn: number // 有效期(秒)
}

// 双Token响应类型
export interface IDoubleTokenRes {
  accessToken: string
  refreshToken: string
  accessExpiresIn: number // 访问令牌有效期(秒)
  refreshExpiresIn: number // 刷新令牌有效期(秒)
}

/**
 * 登录返回的信息，其实就是 token 信息
 */
export type IAuthLoginRes = ISingleTokenRes | IDoubleTokenRes

/**
 * 用户信息
 */
export type UserRole = string

export interface IUserInfoRes {
  userId: number
  username: string
  nickname: string
  avatar?: string
  /** 同时支持单角色和多角色，你自行选择一种就行 */
  role?: UserRole
  roles?: UserRole[]
  [key: string]: any // 允许其他扩展字段
}

// 认证存储数据结构
export interface AuthStorage {
  mode: AuthMode
  tokens: ISingleTokenRes | IDoubleTokenRes
  userInfo?: IUserInfoRes
  loginTime: number // 登录时间戳
}

/**
 * 获取验证码
 */
export interface ICaptcha {
  captchaEnabled: boolean
  uuid: string
  image: string
}

/**
 * iboot 后端登录响应原始结构（/system/auth/login）
 */
export interface IIbootLoginRes {
  userId: number
  accessToken: string
  refreshToken: string
  /** 过期时间，epoch 毫秒 */
  expiresTime: number
}

/**
 * iboot 个人中心信息（/system/user/profile/get）
 */
export interface IProfileDetail {
  id: number
  username: string
  nickname: string
  email?: string
  mobile?: string
  /** 性别，1 男 / 2 女 / 0 未知 */
  sex?: number
  avatar?: string
  dept?: { id: number, name: string } | null
  posts?: { id: number, name: string }[]
  roles?: { id: number, name: string }[]
}

/**
 * 更新个人资料（/system/user/profile/update）
 */
export interface IUpdateInfo {
  nickname?: string
  email?: string
  mobile?: string
  sex?: number
  avatar?: string
}

/**
 * 修改密码（/system/user/profile/update-password）
 */
export interface IUpdatePassword {
  oldPassword: string
  newPassword: string
}

/**
 * 判断是否为单Token响应
 * @param tokenRes 登录响应数据
 * @returns 是否为单Token响应
 */
export function isSingleTokenRes(tokenRes: IAuthLoginRes): tokenRes is ISingleTokenRes {
  return 'token' in tokenRes && !('refreshToken' in tokenRes)
}

/**
 * 判断是否为双Token响应
 * @param tokenRes 登录响应数据
 * @returns 是否为双Token响应
 */
export function isDoubleTokenRes(tokenRes: IAuthLoginRes): tokenRes is IDoubleTokenRes {
  return 'accessToken' in tokenRes && 'refreshToken' in tokenRes
}
