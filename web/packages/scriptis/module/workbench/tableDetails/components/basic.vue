<template>
  <div>
    <Card
      :bordered="false"
      class="basic-card"
      v-for="(type, index1) in info"
      :key="index1">
      <p class="title" slot="title">{{ type.title }}</p>
      <div>
        <span
          v-for="(item, index2) in type.children"
          :key="index2"
          class="basic-card-item">
          <span
            class="basic-card-item-title"
            :style="{'width': enEnv?'270px':'130px'}">{{ item.title }}: </span>
          <span
            class="basic-card-item-value"
            :style="{'width': enEnv?'calc(100% - 274px)':'calc(100% - 134px)'}"
            v-html="formatValue(item)"></span>
        </span>
      </div>
    </Card>
  </div>
</template>
<script>
import utils from '../utils.js';
import aiInferenceMixin from '../mixins/aiInference.js';
export default {
  mixins: [aiInferenceMixin],
  props: {
    tableInfo: {
      type: Object,
    },
    enEnv: Boolean,
    metaData: {
      type: Object
    },
    tableDetailInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      info: [
        {
          title: this.$t('message.scripts.tableDetails.BDAPTableBasicProps'),
          children: [{
            key: 'name',
            title: this.$t('message.scripts.tableDetails.TN'),
          }, {
            key: 'database',
            title: this.$t('message.scripts.tableDetails.DBN'),
          }, {
            key: 'targetTableComment',
            title: this.$t('message.scripts.tableDetails.BDAPTableDesc'),
          }, {
            key: 'targetBusinessMeaning',
            title: this.$t('message.scripts.tableDetails.BDAPTableBusinessMeaning'),
          }, {
            key: 'partitionTable',
            title: this.$t('message.scripts.tableDetails.FQM'),
            type: 'boolean',
          }, {
            key: 'compressTable',
            title: this.$t('message.scripts.tableDetails.compressTable'),
            type: 'boolean',
          }, {
            key: 'latestAccessTime',
            title: this.$t('message.scripts.tableDetails.ZHFWSJ'),
            type: 'timestramp',
          }, {
            key: 'createTime',
            title: this.$t('message.scripts.tableDetails.CJSI'),
            type: 'timestramp',
          }, {
            key: 'creator',
            title: this.$t('message.scripts.tableDetails.CJYH'),
          }],
        },
        {
          title: this.$t('message.scripts.tableDetails.BDPSourceTableBasicProps'),
          children: [{
            key: 'originTable',
            title: this.$t('message.scripts.tableDetails.BDPOriginTable'),
          }, {
            key: 'originDb',
            title: this.$t('message.scripts.tableDetails.BDPOriginDb'),
          }, {
            key: 'originTableComment',
            title: this.$t('message.scripts.tableDetails.BDPOriginTableDesc'),
          }, {
            key: 'originBusinessMeaning',
            title: this.$t('message.scripts.tableDetails.BDPOriginTableBusinessMeaning'),
          }, {
            key: 'updateMethod',
            title: this.$t('message.scripts.tableDetails.BDPUpdateMethod'),
          }, {
            key: 'updateFrequency',
            title: this.$t('message.scripts.tableDetails.BDPUpdateFrequency'),
          }, {
            key: 'proNames',
            title: this.$t('message.scripts.tableDetails.BDPRelatedProduct'),
          }, {
            key: 'subsystems',
            title: this.$t('message.scripts.tableDetails.BDPSubsystem'),
          }, {
            key: 'devmanager',
            title: this.$t('message.scripts.tableDetails.BDPDevManager'),
          }, {
            key: 'devdept',
            title: this.$t('message.scripts.tableDetails.BDPDevDept'),
          }, {
            key: 'deptName',
            title: this.$t('message.scripts.tableDetails.BDPDictDept'),
          }],
        },
        {
          title: this.$t('message.scripts.tableDetails.BMXSX'),
          children: [{
            key: 'lifecycle',
            title: this.$t('message.scripts.tableDetails.SMZQ'),
            type: 'convert',
          }, {
            key: 'modelLevel',
            title: this.$t('message.scripts.tableDetails.MXCJ'),
            type: 'convert',
          }, {
            key: 'useWay',
            title: this.$t('message.scripts.tableDetails.SYFS'),
            type: 'convert',
          }, {
            key: 'externalUse',
            title: this.$t('message.scripts.tableDetails.WBSFSY'),
            type: 'boolean',
          }],
        },
        {
          title: this.$t('message.scripts.tableDetails.BYYSX'),
          children: [{
            key: 'productName',
            title: this.$t('message.scripts.tableDetails.SXCP'),
          }, {
            key: 'projectName',
            title: this.$t('message.scripts.tableDetails.SSXM'),
          }, {
            key: 'usage',
            title: this.$t('message.scripts.tableDetails.YT'),
          }],
        },
      ],
    };
  },
  computed: {
    tableBaseInfo() {
      const { application, base, model } = this.tableInfo.baseInfo;

      // 优先取DSS接口表描述(来自base.comment)，如果DSS接口表描述为空，则取DM接口表描述(来自this.tableDetailInfo.targetTableComment)
      const targetTableComment = base.comment || this.tableDetailInfo.targetTableComment;

      return {
        ...application,
        ...base,
        ...model,
        ...this.tableDetailInfo,
        targetTableComment
      };
    },
  },
  methods: {
    formatValue(item) {
      if (item.key == 'compressTable') {
        if (this.metaData && this.metaData.compressed !== undefined) {
          return utils.formatValue({compressTable: this.metaData.compressed}, item);
        }
        return ''
      }

      // 获取字段值
      const fieldValue = this.tableBaseInfo[item.key];

      // 如果字段值是对象格式 {currentEnv: "", inference: ""}，按AI推断逻辑处理
      if (this.hasAIInference(fieldValue)) {
        return this.formatAIInferenceValue(fieldValue);
      }

      // 其他情况按原有逻辑处理
      return utils.formatValue(this.tableBaseInfo, item);
    },
  },
};
</script>
<style lang="scss" scoped>
@import '@dataspherestudio/shared/common/style/variables.scss';
  .title {
    color: #333;
  }
  .basic-card {
    margin-bottom: 10px;
    height: calc(100% - 52px);
    overflow: hidden;
    .basic-card-item {
        display: inline-flex;
        width: 50%;
        min-height: 36px;
        padding-left: 10px;
        align-items: start;
        .basic-card-item-title {
            display: inline-block;
            width: 130px;
            font-weight: bold;
        }
        .basic-card-item-value {
            display: inline-block;
            width: calc(100% - 134px);
        }
    }
  }
  .basic-card-item {
    .basic-card-item-title,
    .basic-card-item-value {
      font-size: $font-size-small;
      font-weight: 400!important;
    }
  }

  ::v-deep .ai-tag {
    display: inline-block;
    height: 18px;
    border-radius: 3px;
    font-size: 10px;
    line-height: 18px;
    text-align: center;
    margin-right: 4px;
    padding: 0 2px;
    background-color: #f0f2f5;
    color: #93949b;
  }
</style>

