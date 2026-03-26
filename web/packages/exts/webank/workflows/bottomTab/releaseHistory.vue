<template>
  <div class="release-table-wrapper" ref="releaseTableContainer">
    <Table :columns="releaseHistoryCols" :data="releaseHistoryData" :height="tableHeight || 200"></Table>
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
  </div>
</template>
<script>
import axios from "axios";
import api from '@dataspherestudio/shared/common/service/api';
import { ORCHESTRATORMODES } from '@dataspherestudio/shared/common/config/const.js';
import mixin from '@dataspherestudio/shared/common/service/mixin';
import eventbus from "@dataspherestudio/shared/common/helper/eventbus";
export default {
  props: {
    orchestratorId: {
      type: [Number, String],
      default: null
    },
    orchestratorVersionId: {
      type: [Number, String],
      default: null
    }
  },
  mixins: [mixin],
  data() {
    return {
      tableHeight:0,
      releaseHistoryCols: [
        {
          title: 'ID',
          key: 'id'
        },
        {
          title: this.$t('message.ext.webank.Status'),
          key: 'status'
        },
        {
          title: this.$t('message.ext.webank.Description'),
          key: 'recode'
        },
        {
          title: this.$t('message.ext.webank.Publisher'),
          key: 'releaseUser'
        },
        {
          title: this.$t('message.ext.webank.Version'),
          key: 'version'
        },
        {
          title: this.$t('message.ext.webank.Editor'),
          key: 'lastModifyUser'
        },
        {
          title: this.$t('message.ext.webank.PublishTime'),
          key: 'releaseTime'
        },
        {
          title: this.$t('message.ext.webank.Error'),
          key: 'errorMessage',
          width: '180',
          align: 'center',
          render: (h, scope) => {
            return h(
              "span",
              {
                style: {
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis'
                },
                attrs: {
                  title: scope.row.errorMessage
                }
              }, scope.row.errorMessage
            );
          }
        },
        {
          title: this.$t('message.workflow.process.releasehisotry.control'),
          key: 'action',
          width: '180',
          align: 'center',
          render: (h, params) => {
            const that = this
            return h('div', [
              // h('Button', {
              //   props: {
              //     size: 'small',
              //   },
              //   style: {
              //     marginRight: '5px',
              //   },
              //   on: {
              //     click: () => {
              //       this.viewLog(params.row)
              //     }
              //   }
              // }, this.$t('message.workflow.process.releasehisotry.viewlog')),
              h('Button', {
                props: {
                  size: 'small',
                },
                style: {
                  marginRight: '5px',
                },
                on: {
                  click: () => {
                    that.release(params.row)
                  }
                }
              }, this.$t('message.workflow.process.releasehisotry.openversion')),
              h('Button', {
                props: {
                  size: 'small',
                  disabled: params.row.downloadIng
                },
                style: {
                  marginRight: '5px',
                },
                on: {
                  click: () => {
                    that.download(params.row)
                  }
                }
              }, this.$t('message.workflow.process.releasehisotry.download')),
            ]);
          }
        }
      ],
      releaseHistoryData: [],
      page: {
        totalSize: 0,
        sizeOpts: [4, 15, 30, 45],
        pageSize: 4,
        pageNow: 1
      }
    }
  },
  methods: {
    release(row) {
      const obj = {
        appId: row.appId,
        id: this.orchestratorId,
        orchestratorMode: ORCHESTRATORMODES.WORKFLOW,
        readonly: 'true',
        orchestratorVersionId: this.orchestratorVersionId,
        projectID: +this.$route.query.projectID,
        projectName: this.$route.query.projectName,
        version: row.version,
        workspaceId: this.getCurrentWorkspace('id'),
      }
      this.$emit('event-from-ext', {callFn: 'release', params: [obj]})
    },
    download(row) {
      const workspaceId = this.getCurrentWorkspace('id');
      const openWorkList = JSON.parse(sessionStorage.getItem(`work_flow_lists_${workspaceId}`));
      const orchestrator = openWorkList.find(item => item.query && item.query.orchestratorId == this.orchestratorId) || {name: ''};
      const params = {
        workspaceId,
        projectId: +this.$route.query.projectID,
        projectName: this.$route.query.projectName,
        orchestratorId: this.orchestratorId,
        orcVersionId: row.orchestratorVersionId,
        addOrcVersion: false,
        dssLabels: this.getCurrentDsslabels(),
        labels: {
          route: this.getCurrentDsslabels()
        }
      };
      this.$Message.info(this.$t('message.ext.webank.Downloading'))
      this.setDownloadDisable(row, true)
      // 返回结构不一样
      axios
        .get(
          `http://${window.location.host}/api/rest_j/v1/dss/framework/release/exportOrcSqlFile`,
          {
            params,
            responseType: "arraybuffer"
          }
        )
        .then(res => {
          const blob = new Blob([res.data], { type: "application/zip" });
          const url = URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.style.display = "none";
          a.href = url;
          a.setAttribute(
            "download",
            `${this.$route.query.projectName}_${orchestrator.name}_${row.version}`
          );
          const evObj = document.createEvent("MouseEvents");
          evObj.initMouseEvent(
            "click",
            true,
            true,
            window,
            0,
            0,
            0,
            0,
            0,
            false,
            false,
            true,
            false,
            0,
            null
          );
          a.dispatchEvent(evObj);
          setTimeout(() => {
            this.setDownloadDisable(row, false)
          }, 1500);
        })
        .catch(() => {
          this.$Message.error('Export Failed');
        });
    },
    setDownloadDisable(row, dis) {
      this.releaseHistoryData.find(item => {
        if(item.id === row.id) {
          item.downloadIng = dis
        }
      })
      this.releaseHistoryData = [...this.releaseHistoryData]
    },
    getHistoryData() {
      return api.fetch(`${this.$API_PATH.ORCHESTRATOR_PATH}getReleaseHistory`, {
        orchestratorId: this.orchestratorId,
        projectId: +this.$route.query.projectID,
        workspaceId: this.getCurrentWorkspace('id'),
        labels: {route: this.getCurrentDsslabels() },
        currentPage: this.page.pageNow,
        pageSize: this.page.pageSize,
      },'post').then((res) => {
        this.releaseHistoryData = res.releaseDetails || []
        this.page.totalSize = res.totalPage
      }).catch(() => {
        this.loading = false;
      });
    },
    viewLog(row = {}) {
      console.log('row: ', row);
      api.fetch('/filesystem/openLog', {
        path: row.logPath //'hdfs:///appcom/logs/linkis/log/2021-08-26/nodeexecution/stacyyan/753499.log'
      }, 'get').then((rst) => {
        this.isLoading = false;
        if (rst) {
          this.$emit('show-log', rst.log)
        }
      }).catch(() => {
        this.isLoading = false;
      });
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
    onchangeStatus(res) {
      if (res && (res.status.toLowerCase() == 'success' || res.status.toLowerCase() == 'failed')) {
        this.getHistoryData()
      }
    },
    calcTableHeight(curHeight) {
      this.$nextTick(() => {
        if (this.$refs.releaseTableContainer) {
          const containerHeight = curHeight || this.$refs.releaseTableContainer.clientHeight
          this.tableHeight = Math.max(containerHeight - 64, 0)
        }
      })
    }
  },
  mounted() {
    this.getHistoryData()
    eventbus.on('get_publish_status', this.onchangeStatus)
  },
  beforeDestroy() {
    eventbus.off('get_publish_status', this.onchangeStatus)
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
