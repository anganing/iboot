<script lang="ts" setup>
import type { MdmCalendarApi } from '#/api/mdm/calendar';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import {
  createCalendarException,
  deleteCalendarException,
  getCalendarExceptionPage,
  updateCalendarException,
} from '#/api/mdm/calendar';
import { RelationSelect } from '#/components/relation-select';

import { SHIFT_RELATION_PROPS } from '../../relation-config';
import { getExceptionTypeOptions } from '../data';

const calendar = ref<MdmCalendarApi.Calendar>();
const tableData = ref<MdmCalendarApi.CalendarException[]>([]);
const editingId = ref<number>();
const exceptionForm = ref({
  exceptionDate: '',
  exceptionType: 'HOLIDAY',
  shiftId: undefined as number | undefined,
  availableMinutes: undefined as number | undefined,
});

const typeOptions = getExceptionTypeOptions();

async function loadExceptions() {
  if (!calendar.value?.id) {
    return;
  }
  const result = await getCalendarExceptionPage({
    calendarId: calendar.value.id,
    pageNo: 1,
    pageSize: 200,
  });
  tableData.value = result.list;
}

async function handleSave() {
  if (!calendar.value?.id || !exceptionForm.value.exceptionDate) {
    ElMessage.warning('请选择例外日期');
    return;
  }
  const data = {
    id: editingId.value,
    calendarId: calendar.value.id,
    ...exceptionForm.value,
  } as MdmCalendarApi.CalendarException;
  await (editingId.value
    ? updateCalendarException(data)
    : createCalendarException(data));
  ElMessage.success('保存成功');
  editingId.value = undefined;
  await loadExceptions();
}

function handleEditRow(row: MdmCalendarApi.CalendarException) {
  editingId.value = row.id;
  exceptionForm.value = {
    exceptionDate: row.exceptionDate ?? '',
    exceptionType: row.exceptionType ?? 'HOLIDAY',
    shiftId: row.shiftId,
    availableMinutes: row.availableMinutes,
  };
}

async function handleDeleteRow(row: MdmCalendarApi.CalendarException) {
  await deleteCalendarException(row.id);
  ElMessage.success('删除成功');
  await loadExceptions();
}

const [Modal, modalApi] = useVbenModal({
  fullscreenButton: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      calendar.value = undefined;
      return;
    }
    calendar.value = modalApi.getData<MdmCalendarApi.Calendar>();
    await loadExceptions();
  },
});
</script>

<template>
  <Modal class="w-[860px]" :footer="false" title="日期例外（覆盖常规规则）">
    <div class="mx-4 flex flex-col gap-4">
      <el-form inline>
        <el-form-item label="例外日期">
          <el-date-picker
            v-model="exceptionForm.exceptionDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="!w-36"
          />
        </el-form-item>
        <el-form-item label="例外类型">
          <el-select v-model="exceptionForm.exceptionType" class="!w-32">
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班次">
          <RelationSelect
            v-model="exceptionForm.shiftId"
            v-bind="SHIFT_RELATION_PROPS"
          />
        </el-form-item>
        <el-form-item label="可用分钟数">
          <el-input-number
            v-model="exceptionForm.availableMinutes"
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
      <el-alert
        :closable="false"
        show-icon
        title="停工例外可不选班次（表示全天停工）；加班与计划保养必须指定班次和可用分钟数。"
        type="info"
      />
      <el-table :data="tableData" border size="small">
        <el-table-column
          label="例外日期"
          min-width="120"
          prop="exceptionDate"
        />
        <el-table-column label="例外类型" min-width="110">
          <template #default="{ row }">
            {{
              typeOptions.find((item) => item.value === row.exceptionType)
                ?.label ?? row.exceptionType
            }}
          </template>
        </el-table-column>
        <el-table-column label="班次" min-width="120" prop="shiftName" />
        <el-table-column
          label="可用分钟数"
          min-width="110"
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
