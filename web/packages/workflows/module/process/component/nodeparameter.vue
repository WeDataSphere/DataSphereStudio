<template>
  <div>
    <Form label-position="top" ref="baseInfoForm" :model="currentNode" class="node-parameter-bar">
      <template v-for="item in curNodeBaseParamsList">
        <FormItem v-if="['Input', 'Text', 'Disable'].includes(item.uiType)" :rules="paramsValid(item)" :key="poinToLink(item.key)" :label="item.lableName" :prop="item.key">
          <Input v-model="currentNode[item.key]" :type="filterFormType(item.uiType)" :rows="6"
            :placeholder="item.desc" :disabled="item.uiType === 'Disable'"
          />
        </FormItem>
        <FormItem
          v-if="item.uiType === 'Tag'"
          :label="item.lableName"
          :key="poinToLink(item.key)"
          :prop="item.key">
          <we-tag
            :new-label="$t('message.workflow.process.nodeParameter.addLabel')"
            :tag-list="currentNode[item.key]"
            @add-tag="addTag(item.key,$event)"
            @delete-tag="deleteTag(item.key,$event)"></we-tag>
        </FormItem>
        <FormItem v-if="item.uiType === 'Select' || item.uiType === 'MultiSelect'" :rules="paramsValid(item)" :key="poinToLink(item.key)" :label="item.lableName" :prop="item.key">
          <Select
            v-model="currentNode[item.key]"
            :placeholder="item.desc"
            clearable
            :multiple="item.uiType === 'MultiSelect'">
            <Option v-for="subItem in JSON.parse(item.value)" :value="subItem" :key="subItem">{{subItem}}</Option>
          </Select>
        </FormItem>
        <FormItem v-if="item.uiType === 'MultiBinding'" :rules="paramsValid(item)" :key="poinToLink(item.key)" :label="item.lableName" :prop="item.key">
          <Select
            v-model="currentNode[item.key]"
            :placeholder="item.desc"
            clearable
            multiple>
            <Option v-for="(subItem,index) in conditionBindList(item)" :value="subItem.key" :key="index">{{ subItem.name }}</Option>
          </Select>
        </FormItem>
      </template>
    </Form>
    <div v-if="curNodeParamsList.length > 0" class="node-module-param-modal-header">
      <h5>{{$t('message.workflow.process.nodeParameter.SXXX')}}</h5>
    </div>
    <Form v-if="curNodeParamsList.length > 0 && currentNode.jobParams" label-position="top"
      ref="parameterForm"  class="node-parameter-bar" :model="currentNode" :rules="ruleValidate">
      <FormItem label="是否引用资源参数模板">
        <Select
          v-model="isRefTemplate" @on-change="handleRefTemplateChange">
          <Option value="1">是</Option>
          <Option value="0">否</Option>
        </Select>
      </FormItem>
      <template v-if="isRefTemplate === '1'">
        <FormItem label="资源参数模板名称" prop="ecConfTemplateName">
          <Input v-model="currentNode.ecConfTemplateName" placeholder="请选择资源参数模板名称" readonly @on-focus="openTemplateDrawer"></Input>
        </FormItem>
      </template>
      <template>
        <template v-for="item in curNodeParamsList">
          <template v-if="checkShow(item) && ['runtime', 'startup', 'special'].includes(item.position)">
            <FormItem v-if="['Input', 'Text', 'Disable'].includes(item.uiType)" style="position: relative;" :rules="paramsValid(item)" :key="poinToLink(item.key)" :label="item.lableName" :prop="'jobParams.'+ poinToLink(item.key)">
              <template slot="label">
                {{ item.lableName }}
              </template>
              <Poptip popper-class="node-item-popper" :disabled="!['check.object', 'job.desc'].includes(item.lableName)" word-wrap width="270" trigger="focus" :content="item.desc" placement="top-start">
                <Input v-model="currentNode.jobParams[poinToLink(item.key)]" :type="filterFormType(item.uiType)" :rows="6"
                :placeholder="item.desc" :disabled="item.uiType === 'Disable'"/>
              </Poptip>
            </FormItem>
            <FormItem v-if="item.uiType === 'Select' || item.uiType === 'MultiSelect'" :rules="paramsValid(item)" :key="poinToLink(item.key)" :label="item.lableName" :prop="'jobParams.'+ poinToLink(item.key)">
              <Select
                v-model="currentNode.jobParams[poinToLink(item.key)]"
                :placeholder="item.desc"
                clearable
                :multiple="item.uiType === 'MultiSelect'">
                <Option v-for="subItem in JSON.parse(item.value)" :value="subItem" :key="subItem">{{subItem}}</Option>
              </Select>
            </FormItem>
            <FormItem v-if="item.uiType === 'MultiBinding'" :rules="paramsValid(item)" :key="poinToLink(item.key)" :label="item.lableName" :prop="'jobParams.'+ poinToLink(item.key)">
              <Select
                v-model="currentNode.jobParams[poinToLink(item.key)]"
                :placeholder="item.desc"
                clearable
                multiple>
                <Option v-for="(subItem,index) in conditionBindList(item)" :value="subItem.key" :key="index">{{ subItem.name }}</Option>
              </Select>
            </FormItem>
          </template>
          <template v-if="checkShow(item) && item.uiType === 'Upload'">
            <FormItem :rules="paramsValid(item)" :key="poinToLink(item.key)" :label="item.lableName">
              <resource :resources="resources" :is-ripetition="true" :node-type="nodeData.type" :readonly="readonly"
                @update-resources="updateResources"></resource>
            </FormItem>
          </template>
        </template>
      </template>
    </Form>
    <div class="save-button" v-if="isShowSaveButton">
      <Button @click="save"
        :disabled="isNodeMap">{{$t('message.workflow.process.nodeParameter.BC')}}
      </Button>
    </div>
    <templateSelectDrawer ref="templateSelectDrawer" v-show="isTemplateDrawerShow" @isShow="handleTemplateShow"
      :nodeInfo="nodeData" :templateList="templateList" @submit="handleTemplateSelect"
      :defaultTemplateId="currentNode.ecConfTemplateId"></templateSelectDrawer>
  </div>
</template>
<script>
import resource from './resource.vue';
import weTag from '@dataspherestudio/shared/components/tag/index.vue'
import { isEmpty, cloneDeep } from 'lodash';
import templateSelectDrawer from './templateSelectDrawer.vue'
import { useData } from './useData.js';
const {
  getTemplateDatas,
} = useData();
export default {
  name: 'NodeParams',
  components: {
    resource,
    templateSelectDrawer,
    'we-tag': weTag
  },
  props: {
    nodeData: {
      type: Object,
      default: null,
    },
    name: {
      type: String,
      default: '',
    },
    readonly: {
      type: Boolean,
      default: false,
    },
    isShowSaveButton: {
      type: Boolean,
      default: true,
    },
    nodes: {
      type: Array,
      default: () => [],
    },
    isNodeMap: {
      type: Boolean,
      default: false
    },
    consoleParams: {
      type: Array,
      default: () => []
    },
    tabs: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      currentNode: {
      },
      resources: [],
      isTemplateDrawerShow: false,
      templateList: [],
      isRefTemplate: '0',
      ruleValidate: {
        ecConfTemplateName: [
          { required: true, message: '请选择参数模板', trigger: 'change' }
        ],
      },
      valueSemicolonValidReg: /^(\s*(check\.object|source\.type)\.\w+?=[^;\s]+?[ \t]*?;)+$/, // 校验分号分割
      valueWordwrapValidReg: /^(\s*(check\.object|source\.type)\.\w+?=[^;\s]+?[ \t]*?\n+)+$/, // 校验换行符分割
    };
  },
  watch: {
    nodeData(val) {
      this.currentNode = val;
      if (!this.currentNode.params || isEmpty(this.currentNode.params.configuration)) {
        // 有params可能是spark节点的variable参数
        this.$set(this.currentNode, 'params', {
          configuration: {
            special: {},
            runtime: {},
            startup: {}
          }
        })
      }
      this.resources = []
      // 在节点没有保存参数时，需要设置默认值的值,先判断保存的值应该存在哪里starup或者runtime或者是node下
      let jobParams = {};
      this.curNodeParamsList.map((item) => {
        // 多选框是数组值
        const defaultValue = ['MultiBinding'].includes(item.uiType) ? JSON.parse(item.defaultValue) : this.consoleParamsDefault(item.defaultValue, item.key, this.consoleParams);
        if (['runtime', 'startup', 'special'].includes(item.position) && this.currentNode.params) {
          let value = this.currentNode.params.configuration[item.position][item.key] ? this.currentNode.params.configuration[item.position][item.key] : defaultValue;
          jobParams[this.poinToLink(item.key)] = value;
        } else if (item.uiType === 'Upload') {
          if (this.currentNode.resources && this.currentNode.resources.length) {
            this.resources = [...this.currentNode.resources]
            if (this.currentNode.jobContent && this.currentNode.jobContent.script) {
              this.resources = this.currentNode.resources.filter((item) => {
                return item.fileName !== this.currentNode.jobContent.script;
              });
            }
          } else {
            this.resources = [];
          }
        }
      })
      // eslint-disable-next-line no-extra-boolean-cast
      if(!!this.currentNode.ecConfTemplateId) {
        this.isRefTemplate = '1'
      } else {
        this.isRefTemplate = '0'
      }
      jobParams['ec-conf-templateId'] = this.currentNode.ecConfTemplateId
      this.$set(this.currentNode, 'jobParams', jobParams);
    }
  },
  computed: {
    // 获取当前节点参数的基本信息
    curNodeParamsList() {
      return this.currentNode.nodeUiVOS ? this.currentNode.nodeUiVOS.filter((item) => !item.baseInfo && item.nodeMenuType) : [];
    },
    curNodeBaseParamsList() {
      return this.currentNode.nodeUiVOS ? this.currentNode.nodeUiVOS.filter((item) => {
        const fields = ['appTag','businessTag','title','desc'].indexOf(item.key) > -1
        return item.baseInfo && fields// && !item.hidden
      }) : [];
    }
  },
  methods: {
    // 是否选择模板从false切到true时，查询是否有默认模板，有则填入默认值
    async handleRefTemplateChange(v) {
      if(v ==='1' && !this.currentNode.ecConfTemplateName) {
        await this.getTemplateDataByProject()
        this.templateList.forEach((v) => {
          if(v.workflowDefault) {
            this.$set(this.currentNode, 'ecConfTemplateName', v.templateName);
            this.$set(this.currentNode, 'ecConfTemplateId', v.templateId);
            this.currentNode.jobParams['ec-conf-templateId'] = v.templateId
            this.currentNode.params.configuration['startup']['ec.conf.templateId'] = v.templateId
          }
        })
      } else if (this.currentNode.ecConfTemplateName) {
        this.currentNode.jobParams['ec-conf-templateId'] = this.currentNode.ecConfTemplateId
      }
      if (v === '0') {
        delete this.currentNode.jobParams['ec-conf-templateId']
      }
    },
    // 选择参数模板
    handleTemplateSelect(templateObj) {
      this.$set(this.currentNode, 'ecConfTemplateName', templateObj.templateName);
      this.$set(this.currentNode, 'ecConfTemplateId', templateObj.templateId);
      this.currentNode.jobParams['ec-conf-templateId'] = templateObj.templateId
      this.currentNode.params.configuration['startup']['ec.conf.templateId'] = templateObj.templateId
    },
    // 打开选择参数模板的抽屉
    async openTemplateDrawer() {
      await this.getTemplateDataByProject()
      this.isTemplateDrawerShow = true
      this.$refs.templateSelectDrawer.initRadioData()
    },
    // 关闭选择参数模板的抽屉
    handleTemplateShow() {
      this.isTemplateDrawerShow = false
    },
    // 获取当前节点对应的模板信息
    async getTemplateDataByProject() {
      const params = {
        projectId: this.$route.query.projectID,
        orchestratorId: this.$route.query.flowId,
        jobType: this.nodeData.type,
      }
      const res = await getTemplateDatas(params);
      this.templateList = [];
      res.forEach(e=> {
        e.child.forEach(d => {
          this.templateList.push(Object.assign(d))
        })
      });
    },
    // 控制台设置的参数赋值
    // 应当根据不同节点类型获取各自的参数配置，目前有spark和hive及通用配置，后端说不会有重复key值，沿用之前设计加hive的
    consoleParamsDefault(originDefalut, key, rst) {
      let value = originDefalut;
      if (rst.length > 0) {
        rst[0].fullTree.forEach(item =>{
          if (item && item.settings.length > 0) {
            item.settings.forEach((it) => {
              if (it.key === key) {
                value =  it.configValue || it.defaultValue;
              }
            });
          }
        })
        rst[2].fullTree.forEach(item =>{
          if (item && item.settings.length > 0) {
            item.settings.forEach((it) => {
              if (it.key === key) {
                value =  it.configValue || it.defaultValue;
              }
            });
          }
        })
      }
      return value;
    },
    // linkToPoin
    linkToPoin(key) {
      return key.replace(/-/g, '.');
    },
    //降点装换成短横杠
    poinToLink(key) {
      return key.replace(/\./g, '-');
    },
    getJobDescDivide(value) {
      let valid = false;
      let divide = '';
      if (
        this.valueSemicolonValidReg.test(value) ||
        this.valueSemicolonValidReg.test(value + ';')
      ) {
        valid = true;
        divide = ';';
      } else if (
        this.valueWordwrapValidReg.test(value) ||
        this.valueWordwrapValidReg.test(value + '\n')
      ) {
        valid = true;
        divide = '\n';
      }

      return { valid, divide };
    },
    // 各参数的校验规则
    paramsValid(param) {
      // 自定义函数的方法先写这里
      // eslint-disable-next-line no-unused-vars
      const validatorTitle = (rule, value, callback) => {
        if (value === `${this.name}`) {
          callback(new Error(rule.message));
        } else {
          callback();
        }
      }
      // eslint-disable-next-line no-unused-vars
      const validateJson = (rule, value, callback) => {
        const isJsonString = (str) => {
          try {
            JSON.parse(str);
            return true;
          } catch (err) {
            return false;
          }
        };
        if (isJsonString(value)) {
          callback();
        } else {
          callback(new Error(this.$t('message.workflow.process.nodeParameter.QTXZQJSON')));
        }
      };
      // eslint-disable-next-line no-unused-vars
      const validateJobDesc = (rule, value, callback) => {
        let errMsg = '';
        const trimVal = (value || '').trim();
        if (trimVal) {
          const matches = trimVal.split(/[\n;]+\s*/);
          matches.some((item) => {
            const trimItem = item.trim(); // 去除首尾的空格
            if (!/^(check\.object|source\.type)\.[^\=]+?.*?$/.test(trimItem)) {
              errMsg = '每行请以check.object.xx或source.type.xx开头，xx为编号，从01开始';
              return true;
            } else if (/\s+/.test(trimItem)) {
              const temp = trimItem.split(/[\s\=]+/);
              errMsg = `${temp[0]}行内包含空格字符`;
              return true;
            }
          })
          if (!errMsg && !this.getJobDescDivide(trimVal).valid) {
            errMsg = '请正确填写多源配置';
          }
        }
        if (errMsg) {
          callback(new Error(errMsg));
        } else {
          callback()
        }
      }

      // eslint-disable-next-line no-unused-vars
      const validateJobDescDuplication = (rule, value, callback) => {
        let tmp = {}
        let duplicationKey = false;
        const trimVal = (value || '').trim();
        if (value) {
          const matches = trimVal.split(/[\n;]+\s*/);
          matches.some(it => {
            const key = it.trim().split('=')[0]
            if (tmp[key]) {
              duplicationKey = key;
              return true;
            } else {
              tmp[key] = 1;
            }
          })
        }
        if (duplicationKey) {
          callback(new Error(`${duplicationKey}重复,请检查`));
        } else {
          callback();
        }
      }

      const numInterval = (rule, value, callback) => {
        const reg = new RegExp(`[0-9]`);
        // 比较大小
        const valueResult = reg.test(Number(value)) && rule.range[0] <= Number(value) && rule.range[1] >= Number(value);
        if (valueResult || value === '') {
          callback();
        } else {
          callback(new Error())
        }
      };

      const validateCheckObject = (rule, value, callback) => {
        let errMsg = '';
        const trimVal = (value || '').trim();
        if (trimVal) {
          if (trimVal.includes('check.object')) {
            errMsg = '输入参数不能包含check.object字符串';
          } else if (trimVal.match(/\s+/g)) {
            errMsg = '输入参数中间不能包含空格字符';
          }
        }
        if (errMsg) {
          callback(new Error(errMsg));
        } else {
          callback()
        }
      };
      let temRule = [];

      if (param.nodeUiValidateVOS) {
        param.nodeUiValidateVOS.map((item) => {
          // 如果是正则类型的就写成正则
          if (item.validateType === 'Required') {
            temRule.push({
              required: true,
              message: item.message,
              trigger: item.trigger,
              type: ['MultiBinding', 'MultiSelect'].includes(param.uiType) ? 'array' : 'string'
            })
          } else if (item.validateType === 'Regex') {
            temRule.push({
              type: 'string',
              pattern: new RegExp(item.validateRange),
              message: item.message,
              trigger: item.trigger
            })
          } else if (item.validateType === 'Function') {
            temRule.push({
              // 只有和上面定义的函数名相同才让执行
              validator: ['validatorTitle', 'validateJson', 'validateJobDesc', 'validateJobDescDuplication', 'validateCheckObject'].includes(item.validateRange) ? eval(item.validateRange) : (rule, value, callback) => { callback() },
              message: ['validateJobDesc', 'validateCheckObject', 'validateJobDescDuplication'].includes(item.validateRange) ? undefined : item.message,
              trigger: item.trigger || 'blur'
            })
          } else if (item.validateType === 'NumInterval') {
            temRule.push({
              validator: numInterval,
              range: JSON.parse(item.validateRange),
              trigger: 'change',
              message: item.message,
            })
          }
        })
      }
      return temRule;
    },
    // 根据返回的添加去获取需要绑定的列表
    conditionBindList(param) {
      let temArry = [];
      if (param.defaultValue === 'empty') {
        temArry.push({
          name: this.$t('message.workflow.process.notBinding'),
          key: 'empty'
        })
      }
      // 对绑定的参数进行过滤
      const conditionResult = (type) => {
        if (param.value && JSON.parse(param.value)) {
          // 如果是通配符就返回true
          const optionsList = JSON.parse(param.value);
          if (optionsList[0] === '*') {
            return true;
          } else {
            return optionsList.includes(type);
          }
        }
      }
      if (this.nodes && this.nodes.length) {
        this.nodes.forEach((node) => {
          if (node.key !== this.currentNode.key && conditionResult(node.type)) {
          // 当sql节点里面没内容时,resources属性值为[]，这种sql节点不放做选项
            const tempObj = {
              name: node.title,
              key: node.key,
            }
            temArry.push(tempObj)
          }
        })
      }
      return temArry;
    },
    addTag(key, label) {
      if (this.currentNode[key]) {
        this.$set(this.currentNode, key, this.currentNode[key] + `,${label}`);
      } else {
        this.$set(this.currentNode, key, label);
      }
    },
    deleteTag(key, label) {
      const tmpArr = this.currentNode[key].split(',');
      const index = tmpArr.findIndex((item) => item === label);
      tmpArr.splice(index, 1);
      this.$set(this.currentNode, key, tmpArr.toString());
    },

    save() {
      this.validFrom(() => {
        this.resourcesAction();
        // 没有值降默认值回填
        if (this.currentNode.jobParams && this.currentNode.params.configuration) {
          this.curNodeParamsList.map((item) => {
            if (['runtime', 'startup', 'special'].includes(item.position) && this.currentNode.params) {
              let value = this.currentNode.jobParams[this.poinToLink(item.key)] ? this.currentNode.jobParams[this.poinToLink(item.key)] : item.defaultValue;
              if (item.key && ['job.desc', 'check.object'].includes(item.key)) {
                value = (value || '').trim();
                if (item.key === 'job.desc') {
                  const { divide } = this.getJobDescDivide(value);
                  value = value.split(/[\n;]+\s*/).map(item => item.trim()).join(divide);
                }  
              }
              this.currentNode.params.configuration[item.position][item.key] = value;
            }
          })
        }
        // 未选择模板时删除对应模板数据
        if (this.isRefTemplate === '0') {
          delete this.currentNode.ecConfTemplateId
          delete this.currentNode.ecConfTemplateName
          delete this.currentNode.jobParams['ec-conf-templateId']
          delete this.currentNode.params.configuration['startup']['ec.conf.templateId']
        } else {
          this.currentNode.jobParams['ec-conf-templateId'] = this.currentNode.ecConfTemplateId
          this.currentNode.params.configuration['startup']['ec.conf.templateId'] = this.currentNode.ecConfTemplateId
        }
      });
    },
    getCurrentNode() {
      const param = cloneDeep(this.currentNode);
      if (param.jobParams && param.params.configuration) {
        this.curNodeParamsList.map((item) => {
          if (['runtime', 'startup', 'special'].includes(item.position) && param.params) {
            let value = param.jobParams[this.poinToLink(item.key)] ? param.jobParams[this.poinToLink(item.key)] : item.defaultValue;
            if (item.key && ['job.desc', 'check.object'].includes(item.key)) {
                value = (value || '').trim();
                if (item.key === 'job.desc') {
                  const { divide } = this.getJobDescDivide(value);
                  param.params.configuration[item.position][item.key] = value.split(/[\n;]+\s*/).map(item => item.trim()).join(divide);
                } else {
                  param.params.configuration[item.position][item.key] = value;
                } 
              }
          }
        })
      }
      return param;
    },
    validFrom(cb) {
      this.$refs.baseInfoForm.validate((baseInfoValid) => {
        if (baseInfoValid) {
          if (this.$refs.parameterForm) {
            this.$refs.parameterForm.validate((valid) => {
              if (valid) {
                cb()
                const tempNode = this.getCurrentNode();
                this.$emit('saveNode', tempNode);
              } else {
                this.$Message.warning(this.$t('message.workflow.process.nodeParameter.BCSB'));
              }
            });
          } else {
            cb()
            const tempNode = this.getCurrentNode();
            this.$emit('saveNode', tempNode);
          }
        } else {
          this.$Message.warning(this.$t('message.workflow.process.nodeParameter.JDBDXTXBHF'));
        }
      });
    },
    resourcesAction() {
      let resources = [];
      const mapFlag = {}
      if (this.currentNode.jobContent && this.currentNode.jobContent.script) {
        this.currentNode.resources.forEach(item => {
          if (item.fileName === this.currentNode.jobContent.script && !mapFlag[item.fileName]) {
            resources.push(item)
            mapFlag[item.fileName] = 1
          }
        })
      }
      this.resources.forEach((item) => {
        if (!mapFlag[item.fileName]) {
          resources.push(item);
        }
      });
      this.currentNode.resources = resources;
    },
    updateResources(res) {
      this.resources = res.map((item) => {
        return {
          fileName: item.fileName,
          resourceId: item.resourceId,
          version: item.version,
        };
      });
      this.resourcesAction();
      const tempNode = this.getCurrentNode();
      this.$emit('saveNode', tempNode);
    },
    filterFormType(val) {
      switch (val) {
        case 'Text':
          return 'textarea';
        default:
          return 'text';
      }
    },
    /**
     * 检查数据是否符合条件，仅支持runtime starup参数配置时控制
     *  ${params.configuration.runtime['only.receive.today']}=='true'
     *  !${params.configuration.startup['ec.conf.templateId']}
     * @param {*} data
     * @param {*} condition
     * @returns
     */
    checkShow(item, nodeData) {
      let data = nodeData || this.currentNode
      if (typeof item.condition === 'string' && item.condition) {
        let condition = item.condition
        // condition 示例：
        // ${params.configuration.runtime['only.receive.today']}
        // !${params.configuration.startup['ec.conf.templateId']}
        // 后端配置根据params路径位置写规则
        // 前端编辑参数存在jobParams，需要转换，以上示例转换如下
        // ${jobParams['only-receive-today']}
        // !${jobParams['ec-conf-templateId']}
        if (nodeData) {
          // module.vue click 触发，条件检查使用params.configuration.runtime这种路径
        } else {
          // 参数面板点击触发，条件检查使用jobParams这种路径
          if (condition.indexOf('params.configuration') > 0) {
            condition = condition.replace(/params\.configuration\.runtime/g,'jobParams').replace(/params\.configuration\.startup/g,'jobParams')
            condition = this.poinToLink(condition)
          }
        }

        let Fn = Function
        let fn = condition.replace(/\${([^}]+)}/g, function (a, b) {
          return `__node__data.${b}`
        })
        try {
          return new Fn('__node__data', 'return ' + fn)(data);
        } catch (e) {
          console.log(e)
          //
        }
      }
      if (typeof item.condition === 'boolean') {
        return item.condition
      }
      return true
    }
  },
};

</script>
<style>
.node-parameter-bar label.ivu-form-item-label {
  text-align: left;
  line-height: 14px;
  padding-bottom: 6px;
}
.node-parameter-bar .ivu-form-item {
  margin-bottom: 30px;
}
.node-parameter-bar .ivu-poptip {
  width: 100%;
}
.node-parameter-bar .ivu-poptip .ivu-poptip-rel {
  width: 100%;
}
</style>
