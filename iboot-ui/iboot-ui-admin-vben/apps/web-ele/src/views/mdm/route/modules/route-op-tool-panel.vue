<script lang="ts" setup>
import type { MdmToolApi } from '#/api/mdm/tool';

import { ref, watch } from 'vue';

import { DICT_TYPE } from '@vben/constants';

import { ElMessage } from 'element-plus';

import {
  createRouteOpTool,
  deleteRouteOpTool,
  getRouteOpToolPage,
} from '#/api/mdm/tool';
import { DictTag } from '#/components/dict-tag';
import { RelationSelect } from '#/components/relation-select';

import { TOOL_RELATION_PROPS } from '../../relation-config';

defineOptions({ name: 'RouteOpToolPanel' });

const props = defineProps<{
  editable: boolean;
  routeOpId?: number | string;
}>();

const tableData = ref<MdmToolApi.RouteOpTool[]>([]);
const toolForm = ref({
  toolId: undefined as number | undefined,
  qty: 1,
  requiredFlag: true,
});

function resetForm() {
  toolForm.value = { toolId: undefined, qty: 1, requiredFlag: true };
}

async function loadList() {
  if (!props.routeOpId) {
    tableData.value = [];
    return;
  }
  const result = await getRouteOpToolPage({
    routeOpId: props.routeOpId as number,
    pageNo: 1,
    pageSize: 100,
  });
  tableData.value = result.list;
}

async function handleAdd() {
  if (!props.routeOpId || !toolForm.value.toolId) {
    ElMessage.warning('请选择工装');
    return;
  }
  await createRouteOpTool({
    routeOpId: props.routeOpId,
    toolId: toolForm.value.toolId,
    qty: toolForm.value.qty,
    requiredFlag: toolForm.value.requiredFlag,
  } as MdmToolApi.RouteOpTool);
  ElMessage.success('工装已添加');
  resetForm();
  await loadList();
}

async function handleDeleteRow(row: MdmToolApi.RouteOpTool) {
  await deleteRouteOpTool(row.id);
  ElMessage.success('工装已移除');
  await loadList();
}

watch(
  () => props.routeOpId,
  async () => {
    resetForm();
    await loadList();
  },
  { immediate: true },
);
</script>

<template>
  <div class="flex flex-col gap-3">
    <el-form inline>
      <el-form-item label="工装">
        <RelationSelect v-model="toolForm.toolId" v-bind="TOOL_RELATION_PROPS" />
      </el-form-item>
      <el-form-item label="需求数量">
        <el-input-number
          v-model="toolForm.qty"
          :min="1"
          :precision="0"
          class="!w-28"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item label="是否必备">
        <el-switch v-model="toolForm.requiredFlag" />
      </el-form-item>
      <el-form-item>
        <el-button
          :disabled="!editable || !routeOpId"
          type="primary"
          @click="handleAdd"
        >
          添加工装
        </el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" border size="small">
      <el-table-column label="工装编码" min-width="140" prop="toolCode" />
      <el-table-column label="工装名称" min-width="160" prop="toolName" />
      <el-table-column label="需求数量" min-width="100" prop="qty" />
      <el-table-column label="是否必备" min-width="100">
        <template #default="{ row }">
          <DictTag
            :type="DICT_TYPE.INFRA_BOOLEAN_STRING"
            :value="String(row.requiredFlag)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            :disabled="!editable"
            link
            type="danger"
            @click="handleDeleteRow(row)"
          >
            移除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
