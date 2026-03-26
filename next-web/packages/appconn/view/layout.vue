<template>
  <div style="background: #eee; padding: 10px; height: 100%">
    <FBreadcrumb style="padding: 10px">
      <FBreadcrumbItem @click="handleClick">
        <router-link :to="{ path: '/appconn' }"> AppConn管理台 </router-link>
      </FBreadcrumbItem>
      <FBreadcrumbItem
        v-if="['/appconn/addNode', '/appconn/editNode'].includes(route.path)"
      >
        <router-link :to="{ path: '/appconn/queryNode', query: route.query }">
          节点管理
        </router-link>
      </FBreadcrumbItem>
      <FBreadcrumbItem v-if="route.path != '/appconn/manage'">
        {{ routeName }}
      </FBreadcrumbItem>
    </FBreadcrumb>
    <router-view />
  </div>
</template>
<script setup>
import { FBreadcrumb, FBreadcrumbItem } from '@fesjs/fes-design';
import { computed } from 'vue';
import { useRoute } from 'vue-router';
const route = useRoute();
const routeName = computed(() => {
  return route.meta.title;
});
</script>
