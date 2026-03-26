<template>
  <Tabs type="card" class="table-detail-tabs" :animated="false">
    <TabPane :label="$t('message.nebula.basicInfo')" style="height:100%;">
      <div style="padding: 10px;">
        <h4 class="title">{{ $t('message.nebula.basicInfo') }}</h4>
          <div class="basic-card"  v-for="(type, index1) in info" :key="index1">
            <span v-for="(item, index2) in type.children" :key="index2" class="basic-card-item">
              <span class="basic-card-item-title" :style="{ 'width': '120px' }">{{ item.title }}: </span>
              <span class="basic-card-item-value"
                :style="{ 'width': 'calc(100% - 120px)' }">{{ formatValue(item) }}</span>
            </span>
          </div>
      </div>
    </TabPane>
  </Tabs>
</template>
<script>
import utils from '@dataspherestudio/shared/common/util';
export default {
  props: {
    work: {
      type: Object,
      required: true,
    }
  },
  data() {
    return {
      info: [
        {
          children: [{
            key: 'spaceId',
            title: this.$t('message.nebula.spaceId'),
          }, {
            key: 'spaceName',
            title: this.$t('message.nebula.spaceName'),
          }, {
            key: 'partitionNumber',
            title: this.$t('message.nebula.partitionNumber'),
          }, {
            key: 'replicaFactor',
            title: this.$t('message.nebula.replicaFactor')
          }, {
            key: 'charset',
            title: this.$t('message.nebula.charset')
          }, {
            key: 'collate_',
            title: this.$t('message.nebula.collationRule')
          }, {
            key: 'vidType',
            title: this.$t('message.nebula.vidType')
          }, {
            key: 'comment',
            title: this.$t('message.nebula.description')
          }],
        }
      ],
    };
  },
  computed: {
  },
  methods: {
    formatValue(item) {
      return utils.formatValue(this.work.data, item);
    },
  },
};
</script>
<style lang="scss" scoped>
.title {
  padding-left: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  margin-bottom: 5px;
}
.note {
  font-weight: normal;
  padding-left: 10px;
  display: inline-block;
  vertical-align: top;
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
      width: 100%;
      height: auto;
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
        word-break: break-all;
      }
    }
  }
}

.basic-card-item {

  .basic-card-item-title,
  .basic-card-item-value {
    font-size: 12px;
    font-weight: 400 !important;
  }
}
</style>
