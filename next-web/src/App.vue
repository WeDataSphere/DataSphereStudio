<template>
  <f-config-provider :locale="localeLang" :get-container="getContainer">
    <router-view />
  </f-config-provider>
</template>
<script setup lang="ts">
import { enUS, zhCN } from '@fesjs/fes-design';
import { ref, onMounted } from 'vue';

const localeLang = ref(zhCN);
const getContainer = () => document.body;

onMounted(() => {
  const curlanguage = localStorage.getItem('locale');
  if (curlanguage === 'zh-CN') {
    localeLang.value = zhCN;
  } else {
    localeLang.value = enUS;
  }
});
</script>

<style scoped>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
