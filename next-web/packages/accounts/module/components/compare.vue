<template>
  <div ref="eaditorRef" class="monaco-editor" :style="{ height: '300px' }" />
</template>

<script lang="ts" setup name="WeEditorCompare">
import * as monaco from 'monaco-editor';
import { ref, watch, onBeforeUnmount, onMounted } from 'vue';

const props = defineProps({
  original: String,
  value: {
    type: String,
    required: true,
  },
  theme: {
    type: String,
    default: 'myCustomTheme',
  },
  language: {
    type: String,
    default: 'sql',
  },
  options: {
    type: Object,
    default: () => ({}),
  },
  diffEditor: {
    type: Boolean,
    default: false,
  },
  readOnly: {
    type: Boolean,
    default: false,
  },
});

let monacoInstance: Record<string, any>;
let originalModel: Record<string, any>;
let modifiedModel: Record<string, any>;
const eaditorRef = ref<HTMLElement | null>(null);

monaco.editor.defineTheme('myCustomTheme', {
  base: 'vs',
  inherit: true,
  rules: [],
  colors: {
    'editor.background': '#f8f8f8',
    'editor.lineHighlightBackground': '#ECECEC',
  },
});

function initMonacoEditor() {
  const options = {
    value: props.value,
    theme: props.theme,
    language: props.language,
    automaticLayout: true,
    scrollBeyondLastLine: false,
    minimap: {
      enabled: false,
    },
    readOnly: props.readOnly,
    ...props.options,
  };
  if (props.diffEditor) {
    monacoInstance = monaco.editor.createDiffEditor(
      eaditorRef.value as HTMLElement,
      options
    );
    originalModel = monaco.editor.createModel(
      props.original as string,
      props.language
    );
    modifiedModel = monaco.editor.createModel(
      props.value as string,
      props.language
    );
    monacoInstance?.setModel({
      original: originalModel,
      modified: modifiedModel,
    });
  } else {
    monacoInstance = monaco.editor.create(
      eaditorRef.value as HTMLElement,
      options
    );
  }
}

watch(
  () => props.value,
  (newValue) => {
    if (monacoInstance) {
      modifiedModel.dispose(); // 释放修改后模型
      modifiedModel = monaco.editor.createModel(
        newValue as string,
        props.language
      ); // 创建新的修改后模型
      const { original } = monacoInstance?.getModel();
      monacoInstance?.setModel({
        original,
        modified: modifiedModel,
      });
    }
  },
  { deep: true }
);

watch(
  () => props.original,
  (newValue) => {
    if (monacoInstance) {
      originalModel.dispose(); // 释放修改后模型
      originalModel = monaco.editor.createModel(
        newValue as string,
        props.language
      ); // 创建新的修改后模型
      const { modified } = monacoInstance?.getModel();
      monacoInstance?.setModel({
        original: originalModel,
        modified,
      });
    }
  },
  { deep: true }
);

watch(
  () => props.language,
  (newValue) => {
    if (monacoInstance) {
      monaco.editor.setModelLanguage(
        monacoInstance.getModel(),
        newValue as string
      );
    }
  }
);

onMounted(() => {
  initMonacoEditor();
  window.addEventListener('resize', () => {
    if (monacoInstance) {
      monacoInstance.layout(); // 重新布局编辑器
    }
  });
});

onBeforeUnmount(() => {
  monacoInstance?.dispose();
});
</script>

<style lang="less">
.monaco-editor {
  height: 300px;
}
</style>
