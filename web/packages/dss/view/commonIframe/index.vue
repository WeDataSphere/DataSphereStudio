<template>
  <div class="iframeClass" ref="iframeContainer">
    <iframe
      ref="ifr"
      class="iframeClass"
      v-if="shouldLoad && isRefresh"
      id="iframe"
      :src="visualSrc"
      frameborder="0"
      width="100%"
      :height="height"
      @load="onIframeLoad"/>
    <div v-if="!shouldLoad" class="iframe-placeholder">
      <Spin size="large">{{ $t('message.common.Loading') }}</Spin>
    </div>
    <Spin v-if="loading" fix>{{ $t('message.common.Loading') }}</Spin>
  </div>
</template>
<script>
import util from '@dataspherestudio/shared/common/util/index';
import { debounce } from 'lodash';

export default {
  props: {
    url: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      height: 0,
      visualSrc: '',
      isRefresh: true,
      loading: true,
      shouldLoad: false, // 控制iframe是否加载
      observer: null,
      resizeHandler: null,
      unloadTimer: null
    };
  },
  watch: {
    async '$route.query.projectID'() {
      this.getUrl();
      this.reload()
    },
    async '$route.query.type'() {
      this.getUrl();
      this.reload()
    },
    async '$route.query.url'() {
      this.getUrl();
      this.reload()
    }
  },
  mounted() {
    this.getUrl();
    // 创建的时候设置宽高
    this.resize(window.innerHeight);
    
    // 使用防抖优化resize监听
    this.resizeHandler = debounce(() => {
      this.resize(window.innerHeight);
    }, 200);
    window.addEventListener('resize', this.resizeHandler);
    
    // 初始化懒加载观察器
    this.initIntersectionObserver();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeHandler);
    
    // 清理观察器
    if (this.observer) {
      this.observer.disconnect();
      this.observer = null;
    }
    
    // 清理定时器
    if (this.unloadTimer) {
      clearTimeout(this.unloadTimer);
    }
    
    // 清理iframe资源
    this.cleanupIframe();
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
        this.loading = true;
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
        this.cleanupIframe();
      }, 5000);
    },
    
    // iframe加载完成回调
    onIframeLoad() {
      this.loading = false;
      this.$emit('iframe-loaded');
    },
    
    // 清理iframe资源
    cleanupIframe() {
      if (this.shouldLoad) {
        // 卸载iframe
        this.shouldLoad = false;
        
        // 清空iframe内容以释放内存
        this.$nextTick(() => {
          const iframe = this.$refs.ifr;
          if (iframe) {
            iframe.src = 'about:blank';
          }
        });
        
        this.$emit('iframe-unload');
      }
    },
    
    getUrl() {
      let {url,__noreplace} = this.$route.query;
      if (this.url && !url) {
        return this.visualSrc = this.url
      }
      if (__noreplace) {
        this.visualSrc = url
      } else {
        this.visualSrc = util.replaceHolder(url, {
          projectId: this.$route.query.projectID,
          projectName: this.$route.query.projectName,
          workspaceId: this.$route.query.workspaceId,
        });
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
    }
  },
};
</script>
<style>
.iframeClass{
    height: 100%;
    display: block;
    background-color: #fff;
}

.iframe-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
}
</style>
