<!-- 调度中心工作流详情 -->
<template>
  <div class="ScheduleCenterDetailContent">
    <div class="top-nav-bar">
      <span @click="goBack" class="back">
        <Icon type="ios-arrow-back"></Icon>
        {{ $t('message.scheduleCenter.back') }}
      </span>
      <span class="splitor">/</span>
      <span>
        {{$route.query.projectName}}
      </span>
      <span v-if="scheduleCenter.orchestratorName" class="splitor">/</span>
      <span v-if="scheduleCenter.orchestratorName">
        {{scheduleCenter.orchestratorName}}
      </span>
    </div>
    <section class="section">
      <div class="tab-right">
        <Button
          @click="goSchedulis"
          type="primary"
          class="trager"
        >{{$t('message.scheduleCenter.goSchedulis')}}</Button>
      </div>
      <Tabs type="card" :animated="false" :value="tabValue">
        <TabPane :label="$t('message.scheduleCenter.GZL')" name="process">
          <processComponent :query="scheduleCenter"></processComponent>
        </TabPane>
        <TabPane :label="$t('message.scheduleCenter.actionHistory')" name="history">
          <div class="schedulisIframe">
            <iframe
              class="iframeClass"
              :src="schedulislSrc"
              frameborder="0"
              width="100%"
              height="100%"
            />
          </div>
        </TabPane>
        <TabPane :label="$t('message.scheduleCenter.seting')" name="setting">
          <Form
            ref="scheduleParams"
            label-position="top"
            :rules="scheduleParamsRules"
            :model="scheduleParams"
          >
            <Row>
              <i-col span="10" style="margin-right: 200px;">
                <h2 class="title">{{$t('message.scheduleCenter.scheduleSetting')}}</h2>
                <section class="scheduleCenterSet">
                  <FormItem
                    :label="$t('message.scheduleCenter.cycleTime')"
                    prop="scheduleTime"
                    :error="scheduleTimeError"
                  >
                    <Select
                      v-if="oneSelect === 'MIN'"
                      class="margin-right"
                      style="width:80px"
                      v-model="zeroSelect"
                    >
                      <Option
                        v-for="item in zeroList"
                        :value="item.value"
                        :key="item.value"
                      >{{ item.label }}</Option>
                    </Select>
                    <Select
                      style="width:80px"
                      class="margin-right"
                      v-model="oneSelect"
                      @on-change="selectOneChange"
                    >
                      <Option
                        v-for="item in oneList"
                        :value="item.value"
                        :key="item.value"
                      >{{ item.label }}</Option>
                    </Select>
                    <Select
                      class="margin-right"
                      style="width:80px"
                      v-if="towList.length > 0"
                      v-model="towSelect"
                    >
                      <Option
                        v-for="item in towList"
                        :value="item.value"
                        :key="item.value"
                      >{{ item.label }}</Option>
                    </Select>
                    <Select
                      class="margin-right"
                      style="width:80px"
                      v-if="threeList.length > 0"
                      v-model="threeSelect"
                    >
                      <Option
                        v-for="item in threeList"
                        :value="item.value"
                        :key="item.value"
                      >{{ item.label }}</Option>
                    </Select>
                    <Select
                      class="margin-right"
                      style="width:80px"
                      v-if="selectHourAndMinues"
                      v-model="fourSelect"
                    >
                      <Option
                        v-for="item in fourList"
                        :value="item.value"
                        :key="item.value"
                      >{{ item.label }}</Option>
                    </Select>
                    <Select
                      class="margin-right"
                      style="width:80px"
                      v-if="selectHourAndMinues"
                      v-model="fiveSelect"
                    >
                      <Option
                        v-for="item in fiveList"
                        :value="item.value"
                        :key="item.value"
                      >{{ item.label }}</Option>
                    </Select>
                  </FormItem>
                  <!-- <h3 class="time-set-title">{{$t("message.scheduleCenter.failureAlarm")}}</h3> -->
                  <FormItem
                    :label="$t('message.scheduleCenter.alarmUser')"
                    prop="alarmUserEmails"
                    :rules="[{ pattern: /^[a-zA-Z0-9_.@;]+$/, message: this.$t('message.scheduleCenter.emailrules'), trigger: 'change' }]"
                  >
                    <Input
                      v-model="scheduleParams.alarmUserEmails"
                      :placeholder="$t('message.scheduleCenter.inputEmailOrUser')"
                      @on-change="alarmUserChange"
                    />
                  </FormItem>
                  <FormItem :label="$t('message.scheduleCenter.alarmLevel')">
                    <Select
                      class="margin-right"
                      v-model="scheduleParams.alarmLevel"
                      @on-change="selectLevelChange"
                    >
                      <Option v-for="item in levelList" :value="item" :key="item">{{ item }}</Option>
                    </Select>
                  </FormItem>
                </section>
              </i-col>
              <i-col span="10">
                <h2 class="title">{{$t('message.scheduleCenter.powerSetting')}}</h2>
                <section class="scheduleCenterSet">
                  <FormItem :label="$t('message.scheduleCenter.powerModel')">
                    <Select v-model="setData.privModel">
                      <Option :value="0">{{$t('message.scheduleCenter.privacy')}}</Option>
                      <Option :value="1">{{$t('message.scheduleCenter.public')}}</Option>
                    </Select>
                  </FormItem>
                  <FormItem :label="$t('message.scheduleCenter.visiblePerson')">
                    <Select v-model="setData.accessUsers" multiple filterable>
                      <Option v-for="(item, index) in userList" :key="index" :value="item">{{item}}</Option>
                    </Select>
                  </FormItem>
                </section>
              </i-col>
            </Row>
            <FormItem v-if="latestShow" class="submit">
              <Button @click="goSubmit" type="primary">{{$t('message.scheduleCenter.save')}}</Button>
            </FormItem>
          </Form>
        </TabPane>
      </Tabs>
    </section>
  </div>
</template>
<script>
import processComponent from '@dataspherestudio/workflows/module/process'
import util from '@dataspherestudio/shared/common/util/index.js'
import storage from '@dataspherestudio/shared/common/helper/storage'
import api from '@dataspherestudio/shared/common/service/api'
import timeToCronMixin from '@dataspherestudio/shared/common/service/timeToCronMixin.js'
import { GetWorkspaceUserList } from '@dataspherestudio/shared/common/service/apiCommonMethod.js'
export default {
  mixins: [timeToCronMixin],
  data() {
    const scheduleTimeRules = (rule, value, callback) => {
      this.scheduleSettingLoad = false
      callback()
    }
    return {
      levelList: ['INFO', 'MINOR', 'MAJOR', 'CRITICAL'],
      backDisabled: false,
      tabValue: 'process',
      userList: [],
      scheduleParamsRules: {
        scheduleTime: [
          {
            required: true,
            message: this.$t('message.scheduleCenter.requiredScheduleDate'),
            trigger: 'change'
          },
          { validator: scheduleTimeRules, trigger: 'change' }
        ]
      },
      scheduleParams: {
        alarmLevel: '',
        alarmUserEmails: '',
        scheduleTime: ''
      },
      scheduleTimeError: '',
      setData: {
        privModel: 0,
        accessUsers: []
      },
      isLatest: 'true'
    }
  },
  components: {
    processComponent: processComponent.component
  },
  //生命周期 - 创建完成（访问当前this实例）
  created() {
    // 获取所有可供选择的用户
    const scheduleDetail = this.scheduleCenter
    if (scheduleDetail) {
      GetWorkspaceUserList({
        workspaceId: this.$route.query.workspaceId || +scheduleDetail.workspaceId
      }).then(res => {
        this.userList = res.users && res.users.accessUsers
      })
      this.scheduleDetail = scheduleDetail
      this.setDispatch(scheduleDetail.scheduleInfo)
      this.setData.accessUsers = scheduleDetail.accessUsers
      this.setData.privModel = scheduleDetail.privModel
      this.tabValue = scheduleDetail.flag || 'process'
      this.isLatest = scheduleDetail.isLatest
    } else {
      this.goBack()
    }
  },
  computed: {
    latestShow() {
      return this.isLatest === 'true'
    },
    schedulislSrc() {
      return util.replaceHolder(
        this.scheduleDetail.originSchedulisUrl,
        {
          projectName: this.scheduleDetail.projectName,
          projectID: this.scheduleDetail.projectID,
          flowName: this.scheduleDetail.orchestratorName
        }
      )
    },
    scheduleCenter() {
      const params = this.$route.params.appId && this.$route.params
      const last = storage.get('ScheduleCenterDetail_route_params')
      return params || last
    }
  },
  //生命周期 - 挂载完成（访问DOM元素）
  mounted() {},
  watch: {
    showDispatch(val) {
      this.scheduleSettingLoad = val
    }
  },
  methods: {
    statusText(status) {
      if (status === 'SUCCEEDED') {
        return this.$t('message.scheduleCenter.good')
      } else {
        return this.$t('message.scheduleCenter.failed')
      }
    },
    // 跳转Schedulis
    goSchedulis() {
      const url = this.schedulislSrc.replace('%26hideHead%3Dtrue', '')
      util.windowOpen(url)
    },
    // 返回上一页
    goBack() {
      this.$router.go(-1)
    },
    goSubmit() {
      if (!this.scheduleParams.scheduleTime) {
        return (this.scheduleTimeError = this.$t(
          'message.scheduleCenter.requiredScheduleDate'
        ))
      }
      this.dispatchAction()
      this.setOk()
    },
    dispatchAction() {
      api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}scheduleFlow`,
          {
            orchestratorId: this.scheduleDetail.orchestratorId,
            projectName: this.scheduleDetail.projectName,
            projectID: this.scheduleDetail.projectID,
            ...this.scheduleParams
          },
          'post'
        )
        .then(() => {
          this.$Message.success(
            this.$t('message.scheduleCenter.settingSuccess')
          )
        })
        .catch(() => {
          this.scheduleSettingLoad = false
        })
    },
    setOk() {
      api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}setProductWorkflowPriv`,
          {
            ...this.setData,
            projectName: this.scheduleDetail.projectName,
            projectID: this.scheduleDetail.projectID,
            orchestratorId: this.scheduleDetail.orchestratorId,
            workspaceId: +this.scheduleDetail.workspaceId
          },
          'post'
        )
        .then(() => {
          this.$Message.success(
            this.$t('message.scheduleCenter.settingSuccess')
          )
        })
    },
    setDispatch(scheduleParams) {
      if (!scheduleParams) return
      this.scheduleParams = scheduleParams
      this.cronToTime()
    }
  }
}
</script>
<style scoped lang="scss">
@import '@dataspherestudio/shared/common/style/variables.scss';
.breadcrumb {
  display: inline-block;
  margin-left: 20px;
  font-size: $font-size-base;
  vertical-align: middle;
  ::v-deep.ivu-breadcrumb-item-separator {
    @include font-color($title-color, $dark-text-color);
  }
}
.title {
  font-size: $font-size-large;
  flex: none;
  color: $text-title-color;
  @include font-color($text-title-color, $dark-text-color);
  margin: 6px 0;
  padding-left: 12px;
  border-left: 3px solid #2d8cf0;
  line-height: $font-size-large;
  margin-bottom: 25px;
}
.section {
  flex: 1;
  overflow: hidden;
  position: relative;
  .tab-right {
    position: absolute;
    top: 0px;
    right: 0;
    z-index: 5;
    .status-bar {
      margin-right: 15px;
      font-size: $font-size-base;
      @include font-color($text-desc-color, $dark-text-color);
      line-height: 32px;
      display: inline-block;
      vertical-align: middle;
      .point {
        display: inline-block;
        width: 8px;
        height: 8px;
        border-radius: 4px;
      }
      .SUCCEEDED {
        background-color: #52c41a;
      }
      .FAILED {
        background-color: #ff4d4f;
      }
    }
  }
  .scheduleCenterSet {
    margin-left: 25px;
    .margin-right {
      margin-right: 12px;
    }
    ::v-deep .ivu-form-item-label {
      font-size: $font-size-base;
      @include font-color($text-title-color, $dark-text-color);
    }
  }
  .submit {
    text-align: center;
  }
  .ivu-tabs {
    height: 100%;
    ::v-deep.ivu-tabs-content {
      height: calc(100% - 56px);
      .ivu-tabs-tabpane {
        height: 100%;
        overflow: auto;
      }
    }
  }
}
.ScheduleCenterDetailContent {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  padding: 25px 25px 0 25px;
  ::v-deep.ivu-tabs.ivu-tabs-card > .ivu-tabs-bar .ivu-tabs-nav-container {
    height: 40px;
    line-height: 2;
    .ivu-tabs-tab {
      height: 39px;
    }
    .ivu-tabs-tab-active {
      height: 40px;
    }
  }
}
.top-nav-bar {
  margin-bottom: 20px;
  @include font-color(#333, $dark-text-color);
  .back {
    color: $primary-color;
  }
  .splitor {
    margin: 0 2px;
  }
}
</style>
