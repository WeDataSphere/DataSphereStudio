<template>
  <FDrawer
    v-model:show="showDrawer"
    :title="$t('_.资源参数模板')"
    :mask-closable="false"
    display-directive="if"
    dimension="50%"
    :ok-text="$t('_.保存')"
    footer
    @ok="handleOk"
  >
    <FCollapse
      v-model="activePanelArray"
      class="collapse-wrapper"
      @change="handlePanelChange"
    >
      <FCollapseItem
        v-for="(item, index) in templateList"
        :key="item.templateId"
        :name="item.templateId"
      >
        <template #title>
          <div class="panel-title" @click.stop="() => {}">
            <DownOutlined
              class="panel-title__icon"
              @click.stop="handlePanelChange([item.templateId], 'expand')"
            />
            <FRadio
              v-model="radioData[index as number]"
              style="width: 85%; overflow: hidden; text-overflow: ellipsis"
              @change="handleRadioChange"
            >
              <span :title="item.templateName">{{ item.templateName }}</span>
            </FRadio>
          </div>
        </template>
        <div>
          <div v-for="(confItem, confIndex) in item.conf" :key="confIndex">
            <span>{{ confItem.key }}</span
            >:&nbsp;<span style="color: black">{{ confItem.configValue }}</span>
          </div>
          <div>
            <span>{{ $t('_.模板描述') }}</span
            >:&nbsp;<span style="color: black">{{ item.desc }}</span>
          </div>
        </div>
      </FCollapseItem>
    </FCollapse>
  </FDrawer>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

import { ref, computed, watch } from 'vue';
import { DownOutlined } from '@fesjs/fes-design/icon';
import type { BaseType } from '../types/index';
import api from '../api';
import { cloneDeep } from 'lodash-es';

const { t: $t } = useI18n();

const emits = defineEmits<{
  (e: 'update:show', val: boolean): void;
  (e: 'success', val: BaseType): void;
}>();

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  templates: {
    type: Array,
    default: () => [],
  },
  defaultTemplateId: {
    type: String,
    default: '',
  },
});

const curTemplateObj = ref({});
const activePanelArray = ref<string[]>([]);
const cacheRadioData = ref<boolean[]>([]);
const radioData = ref<boolean[]>([]);
const templateList = ref<BaseType>([]);

const initRadioData = () => {
  templateList.value.forEach((item: BaseType, index: number) => {
    if (item.templateId === props.defaultTemplateId) {
      radioData.value[index] = true;
      cacheRadioData.value[index] = true;
    } else {
      radioData.value[index] = false;
      cacheRadioData.value[index] = false;
    }
  });
};
const handleRadioChange = () => {
  cacheRadioData.value.forEach((value, index) => {
    if (value && radioData.value[index]) {
      radioData.value[index] = false;
    }
  });
  cacheRadioData.value = JSON.parse(JSON.stringify(radioData.value));
};

const handlePanelChange = (vArray: string[], type: string) => {
  if (type == 'expand') {
    if (activePanelArray.value.indexOf(vArray[0]) > -1) {
      activePanelArray.value.splice(
        activePanelArray.value.indexOf(vArray[0]),
        1
      );
    } else {
      activePanelArray.value.push(vArray[0]);
    }
  }
  vArray.forEach(async (v) => {
    const index = templateList.value.findIndex((item: BaseType) => {
      return item.templateId == v;
    });
    if (!templateList.value[index].desc) {
      const res = await api.GetNodeTemplateDetail({
        templateId: templateList.value[index]?.templateId,
      });
      const descRes = res.data || {};
      let curTemplate = JSON.parse(JSON.stringify(templateList.value[index]));
      curTemplate.conf = JSON.parse(JSON.stringify(descRes.conf));
      curTemplate.desc = JSON.parse(JSON.stringify(descRes.description));
      templateList.value[index] = curTemplate;
    }
  });
};

const initData = () => {
  activePanelArray.value = [];
  cacheRadioData.value = [];
  radioData.value = [];
};

const showDrawer = computed({
  get() {
    return !!props.show;
  },
  set(val) {
    emits('update:show', val);
  },
});

watch(
  () => showDrawer.value,
  (show) => {
    if (show) {
      initData();
      templateList.value = cloneDeep(props.templates);
      initRadioData();
    }
  },
  { immediate: true }
);

const handleOk = async () => {
  radioData.value.some((value, index) => {
    if (value) {
      curTemplateObj.value = templateList.value[index];
      return true;
    }
    // 最后都没有匹配的值就置空
    if (index === radioData.value.length - 1) {
      curTemplateObj.value = {};
    }
  });
  emits('success', curTemplateObj.value);
  showDrawer.value = false;
};
</script>

<style lang="less" scoped>
.collapse-wrapper {
  .panel-title {
    width: 100%;

    &__icon {
      margin-right: 5px;
      opacity: 0;

      &:hover {
        opacity: 1;
      }
    }
  }

  :deep(.fes-collapse-item__arrow) {
    display: none;
  }
}
</style>
