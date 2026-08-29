import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmSkillApi {
  /** 技能信息 */
  export interface Skill {
    id: number; // 主键
    code?: string; // 技能编码
    name?: string; // 技能名称
    levelDesc?: string; // 等级体系说明
    status?: number; // 状态
    remark?: string; // 备注
    createTime?: Date; // 创建时间
  }

  /** 工序技能要求信息 */
  export interface OpSkillReq {
    id: number; // 主键
    opId?: number; // 标准工序编号
    opCode?: string; // 工序编码
    opName?: string; // 工序名称
    skillId?: number; // 技能编号
    skillCode?: string; // 技能编码
    skillName?: string; // 技能名称
    minLevel?: number; // 最低技能等级
    status?: number; // 状态
    createTime?: Date; // 创建时间
  }

  /** 人员技能信息 */
  export interface MemberSkill {
    id: number; // 主键
    userId?: number; // system 用户编号
    userName?: string; // 用户姓名
    skillId?: number; // 技能编号
    skillCode?: string; // 技能编码
    skillName?: string; // 技能名称
    level?: number; // 技能等级
    effectiveDate?: string; // 生效日期
    expiryDate?: string; // 失效日期
    status?: number; // 状态
    createTime?: Date; // 创建时间
  }
}

/** 查询技能分页 */
export function getSkillPage(params: PageParam) {
  return requestClient.get<PageResult<MdmSkillApi.Skill>>('/mdm/skill/page', {
    params,
  });
}

/** 查询技能详情 */
export function getSkill(id: number) {
  return requestClient.get<MdmSkillApi.Skill>(`/mdm/skill/get?id=${id}`);
}

/** 新增技能 */
export function createSkill(data: MdmSkillApi.Skill) {
  return requestClient.post('/mdm/skill/create', data);
}

/** 修改技能 */
export function updateSkill(data: MdmSkillApi.Skill) {
  return requestClient.put('/mdm/skill/update', data);
}

/** 删除技能 */
export function deleteSkill(id: number) {
  return requestClient.delete(`/mdm/skill/delete?id=${id}`);
}

/** 批量删除技能 */
export function deleteSkillList(ids: number[]) {
  return requestClient.delete(`/mdm/skill/delete-list?ids=${ids.join(',')}`);
}

/** 导出技能 */
export function exportSkill(params: any) {
  return requestClient.download('/mdm/skill/export-excel', { params });
}

/** 查询工序技能要求分页 */
export function getOpSkillReqPage(params: PageParam & { opId: number }) {
  return requestClient.get<PageResult<MdmSkillApi.OpSkillReq>>(
    '/mdm/skill/op-req/page',
    { params },
  );
}

/** 新增工序技能要求 */
export function createOpSkillReq(data: MdmSkillApi.OpSkillReq) {
  return requestClient.post('/mdm/skill/op-req/create', data);
}

/** 修改工序技能要求 */
export function updateOpSkillReq(data: MdmSkillApi.OpSkillReq) {
  return requestClient.put('/mdm/skill/op-req/update', data);
}

/** 删除工序技能要求 */
export function deleteOpSkillReq(id: number) {
  return requestClient.delete(`/mdm/skill/op-req/delete?id=${id}`);
}

/** 查询人员技能分页 */
export function getMemberSkillPage(
  params: PageParam & { skillId?: number; userId?: number },
) {
  return requestClient.get<PageResult<MdmSkillApi.MemberSkill>>(
    '/mdm/skill/member/page',
    { params },
  );
}

/** 新增人员技能 */
export function createMemberSkill(data: MdmSkillApi.MemberSkill) {
  return requestClient.post('/mdm/skill/member/create', data);
}

/** 修改人员技能 */
export function updateMemberSkill(data: MdmSkillApi.MemberSkill) {
  return requestClient.put('/mdm/skill/member/update', data);
}

/** 删除人员技能 */
export function deleteMemberSkill(id: number) {
  return requestClient.delete(`/mdm/skill/member/delete?id=${id}`);
}
