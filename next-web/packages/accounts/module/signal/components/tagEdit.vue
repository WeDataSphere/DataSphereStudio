<template>
  <div class="tag-edit">
    <FSpace>
      <FTag
        v-for="(tag, index) in inputTags"
        :key="`tag_${index}`"
        :closable="!unCloseable"
        type="info"
        @close="handleClose(tag as BaseType)"
      >
        <FTooltip
          :content="getContent(tag as BaseType)"
          placement="bottom-start"
        >
          <p class="item-ellipis">
            {{ getContent(tag as BaseType) }}
          </p>
        </FTooltip>
      </FTag>
      <div v-if="props.mode === 'edit'">
        <FInput
          v-if="state.inputVisible"
          ref="inputRef"
          v-model="state.inputValue"
          class="input-tag"
          size="small"
          :maxlength="30"
          @blur="handleInputConfirm"
          @keyup.enter="handleInputConfirm"
        />
        <FButton v-else class="button-tag" @click="showInput">
          + {{ props.label }}
        </FButton>
      </div>
    </FSpace>
  </div>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

import { ref, computed } from 'vue';
import { useDynamicTags } from '../hooks/useDynamicTags';
import type { BaseType } from '../types/index';

const { t: $t } = useI18n();

const emits = defineEmits(['update:tags']);

const props = defineProps({
  tags: {
    type: [Array, String],
    default: () => [],
  },
  label: {
    type: String,
    default: '添加标签',
  },
  mode: {
    type: String,
    default: 'edit',
  },
});

const inputTags = computed({
  get: () => {
    return props.tags || [];
  },
  set: (val) => {
    emits('update:tags', val);
  },
});

const unCloseable = computed(
  () => props.mode === 'read' && inputTags.value.length <= 1
);
const inputRef = ref(null);
const { state, handleClose, showInput, handleInputConfirm } = useDynamicTags(
  inputRef,
  (data) => {
    inputTags.value = [...data];
    emits('update:tags', [...data]);
  }
);

state.value.tags = (props.tags as BaseType[]) || [];
const getContent = (tag: BaseType | string) => {
  return tag && typeof tag === 'object' ? tag.name : tag;
};
</script>

<style lang="less" scoped>
.tag-edit {
  width: calc(100% - 100px);

  .fes-space,
  .fes-tag {
    max-width: 99%;

    .item-ellipis {
      text-overflow: ellipsis;
      overflow: hidden;
      white-space: nowrap;
      max-width: 100%;
    }
  }
}
</style>
