<template>
  <div class="visual-analysis" v-if="visualSrc">
    <iframe :src="visualSrc" frameborder="0"></iframe>
  </div>
</template>
<script>
import mixin from '@dataspherestudio/shared/common/service/mixin';
import storage from '@dataspherestudio/shared/common/helper/storage';
export default {
  props: {
    script: {
      type: Object,
      required: true
    },
    work: {
      type: Object,
      default: () => ({})
    }
  },
  mixins: [mixin],
  data() {
    return {}
  },
  computed: {
    scriptResult() {
      let res = {
        headRows: [],
        bodyRows: [],
        type: 'EMPTY',
        total: 0,
        path: ''
      };
      if (this.script.resultList) {
        res = this.script.resultList[this.script.resultSet || 0].result || res
      }
      return res
    },
    visualSrc() {
      const dataWranglerParams = {
        simpleMode: true,
        showBottomBar: false,
        importConfig: {
          "dataSourceConfig": {
            "dataSourceType": "linkis",
            "dataSourceOptions": {"taskID": this.getTaskId()}
          },
          "config": {
            "myConfig": {
              "resultSetPath": [this.scriptResult.path]
            },
            "importConfig": {
              "mergeTables": true,
              "limitRows": 5000,
              "pivotTable": false,
              "tableHeaderRows": 1,
            }
          }
        }
      }
      let params = '';
      Object.keys(dataWranglerParams).map((key) => {
        params += `${key}=${JSON.stringify(dataWranglerParams[key])}&`
      })
      params += 'noGoingBack=true';
      const baseinfo = storage.get("baseInfo", "local") || {}
      const dwraisUrl = baseinfo.resExcelUrl || '/#/sheet/add';
      const srcPre = `${dwraisUrl}?${params}`;
      return srcPre;
    }
  },
  methods: {
    getTaskId() {
      const { taskID } = this.$route.query
      return this.work.taskID || taskID || this.work.data && this.work.data.history && this.work.data.history[0].taskID || ''
    }
  }
};
</script>
<style lang="scss" scoped>
.visual-analysis {
  height: 100%;
  iframe {
    width: 100%;
    height: -webkit-fill-available;
  }
}
</style>
