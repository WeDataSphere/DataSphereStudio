import { ref, nextTick, type Ref } from 'vue';
import type { BaseType } from '../types';

export const useDynamicTags = (inputRef: Ref, cb: (val: unknown[]) => void) => {
  const state = ref<{
    tags: any[];
    inputVisible: boolean;
    inputValue: string;
  }>({
    tags: [],
    inputVisible: false,
    inputValue: '',
  });

  /**
   * @description: 删除tag
   * @param tag: 标签名
   * @return {*}
   */
  const handleClose = (tag: BaseType | string) => {
    let index;
    if (tag && typeof tag === 'object') {
      index = state.value.tags.findIndex(
        (item) => (item as BaseType).value === tag.value
      );
    } else {
      index = state.value.tags.indexOf(tag as string);
    }
    state.value.tags.splice(index, 1);
    cb && cb(state.value.tags);
  };

  /**
   * @description: 展示编辑
   * @return {*}
   */
  const showInput = async () => {
    state.value.inputVisible = true;
    await nextTick();
    inputRef.value?.focus();
  };

  /**
   * @description: 添加tags
   * @return {*}
   */
  const handleInputConfirm = () => {
    const inputValue: string = state.value.inputValue;
    if (inputValue) {
      state.value.tags.push(inputValue);
      cb && cb(state.value.tags);
    }
    state.value.inputVisible = false;
    state.value.inputValue = '';
  };

  return {
    state,
    handleClose,
    showInput,
    handleInputConfirm,
  };
};
