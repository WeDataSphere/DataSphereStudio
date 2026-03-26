<template>
  <div class="workflow-resource">
    <div class="workflow-resource-upload">
      {{ fileList }}
      <FUpload
        ref="uploadRef"
        v-model:file-list="fileList"
        :action="updateUrl"
        :multiple-limit="maxSize"
        :before-upload="beforeUpload"
        :disabled="readonly"
        @success="handleSuccess"
        @error="handleError"
        @progress="handleProgress"
        @exceed="exceededSize"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { useI18n } from 'vue-i18n';

import {
  defineComponent,
  ref,
  reactive,
  computed,
  onMounted,
  watch,
  unref,
} from 'vue';
import { FUpload, FMessage } from '@fesjs/fes-design';
import { request } from '@dataspherestudio/shared';
import { remove } from 'lodash';

interface NodeType {
  [key: string]: string;
}

const NODETYPE: NodeType = {
  SHELL: 'linkis.shell.sh',
  HQL: 'linkis.hive.hql',
  SPARKSQL: 'linkis.spark.sql',
  SPARKPY: 'linkis.spark.py',
  SCALA: 'linkis.spark.scala',
  PYTHON: 'linkis.python.python',
  CONNECTOR: 'linkis.control.empty',
  DISPLAY: 'linkis.appconn.visualis.display',
  DASHBOARD: 'linkis.appconn.visualis.dashboard',
  WIDGET: 'linkis.appconn.visualis.widget',
  VIEW: 'linkis.appconn.visualis.view',
  SENDMAIL: 'linkis.appconn.sendemail',
  EVENTCHECKERF: 'linkis.appconn.eventchecker.eventsender',
  EVENTCHECKERW: 'linkis.appconn.eventchecker.eventreceiver',
  DATACHECKER: 'linkis.appconn.datachecker',
  RMBSENDER: 'azkaban.rmbsender',
  FLOW: 'workflow.subflow',
  EXCHANGE: 'linkis.data.exchange',
  QUALITIS: 'linkis.appconn.qualitis',
  PROJECTNODE: 'projectNode',
  MLSS: 'linkis.appconn.mlss',
  JDBC: 'linkis.jdbc.jdbc',
};

export default defineComponent({
  name: 'WorkflowResource',
  components: {
    FUpload,
  },
  props: {
    resources: {
      type: Array as () => any[],
      default: () => [],
    },
    projectName: {
      type: String,
      default: '',
    },
    isRipetition: {
      type: Boolean,
      default: false,
    },
    nodeType: {
      type: String,
      default: '',
    },
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    const { t: $t } = useI18n();
    const fileList = ref<any[]>([]);
    const uploadFiles = ref<any[]>([]);
    const updateUrl = ref('');
    const uploadData = reactive({
      system: 'WTSS',
      isExpire: true,
      projectName: '',
    });
    const maxSize = ref(204800);
    const isUploading = ref(false);
    const uploadRef = ref<any>(null);

    const init = () => {
      updateUrl.value = `bml/uploadShareResource`;
      uploadData.projectName =
        (unref(props) as any).projectName ||
        (unref(props) as any).route.query.projectName;
      maxSize.value = 204800;
      setData();
    };

    const setData = () => {
      if (props.resources.length) {
        uploadFiles.value = props.resources.map((item) => ({
          ...item,
          name: item.fileName,
        }));
      }
    };

    const reset = () => {
      uploadFiles.value = [];
    };

    const beforeUpload = (file: File) => {
      const isInFlag = uploadFiles.value.find(
        (item) => item.name === file.name
      );
      const regLeaf = /^[-.\w\u4e00-\u9fa5]{1,200}\.[A-Za-z]+$/;
      const sizeResult = file.size >= 200 * 1024 * 1024;

      if (props.isRipetition) {
        if (props.nodeType === NODETYPE.SPARKPY) {
          if (!/\\.zip$/i.test(file.name)) {
            FMessage.warning($t('resourceBar.PYSPARKNODE'));
            return false;
          }
        } else if (props.nodeType === NODETYPE.SPARKSQL) {
          if (!/\\.jar$/i.test(file.name)) {
            FMessage.warning($t('resourceBar.SPARKNOSW'));
            return false;
          }
        }
        if (isInFlag) {
          uploadData.resourceId = isInFlag.resourceId;
        }
      }

      if (isInFlag && !props.isRipetition) {
        FMessage.warning($t('resourceBar.WJYCZ'));
        return false;
      } else if (!regLeaf.test(file.name)) {
        FMessage.warning($t('resourceBar.WJMCBHF'));
        return false;
      } else if (sizeResult) {
        FMessage.warning($t('resourceBar.SCBCG200'));
        return false;
      }
    };

    const handleSuccess = (param) => {
      console.log(
        '[upload.singleUpload] [remove] param:',
        param,
        ' fileList.value:',
        fileList.value
      );
      // if (response.status === 0) {
      //   FMessage.success($t('resourceBar.SCCG', { name: file.name }));
      //   if (props.isRipetition) {
      //     remove(fileList, item => item.name === file.name && item.resourceId !== file.resourceId);
      //     remove(uploadFiles.value, item => item.name === file.name);
      //   }
      //   uploadFiles.value.push({ ...file, fileName: file.name, resourceId: response.data.resourceId, version: response.data.version });
      //   emit('update-resources', uploadFiles.value);
      // } else {
      //   remove(fileList, item => item.name === file.name);
      //   FMessage.warning(response.message);
      // }
      // isUploading.value = false;
    };

    const handleError = (err: Error, file: File) => {
      console.log('handleError');
      isUploading.value = false;
      FMessage.error(err ? err.message || $t('_.上传失败') : $t('_.上传失败'));
    };

    const handleProgress = (event: ProgressEvent, file: File) => {
      console.log('handleProgress');
      if (file.status === 'uploading') {
        isUploading.value = true;
      } else {
        isUploading.value = false;
      }
    };

    const exceededSize = () => {
      console.log('exceededSize');
      FMessage.warning($t('resourceBar.WJCCXE'));
    };

    const handleRemove = (file: any) => {
      console.log('handleRemove', file);
      request
        .fetch(
          '/bml/deleteResource',
          {
            resourceId: file.resourceId,
          },
          'post'
        )
        .then(() => {
          remove(
            uploadFiles.value,
            (item) => item.resourceId === file.resourceId
          );
          emit('update-resources', uploadFiles.value);
          FMessage.success($t('resourceBar.WJBSC', { name: file.name }));
        });
    };

    onMounted(() => {
      init();
    });

    watch(
      () => props.resources,
      (val) => {
        setData();
        if (!val.length) {
          reset();
        }
      },
      { immediate: true }
    );

    return {
      uploadFiles,
      updateUrl,
      uploadData,
      maxSize,
      isUploading,
      fileList,
      beforeUpload,
      handleSuccess,
      handleError,
      handleProgress,
      exceededSize,
      handleRemove,
    };
  },
});
</script>
