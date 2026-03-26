<template>
  <div class="main schedule-cener">
    <Row class="search-bar" :gutter="40">
      <Col span="6" class="search-item">
        <span class="lable">{{$t("message.scheduleCenter.name")}}： </span>
        <Input
          v-model="searchName"
          suffix="ios-search"
          class="input"
          :placeholder="$t('message.scheduleCenter.inputWorkflowName')"
          @on-enter="search"
        />
      </Col>
      <Col span="6" class="search-item">
        <span class="lable">{{$t("message.scheduleCenter.status")}}：</span>
        <Select v-model="searchStatus" class="input">
          <Option value="ALL">{{$t("message.scheduleCenter.all")}}</Option>
          <Option value="SUCCEEDED">{{$t("message.scheduleCenter.success")}}</Option>
          <Option value="FAILED">{{$t("message.scheduleCenter.fail")}}</Option>
        </Select>
      </Col>
      <Col span="6" class="search-item">
        <span class="lable">{{$t("message.scheduleCenter.submitter")}}：</span>
        <Input
          v-model="searchCommitter"
          class="input"
          :placeholder="$t('message.scheduleCenter.inputSubmitter')"
        />
      </Col>
      <Col span="6" class="search-item">
        <Button class="search" type="primary" @click="search">{{$t("message.scheduleCenter.find")}}</Button>
      </Col>
    </Row>
    <Table class="table-content" :columns="columns" :data="pageDatalist" :loading="loading">
      <template slot-scope="{row}" slot="orchestratorName">
        <Dropdown placement="right" transfer @on-click="showSet(row, $event)">
          <a class="actionMore">
            <Icon type="md-more" />
          </a>
          <DropdownMenu slot="list">
            <DropdownItem name="setting">{{ $t('message.scheduleCenter.setting') }}</DropdownItem>
            <DropdownItem name="history">{{ $t('message.scheduleCenter.actionHistory') }}</DropdownItem>
          </DropdownMenu>
        </Dropdown>
        <span class="workflow-name" @click.stop="gotoProcess(row)">{{row.orchestratorName}}</span>
      </template>
      <template slot-scope="{row}" slot="scheduleTime">
        <span>{{row.scheduleTime ? row.scheduleTime : ''}}</span>
      </template>
      <template slot-scope="{row}" slot="latestVersion">
        <span class="version-text" @click.stop="showVersion(row)">{{ row.latestVersion }}</span>
      </template>
      <template v-if="row.status" slot-scope="{row}" slot="status">
        <span class="status-bar">
          <i class="point" :class="row.status.split(' ')[2]"></i>
          {{ row.status.split(' ')[2] }}
        </span>
      </template>
      <template slot-scope="{row}" slot="lastUpdateTime">
        <span>{{row.lastUpdateTime}}</span>
      </template>
      <template slot-scope="{row}" slot="labels">
        <span class="label-item" v-for="(item, index) in row.labels" :key="item + index">{{ item }}</span>
      </template>
    </Table>
    <div class="page-bar">
      <Page
        ref="page"
        :total="page.totalSize"
        :page-size-opts="page.sizeOpts"
        :page-size="page.pageSize"
        :current="page.pageNow"
        class-name="page"
        size="small"
        show-total
        show-sizer
        @on-change="change"
        @on-page-size-change="changeSize"
      />
    </div>
    <VersionDetail
      :version-detail-show="versionDetailShow"
      :version-data="versionData"
      @versionDetailShow="versionDetailAction"
      @goto="versionGotoWorkflow"
    ></VersionDetail>
  </div>
</template>
<script>
import api from '@dataspherestudio/shared/common/service/api'
import VersionDetail from '@dataspherestudio/workflows/module/workflow/module/versionDetail.vue'
import storage from '@dataspherestudio/shared/common/helper/storage'
import plugin from '@dataspherestudio/shared/common/util/plugin'
import { GetWorkspaceUserList } from '@dataspherestudio/shared/common/service/apiCommonMethod.js'
export default {
  props: {
  },
  data() {
    return {
      scheduleSettingLoad: false,
      showDispatch: false,
      versionDetailShow: false,
      versionData: [],
      columns: [
        {
          title: this.$t('message.scheduleCenter.workflowName'),
          key: 'orchestratorName',
          minWidth: 150,
          className: 'table-name-column',
        },
        {
          title: this.$t('message.scheduleCenter.Project2'),
          key: 'projectName',
          minWidth: 150,
          className: 'table-name-column'
        },
        {
          title: this.$t('message.scheduleCenter.scheduleLastStatus'),
          key: 'status',
          slot: 'status',
          minWidth: 180
        },
        {
          title: this.$t('message.scheduleCenter.scheduleDate'),
          key: 'scheduleTime',
          minWidth: 150,
          slot: 'scheduleTime',
          sortable: true
        },
        {
          title: this.$t('message.scheduleCenter.period'),
          key: 'scheduleSettings',
          minWidth: 100
        },
        {
          title: this.$t('message.scheduleCenter.newestVersions'),
          key: 'latestVersion',
          minWidth: 100,
          slot: 'latestVersion'
        },

        {
          title: this.$t('message.scheduleCenter.publisher'),
          key: 'releaseUser',
          minWidth: 100
        },
        {
          title: this.$t('message.scheduleCenter.scheduleModifier'),
          key: 'lastUpdater',
          minWidth: 110
        },
        {
          title: this.$t('message.scheduleCenter.lastHandleTime'),
          key: 'lastUpdateTime',
          slot: 'lastUpdateTime',
          minWidth: 150,
          sortable: true
        },
        {
          title: this.$t('message.scheduleCenter.ptime'),
          key: 'publishTime',
          minWidth: 150
        },
        {
          title: this.$t('message.scheduleCenter.desc'),
          key: 'comment',
          minWidth: 150
        },
        {
          title: this.$t("message.scheduleCenter.handle"),
          key: "action",
          minWidth: 370,
          fixed: "right",
          align: "center",
          render: (h, params) => {
            return h("div", this.getButtons(h, params));
          },
        },
      ],
      tableData: [],
      userList: [],
      originUserList: [],
      searchName: '',
      searchStatus: '',
      searchCommitter: '',
      currentRow: {},
      originSchedulisUrl: '',
      page: {
        totalSize: 0,
        sizeOpts: [15, 30, 45],
        pageSize: 15,
        pageNow: 1
      },
      loading: false
    }
  },
  components: {
    VersionDetail,
  },
  created() {
    // 获取所有可供选择的用户
    GetWorkspaceUserList({ workspaceId: +this.$route.query.workspaceId }).then(
      res => {
        this.userList = res.users
      }
    )
    this.getWorkflowData()
  },
  mounted() {
  },
  watch: {
    '$route.query.workspaceId'() {
      this.getWorkflowData()
    },
    showDispatch(val) {
      this.scheduleSettingLoad = val
    },
    tableData(val) {
      this.page.totalSize = val.length
    },
    "$route.query.projectID"() {
      this.search()
    },
  },
  computed: {
    pageDatalist() {
      return this.tableData.filter((item, index) => {
        return (
          (this.page.pageNow - 1) * this.page.pageSize <= index &&
          index < this.page.pageNow * this.page.pageSize
        )
      })
    }
  },
  methods: {
    // 切换分页
    change(val) {
      this.page.pageNow = val
    },
    // 返回上一页
    goBack() {
      this.$router.go(-1)
    },
    // 页容量变化
    changeSize(val) {
      this.page.pageSize = val
      this.page.pageNow = 1
    },
    getButtons(h, params) {
      let cols = [
        h(
          "Button",
          {
            props: {
              type: "primary",
              size: "small",
            },
            style: {
              marginRight: "5px",
            },
            on: {
              click: () => {
                this.showSet(params.row, 'setting');
              },
            },
          },
          this.$t("message.scheduleCenter.setting")
        ),
        h(
          "Button",
          {
            props: {
              type: "primary",
              size: "small",
            },
            style: {
              marginRight: "5px",
            },
            on: {
              click: () => {
                this.showSet(params.row, 'history');
              },
            },
          },
          this.$t("message.scheduleCenter.actionHistory")
        ),
        h(
          "Button",
          {
            props: {
              type: "error",
              size: "small",
            },
            style: {
              marginRight: "5px",
            },
            on: {
              click: () => {
                this.scheduleDelete(params.row);
              },
            },
          },
          this.$t("message.scheduleCenter.deleteDispatch")
        ),
        h(
          "Button",
          {
            props: {
              type: params.row.activeFlag ? "warning" : "primary",
              size: "small"
            },
            on: {
              click: () => {
                this.setFlowSchedule(params.row);
              },
            },
          },
          params.row.activeFlag ? this.$t("message.scheduleCenter.disable") : this.$t("message.scheduleCenter.enable")
        ),
      ]
      plugin.emitHook('scheduler_center_workflow_action', {
        context: this,
        cols,
        h,
        params
      })
      return cols
    },
    // 模糊搜索
    search() {
      // 搜索前先拉下数据，再过滤
      this.getWorkflowData().then(() => {
        let searchStatus = this.searchStatus
        if (this.searchStatus === 'ALL') {
          searchStatus = ''
        }
        this.tableData = this.originUserList.filter(item => {
          return (
            item.orchestratorName.indexOf(this.searchName) > -1 &&
            (item.status||'').indexOf(searchStatus) > -1 &&
            item.releaseUser.indexOf(this.searchCommitter) > -1
          )
        })
      })
    },

    // 获取工作流的数据
    getWorkflowData() {
      this.loading = true
      return api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}getProdOrchestrators`,
          {
            projectId: +(this.$route.query.projectID || -1),
            workspaceId: this.$route.query.workspaceId,
            dssLabel: 'prod'
          },
          'get'
        )
        .then(res => {
          this.originUserList = this.tableData = res.orchestrators
          this.originSchedulisUrl = res.scheduleHistory
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    // 表单操作，执行历史，设置
    showSet(row, name) {
      this.gotoProcess(row, name)
    },
    // 查看工作流版本
    showVersion(row) {
      this.currentRow = row
      this.versionDetailShow = true
      api
        .fetch(
          `dss/framework/project/listAllOrchestratorVersions`,
          {
            projectId: row.projectId,
            orchestratorId: row.orchestratorId,
            dssLabel: 'prod'
          },
          'get'
        )
        .then(res => {
          // 新加tab功能需要工作流名称
          this.versionData = res.orchestratorVersions || []
        })
    },
    // 删除
    scheduleDelete(row) {
      let config = {
        method: "post",
        headers: {
          "Token-User": this.getUserName(),
        },
      };
      let query = {
        projectId: row.projectId,
        orchestratorId: row.orchestratorId,
        orchestratorName: row.orchestratorName
      };

      this.$Modal.confirm({
        title: this.$t("message.scheduleCenter.deleteDispatchTitle", {name: row.orchestratorName}),
        content:
          this.$t("message.scheduleCenter.deleteDispatchMsg"),
        onOk: () => {
          api
            .fetch(
              `dss/framework/project/removeFlowSchedule`,
              query,
              config
            )
            .then(() => {
              this.getWorkflowData(); //重新请求数据获取最新
              this.$Message.success(
                this.$t("message.scheduleCenter.deleteSuccess")
              );
            })
            .catch(() => {});
        },
        onCancel: () => {
          this.$Message.info(this.$t('message.scheduleCenter.clickcancel'));
        },
      });
    },
    async setFlowSchedule(row) {
      let query = {
        orchestratorId: row.orchestratorId,
        projectId: row.projectId,
        orchestratorName: row.orchestratorName,
        activeFlag: !row.activeFlag
      };
      await api
        .fetch(
          `dss/framework/project/setFlowSchedule`,
          query,
          'post'
        )
      this.$Message.success(
        this.$t("message.scheduleCenter.updataSuccess")
      );
      this.getWorkflowData(); //重新请求数据获取最新
    },
    // 调转到工作流编辑页面
    gotoProcess(row, flag = 'process') {
      let data = {
        workspaceId: this.$route.query.workspaceId,
        projectID: row.projectId,
        projectName: row.projectName,
        orchestratorName: row.orchestratorName,
        orchestratorId: row.orchestratorId,
        releaseUser: row.releaseUser,
        lastUpdateTime: row.lastUpdateTime,
        status: row.status,
        latestVersion: row.latestVersion || '',
        privModel: row.flowPriv ? row.flowPriv.privModel : '',
        accessUsers: row.flowPriv ? row.flowPriv.usernames : [],
        scheduleInfo: row.scheduleInfo,
        appId: row.appId,
        originSchedulisUrl: this.originSchedulisUrl,
        flag,
        isLatest: row.isLatest || 'true',
        product: true,
        readonly: 'true'
      }
      storage.set('ScheduleCenterDetail_route_params', data)
      this.$router.push({name: 'ScheduleCenterDetail', query: this.$route.query, params: data})
    },
    versionDetailAction(val) {
      this.versionDetailShow = val
    },
    versionGotoWorkflow(row, index) {
      let data = { ...this.currentRow, ...row}
      data.version = row.version
      data.isLatest = index === 0 ? 'true' : 'false' // 是否是最新版本
      this.gotoProcess(data)
    }
  }
}
</script>
<style lang="scss" scoped>
@import '@dataspherestudio/shared/common/style/variables.scss';
.main.schedule-cener {
  padding: 25px;
  .title {
    font-size: $font-size-large;
    font-weight: 600;
    display: flex;
    align-items: center;
    .back {
      margin-right: 20px;
    }
  }
  .search-bar {
    .search-item {
      display: flex;
      justify-content: flex-start;
      align-items: center;
      font-size: $font-size-base;
      .lable {
        flex-basis: 120px;
        text-align: center;
      }
    }
    .search {
      margin-right: 25px;
    }
  }
  .table-content {
    margin-top: 25px;
    ::v-deep .ivu-table {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.65);
    }
    .latestVersion {
      padding: 0 10px;
      background-color: #169bd5;
      border-radius: $border-radius-small;
      overflow: hidden;
      display: inline-block;
    }
    .label-item {
      display: inline-block;
      font-size: 12px;
      background-color: #f3f3f3;
      line-height: 20px;
      border-radius: 10px;
      padding: 0 10px;
      margin-right: 5px;
    }
    .version-text {
      font-size: 14px;
      color: #2e92f7;
      cursor: pointer;
    }
    .status-bar {
      margin-right: 15px;
      font-size: $font-size-base;
      line-height: 32px;
      display: inline-block;
      vertical-align: middle;
      .point {
        display: inline-block;
        width: 8px;
        height: 8px;
        border-radius: 4px;
        margin-right: 5px;
      }
      .SUCCEEDED {
        background-color: #52c41a;
      }
      .FAILED {
        background-color: #ff4d4f;
      }
    }
  }
  td.table-name-column {
    .workflow-name {
      color: #2e92f7;
      cursor: pointer;
    }
  }
  td.table-project-column {
    color: $success-color;
  }
  .page-bar {
    text-align: center;
    padding: 10px 0;
  }
}
.actionMore {
  font-size: 1rem;
  color: #000;
  display: inline-block;
  margin-right: 10px;
}
</style>
