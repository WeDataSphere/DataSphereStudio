<template>
  <Modal
    v-model="ProjectShow"
    :title="actionType === 'add' ? $t('message.orchestratorModes.createOrchestrator') : $t('message.orchestratorModes.modeifyOrchestrator')"
    :closable="false">
    <Form
      :label-width="118"
      ref="projectForm"
      :model="workflowDataCurrent"
      :rules="formValid"
      v-if="workflowDataCurrent">
      <FormItem
        :label="$t('message.orchestratorModes.orchestratorName')"
        prop="orchestratorName">
        <Input
          :disabled="isPublished"
          v-model="workflowDataCurrent.orchestratorName"
          :placeholder="$t('message.workflow.inputFlowName')"
        ></Input>
      </FormItem>
      <FormItem :label="$t('message.orchestratorModes.orchestratorMode')" prop="orchestratorMode">
        <Select v-model="workflowDataCurrent.orchestratorMode">
          <Option v-for="item in selectOrchestratorList" :key="item.dicKey" :value="item.dicKey">
            {{ item.dicName}}
          </Option>
        </Select>
      </FormItem>
      <!-- 不同的编排类型显示不同的编排方式， 动态接口获取 -->
      <template v-if="workflowDataCurrent.orchestratorMode">
        <FormItem v-if="selectOrchestrator.dicValue === FORMITEMTYPE.RADIO" :label="$t('message.orchestratorModes.orchestratorMethod')" prop="orchestratorWayString"
          :rules="[{ required: true, trigger: 'blur', message: $t('message.workflow.orchestratorWayString') }]" key="orchestratorWayString">
          <RadioGroup v-model="workflowDataCurrent.orchestratorWayString">
            <Radio v-for="item in orchestratorModeList.mapList[workflowDataCurrent.orchestratorMode]" :key="item.dicKey" :label="item.dicKey">
              <span>{{item.dicName}}</span>
            </Radio>
          </RadioGroup>
        </FormItem>
        <FormItem :label="$t('message.orchestratorModes.orchestratorMethod')" key="orchestratorWayString"
          v-if="selectOrchestrator.dicValue === FORMITEMTYPE.SELECT" prop="orchestratorWayString"
          :rules="[{ required: true, trigger: 'blur', message: $t('message.workflow.orchestratorWayString') }]">
          <Select v-model="workflowDataCurrent.orchestratorWayString">
            <Option v-for="item in orchestratorModeList.mapList[workflowDataCurrent.orchestratorMode]" :key="item.dicKey" :value="item.dicKey">
              {{ item.dicName}}
            </Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('message.orchestratorModes.orchestratorMethod')" key="orchestratorWayArray"
          v-if="selectOrchestrator.dicValue === FORMITEMTYPE.CHECKBOX" prop="orchestratorWayArray"
          :rules="[{ required: true, trigger: 'blur', type: 'array', message: $t('message.workflow.orchestratorWayString') }]">
          <CheckboxGroup v-model="workflowDataCurrent.orchestratorWayArray">
            <Checkbox v-for="item in orchestratorModeList.mapList[workflowDataCurrent.orchestratorMode]" :label="item.dicKey" :key="item.dicKey">
              <span style="margin-left: 10px">{{item.dicName}}</span>
            </Checkbox>
          </CheckboxGroup>
        </FormItem>
      </template>
      <FormItem
        :label="$t('message.workflow.use')"
        prop="uses"
        key="uses">
        <we-tag
          :new-label="$t('message.workflow.addUse')"
          :tag-list="workflowDataCurrent.uses"
          @add-tag="addTag"
          @delete-tag="deleteTag"></we-tag>
      </FormItem>
      <FormItem :label="$t('message.orchestratorModes.level')" key="orchestratorLevel">
        <Select v-model="workflowDataCurrent.orchestratorLevel">
          <Option v-for="item in levels" :key="item" :value="item">
            {{ item}}
          </Option>
        </Select>
      </FormItem>
      <FormItem
        :label="$t('message.workflow.workflowDesc')"
        prop="description"
        key="description">
        <Input
          v-model="workflowDataCurrent.description"
          type="textarea"
          :maxlength=201
          :placeholder="$t('message.workflow.inputWorkflowDesc')"></Input>
      </FormItem>
      <FormItem
        :label="$t('message.Project.defaultResTemplate')"
        prop="templateIds"
        key="templateIds"
        v-if="!$APP_CONF.open_source"
      >
        <Select v-model="workflowDataCurrent.templateIds" @on-change="handleTemplateIdsChange" multiple>
          <OptionGroup v-for="e in defaultTemplates" :label="e.enginType" :key="e.type">
            <Option v-for="item in e.child" :value="item.templateId" :key="item.templateId" :disabled="item.disabled">{{ item.templateName }}</Option>
          </OptionGroup>
        </Select>
      </FormItem>
      <FormItem
        :label="$t('message.Project.isQuoteTemplate')"
        prop="isDefaultReference"
        key="isDefaultReference"
        v-if="!$APP_CONF.open_source && workflowDataCurrent.templateIds && workflowDataCurrent.templateIds.length > 0"
      >
        <RadioGroup v-model="workflowDataCurrent.isDefaultReference">
          <Radio label="1"><span>是</span></Radio>
          <Radio label="0"><span>否</span></Radio>
        </RadioGroup>
      </FormItem>
    </Form>
    <div slot="footer">
      <Button
        type="text"
        size="large"
        @click="Cancel">{{$t('message.workflow.cancel')}}</Button>
      <Button
        type="primary"
        size="large"
        :disabled="submiting"
        :loading="submiting"
        @click="Ok">{{$t('message.workflow.ok')}}</Button>
    </div>
  </Modal>
</template>
<script>
import tag from '@dataspherestudio/shared/components/tag/index.vue';
import api from '@dataspherestudio/shared/common/service/api';
import { useData } from './useData.js';
const FORMITEMTYPE = {
  RADIO: 'radio',
  SELECT: 'select',
  CHECKBOX: 'checkbox'
}
const {
  getTemplateDatas,
  getTemplateByOrchestratorId,
} = useData();
export default {
  components: {
    'we-tag': tag,
  },
  props: {
    workflowData: {
      type: Object,
      default: null,
    },
    addProjectShow: {
      type: Boolean,
      default: false,
    },
    actionType: {
      type: String,
      default: '',
    },
    projectData: {
      type: Object,
      default: () => {}
    },
    orchestratorModeList: {
      type: Object,
      default: () => {}
    },
    selectOrchestratorList: {
      type: Array,
      default: () => []
    },
    classifyList: {
      type: Array,
      default: () => []
    },
  },
  data() {
    return {
      submiting: false,
      ProjectShow: false,
      originBusiness: '',
      isPublished: false,
      workflowDataCurrent: {},
      FORMITEMTYPE,
      levels: [],
      initTemplates: [],
      defaultTemplates: [],
    };
  },
  computed: {
    formValid() {
      return {
        orchestratorName: [
          { required: true, message: this.$t('message.workflow.enterName'), trigger: 'blur' },
          { message: `${this.$t('message.workflow.nameLength')}128`, max: 128 },
          { type: 'string', pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: this.$t('message.workflow.validNameDesc'), trigger: 'blur' },
        ],
        description: [
          { required: true, message: this.$t('message.workflow.enterDesc'), trigger: 'blur' },
          { message: `${this.$t('message.workflow.descLength')}200`, max: 200 },
        ],
        isDefaultReference: [
          { required: true, message: this.$t('message.enginelist.ruleform.plsselect'), trigger: 'blur' },
        ],
        orchestratorMode: [
          { required: true, trigger: 'blur', message: this.$t('message.workflow.orchestratorMode') }
        ]
      }
    },
    selectOrchestrator() {
      return this.orchestratorModeList.list.find((item) => item.dicKey === this.workflowDataCurrent.orchestratorMode);
    }
  },
  watch: {
    async addProjectShow(val) {
      this.ProjectShow = val;
    },
    ProjectShow(val) {
      if (val) {
        this.originBusiness = this.workflowDataCurrent.uses;
        this.submiting = false; // ProjectShow为true时确认按钮可以点击
      }
      this.$emit('show', val);
    },
    async workflowData(value){
      const currentData = value;
      if (value && value.orchestratorWays && this.orchestratorModeList.list.length > 0) {
        if ([this.FORMITEMTYPE.RADIO, this.FORMITEMTYPE.SELECT].includes(this.orchestratorModeList.list.find((item) => item.dicKey === value.orchestratorMode).dicValue)) {
          currentData.orchestratorWayString = value.orchestratorWays[0];
        } else {
          currentData.orchestratorWayArray = value.orchestratorWays;
        }
      }
      let res = [];
      if (currentData.orchestratorId) {
        res = await getTemplateByOrchestratorId({orchestratorId: currentData.orchestratorId})
      }
      currentData.templateIds = res;
      this.workflowDataCurrent = {
        ...currentData
      };
      this.getTemplateDataByOrchestrator()
    }
  },
  mounted() {
    this.fetchLevelData()
  },
  methods: {
    async getTemplateDataByOrchestrator() {
      if(!this.$APP_CONF.open_source) {
        const params = {
          projectId: this.projectData.id,
          orchestratorId: this.workflowDataCurrent.orchestratorId
        }
        this.initTemplates = await getTemplateDatas(params);
        // this.defaultTemplates = JSON.parse(JSON.stringify(this.initTemplates))
        this.handleTemplateIdsChange(this.workflowDataCurrent.templateIds)
      }
    },
    handleTemplateIdsChange(vArray) {
      this.defaultTemplates = JSON.parse(JSON.stringify(this.initTemplates))
      vArray.forEach(v => {
        const curType = this.searchValueType(v);
        this.defaultTemplates.forEach((type,index) => {
          if (type.enginType === curType) {
            type.child.forEach(template => {
              if (template.templateId === v) {
                template.disabled = false
              }
              else {
                template.disabled = true
              }
            }
            )
          }
          this.$set(this.defaultTemplates, index, type)
        });
      })
    },
    // 查找
    searchValueType(v) {
      let enginType = '';
      this.defaultTemplates.forEach(type => {
        type.child.forEach(template => {
          if (template.templateId === v) {
            enginType =  template.enginType
          }
        })
      })
      return enginType
    },
    // 处理编排模式值类型不同方法,最终只存数组
    modeValueTypeChange(workflowDataCurrent) {
      let currentData = JSON.parse(JSON.stringify(workflowDataCurrent));
      if ([FORMITEMTYPE.RADIO, FORMITEMTYPE.SELECT].includes(this.selectOrchestrator.dicValue)) {
        currentData.orchestratorWays = [currentData.orchestratorWayString];
        delete currentData.orchestratorWayString;
      } else {
        currentData.orchestratorWays = currentData.orchestratorWayArray;
        delete currentData.orchestratorWayArray;
      }
      if (!workflowDataCurrent.templateIds || workflowDataCurrent.templateIds.length === 0) {
        delete currentData.isDefaultReference
      }
      return currentData;
    },
    Ok() {
      this.$refs.projectForm.validate((valid) => {
        if (valid) {
          this.submiting = true;
          this.$emit('confirm', {
            data: this.modeValueTypeChange(this.workflowDataCurrent),
            cb: (close) => {
              this.submiting = false;
              if (close) this.ProjectShow = false;
            }
          });
        } else {
          this.$Message.warning(this.$t('message.workflow.failedNotice'));
        }
      });
    },
    Cancel() {
      this.ProjectShow = false;
      this.workflowDataCurrent.uses = this.originBusiness;
    },
    addTag(label) {
      if (this.workflowDataCurrent.uses) {
        this.workflowDataCurrent.uses += `,${label}`;
      } else {
        this.workflowDataCurrent.uses = label;
      }
    },
    deleteTag(label) {
      const tmpArr = this.workflowDataCurrent.uses.split(',');
      const index = tmpArr.findIndex((item) => item === label);
      tmpArr.splice(index, 1);
      this.workflowDataCurrent.uses = tmpArr.toString();
    },
    fetchLevelData() {
      api.fetch(`${this.$API_PATH.ORCHESTRATOR_PATH}orchestratorLevels`, {}, 'get').then(res => {
        this.levels = res.orchestratorLevels || []
      })
    }
  },
};
</script>
