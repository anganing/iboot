import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmTeamApi {
  /** 班组信息 */
  export interface Team {
    id: number; // 主键
    code?: string; // 班组编码
    name?: string; // 班组名称
    workshopId?: number; // 所属车间编号
    workshopCode?: string; // 车间编码
    workshopName?: string; // 车间名称
    shiftId?: number; // 常白/常夜班次编号
    shiftName?: string; // 班次名称
    leaderUserId?: number; // 班组长用户编号
    leaderUserName?: string; // 班组长姓名
    memberCount?: number; // 成员数量
    status?: number; // 状态
    remark?: string; // 备注
    createTime?: Date; // 创建时间
  }

  /** 班组成员信息 */
  export interface TeamMember {
    id: number; // 主键
    teamId?: number; // 班组编号
    userId?: number; // system 用户编号
    userName?: string; // 用户姓名
    status?: number; // 状态
    remark?: string; // 备注
    createTime?: Date; // 创建时间
  }
}

/** 查询班组分页 */
export function getTeamPage(params: PageParam) {
  return requestClient.get<PageResult<MdmTeamApi.Team>>('/mdm/team/page', {
    params,
  });
}

/** 查询班组详情 */
export function getTeam(id: number) {
  return requestClient.get<MdmTeamApi.Team>(`/mdm/team/get?id=${id}`);
}

/** 新增班组 */
export function createTeam(data: MdmTeamApi.Team) {
  return requestClient.post('/mdm/team/create', data);
}

/** 修改班组 */
export function updateTeam(data: MdmTeamApi.Team) {
  return requestClient.put('/mdm/team/update', data);
}

/** 删除班组 */
export function deleteTeam(id: number) {
  return requestClient.delete(`/mdm/team/delete?id=${id}`);
}

/** 批量删除班组 */
export function deleteTeamList(ids: number[]) {
  return requestClient.delete(`/mdm/team/delete-list?ids=${ids.join(',')}`);
}

/** 导出班组 */
export function exportTeam(params: any) {
  return requestClient.download('/mdm/team/export-excel', { params });
}

/** 查询班组成员分页 */
export function getTeamMemberPage(
  params: PageParam & { teamId: number },
) {
  return requestClient.get<PageResult<MdmTeamApi.TeamMember>>(
    '/mdm/team/member/page',
    { params },
  );
}

/** 新增班组成员 */
export function createTeamMember(data: MdmTeamApi.TeamMember) {
  return requestClient.post('/mdm/team/member/create', data);
}

/** 修改班组成员 */
export function updateTeamMember(data: MdmTeamApi.TeamMember) {
  return requestClient.put('/mdm/team/member/update', data);
}

/** 查询班组成员详情 */
export function getTeamMember(id: number) {
  return requestClient.get<MdmTeamApi.TeamMember>(
    `/mdm/team/member/get?id=${id}`,
  );
}

/** 删除班组成员 */
export function deleteTeamMember(id: number) {
  return requestClient.delete(`/mdm/team/member/delete?id=${id}`);
}

/** 批量删除班组成员 */
export function deleteTeamMemberList(ids: number[]) {
  return requestClient.delete(
    `/mdm/team/member/delete-list?ids=${ids.join(',')}`,
  );
}
