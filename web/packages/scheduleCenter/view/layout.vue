<template>
  <div class="layout-body layout-top">
    <layout-header
      v-if="!$route.query.noHeader"
      @clear-session="clearSession"
      ref="layoutHeader"></layout-header>
    <router-view/>
  </div>
</template>
<script>
import headerModule from '../module/header';
import layoutMixin from '@dataspherestudio/shared/common/service/layoutMixin.js';
export default {
  components: {
    layoutHeader: headerModule.component,
  },
  mixins: [layoutMixin]
};
</script>
