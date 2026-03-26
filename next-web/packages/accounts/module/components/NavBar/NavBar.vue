<template>
  <div
    class="nav-bar-container"
    :style="`${
      styleType === 'big' &&
      'margin-bottom: 0;padding: 24px 0 0 8px;height: auto'
    }`"
  >
    <FScrollbar style="width: 100%">
      <ul v-if="type === 'route'" class="nav-bar-list">
        <router-link
          v-for="item in data"
          :key="item.value"
          class="nav-bar-item"
          :to="item.path"
        >
          {{ item.label }}<RightOutlined v-if="item.link" class="arrow" />
        </router-link>
        <slot name="suffix" />
      </ul>
      <ul v-if="type === 'change'" class="nav-bar-list">
        <li
          v-for="item in data"
          :key="item.value"
          class="nav-bar-item"
          :class="{
            active: item.value === modelValue,
            'nav-bar-item-big': styleType === 'big',
          }"
          @click="updateActiveNavBar(item)"
        >
          <span class="nav-bar-main">{{ item.label }}</span>
        </li>
      </ul>
    </FScrollbar>
  </div>
</template>
<script setup>
import { FScrollbar } from '@fesjs/fes-design';
import { defineProps, defineEmits } from 'vue';
import { RightOutlined } from '@fesjs/fes-design/icon';

const props = defineProps({
  data: {
    type: Array,
    required: false,
    default: () => [],
  },
  modelValue: {
    type: [String, Number],
    required: false,
  },
  // 组件提供两种tab切换方式
  // type:route,传入path，做路由跳转 -> 页面上的tab切换
  // type:change,更新tab对应value值,在父组件做映射处理 -> 组件内嵌的tab切换
  type: {
    type: String,
    required: true,
  },
  styleType: {
    type: String,
    required: false,
    default: 'default',
  },
});
const emit = defineEmits(['update:modelValue', 'change']);
const updateActiveNavBar = async (navBar) => {
  if (navBar.value === props.modelValue) {
    return;
  }
  emit('update:modelValue', navBar.value);
  emit('change', navBar);
};
</script>
<style lang="less" scoped>
@import './NavBar.less';
.arrow {
  position: absolute;
  top: 18px;
  margin-left: 5px;
}
.nav-bar-list .nav-bar-item.nav-bar-item-big {
  line-height: 25px;
  font-size: 16px;
  font-weight: 500;
}
</style>
