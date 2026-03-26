<template>
  <div class="statistics">
    <!-- 表信息模块 - 移到顶部，改为一行展示所有字段 -->
    <Card
      :bordered="false"
      class="statistics-card table-info-card"
      v-for="(type, index1) in info"
      :key="index1">
      <p class="title" slot="title">{{ type.title }}</p>
      <div class="table-info-row">
        <span
          v-for="(item, index2) in type.children"
          :key="index2"
          class="table-info-item">
          <span
            class="table-info-label"
          >{{ item.title }}: </span>
          <span
            class="table-info-value"
          >{{ formatValue(item) }}</span>
        </span>
      </div>
    </Card>

    <Split v-model="split" :style="{height:panelHeight+'px'}">
      <!-- 左侧：表分区信息 -->
      <Card
        :bordered="false"
        class="statistics-card"
        slot="left">
        <p class="title" slot="title">{{ $t('message.scripts.tableDetails.BFQXX') }}</p>
        <div class="partition-table-container">
          <Table
            :columns="partitionTableColumns"
            :data="currentPageData"
            :loading="partitionTableLoading"
            :height="tableHeight"
          >
          </Table>
          <div class="partition-table-page">
            <Page
              :total="partitionTableData.length"
              :page-size="partitionTablePage.pageSize"
              :current="partitionTablePage.current"
              :page-size-opts="[10, 20, 50, 100]"
              class-name="page"
              size="small"
              show-total
              show-sizer
              @on-change="handlePartitionPageChange"
              @on-page-size-change="handlePartitionPageSizeChange" />
          </div>
        </div>
      </Card>

      <!-- 右侧：实时表分区信息 -->
      <Card
        :bordered="false"
        class="statistics-card real-time-table-partitions-info-card"
        slot="right">
        <p slot="title">{{ $t('message.scripts.tableDetails.SSBFQXX') }}</p>
        <span
          v-show="statisticInfo && statisticInfo.partitions"
          class="statistics-card-label">{{`* ${$t('message.scripts.tableDetails.DJFQCK')}`}}
          <Icon @click="Partitionsort" type="md-swap" style="transform: rotate(90deg);margin-left: 21px;"/>
        </span>
        <Tree
          ref="partTree"
          :data="formatPartions(statisticInfo && statisticInfo.partitions, 0)"
          :empty-text="$t('message.scripts.tableDetails.WFQSJ')"
          class="statistics-card-tree"
          @on-select-change="getCurrentNode"/>
        <div class="statistics-page">
          <Page
            ref="page"
            :total="partitionPage.totalSize"
            :page-size-opts="partitionPage.sizeOpts"
            :page-size="partitionPage.pageSize"
            :current="partitionPage.pageNow"
            class-name="page"
            size="small"
            show-total
            show-sizer
            @on-change="change"
            @on-page-size-change="changeSize" />
        </div>
      </Card>
    </Split>
  </div>
</template>
<script>
import utils from '../utils.js';
import { isEmpty, map } from 'lodash';
import moment from 'moment';
import api from '@dataspherestudio/shared/common/service/api';
import filters from '@dataspherestudio/shared/common/util/filters';

export default {
  props: {
    statisticInfo: {
      type: Object,
      default: () => {}
    },
    enEnv: Boolean,
    work: {
      type: Object,
      required: true,
    },
    partitionPage: {
      type: Object,
      default: () => {}
    },
    cluster: {
      type: String,
      default: 'BDAP'
    },
    partitionTableData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      split: 0.5,
      panelHeight: 400,
      treeArr: [],
      tableHeight: 300,
      partitionTableLoading: false,
      partitionTablePage: {
        current: 1,
        pageSize: 20,
        total: 0
      },
      partitionTableColumns: [
        {
          key: 'partName',
          title: this.$t('message.scripts.tableDetails.FQ')
        },
        {
          key: 'partSize',
          title: this.$t('message.scripts.tableDetails.FQDX'),
          render: (h, params) => {
            const row = params.row;
            let value = '';
            if (row && row.partSize) {
              // 提取数字和单位
              const sizeStr = String(row.partSize);
              const numericMatch = sizeStr.match(/^([\d.]+)/);
              const unitMatch = sizeStr.match(/([a-zA-Z]+)$/);

              if (numericMatch) {
                const numericPart = Number(numericMatch[1]);
                const formattedNumber = numericPart.toLocaleString('en-US', {
                  maximumFractionDigits: 20,
                  minimumFractionDigits: 0
                });
                const unit = unitMatch ? unitMatch[1] : '';
                value = `${formattedNumber} ${unit}`;
              } else {
                // 如果无法解析，使用原始值
                value = sizeStr;
              }
            }
            return h('span', value);
          }
        },
        {
          key: 'partFileCount',
          title: (this.$t('message.scripts.tableDetails.WJS') || '').replace(/[：:]/, '')
        },
        {
          key: 'partCreateTime',
          title: this.$t('message.scripts.tableDetails.FQCJSJ'),
          render: (h, params) => {
            const row = params.row;
            const value = row && row.partCreateTime ? filters.formatDate(+row.partCreateTime * 1000, 'YYYY-MM-DD HH:mm:ss') : '';
            return h('span', value);
          }
        }
      ],
      info: [
        {
          title: this.$t('message.scripts.tableDetails.BXX'),
          children: [{
            key: 'tableSize',
            title: this.$t('message.scripts.tableDetails.BDX'),
          }, {
            key: 'fileNum',
            title: this.$t('message.scripts.tableDetails.WJM'),
          }, {
            key: 'partitionsNum',
            title: this.$t('message.scripts.tableDetails.FQS'),
          }, {
            key: 'tableLastUpdateTime',
            title: this.$t('message.scripts.tableDetails.ZHFWSJ'),
            type: 'timestramp',
          }],
        },
      ],
    };
  },
  computed: {
    // 计算当前页的数据
    currentPageData() {
      const start = (this.partitionTablePage.current - 1) * this.partitionTablePage.pageSize;
      const end = start + this.partitionTablePage.pageSize;
      return this.partitionTableData.slice(start, end);
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.calculateHeight();
    });
  },
  methods: {
    // 计算高度
    calculateHeight() {
      const parentHeight = this.$parent.$parent.$el.clientHeight - 52;
      const topCardElement = this.$el && this.$el.querySelector('.table-info-card');
      const topCardHeight = topCardElement ? topCardElement.offsetHeight + 16 : 120; // 16px是margin-bottom
      this.panelHeight = parentHeight - topCardHeight;
      this.tableHeight = this.panelHeight - 134; // 为分页器预留空间
    },
    // 处理分页变化
    handlePartitionPageChange(page) {
      this.partitionTablePage.current = page;
    },

    // 处理分页大小变化
    handlePartitionPageSizeChange(pageSize) {
      this.partitionTablePage.pageSize = pageSize;
      this.partitionTablePage.current = 1;
    },

    changeSize(val) {
      this.$emit('pageSizeChange', val);
    },
    change(val) {
      this.$emit('pageChange', val)
    },
    async getCurrentNode(node) {
      // 避免分区过大，加载慢，采用动态获取数据
      const partNode = node[0];
      // 如果已经查询过就不用查询
      if (!partNode || partNode.isRender) return;
      const nodePartition = await this.getChildrenPartition(partNode);
      if (nodePartition) {
        const str =!isEmpty(partNode) &&  moment.unix(nodePartition.modificationTime / 1000).format('YYYY-MM-DD HH:mm:ss');
        const isFirstLevel = !isEmpty(partNode);
        const isHasSize = !isEmpty(partNode) && partNode.title.indexOf(this.$t('message.scripts.tableDetails.DX')) === -1;
        if (partNode && isFirstLevel && isHasSize && str) {
          // \xa0代表空格
          partNode.title += `\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0（${this.$t('message.scripts.tableDetails.FQDX')}${nodePartition.partitionSize}，${this.$t('message.scripts.tableDetails.WJS')}${nodePartition.fileNum}，${this.$t('message.scripts.tableDetails.LastWriteTime')}：${str}）`;
          partNode.isRender = true;
        }
      }
    },
    getChildrenPartition(node) {
      if (isEmpty(node)) return;
      const params = {
        database: this.work.data.dbName,
        tableName: this.work.data.name,
        partitionPath: node.partitionPath,
        cluster: this.cluster
      };
      this.$emit('loading', true);
      return api.fetch('datasource/getPartitionStatisticInfo', params, 'get').then((res) => {
        this.$emit('loading', false);
        return res.partitionStatisticInfo;
      }).catch(() => {
        this.$emit('loading', false);
      })
    },
    formatValue(item) {
      const statisticInfo = this.statisticInfo;
      if (!statisticInfo) {
        return null;
      }
      return utils.formatValue(statisticInfo, item);
    },
    // 对partition进行格式化成tree组件需要的格式
    formatPartions(part, level) {
      level = level + 1;
      return this.treeArr = map(part, (o) => {
        return {
          label: o.name,
          title: o.name,
          partitionSize: o.partitionSize,
          expand: false,
          createtime: o.modificationTime,
          children: this.formatPartions(o.childrens, level),
          fileNum: o.fileNum,
          level,
          partitionPath: o.partitionPath,
          isRender: false // 是否查询详情渲染
        };
      });
    },
    Partitionsort(){
      this.$emit('partitionSort');
    }
  },
};
</script>
<style lang="scss" scoped>
@import '@dataspherestudio/shared/common/style/variables.scss';
  .statistics {
      height: 100%;
      overflow: hidden;
      width: 100%;

      .statistics-card {
          height: 100%;
          ::v-deep
          .ivu-card-body {
              height: calc(100% - 52px);
          }

          .partition-table-container {
             height: 100%;
             display: flex;
             flex-direction: column;
             width: 100%;

             .partition-table-page {
                height: 36px;
                margin-top: 20px;
                text-align: center;
             }

             ::v-deep .ivu-table {
               width: 100%;
             }

             ::v-deep .ivu-table-wrapper {
               width: 100%;
             }
           }

          &.real-time-table-partitions-info-card {
            ::v-deep .ivu-card-body {
              display: flex;
              flex-direction: column;
              .statistics-card-tree {
                flex: auto;
                .ivu-tree-empty {
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  height: 100%;
                  width: 100%;
                }
              }
              .statistics-page {
                flex: none;
              }
            }
          }

          .statistics-card-item {
              display: inline-flex;
              width: 100%;
              height: 36px;
              line-height: 36px;
              padding-left: 10px;
              align-items: center;
              .statistics-card-item-title {
                  display: inline-block;
                  width: 100px;
                  font-weight: bold;
              }
              .statistics-card-item-value {
                  display: inline-block;
                  width: calc(100% - 104px);
                  overflow: hidden;
              }
          }
          .statistics-card-label {
              margin-left: 4px;
          }
          .statistics-card-tree {
              overflow-y: auto;
              margin-left: 20px;
          }
          .statistics-page {
              height: 36px;
              margin-top: 20px;
              text-align: center;
          }
      }

    .table-info-card {
      margin-bottom: 16px;
      height: auto;

      .table-info-row {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;

        .table-info-item {
          display: flex;
          align-items: center;
          min-width: 200px;

          .table-info-label {
            font-weight: bold;
            margin-right: 8px;
            white-space: nowrap;
          }

          .table-info-value {
            color: #666;
          }
        }
      }
    }
  }
  .title {
    color: #333;
  }
  .statistics-card-item {
    .statistics-card-item-title,
    .statistics-card-item-value {
      font-size: $font-size-small;
      font-weight: 400!important;
    }
  }
</style>
