<template>
  <div class="iframeClass" ref="iframeContainer">
    <iframe
      class="iframeClass"
      v-if="shouldLoad && isRefresh"
      id="mircoApp"
      :src="visualSrc"
      frameborder="0"
      width="100%"
      sandbox="allow-forms allow-modals allow-popups allow-same-origin allow-scripts allow-top-navigation"
      :height="height"
      @load="onIframeLoad">
    </iframe>
    <div v-if="!shouldLoad" class="iframe-placeholder">
      <Spin size="large">{{ $t('message.common.Loading') }}</Spin>
    </div>
  </div>
</template>
<script>
import api from '@dataspherestudio/shared/common/service/api';
import util from '@dataspherestudio/shared/common/util/index';
import mixin from '@dataspherestudio/shared/common/service/mixin';
import { debounce } from 'lodash';

export default {
  data() {
    return {
      height: 0,
      visualSrc: '',
      isRefresh: true,
      shouldLoad: false, // 控制iframe是否加载
      isLoading: false,
      observer: null,
      resizeHandler: null
    };
  },
  mixins: [mixin],
  mounted() {
    // 创建的时候设置宽高
    this.resize(window.innerHeight);
    
    // 使用防抖优化resize监听
    this.resizeHandler = debounce(() => {
      this.resize(window.innerHeight);
    }, 200);
    window.addEventListener('resize', this.resizeHandler);

    // 初始化懒加载观察器
    this.initIntersectionObserver();
    
    this.initMicroApp();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeHandler);
    
    // 清理观察器
    if (this.observer) {
      this.observer.disconnect();
      this.observer = null;
    }
    
    // 清理iframe资源
    this.cleanupIframe();
  },
  watch: {
    '$route.params.appName'() {
      // 切换应用, 重新获取数据
      this.initMicroApp();
      this.reload();
    },
  },
  methods: {
    resize(height) {
      this.height = height;
    },
    
    // 初始化IntersectionObserver实现懒加载
    initIntersectionObserver() {
      if (!this.$refs.iframeContainer) return;
      
      // 检查浏览器支持
      if (!('IntersectionObserver' in window)) {
        // 不支持则直接加载
        this.shouldLoad = true;
        return;
      }
      
      this.observer = new IntersectionObserver(
        (entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              // iframe进入可视区域，开始加载
              this.loadIframe();
            } else {
              // iframe离开可视区域，延迟卸载以避免频繁切换
              this.deferUnload();
            }
          });
        },
        {
          root: null,
          rootMargin: '50px', // 提前50px开始加载
          threshold: 0.1
        }
      );
      
      this.observer.observe(this.$refs.iframeContainer);
    },
    
    // 加载iframe
    loadIframe() {
      if (!this.shouldLoad) {
        this.shouldLoad = true;
        this.$emit('iframe-load-start');
      }
    },
    
    // 延迟卸载iframe（避免快速切换时频繁加载）
    deferUnload() {
      if (this.unloadTimer) {
        clearTimeout(this.unloadTimer);
      }
      
      // 延迟5秒卸载，给用户足够的切换时间
      this.unloadTimer = setTimeout(() => {
        // 只有当iframe不在可视区域时才卸载
        if (!this.shouldLoad) {
          this.cleanupIframe();
        }
      }, 5000);
    },
    
    // iframe加载完成回调
    onIframeLoad() {
      this.isLoading = false;
      this.$emit('iframe-loaded');
    },
    
    // 清理iframe资源
    cleanupIframe() {
      if (this.shouldLoad) {
        // 保存当前URL以便恢复
        const currentSrc = this.visualSrc;
        
        // 卸载iframe
        this.shouldLoad = false;
        
        // 清空iframe内容以释放内存
        this.$nextTick(() => {
          const iframe = document.getElementById('mircoApp');
          if (iframe) {
            iframe.src = 'about:blank';
          }
        });
        
        this.$emit('iframe-unload');
      }
    },
    
    reload() {
      this.cleanupIframe();
      this.$nextTick(() => {
        this.isRefresh = false;
        this.$nextTick(() => {
          this.isRefresh = true;
          this.loadIframe();
        });
      });
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
      console.log(process.env.VUE_APP_MICRO_PREFIX)
      try {
        // 查询绑定信息
        const mainMfName = this.$route.query.mainMfName ? this.$route.query.mainMfName : 'dss';
        const sub_mf_name = this.humpToLine(this.$route.params.appName);
        const data = await api.fetch(`/mfgov/fesdk/bindQuery/v1?${this.generateUrlTail()}`, {
          main_mf_name: mainMfName,
          sub_mf_name,
        }, {
          method: 'get',
          baseURL: process.env.VUE_APP_MICRO_PREFIX,
          headers: {
            proxyUser: this.getUserName(),
          }
        });
        let queryStr = `baseUrl=${data.accessLocation.split('#')[0].replace(/\/$/, '')}`;
        if(sub_mf_name.toLowerCase() === 'search') {
          queryStr += '&dmsSearch=true'
        }
        if(mainMfName === 'visualis_hub' && sub_mf_name.toLowerCase() === 'dms_data_search') {
          queryStr += '&pageType=commonSearch&user=dsscui&showMenu=false';
          
          // 过滤掉 mainMfName 和 noHeader 参数
          const currentQuery = this.$route.query;
          const filteredParams = Object.keys(currentQuery)
            .filter(key => key !== 'mainMfName' && key !== 'noHeader')
            .map(key => `${key}=${currentQuery[key]}`)
            .join('&');
          
          if (filteredParams) {
            queryStr += `&${filteredParams}`;
          }
        }
        this.visualSrc = util.replaceHolder(`${data.accessLocation}?${queryStr}` || '');
      } catch (err) {
        console.warn('-------', err);
      }
    }
  },
};
</script>
<style>
  .iframeClass {
    height: 100%;
  }
</style>
