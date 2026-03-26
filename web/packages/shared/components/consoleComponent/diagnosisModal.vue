<template>
  <Modal
    v-model="visible"
    :title="modalTitle"
    :width="isFullscreen ? '90%' : 800"
    :fullscreen="isFullscreen"
    :closable="false"
  >
    <template #header>
      <div class="diagnosis-modal-header">
        <span>{{ modalTitle }}</span>
        <div @click="toggleFullscreen" class="fullscreen-icon">
          <Icon
            :type="isFullscreen ? 'md-contract' : 'md-expand'"
            size="20"
          />
          <span style="vertical-align: middle;" >{{ isFullscreen ? $t('message.scripts.constants.logPanelList.releaseFullScreen') : $t('message.scripts.constants.logPanelList.fullScreen') }}</span>
        </div>
      </div>
    </template>
    
    <div class="diagnosis-content" :class="{ 'fullscreen-content': isFullscreen }">
      <VueShowdown :markdown="content" class="markdown-content" />
    </div>
    <template #footer>
        <Button type="primary" @click="handleClose">{{$t('message.dataService.ok')}}</Button>
    </template>
  </Modal>
</template>

<script>
import { VueShowdown } from 'vue-showdown';

export default {
  name: 'DiagnosisModal',
  components: {
    VueShowdown
  },
  props: {
    taskId: {
      type: Number,
      default: null,
    },
    diagnosisRes: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      visible: false,
      isFullscreen: false,
      content: ''
    };
  },
  computed: {
    modalTitle() {
      return `任务${this.taskId}Ai诊断建议`;
    }
  },
  methods: {
    show() {
      this.visible = true;
      this.extractContent();
    },
    handleClose() {
      this.visible = false;
      this.isFullscreen = false;
    },
    toggleFullscreen() {
      this.isFullscreen = !this.isFullscreen;
    },
    extractContent() {
      if (this.diagnosisRes && this.diagnosisRes.diagnosisMsg) {
        const diagnosisMsg = typeof this.diagnosisRes.diagnosisMsg === 'string' 
          ? JSON.parse(this.diagnosisRes.diagnosisMsg).data 
          : this.diagnosisRes.diagnosisMsg;
        
        if (diagnosisMsg.suggestions && diagnosisMsg.suggestions.length > 0) {
          const suggestion = diagnosisMsg.suggestions[0];
          this.content = suggestion.reason || '';
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.diagnosis-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

    
    .fullscreen-icon {
      cursor: pointer;
      transition: color 0.3s;
      
      &:hover {
        color: #2d8cf0;
      }
    }
}

.diagnosis-content {
  max-height: 600px;
  overflow: auto;
  
  &.fullscreen-content {
    max-height: 80vh;
  }
}

.markdown-content {
  line-height: 1.6;
  font-size: 14px;
  
  // Markdown 样式
  :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    margin-top: 1.5em;
    margin-bottom: 0.5em;
    font-weight: 600;
    line-height: 1.25;
  }
  
  :deep(h1) {
    font-size: 2em;
    border-bottom: 1px solid #eaecef;
    padding-bottom: 0.3em;
  }
  
  :deep(h2) {
    font-size: 1.5em;
    border-bottom: 1px solid #eaecef;
    padding-bottom: 0.3em;
  }
  
  :deep(h3) {
    font-size: 1.25em;
  }
  
  :deep(p) {
    margin-top: 0;
    margin-bottom: 1em;
  }
  
  :deep(ul), :deep(ol) {
    padding-left: 2em;
    margin-top: 0;
    margin-bottom: 1em;
  }
  
  :deep(li) {
    margin: 0.25em 0;
  }
  
  :deep(code) {
    padding: 0.2em 0.4em;
    margin: 0;
    font-size: 85%;
    background-color: rgba(27, 31, 35, 0.05);
    border-radius: 3px;
    font-family: monospace;
  }
  
  :deep(pre) {
    padding: 16px;
    overflow: auto;
    font-size: 85%;
    line-height: 1.45;
    background-color: #f6f8fa;
    border-radius: 3px;
    margin: 1em 0;
    
    code {
      background-color: transparent;
      padding: 0;
      font-size: 100%;
    }
  }
  
  :deep(blockquote) {
    padding: 0 1em;
    color: #6a737d;
    border-left: 0.25em solid #dfe2e5;
    margin: 0 0 1em 0;
  }
  
  :deep(table) {
    border-spacing: 0;
    border-collapse: collapse;
    margin: 1em 0;
    width: 100%;
    
    th, td {
      padding: 6px 13px;
      border: 1px solid #dfe2e5;
    }
    
    th {
      font-weight: 600;
      background-color: #f6f8fa;
    }
    
    tr {
      background-color: #fff;
      border-top: 1px solid #c6cbd1;
      
      &:nth-child(2n) {
        background-color: #f6f8fa;
      }
    }
  }
  
  :deep(hr) {
    height: 0.25em;
    padding: 0;
    margin: 24px 0;
    background-color: #e1e4e8;
    border: 0;
  }
  
  :deep(a) {
    color: #0366d6;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
  
  :deep(strong) {
    font-weight: 600;
  }
}
</style>
