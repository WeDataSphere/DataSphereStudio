<template>
  <div class="layout-body" :class="{ 'layout-top': !$route.query.noHeader }">
    <layout-header
      v-if="!$route.query.noHeader"
      @clear-session="clearSession"
      ref="layoutHeader"></layout-header>
    <notice api-path="/dss/framework/workspace/getNotice" />
    <Alert v-if="showLoginNotice" type="info" class="top-login-notice-bar" closable @on-close="closeLoginNotice">
      <SvgIcon
        icon-class="horn"
      />
      {{ $t('message.common.safeTip') }}
    </Alert>
    <router-view />
    <layout-footer ref="layoutFooter" v-if="!$route.query.noFooter"/>
    <newGuidance v-if="$route.path === '/workspaceHome'" ref="newGuidance"/>
  </div>
</template>
<script>
import headerModule from '../module/header';
import footerModule from '../module/footer';
import notice from '@dataspherestudio/shared/components/notice';
import newComerGuidanceModule from '../module/newGuidance'
import layoutMixin from '@dataspherestudio/shared/common/service/layoutMixin.js';
import storage from '@dataspherestudio/shared/common/helper/storage'

export default {
  components: {
    layoutFooter: footerModule.component,
    layoutHeader: headerModule.component,
    newGuidance: newComerGuidanceModule.component,
    notice
  },
  data() {
    return {
      showLoginNoticeStorage: false
    }
  },
  computed: {
    showLoginNotice() {
      const allowedPaths = ['/workspaceHome', '/home'];
      return this.showLoginNoticeStorage && allowedPaths.includes(this.$route.path);
    }
  },
  mixins: [layoutMixin],
  created() {
    // 检查是否需要显示登录提示
    this.showLoginNoticeStorage = storage.get('showLoginNotice', 'session') || false;
  },
  methods: {
    closeLoginNotice() {
      this.showLoginNoticeStorage = false;
      storage.remove('showLoginNotice', 'session');
    }
  },
};
</script>
<style lang="scss" scoped>
@import '@dataspherestudio/shared/common/style/variables.scss';
.top-notice-bar {
  font-size: 14px;
  color: $warning-color;
  z-index:2;
  position: absolute;
  top: 55px;
  width: 100%;
  text-overflow: ellipsis;
  overflow: hidden;
  border-radius: unset;
  white-space: nowrap;
}

.top-login-notice-bar {
  font-size: 14px;
  color: $primary-color;
  z-index:2;
  position: absolute;
  top: 55px;
  width: 100%;
  text-overflow: ellipsis;
  overflow: hidden;
  border-radius: unset;
  white-space: nowrap;
}
</style>
