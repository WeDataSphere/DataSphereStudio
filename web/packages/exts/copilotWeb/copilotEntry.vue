
<template>
  <div
    class="copilot-entry-container"
    @mousemove="onMouseMove" :style="{'pointer-events': `${(isMouseDown) ? 'initial' : 'none'}`}"
  >
    <div v-show="!showCopilot" ref="copilotEntryItemChannel" :style="{top: top, left: left}" class="copilot-entry-tool">
      <div class="entry-btn" @mousedown.prevent.stop="onMouseDown">
        <SvgIcon class="nav-icon copilot-icon" icon-class="copilot" />
        <span class="navbar-item-name" @click="openCopilotWeb">Copilot</span>
      </div>
    </div>
    <div v-show="showCopilot" ref="copilotEntryContainer" :style="isFullscreen ? {} : {top: cTop, left: cLeft}" class="copilot-active" :class="isFullscreen ? 'fullscreen' : ''">
      <div class="tool-move" @mousedown.prevent.stop="(e) => onMouseDown(e, 'container')">
        <Poptip v-model="showCopilotTip" placement="left-start" trigger="hover">
          <div class="tool-empty-block"></div>
          <template #content>
            <span>长按左上角可以进行拖动哦！</span>
          </template>
        </Poptip>
      </div>
      <CopilotContainer @fullscreen="fullscreen" ref="copilot" />
    </div>
  </div>
</template>
<script>
import plugin from '@dataspherestudio/shared/common/util/plugin'
import CopilotContainer from './copilotContainer.vue'
export default {
  name: 'CopilotEntry',
  components: {
    CopilotContainer
  },
  data() {
    return {
      isMouseDown: false,
      isMouseMove: false,
      showCopilot: false,
      top: null,
      left: null,
      cTop: null,
      cLeft: null,
      showCopilotTip: false,
      isFullscreen: false
    }
  },
  mounted() {
    this.positionInfo = { x: 0, y: 0}
    document.addEventListener('mouseup', this.onMouseUp)
    window.addEventListener('resize', this.resetPosition)
    plugin.on('copilot_web_open_change', this.openCopilotWebChange)
  },
  methods: {
    fullscreen(data) {
      this.isFullscreen = data.fullscreen
    },
    onMouseUp () {
      this.isMouseDown = false;
    },
    // copolotWeb
    openCopilotWebChange({ type, message, params }) {
      switch (type) {
        case 'open':
          this.openCopilotWeb()
          break;
        case 'AiSql':
        case 'codeConvert':
        case 'codeExplain':
        case 'CodeCorrection':
        case 'DataAnalyst':
        case 'CodeOptimize':
          this.postActionToCopilotWeb(type, message, params);
          break;
        default:
          this.showCopilot = false
          break;
      }
    },
    postActionToCopilotWeb(type, message, params) { 
      this.showCopilot = true;
      if (this.$refs.copilot && this.$refs.copilot.messageChannel) {
        this.$refs.copilot.messageChannel.post({
          eventType: type, // 事件名
          source: "DSS",
          params: { message, params },
        })
      } else {
        setTimeout(() => {
          this.$refs.copilot.messageChannel.post({
            eventType: type, // 事件名
            source: "DSS",
            params: { message, params },
          })
        }, 3000)
      }
    },
    // 打开copilot
    openCopilotWeb() {
      if (this.isMouseMove) return
      this.showCopilot = true
      this.showCopilotTip = true
      setTimeout(() => {
        this.showCopilotTip = false
      }, 3000)
      this.$emit('openChange')
    },
    onMouseDown(e) {
      const copilotRef = this.showCopilot ? this.$refs.copilotEntryContainer : this.$refs.copilotEntryItemChannel;
      this.positionInfo = {
        x: e.clientX - copilotRef.offsetLeft,
        y: e.clientY - copilotRef.offsetTop
      }
      this.isMouseMove = false;
      this.isMouseDown = true;
    },
    onMouseMove(e) {
      if (!this.isMouseDown) return
      let x = e.clientX - this.positionInfo.x
      let y = e.clientY - this.positionInfo.y
      if (x > document.documentElement.clientWidth - 40) {
        x =  document.documentElement.clientWidth - 40
      }
      if (y > document.documentElement.clientHeight - 160) {
        y =  document.documentElement.clientHeight - 160
      }
      if (x < 20) {
        x = 20
      }
      if (y < 20) {
        y = 20
      }
      if (this.showCopilot) {
        this.cLeft = x + 'px'
        this.cTop = y + 'px'
      } else {
        this.left = x + 'px'
        this.top = y + 'px'
      }
      if (Math.abs(e.movementX) > 10 || Math.abs(e.movementY) > 10) {
        this.isMouseMove = true;
      }
    },
    resetPosition() {
      const coplilotToll = document.querySelector('.copilot-entry-container .copilot-entry-tool')
      if (coplilotToll) {
        coplilotToll.style.left = document.documentElement.clientWidth - 110 + 'px';
        coplilotToll.style.top = document.documentElement.clientHeight - 280 + 'px';
      }
    }
  },
  beforeDestroy() {
    document.removeEventListener('mouseup', this.onMouseUp)
    window.removeEventListener('resize', this.resetPosition)
  },
}
</script>
<style lang="scss">
.copilot-entry-container {
  position: fixed;
  right: 0;
  bottom: 0;
  top: 54px;
  left: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 1000;
  .copilot-entry-tool {
    position: absolute;
    height: auto;
    height: 30px;
    width: 80px;
    right: 50px;
    bottom: 200px;
    .entry-btn {
      background-image: linear-gradient(139deg, #77AEFF 0%, #5384FF 100%);
      border-radius: 12px;
      padding: 0 10px;
      height: 24px;
      line-height: 24px;
      color: #fff !important;
      pointer-events: initial;
      cursor: pointer;
      box-shadow: 0px 1px 4px rgba(0, 0, 0, .2);
      .copilot-icon {
        padding-right: 5px;
        cursor: move;
      }
    }
  }
  .copilot-active {
    position: absolute;
    right: 50px;
    top: 30px;
    width: max-content;
    height: 90%;
    .tool-move {
      height: 50px;
      min-width: calc(100% - 180px);
      pointer-events: initial;
      position: absolute;
      top: 0px;
      z-index: 1;
      cursor: move;
      .tool-empty-block {
        height: 100%;
        width: 100%;
      }
    }
    &.fullscreen {
      width: 90%;
    }
  }
  

  .fullscreen {
    .copilot-container {
      width: 100% !important;
      height: 100% !important;
      resize: none;
    }
  }
}
</style>