<template>
  <div class="table-detail">
    <Tabs
      @on-click="getDatalist"
      type="card"
      class="table-detail-tabs"
      :class="currentTab !== 4 ? 'table-detail-tabs-overflow-auto' : ''"
      :animated="false">
      <TabPane
        :label="item"
        v-for="(item, index) in tabList"
        :key="index"
        style="height:100%;">
        <basic
          v-if="item === $t('message.scripts.tableDetails.BZBSX')"
          :table-info="work.data"
          :meta-data="metadata"
          :table-detail-info="tableDetailInfo"
          :en-env="isEnEnv"></basic>
        <field
          v-if="item === $t('message.scripts.tableDetails.BZDXX') && table"
          :table="table"></field>
        <statistics
          v-if="item === $t('message.scripts.tableDetails.BDAPBTJXX') && statisticInfo"
          key="bdap-statistics"
          :statisticInfo="statisticInfo"
          :work="work"
          :en-env="isEnEnv"
          :partitionPage="partitionPage"
          :cluster="'BDAP'"
          :partition-table-data="partitionTableData"
          @loading="loadingChange"
          @pageSizeChange="pageSizeChange"
          @pageChange="pageChange"
          @partitionSort="partitionSortAction"></statistics>
        <!-- BDP统计信息标签页 -->
        <template v-if="item === $t('message.scripts.tableDetails.BDPBTJXX')">
          <statistics
            v-if="bdpStatisticInfo"
            key="bdp-statistics"
            :statisticInfo="bdpStatisticInfo"
            :work="bdpWork"
            :en-env="isEnEnv"
            :partitionPage="bdpPartitionPage"
            :cluster="'BDP'"
            :partition-table-data="bdpPartitionTableData"
            @loading="loadingChange"
            @pageSizeChange="bdpPageSizeChange"
            @pageChange="bdpPageChange"
            @partitionSort="bdpPartitionSortAction"></statistics>
          <div v-else-if="!tableDetailInfo.originDb || !tableDetailInfo.originTable" class="no-data-container">
            <div class="no-data-text">{{ $t('message.scripts.emptyText') }}</div>
          </div>
        </template>
        <!-- BDP源表变更详情标签页 -->
        <template v-if="item === $t('message.scripts.tableDetails.BDPSrcChangeDetail')">
          <bdpSrcChangeMicroApp
            v-if="tableDetailInfo.originDb && tableDetailInfo.originTable"
            :cluster="'BDP'"
            :dbCode="tableDetailInfo.originDb"
            :tableName="tableDetailInfo.originTable"
            class="tab-content-full-height">
          </bdpSrcChangeMicroApp>
          <div v-else class="no-data-container">
            <div class="no-data-text">{{ $t('message.scripts.emptyText') }}</div>
          </div>
        </template>
      </TabPane>
    </Tabs>
    <Spin
      v-if="loading"
      class="current-spin"
      fix>
      <Icon type="ios-loading" size="18" class="spin-icon-load"></Icon>
      <div>Loading</div>
    </Spin>
  </div>
</template>
<script>
import basic from './components/basic.vue';
import field from './components/field.vue';
import bdpSrcChangeMicroApp from './components/bdpSrcChangeMicroApp.vue';
import statistics from './components/statistics.vue';
import api from '@dataspherestudio/shared/common/service/api';
export default {
  components: {
    basic,
    field,
    statistics,
    bdpSrcChangeMicroApp,
  },
  props: {
    work: {
      type: Object,
      required: true,
    },
  },
  computed: {
    isEnEnv() {
      return localStorage.getItem('locale') === 'en';
    },
    bdpWork() {
      return {
        data: {
          dbName: this.tableDetailInfo.originDb,
          name: this.tableDetailInfo.originTable,
        },
      };
    }
  },
  methods: {
    // 切换页码
    pageChange(val) {
      this.partitionPage.pageNow = val;
      const params = {
        database: this.work.data.dbName,
        tableName: this.work.data.name,
      };
      this.getTableStatisticInfo(params);
    },
    // 也容量发生变化
    pageSizeChange(val) {
      this.partitionPage.pageSize = val;
      this.partitionPage.pageNow = 1;
      const params = {
        database: this.work.data.dbName,
        tableName: this.work.data.name,
      };
      this.getTableStatisticInfo(params);
    },
    // 分区组件调用单分区查询
    loadingChange(val) {
      this.loading = val;
    },
    getDatalist(id){
      this.currentTab = id;
      const params = {
        database: this.work.data.dbName,
        tableName: this.work.data.name,
      };
      switch (id) {
        case 1:
          this.getTableFieldsInfo(params)
          break;
        case 2:
          if (!this.statisticInfo || !this.statisticInfo.partitions) {
            this.getTableStatisticInfo(params)
          }
          if (this.partitionTableData.length === 0) {
            this.loadPartitionTableData('BDAP', this.work.data.dbName, this.work.data.name)
          }
          break;
        case 3:
          if (!this.tableDetailInfo.originDb || !this.tableDetailInfo.originTable) {
            this.$Message.error(this.$t('message.scripts.tableDetails.HQBDPBXXS'));
            return;
          }
          if (!this.bdpStatisticInfo || !this.bdpStatisticInfo.partitions) {
            this.getBdpTableStatisticInfo()
          }
          if (this.bdpPartitionTableData.length === 0) {
            this.loadPartitionTableData('BDP', this.tableDetailInfo.originDb, this.tableDetailInfo.originTable)
          }
          break;
        case 4:
          if (!this.tableDetailInfo.originDb || !this.tableDetailInfo.originTable) {
            this.$Message.error(this.$t('message.scripts.tableDetails.HQBDPBXXS'));
            return;
          }
        case 0:
          this.getTableComperssInfo(params)
          // 获取DM侧数据
          this.getTableDetailInfo(params)
          break;
        default:
          break;
      }
    },
    getTableComperssInfo(data) {
      const params = {
        dbName: data.database,
        isTableOwner: '0',
        orderBy: '1',
        tableName: data.tableName,
        isRealTime: true,
        exactTableName: true,
        pageSize: 1,
        currentPage: 1
      }
      api.fetch('/dss/datapipe/datasource/getTableMetaDataInfo', params, 'get').then((rst) => {
        this.metadata = rst.tableList[0] || {}
      }).catch(() => {
      });
    },
    getTableDetailInfo(data) {
      const params = {
        cluster: "BDAP",
        dbCode: data.database,
        tableName: data.tableName
      }
      api.fetch('/dss/datapipe/datasource/getDetailDatasetMaskInfo', params, 'post').then((rst) => {
        this.tableDetailInfo = rst && rst.result || {}
        // 如果BDP标签已经激活且有数据，则加载BDP统计信息
        if (this.currentTab === 3 && this.bdpStatisticInfo === null && this.tableDetailInfo.originDb && this.tableDetailInfo.originTable) {
          this.getBdpTableStatisticInfo();
          this.loadPartitionTableData('BDP', this.tableDetailInfo.originDb, this.tableDetailInfo.originTable)
        }
      }).catch(() => {
        this.tableDetailInfo = {}
      });
    },
    getTableFieldsInfo(params) {
      this.loading = true;

      // 准备新接口的参数
      const bdpParams = {
        cluster: "BDAP",
        dbCode: params.database,
        tableName: params.tableName
      };

      // 同时请求两个接口
      const bdapRequest = api.fetch('/datasource/getTableFieldsInfo', params, 'get');
      const bdpRequest = api.fetch('/dss/datapipe/datasource/getColumnMaskInfoList', bdpParams, 'post');

      Promise.allSettled([bdapRequest, bdpRequest]).then((results) => {
        let bdapData = [];
        let bdpData = [];

        // 处理原接口的结果
        if (results[0].status === 'fulfilled') {
          bdapData = results[0].value.tableFieldsInfo || [];
        } else {
          console.error('原接口请求失败:', results[0].reason);
        }

        // 处理新接口的结果
        if (results[1].status === 'fulfilled') {
          bdpData = results[1].value.result || [];
        } else {
          console.error('BDP接口请求失败:', results[1].reason);
        }

        // 如果两个接口都失败了，显示错误信息
        if (results[0].status === 'rejected' && results[1].status === 'rejected') {
          this.$Message.error(this.$t('message.scripts.tableDetails.HQBZDXXSB'));
          this.table = [];
          this.loading = false;
          return;
        }

        // 合并数据
        const mergedData = bdapData.map(originalItem => {
          // 根据name字段匹配targetColumnName
          const matchedBdpItem = bdpData.find(bdpItem => bdpItem.targetColumnName === originalItem.name);

          if (matchedBdpItem) {
            return {
              ...originalItem,
              targetColumnName: matchedBdpItem.targetColumnName,
              // 优先取DSS接口字段描述，如果DSS返回该字段描述为空，则取DM接口的字段描述
              targetColumnComment: originalItem.comment || matchedBdpItem.targetColumnComment,
              originColumnName: matchedBdpItem.originColumnName,
              originColumnComment: matchedBdpItem.originColumnComment,
              fieldRule: matchedBdpItem.fieldRule,
              dataItemName: matchedBdpItem.dataItemName,
              classficationName: matchedBdpItem.classficationName,
              funcDescription: matchedBdpItem.funcDescription,
              funcName: matchedBdpItem.funcName
            };
          }

          return {
            ...originalItem,
            targetColumnName: originalItem.name,
            targetColumnComment: originalItem.comment,
            originColumnName: '',
            originColumnComment: '',
            fieldRule: '',
            dataItemName: '',
            classficationName: '',
            funcDescription: '',
            funcName: ''
          };
        });

        this.table = mergedData;
        this.loading = false;
      }).catch((error) => {
        console.error('请求过程中发生错误:', error);
        this.$Message.error(this.$t('message.scripts.tableDetails.HQBZDXXSB'));
        this.table = [];
        this.loading = false;
      });
    },
    // 获取BDAP表实时分区信息
    getTableStatisticInfo(params) {
      params.pageSize = this.partitionPage.pageSize;
      params.pageNow = this.partitionPage.pageNow;
      params.partitionSort = this.partitionSort;
      params.cluster = 'BDAP';
      this.loading = true;
      api.fetch('/datasource/getTableStatisticInfo', params, 'get').then((rst) => {
        this.statisticInfo = rst.tableStatisticInfo;
        this.partitionPage.totalSize = rst.totalSize,
        this.partitionPage.pageSize = rst.pageSize,
        this.partitionPage.pageNow = rst.pageNow
        this.loading = false;
      }).catch(() => {
        this.$Message.error(this.$t('message.scripts.tableDetails.JZBSSFQXXSB'));
      }).finally(() => {
        this.loading = false;
      });
    },
    partitionSortAction() {
      if(this.partitionSort === 'desc') {
        this.partitionSort = 'asc'
      } else {
        this.partitionSort = 'desc'
      }
      this.pageSizeChange(500);
    },
    // BDP相关方法
    bdpPageChange(val) {
      this.bdpPartitionPage.pageNow = val;
      this.getBdpTableStatisticInfo();
    },
    bdpPageSizeChange(val) {
      this.bdpPartitionPage.pageSize = val;
      this.bdpPartitionPage.pageNow = 1;
      this.getBdpTableStatisticInfo();
    },
    // 加载表分区信息数据
    async loadPartitionTableData(cluster, dbCode, tableName) {
      this.loading = true;
      try {
        const params = {
          cluster: cluster,
          dbCode: dbCode,
          tableName: tableName
        };

        const response = await api.fetch('/dss/datapipe/datasource/getBdpTableDetail', params, 'post');

        if (response && response.result) {
          const { partitions, tableStorageUsedBytes, smallFileNum, tablePartNum, latestPartAccessTime } = response.result;
          if (cluster === 'BDAP') {
            this.partitionTableData = partitions || [];
          } else if (cluster === 'BDP') {
            this.bdpPartitionTableData = partitions || [];
            this.bdpStatisticInfo = Object.assign({}, this.bdpStatisticInfo || {}, {
              tableSize: (tableStorageUsedBytes && `${(Number(tableStorageUsedBytes)).toLocaleString('en-US', {
                maximumFractionDigits: 20,
                minimumFractionDigits: 0
              })} GB`) || '',
              fileNum: smallFileNum || '',
              partitionsNum: tablePartNum || '',
              tableLastUpdateTime: latestPartAccessTime || ''
            });
          }
        } else {
          if (cluster === 'BDAP') {
            this.partitionTableData = [];
          } else if (cluster === 'BDP') {
            this.bdpPartitionTableData = [];
            this.bdpStatisticInfo = Object.assign({}, this.bdpStatisticInfo || {}, {
              tableSize: '',
              fileNum: '',
              partitionsNum: '',
              tableLastUpdateTime: ''
            });
          }
        }
      } catch (error) {
        console.error('加载表分区信息失败:', error);
        this.$Message.error(this.$t('message.scripts.tableDetails.JZBFQXXSB'));
      } finally {
        this.loading = false;
      }
    },
    // 获取BDP表实时分区信息
    getBdpTableStatisticInfo() {
      if (!this.tableDetailInfo.originDb || !this.tableDetailInfo.originTable) {
        return;
      }
      const params = {
        database: this.tableDetailInfo.originDb,
        tableName: this.tableDetailInfo.originTable,
        pageSize: this.bdpPartitionPage.pageSize,
        pageNow: this.bdpPartitionPage.pageNow,
        partitionSort: this.bdpPartitionSort,
        cluster: 'BDP'
      };
      this.loading = true;
      api.fetch('/datasource/getTableStatisticInfo', params, 'get').then((rst) => {
        this.bdpStatisticInfo = Object.assign({}, this.bdpStatisticInfo || {}, {
          partitions: rst.tableStatisticInfo && rst.tableStatisticInfo.partitions
        });
        this.bdpPartitionPage.totalSize = rst.totalSize,
        this.bdpPartitionPage.pageSize = rst.pageSize,
        this.bdpPartitionPage.pageNow = rst.pageNow
        this.loading = false;
      }).catch(() => {
        this.$Message.error(this.$t('message.scripts.tableDetails.JZBSSFQXXSB'));
      }).finally(() => {
        this.loading = false;
      });
    },
    bdpPartitionSortAction() {
      if(this.bdpPartitionSort === 'desc') {
        this.bdpPartitionSort = 'asc'
      } else {
        this.bdpPartitionSort = 'desc'
      }
      this.bdpPageSizeChange(500);
    }
  },
  data() {
    return {
      metadata: {},
      tableDetailInfo: {},
      tabList: [this.$t('message.scripts.tableDetails.BZBSX'), this.$t('message.scripts.tableDetails.BZDXX'), this.$t('message.scripts.tableDetails.BDAPBTJXX'), this.$t('message.scripts.tableDetails.BDPBTJXX'), this.$t('message.scripts.tableDetails.BDPSrcChangeDetail')],
      currentTab: 0,
      table: null,
      statisticInfo: null,
      bdpStatisticInfo: null,
      loading: false,
      partitionPage: {
        totalSize: 0,
        pageSize: 100,
        pageNow: 1,
        sizeOpts: [100, 250, 500]
      }, // 分区信息的分页
      bdpPartitionPage: {
        totalSize: 0,
        pageSize: 100,
        pageNow: 1,
        sizeOpts: [100, 250, 500]
      }, // BDP分区信息的分页
      partitionSort: 'desc', // asc 分区的排序顺序
      bdpPartitionSort: 'desc', // BDP asc 分区的排序顺序
      partitionTableData: [], // BDAP表分区信息数据
      bdpPartitionTableData: [] // BDP表分区信息数据
    };
  },
  mounted() {
    if (!this.metadata.tableName) {
      this.getDatalist(0)
    }
  }
};
</script>
<style lang="scss" scoped>
@import '@dataspherestudio/shared/common/style/variables.scss';
.table-detail {
    height: 100%;
    @include bg-color($table-thead-bg, $dark-base-color);
    padding: 16px 10px 10px 16px;
    .table-detail-tabs-overflow-auto {
      ::v-deep .ivu-tabs-content {
            overflow: auto;
        }
    }
    .table-detail-tabs {
        height: 100%;
        @include font-color($light-text-color, $dark-text-color);
        .ivu-tabs-bar {
            margin-bottom: 10px;
        }
        ::v-deep .ivu-tabs-content {
            height: calc(100% - 48px); // 减去tabs header的高度
        }
        .basic-card {
            margin-bottom: 10px;
            height: calc(100% - 52px);
            overflow: hidden;
            .basic-card-item {
                display: inline-flex;
                width: 50%;
                height: 36px;
                padding-left: 10px;
                align-items: center;
                &.comment {
                    height: 42px;
                    align-items: start;
                }
                .basic-card-item-title {
                    display: inline-block;
                    width: 100px;
                    font-weight: bold;
                }
                .basic-card-item-value {
                    display: inline-block;
                    width: calc(100% - 104px);
                    overflow: hidden;
                    &.comment {
                        overflow-y: auto;
                        height: 100%;
                    }
                }
            }
        }
    }
}
.current-spin {
  z-index: 10;
}
.spin-icon-load{
  animation: ani-demo-spin 1s linear infinite;
}
.table-detail-tabs {
  ::v-deep
  .ivu-tabs-bar {
    .ivu-tabs-nav-container {
      font-size: $tag-font-size;
    }
  }
}
.tab-content-full-height {
  height: 100%;
}
.no-data-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  @include font-color($light-text-color, $dark-text-color);
}
.no-data-text {
  font-size: 14px;
  color: #999;
}
@keyframes ani-demo-spin {
  from { transform: rotate(0deg);}
  50%  { transform: rotate(180deg);}
  to   { transform: rotate(360deg);}
}
</style>
