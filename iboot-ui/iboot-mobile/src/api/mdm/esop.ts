import { http } from '@/http/http'

/**
 * MDM 工序 ESOP 作业指导书。
 *
 * 只提供「按路线工序取全部作业步骤」一个入口。
 *
 * 不要用 `/mdm/route-op-esop/page`——它入参是 routeOpId + title + status 且返回分页包装，
 * 而作业步骤天然是「一道工序下的一串步骤」，没有分页语义。
 * `list-by-route-op` 才是后端为此准备的正解，一次返回该工序的全部步骤（含附件）。
 */

/**
 * 附件类型。
 *
 * **由后端按文件扩展名判定**（`RouteOpEsopServiceImpl.resolveFileType`），
 * 前端据此分流「图片走 previewImage、其余走文件打开」，不要在前端再维护一套扩展名映射表。
 */
export type EsopFileType = 'IMAGE' | 'VIDEO' | 'PDF' | 'DRAWING' | 'OFFICE' | 'OTHER'

/** ESOP 作业步骤附件（后端 RouteOpEsopFileRespVO） */
export interface EsopFile {
  id: number | string
  /** 所属作业步骤编号 */
  esopId?: number | string
  /** 附件展示顺序 */
  seq?: number
  /** 文件类型，后端按扩展名判定 */
  fileType: EsopFileType | string
  /** 原始文件名，展示一律用它，不要从 URL 反解 */
  fileName: string
  /**
   * 文件地址。
   *
   * 这是文件存储器签发的**预签名地址，24 小时过期**（后端落库前已 `removeUrlQuery` 去签名，
   * 读取时重新签发）。所以**不要缓存 URL**：每次进页面重新调接口取，否则隔天打开必然是 403。
   */
  fileUrl: string
  /** infra 文件编号 */
  fileId?: number | string
}

/** ESOP 作业步骤（后端 RouteOpEsopRespVO） */
export interface EsopStep {
  id: number | string
  /** 路线工序编号 */
  routeOpId?: number | string
  /** 展示顺序 */
  seq?: number
  /** 标题 */
  title: string
  /** 作业要点文本 */
  content?: string
  /** 工艺参数 JSON 字符串，解析失败要兜住，不能白屏 */
  paramJson?: string
  /** 附件列表 */
  files?: EsopFile[]
  /** 附件数量 */
  fileCount?: number
  /** 是否要求作业者确认阅读 */
  requiredConfirmFlag?: boolean
  /** 状态：0-启用，1-停用 */
  status?: number
}

/** 按路线工序查全部作业步骤（含附件），routeOpId 来自工序任务详情的 routeOpId */
export function getEsopListByRouteOp(routeOpId: number | string) {
  return http.get<EsopStep[]>('/mdm/route-op-esop/list-by-route-op', { routeOpId })
}

/**
 * 附件类型对应的图标名。
 *
 * 取 wot-ui 内置图标名（`wd-icon` 的 name 直接用），不引外部图标库。
 */
const ESOP_FILE_ICON: Record<string, string> = {
  VIDEO: 'file-video',
  PDF: 'file-pdf',
  DRAWING: 'file',
  OFFICE: 'file',
  OTHER: 'file',
}

/** 取附件图标名，图片不在此列（图片自己就是缩略图）；未知类型回落通用文件图标 */
export function getEsopFileIcon(fileType?: string): string {
  if (!fileType)
    return 'file'
  return ESOP_FILE_ICON[fileType] ?? 'file'
}

/**
 * 判断是否图片附件。
 *
 * 直接信后端的 `fileType === 'IMAGE'`，不在前端按扩展名二次判断——
 * 两套判定规则迟早漂移，后端的 `resolveFileType` 是唯一事实源。
 */
export function isImageFile(file: EsopFile): boolean {
  return file.fileType === 'IMAGE'
}
