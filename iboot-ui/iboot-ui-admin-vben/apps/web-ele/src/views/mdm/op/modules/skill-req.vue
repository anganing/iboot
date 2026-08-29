<script lang="ts" setup>
import type { MdmOpApi } from '#/api/mdm/op';
import type { MdmSkillApi } from '#/api/mdm/skill';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import {
  createOpSkillReq,
  deleteOpSkillReq,
  getOpSkillReqPage,
  updateOpSkillReq,
} from '#/api/mdm/skill';
import { RelationSelect } from '#/components/relation-select';

import { SKILL_RELATION_PROPS } from '../../relation-config';

defineOptions({ name: 'OpSkillReqModal' });

const op = ref<MdmOpApi.Op>();
const tableData = ref<MdmSkillApi.OpSkillReq[]>([]);
const editingId = ref<number>();
const reqForm = ref({
  skillId: undefined as number | undefined,
  minLevel: 1,
});

function resetForm() {
  editingId.value = undefined;
  reqForm.value = { skillId: undefined, minLevel: 1 };
}

async function loadList() {
  if (!op.value?.id) {
    return;
  }
  const result = await getOpSkillReqPage({
    opId: op.value.id,
    pageNo: 1,
    pageSize: 100,
  });
  tableData.value = result.list;
}

async function handleSave() {
  if (!op.value?.id || !reqForm.value.skillId) {
    ElMessage.warning('请选择技能');
    return;
  }
  const data = {
    id: editingId.value,
    opId: op.value.id,
    skillId: reqForm.value.skillId,
    minLevel: reqForm.value.minLevel,
    status: 0,
  } as MdmSkillApi.OpSkillReq;
  await (editingId.value ? updateOpSkillReq(data) : createOpSkillReq(data));
  ElMessage.success('保存成功');
  resetForm();
  await loadList();
}

function handleEditRow(row: MdmSkillApi.OpSkillReq) {
  editingId.value = row.id;
  reqForm.value = {
    skillId: row.skillId,
    minLevel: row.minLevel ?? 1,
  };
}

async function handleDeleteRow(row: MdmSkillApi.OpSkillReq) {
  await deleteOpSkillReq(row.id);
  ElMessage.success('删除成功');
  await loadList();
}

const [Modal, modalApi] = useVbenModal({
  fullscreenButton: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      op.value = undefined;
      tableData.value = [];
      resetForm();
      return;
    }
    op.value = modalApi.getData<MdmOpApi.Op>();
    await loadList();
  },
});
</script>

<template>
  <Modal
    class="w-[860px]"
    :footer="false"
    :title="`工序技能要求 - ${op?.name ?? ''}`"
  >
    <div class="mx-4 flex flex-col gap-4">
      <el-form inline>
        <el-form-item label="技能">
          <RelationSelect
            v-model="reqForm.skillId"
            v-bind="SKILL_RELATION_PROPS"
          />
        </el-form-item>
        <el-form-item label="最低技能等级">
          <el-input-number
            v-model="reqForm.minLevel"
            :min="1"
            :precision="0"
            controls-position="right"
            class="!w-32"
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
        <el-table-column label="技能编码" min-width="140" prop="skillCode" />
        <el-table-column label="技能名称" min-width="160" prop="skillName" />
        <el-table-column label="最低技能等级" min-width="120" prop="minLevel" />
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
