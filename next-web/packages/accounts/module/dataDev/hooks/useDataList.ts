import { useI18n } from 'vue-i18n';
import { ref } from 'vue';
import api from '../api';
import type { DataType } from '../types/index';

export const useDataList = () => {
  const { t: $t } = useI18n();
  const booleanList = ref([
    {
      label: $t('_.是'),
      value: 'true',
    },
    {
      label: $t('_.否'),
      value: 'false',
    },
  ]);

  return {
    booleanList,
  };
};
