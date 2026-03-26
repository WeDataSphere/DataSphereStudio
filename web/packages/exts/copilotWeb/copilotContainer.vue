<template>
  <div class="copilot-container" v-dragresize="config" @mousedown="onMouseDown" @mousemove="onMouseMove" @mouseup="isMouseDown = false">
    <iframe
      class="copilot-container-iframe"
      v-if="isRefresh"
      id="copilotIframe"
      ref="copilotIframe"
      :src="visualSrc"
      frameborder="0"
      @load="copilotLoad"
      :style="{'pointer-events': `${(isMouseDown && isMouseMove) ? 'none' : 'initial'}`}"
    />
    <div class="copilot-container-loadding" v-if="!isLoaded">
      <p class="loading-text">加载中，请稍后...</p>
    </div>
  </div>
</template>
<script>
import vue from 'vue'
import dragresize from 'v-dragresize'
import plugin from '@dataspherestudio/shared/common/util/plugin'
import eventbus from '@dataspherestudio/shared/common/helper/eventbus';
import Broadcastchannel from './postMessage'
vue.use(dragresize)
export default {
  props: {
    visualSrc: {
      type: String,
      default: origin + `/dss/copilot/?timestamp=${Date.now()}#/chat`
    },
    eventId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      height: 0,
      isRefresh: true,
      showCopilot: false,
      messageChannel: null,
      isLoaded: false,
      isMouseDown: false,
      isMouseMove: false,
      config: [{
        dragBorder: 'bottom',
        setCssProperty: 'height',
        eventPropagation: true,
        dragDoneHandle: () => {
          this.isMouseDown = false
          this.isMouseMove = false
        }
      },{
        dragBorder: 'right',
        setCssProperty: 'width',
        eventPropagation: true,
        dragDoneHandle: () => {
          this.isMouseDown = false
          this.isMouseMove = false
        }
      }]
    };
  },
  watch: {
    async 'visualSrc'() {
      this.reload()
    }
  },
  mounted() {
    // 创建的时候设置宽高
    this.height = window.innerHeight
    eventbus.on('theme.change', this.postEhemeChange);
  },
  beforeDestroy() {
    this.messageChannel && this.messageChannel.removeListener();
    eventbus.off('theme.change', this.postEhemeChange);
    this.messageChannel = null
  },
  methods: {
    onMouseDown(e) {
      this.isMouseDown = true;
    },
    onMouseMove(e) {
      this.isMouseMove = true;
    },
    postEhemeChange(theme) {
      this.messageChannel.post({
        eventType: "theme", // 事件名
        source: "DSS",
        params: {"theme": theme},
      })
    },
    copilotLoad() {
      this.isLoaded = true
      if (this.$refs.copilotIframe && !this.messageChannel) {
        this.messageChannel = new Broadcastchannel(this.$refs.copilotIframe.contentWindow);
      }
      this.messageChannel.listener(this.messageChannelListener);
    },
    messageChannelListener(data) {
      if (data.source === 'common') {
        switch (data.eventType) {
          case 'mounted':
            this.postEhemeChange(localStorage.getItem('theme'))
            break;
          case 'fullscreen':
            this.$emit('fullscreen', data.params)
            break;
          case 'close':
            plugin.emit('copilot_web_open_change', { type: 'close' })
            break;
          default:
            break;
        }
      }
      plugin.emit('copilot_web_listener_event', data)
    },
    reload() {
      this.isRefresh = false;
      this.$nextTick(() => this.isRefresh = true);
    }
  },
};
</script>
<style lang="scss">
@import "@dataspherestudio/shared/common/style/variables.scss";
.copilot-container{
  pointer-events: initial;
  height: 100%;
  width: 800px;
  position: relative;
  // top: 30px;
  // left: 0;
  min-width: 300px;
  min-height: 40%;
  resize: both;
  cursor: auto;
  overflow: hidden;
  @include guide-box-shadow(#eee, $dark-border-color-base);
  border: 1px solid;
  @include border-color(#eee, $dark-border-color-base);
  .copilot-container-iframe {
    height: 100%;
    width: 100%;
  }
  .copilot-container-loadding {
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    bottom: 0;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    @include bg-color($workspace-background, $dark-workspace-background);
  }
}
</style>
  