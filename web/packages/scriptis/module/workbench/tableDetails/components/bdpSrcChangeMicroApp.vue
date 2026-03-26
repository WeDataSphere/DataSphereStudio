<template>
  <div class="bdp-src-change-container">
    <iframe
      class="bdp-src-change-iframe"
      v-if="isRefresh"
      id="mircoApp"
      :src="visualSrc"
      frameborder="0"
      width="100%"
      sandbox="allow-forms allow-modals allow-popups allow-same-origin allow-scripts allow-top-navigation"
      :height="height">
    </iframe>
  </div>
</template>
<script>
import api from '@dataspherestudio/shared/common/service/api';
import util from '@dataspherestudio/shared/common/util/index';
import mixin from '@dataspherestudio/shared/common/service/mixin';

export default {
  props: {
    cluster: {
      type: String,
      required: true,
    },
    dbCode: {
      type: String,
      required: false,
    },
    tableName: {
      type: String,
      required: false,
    },
  },
  data() {
    return {
      height: 0,
      visualSrc: '',
      isRefresh: true,
      hasInitialized: false, // 防止重复初始化
    };
  },
  mixins: [mixin],
  mounted() {
    // 创建的时候设置宽高
    this.resize(window.innerHeight);
    // 监听窗口变化，获取浏览器宽高
    window.addEventListener('resize', this.resize(window.innerHeight));

    // 初始时如果 dbCode 已存在，直接初始化
    if (this.dbCode) {
      this.initMicroApp();
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resize(window.innerHeight));
  },
  methods: {
    resize(height) {
      this.height = height;
    },
    reload() {
      this.isRefresh = false;
      this.$nextTick(() => this.isRefresh = true);
    },
    generateUrlTail(){
      const tail = [];
      tail.push('app_id=facade-framework');
      tail.push('timestamp=1659924920342');
      tail.push('nonce=12345');
      tail.push('user=dss');
      tail.push('signature=eca1a93c2c2bb8fc55972d76d0c1267c7782f51552a5d4562a2d871a63f64168');
      return tail.join('&');
    },
    humpToLine(str){
      let s = str.replace(/([A-Z])/g, '_$1').toLowerCase().slice(1);
      // 去掉多余空格
      return s.replace(/\s+/g, '');
    },
    async initMicroApp() {
      if (this.hasInitialized) return; // 防止重复初始化
      try {
        // 查询绑定信息
        const sub_mf_name = 'dms_bdp_source_table_change';
        const data = await api.fetch(`/mfgov/fesdk/bindQuery/v1?${this.generateUrlTail()}`, {
          main_mf_name: 'dss',
          sub_mf_name,
        }, {
          method: 'get',
          baseURL: process.env.VUE_APP_MICRO_PREFIX,
          headers: {
            proxyUser: this.getUserName(),
          }
        });
        let queryStr = `baseUrl=${data.accessLocation.split('#')[0].replace(/\/$/, '')}`;
        queryStr += `&cluster=${this.cluster}`;
        if(this.dbCode) {
          queryStr += `&dbCode=${this.dbCode}`
        }
        if(this.tableName) {
          queryStr += `&tableName=${this.tableName}`
        }
        this.visualSrc = util.replaceHolder(`${data.accessLocation}?${queryStr}` || '');
        this.hasInitialized = true;
      } catch (err) {
        console.warn('-------', err);
      }
    }
  },
  watch: {
    dbCode: {
      handler(newVal) {
        if (newVal) {
          this.initMicroApp();
        }
      },
      immediate: false, // 不立即执行，因为 mounted 中已处理
    },
  },
};
</script>
<style scoped>
.bdp-src-change-container {
  height: 100%;
  width: 100%;
}

.bdp-src-change-iframe {
  height: 100%;
  width: 100%;
  border: none;
}
</style>
