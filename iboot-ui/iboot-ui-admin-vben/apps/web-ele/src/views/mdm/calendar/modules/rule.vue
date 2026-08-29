<script lang="ts" setup>
import type { MdmCalendarApi } from '#/api/mdm/calendar';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import {
  createCalendarRule,
  deleteCalendarRule,
  getCalendarRulePage,
  updateCalendarRule,
} from '#/api/mdm/calendar';
import { RelationSelect } from '#/components/relation-select';

import { SHIFT_RELATION_PROPS } from '../../relation-config';
import { getDayOfWeekOptions } from '../data';

const calendar = ref<MdmCalendarApi.Calendar>();
const tableData = ref<MdmCalendarApi.CalendarRule[]>([]);
const editingId = ref<number>();
const ruleForm = ref({
  dayOfWeek: 1,
  shiftId: undefined as number | undefined,
  availableMinutes: 480,
});

const dayOptions = getDayOfWeekOptions();

function dayLabel(day: number) {
  return dayOptions.find((item) => item.value === day)?.label ?? day;
}

async function loadRules() {
  if (!calendar.value?.id) {
    return;
  }
  const result = await getCalendarRulePage({
    calendarId: calendar.value.id,
    pageNo: 1,
    pageSize: 100,
  });
  tableData.value = result.list;
}

async function handleSave() {
  if (!calendar.value?.id || !ruleForm.value.shiftId) {
    ElMessage.warning('请选择班次');
    return;
  }
  const data = {
    id: editingId.value,
    calendarId: calendar.value.id,
    ...ruleForm.value,
  } as MdmCalendarApi.CalendarRule;
  await (editingId.value ? updateCalendarRule(data) : createCalendarRule(data));
  ElMessage.success('保存成功');
  editingId.value = undefined;
  await loadRules();
}

function handleEditRow(row: MdmCalendarApi.CalendarRule) {
  editingId.value = row.id;
  ruleForm.value = {
    dayOfWeek: row.dayOfWeek ?? 1,
    shiftId: row.shiftId,
    availableMinutes: row.availableMinutes ?? 480,
  };
}

async function handleDeleteRow(row: MdmCalendarApi.CalendarRule) {
  await deleteCalendarRule(row.id);
  ElMessage.success('删除成功');
  await loadRules();
}

const [Modal, modalApi] = useVbenModal({
  fullscreenButton: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      calendar.value = undefined;
      return;
    }
    calendar.value = modalApi.getData<MdmCalendarApi.Calendar>();
    await loadRules();
  },
});
</script>

<template>
  <Modal class="w-[860px]" :footer="false" title="常规工作模式规则">
    <div class="mx-4 flex flex-col gap-4">
      <el-form inline>
        <el-form-item label="星期几">
          <el-select v-model="ruleForm.dayOfWeek" class="!w-28">
            <el-option
              v-for="item in dayOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班次">
          <RelationSelect
            v-model="ruleForm.shiftId"
            v-bind="SHIFT_RELATION_PROPS"
          />
        </el-form-item>
        <el-form-item label="可用分钟数">
          <el-input-number
            v-model="ruleForm.availableMinutes"
            :min="0"
            :precision="0"
            controls-position="right"
            class="!w-32"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">
            {{ editingId ? '更新' : '新增' }}
          </el-button>
          <el-button v-if="editingId" @click="editingId = undefined">
            取消编辑
          </el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border size="small">
        <el-table-column label="星期几" min-width="100">
          <template #default="{ row }">{{ dayLabel(row.dayOfWeek) }}</template>
        </el-table-column>
        <el-table-column label="班次编码" min-width="120" prop="shiftCode" />
        <el-table-column label="班次名称" min-width="140" prop="shiftName" />
        <el-table-column
          label="可用分钟数"
          min-width="120"
          prop="availableMinutes"
        />
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
