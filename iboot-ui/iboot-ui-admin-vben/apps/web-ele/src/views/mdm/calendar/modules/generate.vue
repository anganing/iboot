<script lang="ts" setup>
import type { MdmCalendarApi } from '#/api/mdm/calendar';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { generateResCalendars } from '#/api/mdm/calendar';
import { RelationSelect } from '#/components/relation-select';

import { RES_RELATION_PROPS } from '../../relation-config';

const calendar = ref<MdmCalendarApi.Calendar>();
const generateForm = ref({
  resIds: [] as number[],
  dateRange: ['', ''] as [string, string],
});

const [Modal, modalApi] = useVbenModal({
  fullscreenButton: false,
  async onConfirm() {
    if (!calendar.value?.id) {
      return;
    }
    if (generateForm.value.resIds.length === 0) {
      ElMessage.warning('请选择资源');
      return;
    }
    const [dateFrom, dateTo] = generateForm.value.dateRange;
    if (!dateFrom || !dateTo) {
      ElMessage.warning('请选择日期区间');
      return;
    }
    modalApi.lock();
    try {
      const count = await generateResCalendars({
        calendarId: calendar.value.id,
        resIds: generateForm.value.resIds,
        dateFrom,
        dateTo,
      });
      ElMessage.success(
        `生成完成，共 ${count} 条日历明细（人工调整行不受影响）`,
      );
      await modalApi.close();
    } finally {
      modalApi.unlock();
    }
  },
  onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      calendar.value = undefined;
      generateForm.value = { resIds: [], dateRange: ['', ''] };
      return;
    }
    calendar.value = modalApi.getData<MdmCalendarApi.Calendar>();
  },
});
</script>

<template>
  <Modal class="w-[640px]" confirm-text="开始生成" title="按模板生成资源日历">
    <div class="mx-4 flex flex-col gap-4">
      <el-alert
        :closable="false"
        show-icon
        title="生成按资源+日期+班次幂等：重复执行不会产生重复行；人工调整过的日历行（非模板生成）不会被覆盖。"
        type="info"
      />
      <el-form label-width="100px">
        <el-form-item label="日历模板">
          <el-input :model-value="calendar?.name" disabled />
        </el-form-item>
        <el-form-item label="资源">
          <RelationSelect
            v-model="generateForm.resIds"
            v-bind="RES_RELATION_PROPS"
          />
        </el-form-item>
        <el-form-item label="日期区间">
          <el-date-picker
            v-model="generateForm.dateRange"
            type="daterange"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
    </div>
  </Modal>
</template>
