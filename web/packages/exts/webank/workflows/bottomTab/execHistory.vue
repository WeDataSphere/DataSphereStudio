<template>
  <div class="exec-table-wrapper" ref="execTableContainer">
    <Table :columns="execHistoryCols" :data="execHistoryData" :height="tableHeight || 200"></Table>
    <div class="version-page-bar">
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
        @on-page-size-change="changeSize" />
    </div>
  </div>
</template>
<script>
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
  mixins: [mixin],
  data() {
    return {
      tableHeight:0,
      execHistoryCols: [
        {
          title: 'ID',
          key: 'id'
        },
        {
          title: this.$t('message.ext.webank.Exestatus'),
          key: 'status',
          render: (h, scope) => {
            const statusMap = {
              Succeed: {
                text: 'Succeed',
                color: '#52C41A'
              },
              Failed: {
                text: 'Failed',
                color: '#FF4D4F'
              }
            }
            const it = statusMap[scope.row.status] || {
              text: scope.row.status
            }
            return h(
              "span",
              {
                style: { color: it.color || ''}
              },
              it.text
            );
          }
        },
        {
          title: this.$t('message.ext.webank.Execution'),
          key: 'duration'
        },
        {
          title: this.$t('message.ext.webank.Startup'),
          key: 'startTime'
        },
        {
          title: this.$t('message.ext.webank.Errorcode'),
          key: 'errorCode'
        },
        {
          title: this.$t('message.ext.webank.key'),
          key: 'errorMessage',
          render: (h, scope) => {
            const statusMap = {
              Succeed: {
                color: '#52C41A'
              },
              Failed: {
                color: '#FF4D4F'
              }
            }
            const it = statusMap[scope.row.status] || {
            }
            return h(
              "span",
              {
                style: { color: it.color || ''}
              },
              scope.row.errorMessage
            );
          }
        },
        {
          title: this.$t('message.workflow.process.exechisotry.control'),
          key: 'action',
          width: '215',
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
                    this.viewLog(params.row)
                  }
                }
              }, this.$t('message.workflow.process.exechisotry.viewlog')),
            ]);
          }
        }
      ],
      execHistoryData: [],
      page: {
        totalSize: 0,
        sizeOpts: [4, 15, 30, 45],
        pageSize: 4,
        pageNow: 1
      },
    }
  },
  methods: {
    getHistoryData() {
      return api.fetch(`${this.$API_PATH.ORCHESTRATOR_PATH}getExecutionHistory`, {
        orchestratorId: this.orchestratorId,
        appId: this.appId,
        projectId: +this.$route.query.projectID,
        workspaceId: this.getCurrentWorkspace('id'),
        labels: {route: this.getCurrentDsslabels() },
        currentPage: this.page.pageNow,
        pageSize: this.page.pageSize,
      },'post').then((res) => {
        this.execHistoryData = res.executionHistory
        this.page.totalSize = res.totalPage
      }).catch(() => {
        this.loading = false;
      });
    },
    viewLog(row = {}) {
      this.$emit('event-from-ext', {callFn: 'showLogPanel', params: [row.logPath]})
    },
    // 切换分页
    change(val) {
      this.page.pageNow = val;
      this.getHistoryData();
    },
    // 页容量变化
    changeSize(val) {
      this.page.pageSize = val;
      this.page.pageNow = 1;
      this.getHistoryData();
    },
    calcTableHeight(curHeight) {
      this.$nextTick(() => {
        if (this.$refs.execTableContainer) {
          const containerHeight = curHeight || this.$refs.execTableContainer.clientHeight
          this.tableHeight = Math.max(containerHeight - 64, 0)
        }
      })
    }
  },
  mounted() {
    this.getHistoryData()
  }
}
</script>

<style lang="scss" scoped>
  .version-page-bar {
    text-align: center;
    height: 30px;
    padding-top: 4px;
  }
</style>
