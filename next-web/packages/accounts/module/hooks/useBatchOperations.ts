import { useI18n } from 'vue-i18n';
import { ref } from 'vue';
import { FTable, FMessage } from '@fesjs/fes-design';

type BaseType = Record<string, any>;
export type OperStatusType = 'fullfield' | 'prepare' | 'pendding';

export const useBatchOperations = () => {
  const { t: $t } = useI18n();
  const operStatus = ref<OperStatusType>('fullfield'); // 等待选择
  const selectedRows = ref<BaseType[]>([]); // 选中的表格项
  const tableRef = ref<typeof FTable | null>(null);

  function resetOperStatus(status: OperStatusType = 'fullfield') {
    if (['fullfield', 'prepare', 'pendding'].includes(status)) {
      operStatus.value = status;
    }
  }

  // 重置选择
  function selectionReset(type = 'all') {
    if (type === 'all') {
      operStatus.value = 'fullfield';
    }
    tableRef.value?.clearSelection();
    selectedRows.value = [];
  }

  // selection的选择事件
  function selectionChange(row: BaseType[]) {
    selectedRows.value = row;
  }

  // selection的validte事件
  function selectionValidate(cb?: (rows: BaseType[]) => boolean) {
    if (!selectedRows.value.length) {
      FMessage.warn($t('_.请至少选择一项'));
      return Promise.reject(false);
    }
    if (cb && !cb(selectedRows.value)) {
      return Promise.reject(false);
    }
    operStatus.value = 'pendding';
    return Promise.resolve(selectedRows.value);
  }

  return {
    tableRef,
    operStatus,
    selectedRows,
    selectionReset,
    resetOperStatus,
    selectionChange,
    selectionValidate,
  };
};
