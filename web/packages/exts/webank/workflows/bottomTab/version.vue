<template>
  <div class="version-tab">
    <Split v-model="split" min="520">
      <div class="left-list" slot="left">
        <Form  class="search-head" label-position="left" :label-width="48" inline>
          <Form-item prop="desc" :label="$t('message.ext.webank.Description:')">
            <Input type="text" v-model="formValue.desc" style="width:100px" :placeholder="$t('message.ext.webank.Please')" />
          </Form-item>
          <Form-item prop="user" :label="$t('message.ext.webank.User:')">
            <Select v-model="formValue.user" clearable style="width:100px">
              <Option
                v-for="(item, index) in users"
                :key="index"
                :value="item"
              >{{item}}</Option>
            </Select>
          </Form-item>
          <Form-item prop="date" :label="$t('message.ext.webank.Time:')">
            <DatePicker
              :transfer="true"
              class="datepicker"
              :options="shortcutOpt"
              v-model="formValue.date"
              type="daterange"
              placement="bottom-start"
              style="width: 120px"
              :editable="false"/>
          </Form-item>
        </Form>
        <div class="list" :style="{ height: tableHeight - 72 + 'px' }" @click="viewVersionDiff">
          <div class="list-item" v-for="(item, index) in versionLists" :key="item.updateTime+'_'+index" :data-index="index" :class="{active: currentV && currentV.id === item.id}">
            <Icon type="md-git-commit" class="item-icon"/>
            <div class="list-item-field item-version" :title="index === 0 && page.pageNow === 1 ? $t('message.ext.webank.editing') : item.version">
              {{index === 0 && page.pageNow === 1 ? $t('message.ext.webank.editing') : item.version}}
            </div>
            <div class="list-item-field item-date">{{item.releaseTime}}</div>
            <div class="list-item-field item-user">{{item.releaseUser}}</div>
            <div class="list-item-field item-desc" :title="item.recode">{{item.recode}}</div>
          </div>
          <div v-if="versionLists.length < 1" class="no-data">{{ $t('message.ext.webank.Nodata') }}</div>
        </div>
        <Page
          v-if="versionLists.length"
          :total="page.totalSize"
          :page-size-opts="page.sizeOpts"
          :page-size="page.pageSize"
          :current="page.pageNow"
          class-name="page version-page-bar"
          size="small"
          show-total
          show-sizer
          @on-change="change"
          @on-page-size-change="changeSize" />
      </div>
      <div class="right-list" slot="right" ref="versionTableContainer">
        <Table :columns="cols" :data="versionDiffs" width="100%" :height="tableHeight || 230" :loading="isLoading" :no-data-text="$t('message.ext.webank.Nodiff')">
          <template slot-scope="{row}" slot="child_nodes">
            <span class="relate-nodes" :title="row.child_nodes">{{row.child_nodes}}</span>
          </template>
          <template slot-scope="{row}" slot="parent_nodes">
            <span class="relate-nodes" :title="row.parent_nodes">{{row.parent_nodes}}</span>
          </template>
        </Table>
      </div>
    </Split>
    <Modal
      v-model="showCompare"
      width="80%"
      :footer-hide="true"
      :closable="!fullscreen"
      :title="`${$t('message.ext.webank.Versioncomparison')} (${versionDetail.appName }/${versionDetail.nodeName })`">
      <div class="verion-modal-head">
        <div class="left">{{versionDetail.leftVersion}}</div>
        <div class="middle"></div>
        <div class="right">{{versionDetail.rightVersion}}</div>
      </div>
      <Tabs
        v-model="curTab">
        <TabPane
          name="content"
          :label="$t('message.ext.webank.scriptcontent')"
        />
        <TabPane
          name="basic"
          :label="$t('message.ext.webank.nodebasic')"
        />
      </Tabs>
      <we-editor-compare
        :style="'height:' + editorHeight"
        :value="compareDetail.right"
        :original="compareDetail.left"
        :diffEditor="true"
        :readOnly="true"
        @full-screen-change="onchangeFull"
      />
    </Modal>
  </div>
</template>
<script>
import moment from 'moment';
import api from '@dataspherestudio/shared/common/service/api';
import mixin from '@dataspherestudio/shared/common/service/mixin';

export default {
  props: {
    orchestratorId: {
      type: [Number, String],
      default: null
    },
    appId: {
      type: [Number, String]
    }
  },
  data() {
    return {
      tableHeight:0,
      editorHeight: '400px',
      isLoading: false,
      showCompare: false,
      curTab: "content",
      versionDiffs: [],
      cols: [{
        title: this.$t('message.ext.webank.Name'),
        minWidth: 90,
        key: 'nodeName'
      },{
        title: this.$t('message.ext.webank.Status'),
        width: '65',
        key: 'status',
        render: (h, scope) => {
          const statusMap = {
            add: {
              color: '#52C41A',
              text: this.$t('message.ext.webank.Add')
            },
            delete: {
              color: '#FF4D4F',
              text: this.$t('message.ext.webank.Delete')
            },
            modify: {
              text: this.$t('message.ext.webank.Modify')
            }
          }
          const it = statusMap[scope.row.status] || {
          }
          return h(
            "span",
            {
              style: { color: it.color || ''}
            },
            it.text|| ''
          );
        }
      },{
        title: this.$t('message.ext.webank.Upstream'),
        minWidth: 90,
        key: 'parent_nodes',
        slot: 'parent_nodes'
      },{
        title: this.$t('message.ext.webank.Downstream'),
        minWidth: 90,
        key: 'child_nodes',
        slot: 'child_nodes'
      },{
        title: this.$t('message.ext.webank.Editor'),
        minWidth: 90,
        key: 'updateUser'
      },{
        title: this.$t('message.ext.webank.Last'),
        width: 145,
        key: 'updateTime'
      },{
        title: this.$t('message.workflow.process.exechisotry.control'),
        key: 'action',
        minWidth: 90,
        align: 'center',
        render: (h, params) => {
          return h('div', [
            h('Button', {
              props: {
                size: 'small',
              },
              style: {
                marginRight: '5px',
              },
              on: {
                click: () => {
                  this.showCompareModel(params.row)
                }
              }
            }, this.$t('message.ext.webank.Versioncomparison')),
          ]);
        }
      }],
      versionLists: [],
      versionDetail: {
        appName: '',
        nodeName: '',
        left: '',
        right: '',
        leftParams: '',
        rightParams: '',
        leftVersion: '',
        rightVersion: ''
      },
      formValue: {
        desc: '',
        user: '',
        date: ''
      },
      users: [],
      split: 0.45,
      page: {
        totalSize: 0,
        sizeOpts: [5, 10, 20, 50],
        pageSize: 5,
        pageNow: 1
      },
      shortcutOpt: {
        shortcuts: [
          {
            text: this.$t('message.workflow.shortcuts.week'),
            value() {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              return [start, end];
            },
          },
          {
            text: this.$t('message.workflow.shortcuts.month'),
            value() {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              return [start, end];
            },
          },
          {
            text: this.$t('message.workflow.shortcuts.threeMonths'),
            value() {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
              return [start, end];
            },
          },
        ],
      },
      currentV: null,
      fullscreen: false
    }
  },
  filters: {
    formatTime(value){
      return  moment.unix(value / 1000).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  computed: {
    compareDetail() {
      if (this.curTab === 'content') {
        return {
          left: this.versionDetail.left,
          right: this.versionDetail.right
        }
      } else if(this.curTab === 'basic') {
        return {
          left: this.versionDetail.leftParams,
          right: this.versionDetail.rightParams
        }
      }
      return {
        left: '',
        right: ''
      }
    }
  },
  mixins: [mixin],
  watch: {
    formValue: {
      handler () {
        this.getVerionList()
      },
      deep: true
    }
  },
  methods: {
    showCompareModel(row) {
      this.editorHeight = window.innerHeight * 0.6 > 350 ?  window.innerHeight * 0.6 + 'px' : '350px'
      this.showCompare = true
      this.getScriptContent(row, row.resourceList && row.resourceList[0], 'left')
      this.getScriptContent(row, row.compareResourceList && row.compareResourceList[0], 'right')
    },
    getDiffList(secondVersionId, firstVersionId) {
      if (firstVersionId === secondVersionId) {
        return this.$Message.warning(this.$t('message.ext.webank.Same'));
      }
      const params = {
        secondVersionId,
        labels: {
          route: this.getCurrentDsslabels()
        }
      }
      if (firstVersionId !== undefined) {
        params.firstVersionId = firstVersionId
      }
      const lastPage = this.page.pageNow * this.page.pageSize >= this.page.totalSize
      if (secondVersionId === undefined || (lastPage && firstVersionId === undefined)) {
        this.versionDiffs = [];
        return this.$Message.warning(this.$t('message.ext.webank.Noverion'));
      } else {
        this.isLoading = true
        api.fetch(`${this.$API_PATH.ORCHESTRATOR_PATH}compareOrchestrator`, params, 'post').then((rst) => {
          this.isLoading = false;
          if (rst) {
            this.versionDiffs = (rst.list || []).map(it => {
              it.updateTime = moment(it.updateTime).format('YYYY-MM-DD HH:mm:ss')
              it.child_nodes = (it.childNodeList || []).map( it => {
                return it.nodeName
              }).join(',')
              it.parent_nodes = (it.parentNodeList || []).map( it => {
                return it.nodeName
              }).join(',')
              return it
            })
          }
        }).catch(() => {
          this.isLoading = false;
        });
      }
    },
    viewVersionDiff(e) {
      const index = ( e.target.dataset.index || e.target.parentNode.dataset.index ) - 0
      const item = this.versionLists[index]
      const prevVersion = this.versionLists[index+1]
      if (item) {
        this.currentV = item
        this.getDiffList(item.orchestratorVersionId,  prevVersion && prevVersion.orchestratorVersionId)
      }
    },
    getVerionList() {
      const params = {
        orchestratorId: this.orchestratorId,
        projectId: +this.$route.query.projectID,
        workspaceId: this.getCurrentWorkspace('id'),
        labels: {route: this.getCurrentDsslabels() },
        currentPage: this.page.pageNow,
        pageSize: this.page.pageSize
      }
      if (this.formValue.user) {
        params.releaseUser = this.formValue.user
      }
      if (this.formValue.desc) {
        params.comment = this.formValue.desc
      }
      if (this.formValue.date[0] && this.formValue.date[1]) {
        params.startTime = moment(this.formValue.date[0]).format('YYYY-MM-DD')
        params.endTime = moment(this.formValue.date[1]).format('YYYY-MM-DD')
      }
      api.fetch(`${this.$API_PATH.ORCHESTRATOR_PATH}getOrchestratorVersionList`, params, 'post').then((res) => {
        this.versionLists = res.releaseDetails || [];
        this.page.totalSize = res.totalPage
        if (this.versionLists.length > 1) {
          this.currentV = this.versionLists[0]
          const prev = this.versionLists[1]
          this.getDiffList(this.currentV.orchestratorVersionId,  prev && prev.orchestratorVersionId)
        }
      });
    },
    getUsers() {
      const params = {
        orchestratorId: this.orchestratorId,
        labels: {
          route: this.getCurrentDsslabels()
        }
      }
      api.fetch(`${this.$API_PATH.ORCHESTRATOR_PATH}getOrchestratorVersionUserList`, params, 'post').then(res => {
        this.users = res.releaseUserList || []
      })
    },
    getScriptContent(row, versionItem, panel) {
      this.versionDetail.appName = row.appName;
      this.versionDetail.nodeName = row.nodeName;
      this.versionDetail.leftVersion = row.firstOrcVersion;
      this.versionDetail.rightVersion = row.secondOrcVersion;
      this.versionDetail.leftParams = row.nodeContent ? JSON.stringify(JSON.parse(row.nodeContent), null, 4) : '';
      this.versionDetail.rightParams = row.compareNodeContent ? JSON.stringify(JSON.parse(row.compareNodeContent), null, 4): '';
      if (versionItem) {
        const params = {
          fileName: versionItem.fileName,
          resourceId: versionItem.resourceId,
          version: versionItem.version,
          creator: '',
          projectName: this.$route.query.projectName || ''
        };
        api.fetch('/filesystem/openScriptFromBML', params, 'get').then((res) => {
          this.versionDetail[panel] = res.scriptContent
        });
      } else {
        this.versionDetail[panel] = '';
      }
    },
    // 切换分页
    change(val) {
      this.page.pageNow = val;
      this.getVerionList();
    },
    // 页容量变化
    changeSize(val) {
      this.page.pageSize = val;
      this.page.pageNow = 1;
      this.getVerionList();
    },
    onchangeFull(v) {
      this.fullscreen = v
    },
    calcTableHeight(curHeight) {
      this.$nextTick(() => {
        if (this.$refs.versionTableContainer) {
          const containerHeight = curHeight || this.$refs.versionTableContainer.clientHeight
          this.tableHeight = Math.max(containerHeight - 34, 0)
        }
      })
    }
  },
  mounted() {
    this.getUsers()
  }
}
</script>

<style lang="scss" scoped>
.search-head {
  margin: 5px 5px 0 5px
}
.list {
  height: 145px;
  overflow-y: auto;
  .list-item {
    height: 28px;
    line-height: 28px;
    border-top: 1px solid #e6e6e6;
    cursor: pointer;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    &.active {
      background-color: #ebf7ff75
    }
    &:last-of-type {
      border-bottom: 1px solid #e6e6e6;
    }
  }
  .list-item-field {
    display: inline-block;
    text-align: left;
    padding: 0 10px
  }
  .item-version {
    width: 18%;
    text-overflow: ellipsis;
    overflow: hidden;
    vertical-align: top;
  }
  .item-date {
    width: 22%
  }
  .item-user {
    width: 20%
  }
  .item-desc {
    width: 30%;
    text-overflow: ellipsis;
    overflow: hidden;
    vertical-align: top;
  }
  .item-icon {
    color: rgb(34, 194, 114);
    transform: scale(1.2,1.58) rotate(90deg);
    font-size: 20px;
  }
  .no-data {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    padding-top: 20px;
  }
}
.version-page-bar {
  text-align: center;
  height: 30px;
  padding-top: 4px;
}
.relate-nodes {
  word-break: normal;
  text-overflow: ellipsis;
  overflow: hidden;
  width: 100%;
  display: inline-block;
}
.verion-modal-head {
  display: flex;
  padding-bottom: 5px;
  .left {
    text-align: right;
    flex: 1
  }
  .middle {
    flex: .5;
    margin: 0 20px;
    height: 10px;
    border-bottom: 2px solid #57a3f3;
  }
  .right {
    text-align: left;
    flex: 1
  }
}
</style>
<style lang="css">
.left-list .ivu-form-inline .ivu-form-item {
  margin-bottom: 0px;
}
.version-tab .right-list .ivu-table-cell {
 padding-left: 12px;
 padding-right: 12px;
}
</style>
