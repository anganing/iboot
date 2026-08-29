<script lang="ts" setup>
import type { MdmTeamApi } from '#/api/mdm/team';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import {
  createTeamMember,
  deleteTeamMember,
  getTeamMemberPage,
} from '#/api/mdm/team';
import { RelationSelect } from '#/components/relation-select';

import { USER_RELATION_PROPS } from '../../relation-config';

defineOptions({ name: 'TeamMemberModal' });

const emit = defineEmits(['success']);

const team = ref<MdmTeamApi.Team>();
const tableData = ref<MdmTeamApi.TeamMember[]>([]);
const memberForm = ref({
  userId: undefined as number | undefined,
  remark: '',
});

async function loadMembers() {
  if (!team.value?.id) {
    return;
  }
  const result = await getTeamMemberPage({
    teamId: team.value.id,
    pageNo: 1,
    pageSize: 100,
  });
  tableData.value = result.list;
}

async function handleAdd() {
  if (!team.value?.id || !memberForm.value.userId) {
    ElMessage.warning('请选择成员');
    return;
  }
  await createTeamMember({
    teamId: team.value.id,
    userId: memberForm.value.userId,
    remark: memberForm.value.remark,
    status: 0,
  } as MdmTeamApi.TeamMember);
  ElMessage.success('成员已添加');
  memberForm.value = { userId: undefined, remark: '' };
  await loadMembers();
  emit('success');
}

async function handleDeleteRow(row: MdmTeamApi.TeamMember) {
  await deleteTeamMember(row.id);
  ElMessage.success('成员已移除');
  await loadMembers();
  emit('success');
}

const [Modal, modalApi] = useVbenModal({
  fullscreenButton: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      team.value = undefined;
      tableData.value = [];
      memberForm.value = { userId: undefined, remark: '' };
      return;
    }
    team.value = modalApi.getData<MdmTeamApi.Team>();
    await loadMembers();
  },
});
</script>

<template>
  <Modal
    class="w-[860px]"
    :footer="false"
    :title="`班组成员管理 - ${team?.name ?? ''}`"
  >
    <div class="mx-4 flex flex-col gap-4">
      <el-form inline>
        <el-form-item label="成员">
          <RelationSelect
            v-model="memberForm.userId"
            v-bind="USER_RELATION_PROPS"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="memberForm.remark"
            class="!w-48"
            placeholder="请输入备注"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAdd">添加成员</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border size="small">
        <el-table-column label="用户姓名" min-width="140" prop="userName" />
        <el-table-column label="备注" min-width="160" prop="remark" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDeleteRow(row)">
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </Modal>
</template>
