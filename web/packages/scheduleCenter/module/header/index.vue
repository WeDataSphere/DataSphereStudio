<template>
  <div>
    <div class="layout-header">
      <div class="layout-header-menu-icon">
        <div class="logo">
          <img
            class="logo-img"
            src="../../assets/images/smart-ds-white.svg"
            :alt="$APP_CONF.app_name"
          />
          <span class="version">{{sysVersion}}</span>
        </div>
      </div>

      <div
        v-clickoutside="handleOutsideClick"
        :class="{'selected': isUserMenuShow}"
        class="user"
        @click="handleUserClick"
      >
        <span>{{ userName }}</span>
        <Icon v-show="!isUserMenuShow" type="ios-arrow-down" class="user-icon" />
        <Icon v-show="isUserMenuShow" type="ios-arrow-up" class="user-icon" />
        <userMenu v-show="isUserMenuShow" @clear-session="clearSession" />
      </div>
    </div>
  </div>
</template>
<script>
import userMenu from './userMenu.vue'
import clickoutside from '@dataspherestudio/shared/common/helper/clickoutside'
import mixin from '@dataspherestudio/shared/common/service/mixin'
import tree from '@/scriptis/service/db/tree.js';
import storage from '@dataspherestudio/shared/common/helper/storage';
import eventbus from '@dataspherestudio/shared/common/helper/eventbus';
import {
  GetWorkspaceData,
} from '@dataspherestudio/shared/common/service/apiCommonMethod.js';
export default {
  directives: {
    clickoutside
  },
  components: {
    userMenu
  },
  data() {
    return {
      sysVersion: process.env.VUE_APP_VERSION,
      isUserMenuShow: false,
      userName: '',
      isHomePage: true,
      isConsolePage: false
    }
  },
  mixins: [mixin],
  created() {
    // 获取进入工作空间的用户权限
    this.getWorkspacesRoles()
  },
  mounted() {
    this.userName = this.getUserName();
  },
  computed: {},
  methods: {
    handleOutsideClick() {
      this.isUserMenuShow = false
    },
    handleUserClick() {
      this.isUserMenuShow = !this.isUserMenuShow
    },
    // 获取进入工作空间的用户权限
    getWorkspacesRoles() {
      return new Promise((resolve, reject) => {
        if (this.$route.query.workspaceId) {
          GetWorkspaceData(this.$route.query.workspaceId || "")
            .then((res) => {
              // 缓存数据，供其他页面判断使用
              storage.set(`workspaceRoles`, res.roles || [], "session");
              // roles主动触发，防止接口请求和sessionstorge之间的时间差导致角色没有及时转换
              eventbus.emit("workspace.change", res.roles || []);
              // 同步改变cookies在请求中的附带
              resolve(true);
              // 改变了cookies通知其他地方
              this.$router.app.$emit("getChangeCookies");
            })
            .catch((err) => {
              reject(err);
            });
        } else {
          resolve(false);
        }
      });
    },
    clearSession() {
      tree.remove('scriptTree');
      tree.remove('hdfsTree');
      tree.remove('hiveTree');
      tree.remove('udfTree');
      tree.remove('functionTree');
      this.$emit('clear-session')
    },
  }
}
</script>
<style lang="scss" scoped src="./index.scss"></style>
