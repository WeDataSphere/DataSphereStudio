<template>
  <div class="visual-analysis" v-if="project">
    <iframe id="visualIframe" :src="visualSrc" frameborder="0"></iframe>
    <Modal
      v-model="project.show"
      :title="$t('message.common.saveToWidget')"
      @on-ok="confirm"
      @on-cancel="cancel"
    >
      <Select v-model="project.id">
        <Option
          v-for="item in apps"
          :value="item.id"
          :key="item.id"
          style="width: 200px;"
        >{{ item.name }}</Option>
      </Select>
    </Modal>
  </div>
</template>
<script>
import api from '@dataspherestudio/shared/common/service/api';
import mixin from '@dataspherestudio/shared/common/service/mixin';
import storage from '@dataspherestudio/shared/common/helper/storage';
export default {
  props: {
    script: {
      type: Object,
      required: true
    }
  },
  mixins: [mixin],
  data() {
    return {
      project: {
        show: false,
        id: '',
        data: {}
      },
      projectid: null,
      apps: []
    };
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
    visualParams() {
      let rows = this.scriptResult.headRows;
      let model = {}
      let dates = ["DATE", "DATETIME", "TIMESTAMP", "TIME", "YEAR"]
      let numbers = [
        "TINYINT",
        "SMALLINT",
        "MEDIUMINT",
        "INT",
        "INTEGER",
        "BIGINT",
        "FLOAT",
        "DOUBLE",
        "DOUBLE PRECISION",
        "REAL",
        "DECIMAL",
        "BIT",
        "SERIAL",
        "BOOL",
        "BOOLEAN",
        "DEC",
        "FIXED",
        "NUMERIC"];
      rows.forEach(item=>{
        let sqlType = item.dataType.toUpperCase();
        let visualType = 'string'
        if (numbers.indexOf(sqlType) > -1) {
          visualType = 'number'
        } else if(dates.indexOf(sqlType) > -1) {
          visualType = 'date'
        }
        model[item.columnName] =  {
          sqlType,
          visualType,
          modelType: visualType ==="number" ? "value": "category"
        }
      })
      return {
        json: {
          name: `${this.script.fileName.replace(/\./g,'')}${this.script.resultSet}`,
          model,
          source: {
            "engineType": "spark", //引擎类型
            "dataSourceType": "resultset", //数据源类型，结果集、脚本、库表
            "dataSourceContent": {
              "resultLocation": this.scriptResult.path
            },
            "creator": "IDE"
          }
        }
      }
    },
    visualSrc() {
      let { json } = this.visualParams;
      const baseinfo = storage.get("baseInfo", "local") || {}
      const dwraisUrl = baseinfo.resVisualisUrl || '/dss/visualis/#/project/';
      const srcPre = `${dwraisUrl}${this.projectid}/widget`;
      let viewJson = {
        ...json,
        params: {}
      };
      viewJson = JSON.stringify(viewJson);
      if (this.projectid) {
        return json ? `${srcPre}/add?view=${viewJson}` : "";
      }else{
        return ''
      }
    }
  },
  beforeDestroy() {
    window.removeEventListener("message", this.fn, false);
  },
  created() {
    api.fetch("/visualis/project/default?labelsRoute=dev", "get").then(res => {
      this.projectid = res.project.id;
    });
  },
  mounted() {
    this.getproject();
    this.hiddenArrow();
    this.fn = ev => {
      if (typeof ev.data === "string") {
        try {
          let data = JSON.parse(ev.data);
          if (data.type === "saveWidget") {
            this.queryApplication();
            this.project.show = true;
            delete data.type;
            this.project.data = data;
          }
        } catch (error) {
          console.error(error);
        }
      }
    };
    window.addEventListener("message", this.fn, false);
  },
  methods: {
    getproject() {},
    async queryApplication() {
      let data = await api.fetch("/application/list");
      this.apps = data.applications;
    },
    saveWidgetToDws(data) {
      data = Object.assign(data, {
        projectId: this.project.id
      });
      const vsBiUrl = ''  // todo vsurl
      api.fetch(`${vsBiUrl}/api/rest_j/v1/visualis/widgets`, data).then(() => {
        this.$Message.success(this.$t("message.common.saveSuccess"));
      });
    },
    confirm() {
      this.saveWidgetToDws(this.project.data);
    },
    cancel() {},
    hiddenArrow() {
      let iframe = document.getElementById("visualIframe");
      let iwindow = iframe.contentWindow;
      iwindow.onload = function() {
        let dom = iwindow.document.querySelector(".anticon.anticon-left");
        dom.style.display = "none";
      };
    }
  }
};
</script>
<style lang="scss" scoped>
.visual-analysis {
  overflow-y: scroll;
  iframe {
    width: 100%;
    height: 100%;
    min-height: 400px;
  }
}
</style>
