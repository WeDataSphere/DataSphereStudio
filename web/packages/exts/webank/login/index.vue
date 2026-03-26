<template>
  <div class="login" @keyup.enter.stop.prevent="handleSubmit('loginForm')">
    <i class="login-bg" />
    <div class="login-main" :class="{ sysuer: userType == 1 }">
      <span class="login-title">{{
        $t('message.common.login.loginTitle', { app_name: $APP_CONF.app_name })
      }}</span>
      <Tabs @on-click="changeUserType" :value="userType">
        <TabPane :label="$t('message.login.normalUser')" name="0"></TabPane>
        <TabPane :label="$t('message.login.systemUser')" name="1"></TabPane>
      </Tabs>
      <Form ref="loginForm" :model="loginForm" :rules="ruleInline">
        <FormItem prop="user">
          <div class="label">{{ $t('message.common.dss.Username') }}</div>
          <Input
            v-model="loginForm.user"
            type="text"
            :placeholder="$t('message.common.login.userName')"
          />
        </FormItem>
        <FormItem prop="password">
          <div class="label">{{ $t('message.common.dss.Password') }}</div>
          <Input
            v-model="loginForm.password"
            type="password"
            :placeholder="$t('message.common.dss.inputPasswordNext')"
          />
        </FormItem>
        <FormItem v-if="userType == 1" prop="proxyUser">
          <div class="label">{{ $t('message.login.proxyUser') }}</div>
          <Input
            v-model="loginForm.proxyUser"
            type="text"
            :placeholder="$t('message.login.proxyUserPlaceholder')"
          />
        </FormItem>
        <FormItem>
          <Checkbox
            v-model="rememberUserNameAndPass"
            class="remember-user-name"
          >{{ $t('message.common.login.remenber') }}
          </Checkbox>
          <Button
            :loading="loading"
            type="primary"
            long
            @click="handleSubmit('loginForm')"
          >{{ $t('message.common.login.login') }}
          </Button>
        </FormItem>
      </Form>
    </div>
  </div>
</template>
<script>
import api from '@dataspherestudio/shared/common/service/api'
import storage from '@dataspherestudio/shared/common/helper/storage'
import mixin from '@dataspherestudio/shared/common/service/mixin'
import { db } from '@dataspherestudio/shared/common/service/db/index.js'
import { config } from '@dataspherestudio/shared/common/config/db.js'
import JSEncrypt from 'jsencrypt'
import util from '@dataspherestudio/shared/common/util/'
import tab from '@/scriptis/service/db/tab.js'
import eventbus from '@dataspherestudio/shared/common/helper/eventbus'
import plugin from '@dataspherestudio/shared/common/util/plugin'
import tree from '@dataspherestudio/scriptis/service/db/tree.js'

export default {
  data() {
    return {
      loading: false,
      loginForm: {
        user: '',
        proxyUser: '',
        password: '',
      },
      userType: '0',
      ruleInline: {
        user: [
          {
            required: true,
            message: this.$t('message.common.login.userName'),
            trigger: 'blur',
          },
          {
            required: true,
            validator: (rule, value, callback) => {
              value = value.trim()
              if (/^hduser/i.test(value) || /^hadoop/i.test(value)) {
                callback(new Error(this.$t('message.login.hduserForbidden')))
              }
              return callback()
            },
            message: this.$t('message.login.hduserForbidden'),
            trigger: 'blur',
          },
        ],
        proxyUser: [
          { required: true, message: this.$t('message.login.proxyUserPlaceholder'), trigger: 'blur' },
        ],
        password: [
          {
            required: true,
            message: this.$t('message.common.login.password'),
            trigger: 'blur',
          },
        ],
      },
      rememberUserNameAndPass: false,
      publicKeyData: null,
    }
  },
  mixins: [mixin],
  created() {
    let userNameAndPass = storage.get('saveUserNameAndPass', 'local')
    if (userNameAndPass) {
      this.rememberUserNameAndPass = true
      this.loginForm.user = userNameAndPass.split('&')[0]
      this.loginForm.password = userNameAndPass.split('&')[1]
    }
    this.getPublicKey()
    // 已登录跳转去首页
    // 未登录停留再登陆页面
    let baseinfo = storage.get('baseInfo', 'local')
    if (baseinfo) {
      this.getIsAdmin(() => {
        window.username = baseinfo.username
        this.afterLogin()
      })
    }
  },
  mounted() {
    storage.set('close_db_table_suggest', true)
    storage.remove('scriptis_execute_req_log')
    storage.remove('all-db-tables-length', 'local')
    const workspaceId = this.getCurrentWorkspaceId()
    sessionStorage.removeItem(`work_flow_lists_${workspaceId}`)
    this.checkChromeVersion()
  },
  methods: {
    logout() {
      api.fetch('/user/logout', {}).then(() => {
        this.$emit('clear-session')
        storage.set('need-refresh-proposals-hql', true)
        storage.set('need-refresh-proposals-python', true)
        this.$router.push({ path: '/login' })
      })
    },
    // 获取登录后的url调转
    getPageHomeUrl() {
      const currentModules = util.currentModules()
      return api
        .fetch(
          `${this.$API_PATH.WORKSPACE_PATH}getWorkspaceHomePage`,
          {
            micro_module: currentModules.microModule || 'dss',
          },
          'get'
        )
        .then((res) => {
          storage.set('noWorkSpace', false, 'local')
          return res.workspaceHomePage
        })
        .catch((e) => {
          storage.set('noWorkSpace', true, 'local')
          this.logout()
          throw e
        })
    },
    // 获取公钥接口
    getPublicKey() {
      api.fetch('/user/publicKey', 'get').then((res) => {
        this.publicKeyData = res
      })
    },
    handleSubmit(name) {
      this.$refs[name].validate(async (valid) => {
        if (valid) {
          this.loading = true
          if (!this.rememberUserNameAndPass) {
            storage.remove('saveUserNameAndPass', 'local')
          }
          // this.loginForm.user = this.loginForm.user.toLocaleLowerCase();
          // 需要判断是否需要给密码加密
          let password = this.loginForm.password
          let params = {}
          if (this.publicKeyData && this.publicKeyData.enableLoginEncrypt) {
            const key = `-----BEGIN PUBLIC KEY-----${this.publicKeyData.publicKey}-----END PUBLIC KEY-----`
            const encryptor = new JSEncrypt()
            encryptor.setPublicKey(key)
            password = encryptor.encrypt(this.loginForm.password)
            params = {
              userName: this.loginForm.user,
              password,
            }
          } else {
            params = {
              userName: this.loginForm.user,
              password,
            }
          }
          // 登录清掉本地缓存
          // 保留Scripts页面打开的tab页面
          // 连续两次退出登录后，会导致数据丢失，所以得判断是否已存切没有使用
          let tabs = (await tab.get()) || []
          const tablist = storage.get(this.loginForm.user + 'tabs', 'local')
          if (!tablist || tablist.length <= 0) {
            storage.set(this.loginForm.user + 'tabs', tabs, 'local')
          }
          Object.keys(config.stores).map((key) => {
            db.db[key].clear()
          })
          let rst;
          let loginSucc  = false;
          try {
            rst = await api.fetch(`/user/login`, params)
            this.loading = false
            loginSucc = true;
            // 保存用户名
            if (this.rememberUserNameAndPass) {
              storage.set(
                'saveUserNameAndPass',
                `${this.loginForm.user}&${this.loginForm.password}`,
                'local'
              )
            }
            window.username = this.loginForm.user
            if ( window.$Wa) {
              window.$Wa.track('user_login', {
                userName: window.username,
                timestamp: Date.now()
              })
            }
           
            if (rst) {
              // 跳转去旧版
              if (rst.redirectLinkisUrl) {
                location.href = rst.redirectLinkisUrl
                return
              }
              this.baseInfo = { username: this.loginForm.user }
              storage.set('baseInfo', this.baseInfo, 'local')
              this.getIsAdmin()
              storage.set('showLoginNotice', true, 'session')
              if (this.userType == 1) {
                await this.setProxyUser()
                this.baseInfo.proxyUserName = this.loginForm.proxyUser
                storage.set('baseInfo', this.baseInfo, 'local')
                this.afterLogin()
              } else {
                this.afterLogin()
              }
              this.$Message.success(
                this.$t('message.common.login.loginSuccess')
              )
            }
          } catch (error) {
            console.error(error)
            if (loginSucc) {
              this.logout()
            }
            this.loading = false
          }
        } else {
          this.$Message.error(this.$t('message.common.login.vaildFaild'))
        }
      })
    },
    // 清楚本地缓存
    clearSession() {
      storage.clear()
    },
    getIsAdmin(cb) {
      api.fetch(`/jobhistory/governanceStationAdmin`, {}, 'get').then((rst) => {
        if (cb) {
          cb()
        } else {
          this.baseInfo = { username: this.loginForm.user, isAdmin: rst.admin }
          storage.set('baseInfo', this.baseInfo, 'local')
        }
      })
    },
    async afterLogin() {
      // 登录之后需要获取当前用户的调转首页的路径
      const homePageRes = await this.getPageHomeUrl()
      eventbus.emit('watermark.refresh')
      const all_after_login = await plugin.emitHook('after_login', {
        context: this,
        homePageRes,
      })
      if (all_after_login.length) {
        // 有hook返回则hook处理
      } else {
        this.$router.replace({ path: homePageRes.homePageUrl })
      }
    },
    checkChromeVersion() {
      let arr = navigator.userAgent.split(' ')
      let chromeVersion = ''
      for (let i = 0; i < arr.length; i++) {
        if (/chrome/i.test(arr[i])) chromeVersion = arr[i]
      }
      let showversionTip = false
      if (chromeVersion) {
        chromeVersion = Number(chromeVersion.split('/')[1].split('.')[0])
        showversionTip = chromeVersion < 78
      } else {
        showversionTip = true
      }
      const hasTip = storage.get('chrome-version-tip', 'local')
      if (showversionTip && !hasTip) {
        const link = `，<a href="${this.$APP_CONF.handbook}${this.$APP_CONF.update_chrome}">${this.$t(
          'message.common.dss.guide'
        )}</a>`
        const contact = `，${this.$t('message.common.dss.contactadmin')}`
        this.$Modal.confirm({
          title: this.$t('message.common.dss.Prompt'),
          cancelText: this.$t('message.common.dss.noPrompt'),
          onCancel: () => {
            storage.set('chrome-version-tip', true, 'local')
          },
          content: `${
            chromeVersion
              ? this.$t('message.common.dss.currentbrower') +
                chromeVersion +
                '，'
              : ''
          }
            ${this.$t('message.common.dss.Recommend')}Chrome 78+ ${
  this.$APP_CONF.update_chrome ? link : contact
}`,
        })
      }
    },
    changeUserType(v) {
      this.userType = v;
      this.$refs['loginForm'].resetFields()
    },
    setProxyUser() {
      // 设置代理用户
      storage.set('shareRootPath', '');
      storage.set('hdfsRootPath', '');
      tree.remove('scriptTree');
      tree.remove('hdfsTree');
      tree.remove('hiveTree');
      tree.remove('udfTree');
      tree.remove('functionTree');
      storage.remove(this.loginForm.user + "tabs", "local")
      return api.fetch(
        `/dss/scriptis/proxy/setProxyUser`,
        {
          userName: this.loginForm.user,
          proxyUserName: this.loginForm.proxyUser,
        },
        'post'
      )
    }
  },
}
</script>
<style lang="scss">
@import '@dataspherestudio/shared/common/style/variables.scss';
.login {
  position: $absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background-color: $body-background;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  .login-bg {
    position: $absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    background: url('../../../dss/assets/images/loginbgc.svg') no-repeat;
    background-position: 55% 40%;
    background-color: #001c40;
  }
  .login-main {
    width: 450px;
    height: 427px;
    background-color: $body-background;
    margin-right: 10.5%;
    padding: 25px 50px;
    border-radius: 6px;
    box-shadow: 2px 2px 40px 0px rgba(0, 0, 0, 0.9);
    z-index: $zindex-spin;
    &.sysuer {
      height: 487px;
    }
    .login-title {
      font-size: 20px;
      margin-bottom: 10px;
      display: block;
      text-align: center;
      color: #044b93;
      font-weight: 600;
    }
    .label {
      font-size: 14px;
      color: #333;
    }
    input {
      border: none;
      border-radius: 4px;
      padding-left: 15px;
      color: $input-color;
      height: 40px;
      border: 1px solid #dee4ec;
      background-color: $body-background !important;
    }
    button {
      height: 40px;
      background-color: $btn-primary-bg;
      box-shadow: 0px 5px 10px 0px $shadow-color;
      border-color: $btn-primary-bg;
    }
  }
  .remember-user-name {
    margin: 0 0 15px 10px;
    color: #333 !important;
  }
  .ivu-form-item {
    margin-bottom: 14px;
  }
  .ivu-tabs-nav div.ivu-tabs-tab {
    color: #333;
    &:hover {
      color: $primary-color;
    }
    &.ivu-tabs-tab-active {
      color: $primary-color;
    }
  }
}
</style>
