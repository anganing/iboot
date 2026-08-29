<script lang="ts" setup>
import type { MdmSkillApi } from '#/api/mdm/skill';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import {
  createMemberSkill,
  deleteMemberSkill,
  getMemberSkillPage,
  updateMemberSkill,
} from '#/api/mdm/skill';
import { RelationSelect } from '#/components/relation-select';

import { USER_RELATION_PROPS } from '../../relation-config';

defineOptions({ name: 'MemberSkillModal' });

const skill = ref<MdmSkillApi.Skill>();
const tableData = ref<MdmSkillApi.MemberSkill[]>([]);
const editingId = ref<number>();
const memberForm = ref({
  userId: undefined as number | undefined,
  level: 1,
  effectiveDate: undefined as string | undefined,
  expiryDate: undefined as string | undefined,
});

function resetForm() {
  editingId.value = undefined;
  memberForm.value = {
    userId: undefined,
    level: 1,
    effectiveDate: undefined,
    expiryDate: undefined,
  };
}

async function loadList() {
  if (!skill.value?.id) {
    return;
  }
  const result = await getMemberSkillPage({
    skillId: skill.value.id,
    pageNo: 1,
    pageSize: 100,
  });
  tableData.value = result.list;
}

async function handleSave() {
  if (!skill.value?.id || !memberForm.value.userId) {
    ElMessage.warning('请选择人员');
    return;
  }
  const data = {
    id: editingId.value,
    skillId: skill.value.id,
    userId: memberForm.value.userId,
    level: memberForm.value.level,
    effectiveDate: memberForm.value.effectiveDate,
    expiryDate: memberForm.value.expiryDate,
    status: 0,
  } as MdmSkillApi.MemberSkill;
  await (editingId.value ? updateMemberSkill(data) : createMemberSkill(data));
  ElMessage.success('保存成功');
  resetForm();
  await loadList();
}

function handleEditRow(row: MdmSkillApi.MemberSkill) {
  editingId.value = row.id;
  memberForm.value = {
    userId: row.userId,
    level: row.level ?? 1,
    effectiveDate: row.effectiveDate,
    expiryDate: row.expiryDate,
  };
}

async function handleDeleteRow(row: MdmSkillApi.MemberSkill) {
  await deleteMemberSkill(row.id);
  ElMessage.success('删除成功');
  await loadList();
}

const [Modal, modalApi] = useVbenModal({
  fullscreenButton: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      skill.value = undefined;
      tableData.value = [];
      resetForm();
      return;
    }
    skill.value = modalApi.getData<MdmSkillApi.Skill>();
    await loadList();
  },
});
</script>

<template>
  <Modal
    class="w-[960px]"
    :footer="false"
    :title="`人员技能 - ${skill?.name ?? ''}`"
  >
    <div class="mx-4 flex flex-col gap-4">
      <el-form inline>
        <el-form-item label="人员">
          <RelationSelect
            v-model="memberForm.userId"
            v-bind="USER_RELATION_PROPS"
          />
        </el-form-item>
        <el-form-item label="技能等级">
          <el-input-number
            v-model="memberForm.level"
            :min="1"
            :precision="0"
            controls-position="right"
            class="!w-28"
          />
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker
            v-model="memberForm.effectiveDate"
            class="!w-36"
            placeholder="请选择"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="失效日期">
          <el-date-picker
            v-model="memberForm.expiryDate"
            class="!w-36"
            placeholder="请选择"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">
            {{ editingId ? '更新' : '新增' }}
          </el-button>
          <el-button v-if="editingId" @click="resetForm">取消编辑</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border size="small">
        <el-table-column label="人员" min-width="140" prop="userName" />
        <el-table-column label="技能等级" min-width="100" prop="level" />
        <el-table-column label="生效日期" min-width="120" prop="effectiveDate" />
        <el-table-column label="失效日期" min-width="120" prop="expiryDate" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEditRow(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDeleteRow(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </Modal>
</template>
