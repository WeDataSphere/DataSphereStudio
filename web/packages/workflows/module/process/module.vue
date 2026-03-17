<template>
  <div ref="processModule" class="process-module" :class="{'is-publishing': isFlowPubulish, 'locked': locked}">
    <vueProcess
      v-if="viewMode === 'vueprocess'"
      ref="process"
      :shapes="shapes"
      :value="originalData"
      :ctx-menu-options="nodeMenuOptions"
      :view-options="viewOptions"
      :disabled="workflowIsExecutor || myReadonly"
      :newTipVisible="newTipVisible"
      @toggle-shape="toggleShape"
      @change="change"
      @message="message"
      @node-click="click"
      @node-dblclick="dblclick"
      @node-delete="nodeDelete"
      @add="addNode"
      @on-ctx-menu="onContextMenu"
      @search-node-path="showSearchPath"
      @link-delete="linkDelete"
      @changeViewMode="handleSwitchViewMode"
      @link-add="linkAdd">
      <DesignToolbar
        viewMode="vueprocess"
        :readonly="myReadonly"
        :workflowIsExecutor="workflowIsExecutor"
        :needReRun="needReRun"
        :isFlowPubulish="isFlowPubulish"
        :isFlowSubmit="isFlowSubmit"
        :isLatest="isLatest"
        :publish="publish"
        :product="product"
        :flowId="flowId"
        :flowStatus="flowStatus"
        :associateGit="associateGit"
        :isFlowSubmited="isFlowSubmited"
        :isMainFlow="isMainFlow"
        :type="type"
        @click-itembar="handleClickToolbar"
      />
    </vueProcess>
    <div v-else class="designer" :style="{ 'z-index': isfullScreen ? 6 : 1}">
      <div class="designer-toolbar">
        <DesignToolbar
          :viewMode="viewMode"
          :readonly="myReadonly"
          :workflowIsExecutor="workflowIsExecutor"
          :needReRun="needReRun"
          :isFlowPubulish="isFlowPubulish"
          :isFlowSubmit="isFlowSubmit"
          :isLatest="isLatest"
          :publish="publish"
          :product="product"
          :flowId="flowId"
          :flowStatus="flowStatus"
          :associateGit="associateGit"
          :isFlowSubmited="isFlowSubmited"
          :isMainFlow="isMainFlow"
          :type="type"
          @click-itembar="handleClickToolbar"
        />
      </div>
      <cyeditor v-if="viewMode === 'cyeditor'"
        style="top: 36px"
        ref="process"
        :shapes="shapes"
        :value="originalData || {nodes:[],edges: []}"
        :ctx-menu-options="nodeMenuOptions"
        :disabled="workflowIsExecutor || myReadonly"
        :readable="myReadonly"
        :viewMode="viewMode"
        :newTipVisible="newTipVisible"
        @screenSizeChange="screenSizeChange"
        @change="change"
        @message="message"
        @node-click="click"
        @edge-click="clickEdge"
        @node-dblclick="dblclick"
        @node-delete="nodeDelete"
        @node-add="addNode"
        @on-ctx-menu="onContextMenu"
        @link-add="linkAdd"
        @search-node-path="showSearchPath"
        @changeViewMode="handleSwitchViewMode"
      />
      <template v-if="viewMode === 'table'">
        <iframe class="iframeClass" id="iframe" ref="ifr" style="padding-top:36px" :src="tableViewUrl" frameborder="0" width="100%" height="100%" />
        <Spin v-if="iframeloading" fix>{{ $t('message.common.Loading') }}</Spin>
      </template>
    </div>
    <div
      class="process-module-param"
      v-clickoutside="handleOutsideClick"
      v-show="isParamModalShow">
      <div class="process-module-param-modal-header">
        <h5>{{ isResourceShow ? $t('message.workflow.process.resource') : isDispatch ? $t('message.workflow.process.schedule') : $t('message.workflow.process.params') }}{{$t('message.workflow.process.seting')}}</h5>
      </div>
      <div class="process-module-param-modal-content">
        <argument
          v-show="!isResourceShow"
          :props="props"
          :isDispatch="isDispatch"
          :scheduleParamsProp="scheduleParams"
          @change-schedule="onScheduleChange"
          @change-props="onPropsChange"></argument>
        <resource
          v-show="isResourceShow"
          :resources="resources"
          :flow-name="name"
          @update-resources="updateResources"></resource>
      </div>
    </div>
    <div
      class="process-module-param"
      v-show="nodebaseinfoShow"
      @click="clickBaseInfo">
      <div class="process-module-param-modal-header">
        <h5>{{$t('message.workflow.process.baseInfo')}}</h5>
        <div class="save-button">
          <Button  v-if="!myReadonly" size="small" @click.stop="saveNodeParameter" :loading="saveLoading"
            :disabled="false">{{$t('message.workflow.process.nodeParameter.BC')}}
          </Button>
        </div>
        <Icon class="close-icon" type="md-close" size="16" @click.stop="closeParamsBar"></Icon>
      </div>
      <div class="process-module-param-modal-content">
        <nodeParameter
          ref="nodeParameter"
          :node-data="clickCurrentNode"
          :name="name"
          :readonly="myReadonly"
          :nodes="json && json.nodes"
          :consoleParams="consoleParams"
          :tabs="tabs"
          @saveNode="saveNode"
          @saveButtonStatus="saveButtonStatus"
        ></nodeParameter>
      </div>
    </div>
    <Modal
      v-model="edgeConfigShow"
      width="420">
      <div class="process-module-title" slot="header">
        分支连线配置
      </div>
      <Form :label-width="100">
        <FormItem label="来源节点">
          <Input :value="currentEdgeSourceName" disabled />
        </FormItem>
        <FormItem label="目标节点">
          <Input :value="currentEdgeTargetName" disabled />
        </FormItem>
        <FormItem label="分支名称">
          <Input v-model="edgeForm.branchLabel" placeholder="例如：命中条件" />
        </FormItem>
        <FormItem label="条件表达式">
          <Input v-model="edgeForm.condition" type="textarea" :rows="3" :disabled="edgeForm.isDefault" placeholder="例如：${run_date == '2026-03-17'}" />
        </FormItem>
        <FormItem label="优先级">
          <InputNumber v-model="edgeForm.priority" :min="1" :max="999" />
        </FormItem>
        <FormItem>
          <Checkbox v-model="edgeForm.isDefault">默认分支</Checkbox>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="closeEdgeConfig">取消</Button>
        <Button type="primary" v-if="!myReadonly" @click="saveEdgeConfig">保存</Button>
      </div>
    </Modal>
    <Modal
      width="450"
      v-model="saveModal">
      <div
        class="process-module-title"
        slot="header">
        {{$t('message.workflow.process.save')}}
      </div>
      <Form
        ref="formSave"
        :model="saveModel"
        :label-width="85" >
        <FormItem
          :label="$t('message.workflow.comment')"
          prop="comment"
          :rules="[{ required: true, message: $t('message.workflow.process.inputComment') },{message: $t('message.workflow.process.commentLengthLimit'), max: 255}]">
          <Input
            v-model="saveModel.comment"
            type="textarea"
            :placeholder="$t('message.workflow.process.inputComment')"
            style="width: 300px;" />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button
          type="primary"
          @click="handleSave">{{$t('message.workflow.process.confirmSave')}}</Button>
      </div>
    </Modal>
    <Spin
      v-if="loading"
      size="large"
      fix/>
    <Modal
      v-model="repetitionNameShow"
      :title="$t('message.workflow.process.nodeNameNotice')"
      class="repetition-name">
      {{$t('message.workflow.process.repeatNode')}}{{ repeatTitles.join(', ') }}
      <div slot="footer">
        <Button
          type="primary"
          @click="repetitionName">{{$t('message.workflow.ok')}}</Button>
      </div>
    </Modal>
    <associate-script
      ref="associateScript"
      @click="associateScript"/>
    <generate-datachecker ref="datachecker" @confirm="addDatachecker"/>
    <!-- 鍒涘缓鑺傜偣寮圭獥 -->
    <Modal
      :title="addNodeTitle"
      v-model="addNodeShow"
      :closable="false"
      :mask-closable="false"
    >
      <Form
        v-if="createNodeParamsList.length > 0"
        label-position="left"
        :label-width="130"
        ref="addFlowfoForm"
        :model="clickCurrentNode"
        :rules="formRules">
        <template v-for="item in createNodeParamsList">
          <FormItem v-if="['Input', 'Text', 'Disable'].includes(item.uiType)" :key="item.key" :label="item.lableName" :prop="item.key" >
            <Input v-model="clickCurrentNode[item.key]" :type="filterFormType(item.uiType)"
              :placeholder="item.desc" :disabled="item.uiType === 'Disable'"
            />
          </FormItem>
          <FormItem v-if="item.uiType === 'Select'" :key="item.key" :label="item.lableName" :prop="item.key" >
            <Select
              v-model="clickCurrentNode[item.key]"
              :placeholder="item.desc">
              <Option v-for="subItem in JSON.parse(item.value)" :value="subItem" :key="subItem">{{subItem}}</Option>
            </Select>
          </FormItem>
          <FormItem v-if="item.uiType === 'Binding'" :key="item.key" :label="item.lableName" :prop="item.key" >
            <Select
              v-model="clickCurrentNode[item.key]"
              :placeholder="item.desc">
              <Option v-for="subItem in conditionBindList(item)" :value="subItem.key" :key="subItem.key">{{subItem.name}}</Option>
            </Select>
          </FormItem>
        </template>
      </Form>
      <div slot="footer">
        <Button
          type="text"
          size="large"
          @click="addFlowCancel">{{$t('message.workflow.cancel')}}</Button>
        <Button
          type="primary"
          size="large"
          @click="addFlowOk">{{$t('message.workflow.ok')}}</Button>
      </div>
    </Modal>
    <!-- 鍙戝竷寮圭獥 -->
    <FlowDiffPublish
      :visible.sync="pubulishShow"
      :projectName="$route.query.projectName" 
      :orchestratorId="orchestratorId"
      :associateGit="associateGit"
    >
      <Form
        label-position="top">
        <FormItem
          :label="$t('message.workflow.desc')">
          <Input
            :rows="4"
            type="textarea"
            v-model="pubulishFlowComment"
            :placeholder="$root.$t('message.workflow.publish.inputDesc')"></Input>
        </FormItem>
        <FormItem
          v-if="associateGit"
          label="鎻愪氦璁板綍">
          <Table border :columns="publishFlowColumns" :data="publishFlowData" :height="300"></Table>
        </FormItem>
      </Form>
      <template slot="footer">
        <Button
          type="primary"
          :loading="saveingComment"
          :disabled="saveingComment"
          @click="handleWorkflowPublish">{{$t('message.workflow.ok')}}</Button>
        <Button
          @click="showDiff">{{$t('message.workflow.showVersionDiff')}}</Button>
        <Button
          @click="pubulishShow = false">{{$t('message.workflow.cancel')}}</Button>
      </template>
    </FlowDiffPublish>
    <!-- 鎻愪氦寮圭獥 -->
    <FlowDiffSubmit
      :visible.sync="showSubmit"
      :projectName="$route.query.projectName" 
      :orchestratorId="orchestratorId" 
    >
      <Input
        :rows="2"
        type="textarea"
        v-model="submitDesc"
        :placeholder="$root.$t('message.workflow.publish.submitDesc')"></Input>
      <template slot="footer">
        <Button
          type="primary"
          :disabled="!submitDesc"
          @click="submitGit">{{$t('message.workflow.ok')}}</Button>
        <Button
          @click="showSubmit = false">{{$t('message.workflow.cancel')}}</Button>
      </template>
    </FlowDiffSubmit>
    <!-- 瀵煎嚭寮圭獥 -->
    <Modal
      v-model="workflowExportShow"
      :title="$t('message.workflow.exportWorkflow')"
      @on-ok="workflowExportOk">
      <Form
        :label-width="100"
        label-position="left"
        ref="exportForm"
      >
        <FormItem :label="$t('message.workflow.desc')" porp="desc">
          <Input
            v-model="exportDesc"
            type="textarea"
            :placeholder="$t('message.workflow.inputWorkflowDesc')"></Input>
        </FormItem>
        <FormItem porp="changeVersion">
          <Checkbox v-model="exporTChangeVersion">{{$t('message.workflow.synchronousPublishing')}}</Checkbox>
        </FormItem>
      </Form>
    </Modal>
    <!-- 鎵归噺鍏宠仈涓婁笅娓歌妭鐐?-->
    <Modal
      v-model="addEdgesShow"
      :title="`鎵归噺鍏宠仈鑺傜偣锛堟牴鑺傜偣锛?{addEdgesForm.currentNodeName}锛塦"
      class="repetition-name"
      @on-visible-change="cancelEdges">
      <Form
        label-position="top"
        ref="addChildrenRef"
      >
        <FormItem label="涓婃父涓€绾ц妭鐐? >
          <Select
            v-model="addEdgesForm.upstreamNodes"
            placeholder="璇烽€夋嫨"
            multiple
            filterable
            @on-change="changeNodes('upstream', $event)">
            <Option v-for="item in upstreamNodeList" :value="item.key" :key="item.key">{{item.title}}</Option>
          </Select>
        </FormItem>
        <FormItem label="涓嬫父涓€绾ц妭鐐? >
          <Select
            v-model="addEdgesForm.downstreamNodes"
            placeholder="璇烽€夋嫨"
            multiple
            filterable
            @on-change="changeNodes('downstream', $event)">
            <Option v-for="item in downstreamNodeList" :value="item.key" :key="item.key">{{item.title}}</Option>
          </Select>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button
          type="text"
          size="large"
          @click="cancelEdges(false)">{{$t('message.workflow.cancel')}}</Button>
        <Button
          type="primary"
          @click="addEdges">{{$t('message.workflow.ok')}}</Button>
      </div>
    </Modal>
    <!-- 杩愯鎺у埗鍙?-->
    <console
      v-if="openningNode && viewMode !== 'table'"
      ref="currentConsole"
      :node="openningNode"
      :stop="workflowIsExecutor"
      class="process-console"
      :height="consoleHeight"
      :style="getConsoleStyle"
      @close-console="closeConsole"></console>
    <BottomTab
      v-show="viewMode !== 'table'"
      ref="bottomTab"
      :orchestratorId="orchestratorId"
      :orchestratorVersionId="orchestratorVersionId"
      :flowId="flowId"
      :product="product"
      :readonly="readonly"
      @release="release"
    />
    <NodePath :data="json" :show="showNodePathPanel" @close="showNodePathPanel = false" @open-params="click" @open-node="dblclick" />
  </div>
</template>
<script>
import argument from './component/arguments.vue';
import resource from './component/resource.vue';
import nodeParameter from './component/nodeparameter.vue';
import vueProcess from '@dataspherestudio/shared/components/vue-process';
import console from './component/console.vue';
import api from '@dataspherestudio/shared/common/service/api';
import clickoutside from '@dataspherestudio/shared/common/helper/clickoutside';
import associateScript from './component/associateScript.vue';
import generateDatachecker from './component/generateDatachecker.vue';
import { throttle, debounce  } from 'lodash';
import { NODETYPE, ext } from '@/workflows/service/nodeType';
import storage from '@dataspherestudio/shared/common/helper/storage';
import mixin from '@dataspherestudio/shared/common/service/mixin';
import eventbus from "@dataspherestudio/shared/common/helper/eventbus";
import moment from 'moment';
import { getPublishStatus } from '@/workflows/service/api.js';
import module from './index';
import nodeIcons from './nodeicon';
import BottomTab from './component/bottomTab.vue';
import NodePath from './component/nodePath.vue';
import FlowDiffSubmit from './component/flowDiffSubmit.vue';
import FlowDiffPublish from './component/flowDiffPublish.vue';
import cyeditor from './cyeditor/index.vue'
import DesignToolbar from './component/designtoolbar.vue';
import { hasCycle } from './utils';
import { useData } from './component/useData.js';

const {
  getTemplateDatas,
} = useData();

export default {
  components: {
    vueProcess,
    argument,
    resource,
    nodeParameter,
    associateScript,
    generateDatachecker,
    console,
    BottomTab,
    NodePath,
    cyeditor,
    DesignToolbar,
    FlowDiffSubmit,
    FlowDiffPublish
  },
  mixins: [mixin],
  directives: {
    clickoutside,
  },
  props: {
    workspaceId: {
      type: [String, Number],
      default: ''
    },
    flowId: {
      type: [String, Number],
      default: '',
    },
    version: {
      type: [String, Number],
      default: '',
    },
    readonly: {
      type: [String, Boolean],
      default: false,
    },
    publish: {
      type: Boolean,
      default: false
    },
    product: {
      type: [Boolean],
      default: false,
    },
    importReplace: {
      type: Boolean,
      default: false,
    },
    openFiles: {
      type: Object,
      default: () => {},
    },
    tabs: {
      type: Array,
      default: () => [],
    },
    activeTabKey: {
      type: String
    },
    isLatest: {
      type: Boolean,
      default: true
    },
    orchestratorId: {
      type: [Number, String],
      default: null
    },
    orchestratorVersionId: {
      type: [Number, String],
      default: null
    },
    newTipVisible: {
      type: Boolean,
      default: false
    },
    flowStatus: {
      type: String,
      default: ''
    },
    associateGit: {
      type: Boolean,
      default: false
    },
    isMainFlow: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 鎻愪氦
      showSubmit: false,
      submitDesc: '',
      isFlowSubmit: false,
      // 鍙戝竷鍓嶄繚瀛?      saveingComment: false,
      // 鏄惁涓虹埗宸ヤ綔娴?      isRootFlow: true,
      name: '',
      shapes: [],
      // 鍘熷鏁版嵁
      originalData: null,
      // 鎻掍欢杩斿洖鐨刯son鏁版嵁
      json: null,
      // 宸ヤ綔娴佺骇鍒殑鍙傛暟
      props: [
        {'user.to.proxy': ''}
      ],
      flowProxyUser: '',
      // 璋冨害璁剧疆鍙傛暟
      scheduleParams: {},
      // 宸ヤ綔娴佺骇鍒殑璧勬簮
      resources: [],
      // 鏄惁鏄剧ず淇濆瓨妯℃€佹
      saveModal: false,
      saveModel: {
        comment: '',
      },
      // 鎺у埗鍙傛暟妯℃€佹鏄惁鏄剧ず
      isParamModalShow: false,
      isResourceShow: false,
      // 鏄惁鏈夋敼鍙?      jsonChange: false,
      loading: false,
      repetitionNameShow: false,
      repeatTitles: [],
      nodebaseinfoShow: false, // 鑷畾涔夎妭鐐逛俊鎭脊绐楀睍绀?      clickCurrentNode: {}, // 褰撳墠鐐瑰嚮鐨勮妭鐐?      edgeConfigShow: false,
      currentEdge: {},
      edgeForm: {
        branchLabel: '',
        condition: '',
        priority: 1,
        isDefault: false,
      },
      viewOptions: {
        showBaseInfoOnAdd: false, // 涓嶆樉绀洪粯璁ょ殑鎷栨嫿娣诲姞鑺傜偣寮瑰嚭鐨勫熀纭€淇℃伅闈㈡澘
        shapeView: true, // 宸︿晶shape鍒楄〃
        control: true,
        linkType: 'straight' // straight锛氱洿绾匡紙鐩磋鎶樼嚎锛夛紱curve锛氭枩绾?      },
      addNodeShow: false, // 鍒涘缓鑺傜偣鐨勫脊绐楁樉绀?      addEdgesShow: false, // 鎵归噺鍒涘缓鑺傜偣鐨勫脊绐楁樉绀?      addEdgesForm: {
        currentNode: '',
        currentNodeName: '',
        upstreamNodes: [],
        downstreamNodes: []
      },
      upstreamNodeList: [],
      downstreamNodeList: [],
      cacheNode: null,
      addNodeTitle: this.$t('message.workflow.process.createSubFlow'), // 鍒涘缓鑺傜偣鏃跺脊绐楃殑title
      workflowIsExecutor: false, // 褰撳墠宸ヤ綔娴佹槸鍚﹀啀鎵ц
      openningNode: null, // 涓婁竴娆℃墦寮€鎺у埗鍙扮殑鑺傜偣
      shapeWidth: 0, // 娴佺▼鍥炬彃浠跺乏渚у伐鍏锋爮鐨勫搴?      workflowExeteId: '',
      workflowTaskId: '',
      excuteTimer: '',
      executorStatusTimer: '',
      workflowExecutorCache: [],
      isDispatch: false,
      contextID: '',
      pubulishFlowComment: '',
      pubulishShow: false,
      publishFlowColumns:  [
          {
              title: '鎻愪氦ID',
              key: 'commitId',
              minWidth: 220
          },
          {
              title: '鎻愪氦鏃堕棿',
              key: 'commitTime',
              minWidth: 160
          },
          {
              title: '鎻愪氦浜?,
              key: 'commitUser',
              minWidth: 120
          },
          {
              title: '娉ㄩ噴',
              key: 'comment',
              minWidth: 260
          }
      ],
      publishFlowData: [],
      flowVersion: '',
      isFlowPubulish: false,
      workflowExportShow: false,
      exportDesc: '',
      exporTChangeVersion: false,
      consoleParams: [],
      consoleHeight: 250,
      needReRun: false,
      locked: false,
      showNodePathPanel: false,
      iframeloading: false,
      isfullScreen: false,
      saveLoading: false, // 鑺傜偣鍙傛暟闈㈡澘淇濆瓨鎸夐挳
      viewMode: 'vueprocess' //  vueprocess, cyeditor or table
    };
  },
  computed: {
    getConsoleStyle() {
      return {
        'left': this.shapeWidth + 'px',
        'width': `calc(100% - ${this.shapeWidth}px)`
      }
    },
    // 鑾峰彇鏂板缓鑺傜偣鏃堕渶瑕佺殑鍙傛暟鍒楄〃
    createNodeParamsList() {
      return this.clickCurrentNode.nodeUiVOS ? this.clickCurrentNode.nodeUiVOS.filter((item) => item.baseInfo) : [];
    },
    formRules() {
      let rules = {};
      this.createNodeParamsList.map((item) => {
        rules[item.key] = this.paramsValid(item);
      })
      return rules;
    },
    currentEdgeSourceName() {
      const node = this.getNodeByKey(this.currentEdge.source);
      return node ? node.title : '';
    },
    currentEdgeTargetName() {
      const node = this.getNodeByKey(this.currentEdge.target);
      return node ? node.title : '';
    },
    showDispatchHistoryButton() {
      return this.$route.query.notPublish === 'true' || this.$route.query.notPublish === true
    },
    myReadonly() {
      return JSON.parse(this.readonly);
    },
    type() {
      return !this.isRootFlow ? 'subFlow' : 'flow'; // flow宸ヤ綔娴侊紝 subFlow瀛愬伐浣滄祦
    },
    nodeMenuOptions() {
      return {
        defaultMenu: {
          config: false, // 涓嶅睍绀洪粯璁ょ殑鍩虹淇℃伅鑿滃崟椤?          param: false, // 涓嶅睍绀洪粯璁ょ殑鍙傛暟閰嶇疆鑿滃崟椤?          copy: false,
          delete: !this.workflowIsExecutor && !this.myReadonly
        },
        userMenu: [],
        beforeShowMenu: (node, arr, type) => {
          if (this.myReadonly) arr = []
          // type : 'node' | 'link' | 'view' 鍒嗗埆鏄妭鐐瑰彸閿紝杈瑰彸閿紝鐢诲竷鍙抽敭
          // 濡傛灉鏈塺unState璇存槑宸茬粡鎵ц杩?          if (node && node.runState) {
            if (node.runState.showConsole && node.runState.taskID) {
              arr.push({
                text: this.$t('message.workflow.process.console'),
                value: 'console',
                icon: 'xitongguanlitai'
              })
            }
          }
          if (!this.workflowIsExecutor&& !this.myReadonly) {
            if (type === 'node') {
              if ([NODETYPE.SPARKSQL, NODETYPE.HQL, NODETYPE.SPARKPY, NODETYPE.SCALA, NODETYPE.NEBULA].includes(node.type)) {
                arr.push({
                  text: this.$t('message.workflow.process.associate'),
                  value: 'associate',
                  icon: 'associate', // 鍥炬爣璧勬簮鏂囦欢锛屼篃鍙互閫氳繃icon閰嶇疆鍐呯疆瀛椾綋鏂囦欢鏀寔鐨刢lassName
                });
              }
              arr.push({
                text: this.$t('message.workflow.process.relySelect'),
                value: 'relySelect',
                icon: 'depselect',
                children: [{
                  text: this.$t('message.workflow.process.upstreamLevelOne'),
                  value: 'relySelectUpOne',
                  icon: 'depselect',
                }, {
                  text: this.$t('message.workflow.process.downstreamLevelOne'),
                  value: 'relySelectDownOne',
                  icon: 'depselect',
                }, {
                  text: this.$t('message.workflow.process.upAllLevel'),
                  value: 'relySelectUp',
                  icon: 'depselect',
                }, {
                  text: this.$t('message.workflow.process.downAllLevel'),
                  value: 'relySelectDown',
                  icon: 'depselect',
                }
                ]
              });
              // 閫氳繃鑺傜偣绫诲瀷鍘诲垽鏂槸鍚︽敮鎸佸鍒?              if (this.nodeCopy(node)) {
                arr.push({
                  text: this.$t('message.workflow.copy'),
                  value: 'mycopy',
                  icon: 'fuzhi',
                });
              }
              arr.push({
                value: 'addEdges',
                text: '鎵归噺鍏宠仈鑺傜偣',
                icon: 'addLink'
              })
              if ([NODETYPE.SPARKSQL, NODETYPE.HQL].includes(node.type)) {
                arr.push({
                  value: 'addDatachecker',
                  text: '鐢熸垚Datachecker',
                  icon: 'icon-datacheck'
                })
              }
            }
          }
          if (type === 'view'&& !this.myReadonly) {
            arr.push({
              text: this.$t('message.workflow.paste'),
              value: 'mypaste',
              icon: 'zhantie',
            });
            arr.push({
              text: this.$t('message.workflow.process.allDelete'),
              value: 'allDelete',
              icon: 'delete'
            });
          }
          return arr;
        }
      }
    },
    tableViewUrl() {
      return `/next-web/#/workspace/workflow?workspaceId=${this.$route.query.workspaceId}&projectId=${this.$route.query.projectID}&flowId=${this.flowId}&labels=${this.getCurrentDsslabels()}`
    },
    isFlowSubmited() {
      return (this.associateGit && ['push', 'publish'].includes(this.flowStatus)) || !this.associateGit;
    }
  },
  created() {
    this.viewOptions.shapeView = !this.myReadonly;
  },
  watch: {
    jsonChange(val) {
      this.$emit('isChange', val);
    },
    workflowExecutorCache() {
      storage.set("workflowExecutorCache", this.workflowExecutorCache, 'local');
    }
  },
  mounted() {
    this.workflowExecutorCache = storage.get('workflowExecutorCache', 'local') || [];
    // 鏌ユ壘缂撳瓨涓槸鍚︽湁褰撳墠宸ヤ綔娴?    const currentExecutorFlow = this.workflowExecutorCache.filter((item) => item.flowId === this.flowId)
    if (currentExecutorFlow.length > 0) {
      this.workflowIsExecutor = true;
      this.queryWorkflowExecutor(currentExecutorFlow[0].execID, currentExecutorFlow[0].taskID)
      this.workflowExeteId = currentExecutorFlow[0].execID
      this.workflowTaskId = currentExecutorFlow[0].taskID
    }
    // 鍩虹淇℃伅
    this.setShapes().then(() => {
      this.getBaseInfo();
    });
    this.shapeWidth = this.$refs.process && this.$refs.process.state.shapeOptions.viewWidth; // 鑷€傚簲鎺у埗鍙板搴?    this.getConsoleParams();
    document.addEventListener('keyup', this.onKeyUp)
    eventbus.on('workflow.opennode.by.name', this.openNodeByName);
    eventbus.on('workflow.fold.left.tree', this.foldHandler);
    eventbus.on('workflow.copying', this.onCopying);
    window.addEventListener('message', this.msgEvent, false);
    window.addEventListener('resize', this.resizeConsole, false);
    this.checkSubmitStatus('init');
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
    if (this.excuteTimer) {
      clearTimeout(this.excuteTimer);
    }
    if (this.executorStatusTimer) {
      clearTimeout(this.executorStatusTimer);
    }
    if (this.updateLockTimer) {
      clearTimeout(this.updateLockTimer)
    }
    eventbus.off('workflow.fold.left.tree', this.foldHandler);
    eventbus.off('workflow.copying', this.onCopying);
    document.removeEventListener('keyup', this.onKeyUp);
    window.removeEventListener('message', this.msgEvent, false);
    window.removeEventListener('resize', this.resizeConsole, false);
  },
  methods: {
    // 鑾峰彇褰撳墠鑺傜偣瀵瑰簲鐨勬ā鏉夸俊鎭?    async getTemplateDataByProject(jobType) {
      const params = {
        projectId: this.$route.query.projectID,
        orchestratorId: this.$route.query.flowId,
        jobType,
      }
      const res = await getTemplateDatas(params);
      let templateList = [];
      res.forEach(e=> {
        e.child.forEach(d => {
          templateList.push(Object.assign(d))
        })
      });
      return templateList;
    },
    resizeConsole: debounce(function() {
      this.consoleHeight = this.$el ? this.$el.clientHeight / 2 : 250
    }, 300),
    msgEvent(e) {
      if (e.data) {
        try {
          let data = typeof e.data === 'string' ? JSON.parse(e.data) : e.data || {}
          if (data.type === 'dss-nextweb'&& data.flowId == this.flowId) {
            if (data.action === 'open_node') {
              this.dblclick(data.node)
            } else if(data.action === 'node_dependance') {
              this.showSearchPath()
            } else if(data.action === 'node_params') {
              this.click(data.node)
            }
          }
        } catch(err) {
          window.console.error(err)
        }
      }
    },
    foldHandler() {
      if (this.viewMode === 'vueprocess') {
        const refs = this.$refs
        refs.process && refs.process.layoutView()
      }
    },
    onCopying(data) {
      if (data.source.orchestratorId == this.orchestratorId) {
        this.locked = !data.done
      }
    },
    eventFromExt(evt) {
      if (evt && evt.callFn && typeof this[evt.callFn] === 'function') {
        this[evt.callFn](...evt.params)
      }
    },
    release(obj) {
      this.$emit('release', obj);
    },
    // 淇濆瓨node鍙傛暟淇敼
    saveNodeParameter() {
      this.$refs.nodeParameter.save();
    },
    saveButtonStatus(loading) {
      this.saveLoading = loading
    },
    // 鍙抽敭鍒ゆ柇鏄惁鏀寔澶嶅埗
    nodeCopy(node) {
      let flag = false;
      this.shapes.forEach((item) => {
        if (item.children) {
          item.children.forEach((subItem) => {
            if (subItem.type === node.type) {
              flag = subItem.enableCopy;
              return;
            }
          })
        }
      })
      return flag;
    },
    // 鍚勫弬鏁扮殑鏍￠獙鏂规硶
    paramsValid(param) {
      // 鑷畾涔夊嚱鏁扮殑鏂规硶鍏堝啓杩欓噷
      const validatorTitle = (rule, value, callback) => {
        if (value === `${this.name}`) {
          callback(new Error(rule.message));
        } else {
          callback();
        }
      }
      let temRule = [];
      if (param.nodeUiValidateVOS) {
        param.nodeUiValidateVOS.map((item) => {
          // 濡傛灉鏄鍒欑被鍨嬬殑灏卞啓鎴愭鍒?          if (item.validateType === 'Required') {
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
              validator: ['validatorTitle'].includes(item.validateRange) ? validatorTitle : () => {},
              trigger: 'blur'
            })
          }
        })
      }
      return temRule;
    },
    // 鏍规嵁杩斿洖鐨勬坊鍔犲幓鑾峰彇闇€瑕佺粦瀹氱殑鍒楄〃
    conditionBindList(param) {
      let temArry = [];
      if (param.defaultValue === 'empty') {
        temArry.push({
          name: this.$t('message.workflow.process.notBinding'),
          key: 'empty'
        })
      }
      // 瀵圭粦瀹氱殑鍙傛暟杩涜杩囨护
      const conditionResult = (type) => {
        if (param.value && JSON.parse(param.value)) {
          // 濡傛灉鏄€氶厤绗﹀氨杩斿洖true
          const optionsList = JSON.parse(param.value);
          if (optionsList[0] === '*') {
            return true;
          } else {
            return optionsList.includes(type);
          }
        }
      }
      if (this.json.nodes && this.json.nodes.length) {
        this.json.nodes.forEach((node) => {
          if (node.key !== this.clickCurrentNode.key && conditionResult(node.type)) {
          // 褰搒ql鑺傜偣閲岄潰娌″唴瀹规椂,resources灞炴€у€间负[]锛岃繖绉峴ql鑺傜偣涓嶆斁鍋氶€夐」
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
    filterFormType(val) {
      switch (val) {
        case 'Text':
          return 'textarea';
        default:
          return 'text';
      }
    },
    updateOriginData(node, scriptisSave) {
      this.json.nodes = this.json.nodes.map((item) => {
        if (item.key === node.key) {
          item.jobContent = node.jobContent;
          item.resources = node.resources;
          item.params = node.params;
          item.modifyUser = this.getUserName();
          item.modifyTime = Date.now();
        }
        return item;
      })
      // 閬垮厤鍦ㄤ繚瀛樿剼鏈椂锛屽凡鎵撳紑鍙充晶鍙傛暟鏍忥紝姝ゆ椂淇濆瓨鐨勪細鏄棫鍊?      if (this.clickCurrentNode && this.clickCurrentNode.key === node.key) {
        this.clickCurrentNode.jobContent = node.jobContent;
        this.clickCurrentNode.resources = node.resources;
        this.clickCurrentNode.params = node.params;
      }
      this.originalData = this.json;
      // 鏇存柊鑺傜偣涔嬪悗鑷姩淇濆瓨json
      if (scriptisSave) {
        this.autoSave(this.$t('message.workflow.Save'), false);
      }
    },
    urlContainsParams(paramsToCheck) {
      // 鑾峰彇褰撳墠URL
      let url = new URL(window.location.href);
      // window.console.log('Current URL:', url.toString());

      // 妫€鏌ユ煡璇㈠瓧绗︿覆閮ㄥ垎
      let searchParams = new URLSearchParams(url.search);

      // 灏嗚妫€鏌ョ殑鍙傛暟杞崲涓烘暟缁?      if (typeof paramsToCheck === 'string') {
          paramsToCheck = [paramsToCheck];
      }

      // 鐢ㄤ簬瀛樺偍鍝簺鍙傛暟琚壘鍒?      let foundParams = [];

      // 閬嶅巻闇€瑕佹鏌ョ殑鍙傛暟鍒楄〃
      paramsToCheck.forEach(param => {
          if (searchParams.has(param)) {
              foundParams.push(param);
              // window.console.log(`Parameter "${param}" found in query string.`);
          }
      });

      // 鑾峰彇骞惰В鏋愬搱甯岄儴鍒?      let hash = window.location.hash.slice(1); // 鍘绘帀寮€澶寸殑 #
      if (hash) {
          // 浣跨敤鍗犱綅绗︽潵鍒涘缓涓€涓湁鏁堢殑URL浠ヨВ鏋愬搱甯岄儴鍒?          let hashUrl = new URL(`http://placeholder.com/?${hash}`);
          let hashSearchParams = new URLSearchParams(hashUrl.search);

          // 鍐嶆閬嶅巻闇€瑕佹鏌ョ殑鍙傛暟鍒楄〃锛岃繖娆℃槸閽堝鍝堝笇閮ㄥ垎
          paramsToCheck.forEach(param => {
              if (!foundParams.includes(param) && hashSearchParams.has(param)) {
                  foundParams.push(param);
                  // window.console.log(`Parameter "${param}" found in hash.`);
              }
          });
      }

      // 濡傛灉鎵€鏈夎妫€鏌ョ殑鍙傛暟閮借鎵惧埌浜?      if (foundParams.length === paramsToCheck.length) {
          // window.console.log('All specified parameters are present.');
          return true;
      } else {
          window.console.log('Some or none of the specified parameters are present.');
          return false;
      }
    },
    getBaseInfo() {
      this.loading = true;
      this.clickCurrentNode = {};
      this.nodebaseinfoShow = false;
      this.getOriginJson();
      // 鑷姩鎵撳紑瀛愬伐浣滄祦
      let containsParams = this.urlContainsParams(['appId']);
      if(containsParams && this.$route.query.appId && this.flowId !== Number(this.$route.query.appId)) {
        api.fetch(`/dss/workflow/get`, {
          flowId: Number(this.$route.query.appId),
          labels: this.getCurrentDsslabels()
        },'get').then((res) => {
          const arg = {
          appId: Number(this.$route.query.appId),     // (璺宠浆鑺傜偣鎵€灞?鐩存帴宸ヤ綔娴乮d
          flowId: Number(this.$route.query.flowId),  // 椤跺眰宸ヤ綔娴乮d
          flowName: res.flow.name || '', // 鐩存帴宸ヤ綔娴佸悕绉?          flowNodeId: res.flow.resourceId || '', // 鐩存帴宸ヤ綔娴佽妭鐐筰d
          jumpNodeName: this.$route.query.jumpNodeName, // 璺宠浆鑺傜偣鍚嶇О
        }
        this.$emit('open-subFlow', arg);
        }).catch((err) => {
          window.console.error(err);
        })
      }
    },
    initAction(json) {
      // 鍒涘缓宸ヤ綔娴佷箣鍚庡氨鏈夊€?      this.contextID = json.contextID;
      // 淇濆瓨鑺傜偣鎵嶆湁鐨勫€?      this.schedulerAppConnName = json.schedulerAppConnName
      if (json) {
        if (json.nodes) {
          this.originalData = this.json = JSON.parse(JSON.stringify(json));
          this.resources = json.resources;
        }
        if (json.props) {
          this.props = json.props;
          this.flowProxyUser = (json.props[0] || {})['user.to.proxy'];
          this.scheduleParams = json.scheduleParams || {};
        }
      }
      if (json.config && json.config.type != 'table') {
        this.viewMode = json.config.type
      }
      this.pollUpdateLock();
      this.$nextTick(() => {
        this.loading = false;
        let containsParams = this.urlContainsParams(['appId', 'nodeName']);
        if (containsParams &&  this.$route.query.origin === 'gitSearch') {
          this.openSubNodeByName()
        } else {
          this.openNodeByName()
        }
      })
    },
    openSubNodeByName() {
      if (!this.originalData) return
      let nodeName = this.$route.query.nodeName;
      const node = this.originalData.nodes.find(node => {
        return node.title === nodeName
      })
      if (node) {
        this.dblclick(node)
      }
      setTimeout(() => {
        storage.remove('openflownode');
      }, 2500)
    },
    openNodeByName(params) {
      if (!this.originalData) return
      const openflownode = storage.get('openflownode');
      let nodeName;
      let flowId;
      if (params) {
        flowId = params.flowId
        nodeName = params.nodeName
      } else if (openflownode && !params) {
        // 鏌ユ壘宸ヤ綔娴佸唴瀹规柊绐楀彛鎵撳紑鑺傜偣鐨勬儏鍐?        params = openflownode.split('_flowidname_')
        flowId = params[0] 
        nodeName = params[1] 
      }
      const node = this.originalData.nodes.find(node => {
        return node.title === nodeName
      })
      if (flowId == this.$route.query.flowId && node) {
        this.dblclick(node)
      }
      setTimeout(() => {
        storage.remove('openflownode');
      }, 2500)
    },
    getOriginJson() {
      return api.fetch(`/dss/workflow/get`, {
        flowId: this.flowId,
        labels: this.getCurrentDsslabels()
      },'get').then((res) => {
        let json = this.convertJson(res.flow);
        let flowEditLock = res.flow.flowEditLock;
        if (flowEditLock && !this.myReadonly) {
          this.setFlowEditLock(flowEditLock);
        }
        if (json) {
          this.initAction(json);
        } else {
          this.loading = false;
        }
      }).catch((err) => {
        window.console.error(err);
        this.loading = false;
        this.locked = true;
        if (this.updateLockTimer) {
          clearTimeout(this.updateLockTimer)
        }
        this.$emit('close')
      });
    },
    convertJson(flow) {
      this.name = flow.name;
      this.isRootFlow = flow.rootFlow;
      this.rank = flow.rank; // 宸ヤ綔娴佸眰绾?      let json;
      json = flow.flowJson;

      if (json) {
        json = JSON.parse(json);
        if (json.nodes && Array.isArray(json.nodes)) {
          json.nodes = json.nodes.map((node) => {
            node.disabled = false;
            if (node.params && node.params.configuration && node.params.configuration.special) {
              node.disabled = node.params.configuration.special['auto.disabled'] === 'true';
            }
            node.type = node.jobType;
            delete node.jobType;
            return node;
          });
        }
        this.orcVersion = json.orcVersion
        // 浠ｇ悊鐢ㄦ埛鏈夐粯璁ゅ€煎仛榛樿璧嬪€?        if(flow.defaultProxyUser) {
          if(!json.scheduleParams) {
            json.scheduleParams = {}
            json.scheduleParams.proxyuser = flow.defaultProxyUser
          }
          if(!json.props) {
            json.props= [{
              'user.to.proxy': flow.defaultProxyUser,
            }]
          }
        }
      }
      return json;
    },
    setShapes() {
      return api.fetch(`${this.$API_PATH.WORKFLOW_PATH}listNodeType`, {
        labels: this.getCurrentDsslabels()
      }, {
        method: 'get',
        cacheOptions: { time: 2 * 60 * 1000 },
      }).then((res) => {
        this.shapes = res.nodeTypes.map((item) => {
          if (item.children.length > 0) {
            item.children = item.children.map((subItem) => {
              // svg缁樺埗鐨勭偣澶锛屽鑷村姩鐢诲崱椤匡紝浣跨敤鍥剧墖浠ｆ浛
              if (nodeIcons[subItem.title]) {
                subItem.image = nodeIcons[subItem.title];
              } else if (subItem.image) {
                subItem.image = 'data:image/svg+xml;base64,' + window.btoa(unescape(encodeURIComponent(subItem.image)))
              } else {
                subItem.image = `/api/rest_j/v1/dss/workflow/nodeIcon/${subItem.type}`
              }
              return subItem
            })
          }
          return item;
        });
      });
    },
    change(obj) {
      if (!obj) return
      const change = this.checkChange(obj)
      this.json = obj;
      if (change) {
        this.heartBeat();
        this.jsonChange = true;
      }
    },
    checkChange(obj) {
      // 鑺傜偣澧炲垹,杩炵嚎澧炲垹瑙嗕负鍙戠敓鏀瑰彉
      return this.json ? obj.edges.length != this.json.edges.length ||  obj.nodes.length != this.json.nodes.length : true
    },
    initNode(arg) {
      if(this.clickCurrentNode.id && this.clickCurrentNode.id === arg.id) return; // 澶氬嚭鐐瑰嚮鏃讹紝閬垮厤鏁版嵁鍒濆鍖?      arg = this.bindNodeBasicInfo(arg);
      this.clickCurrentNode = JSON.parse(JSON.stringify(arg));
    },
    click(arg) {
      clearTimeout(this.timerClick);
      this.timerClick = setTimeout(() => {
        if (this.workflowIsExecutor) return;
        this.edgeConfigShow = false;
        this.nodebaseinfoShow = true;
        this.initNode(arg);
        this.$emit('node-click', arg);
      }, 200);
    },
    clickEdge(edge) {
      if (!edge) return;
      const sourceNode = this.getNodeByKey(edge.source);
      if (!this.isBranchNode(sourceNode)) {
        this.$Message.info('只有分支节点的出边支持条件配置');
        return;
      }
      this.nodebaseinfoShow = false;
      this.isParamModalShow = false;
      this.currentEdge = JSON.parse(JSON.stringify(edge));
      this.edgeForm = {
        branchLabel: edge.branchLabel || '',
        condition: edge.condition || '',
        priority: edge.priority || 1,
        isDefault: `${edge.isDefault}` === 'true' || edge.isDefault === true,
      };
      this.edgeConfigShow = true;
    },
    dblclick(...arg) {
      if (this.lastDblClickTime && Date.now() - this.lastDblClickTime < 600) {
        return; // 闃叉鍙屽嚮浜嬩欢瑙﹀彂澶氭
      }
      this.lastDblClickTime = Date.now();
      arg[0] = this.bindNodeBasicInfo(arg[0]);
      arg[0].contextID = this.contextID;
      // 鐢卞悗鍙版帶鍒舵槸鍚︽敮鎸佽烦杞?      clearTimeout(this.timerClick);
      // 鎵ц杩囩▼涓彧鏈夊瓙宸ヤ綔娴佸彲鍙屽嚮鎵撳紑
      if (!arg[0].supportJump || (this.workflowIsExecutor && arg[0].type !== NODETYPE.FLOW)) return;
      if ((!arg[0].jobContent || Object.keys(arg[0].jobContent).length === 0) && arg[0].shouldCreationBeforeNode) {
        this.addNodeShow = true;
        this.clickCurrentNode = JSON.parse(JSON.stringify(arg[0]));
        this.addNodeTitle = this.$t('message.workflow.process.createNode');
      } else {
        // 涓簄ode淇℃伅娣诲姞modelType瀛楁鏂逛究鑴氭湰鏍煎紡鍒ゆ柇
        arg[0].modelType = ext[arg[0].type];
        // dpms  /product/100199/story/detail/365928 閰嶇疆浜嗘ā鏉垮垯浼犻€掑弬鏁版椂鏍规嵁閰嶇疆杩囨护
        if (arg[0].params && arg[0].params.configuration && arg[0].params.configuration.startup["ec.conf.templateId"]) {
          arg[0].nodeUiVOS.forEach((item) => {
            let show = this.$refs.nodeParameter.checkShow(item, arg[0]);
            if (show === false) {
              if (typeof item.condition === 'string' && item.condition && item.condition.indexOf('configuration.startup') > -1) {
                delete arg[0].params.configuration.startup[item.key];
              }
              if (typeof item.condition === 'string' && item.condition && item.condition.indexOf('configuration.runtime') > -1) {
                delete arg[0].params.configuration.runtime[item.key];
              }
            }
          })
        }
        if (arg[0].params && arg[0].params.configuration && arg[0].params.configuration.startup) {
          delete arg[0].params.configuration.startup['wds.linkis.rm.yarnqueue']
        }
        this.$emit('node-dblclick', arg);
      }
    },
    message(obj) {
      let type = {
        warning: 'warn',
        error: 'error',
        info: 'info',
      };
      this.$Message[type[obj.type]]({
        content: obj.msg,
        duration: 2,
      });
    },
    async saveNodeBaseInfo(arg) {
      if (this.lastSaveTime && new Date().getTime() - this.lastSaveTime < 2000) {
        return
      } else {
        this.lastSaveTime = new Date().getTime();
      }
      this.$emit('saveBaseInfo', arg);
      // 濡傛灉鏄彲缂栬緫鑴氭湰寰楁敼鍙樻墦寮€鐨勮剼鏈緱鍚嶇О
      this.dispatch('Workbench:updateFlowsNodeName', arg);
      // 褰撲繚瀛樺瓙娴佺▼鑺傜偣鐨勫熀纭€淇℃伅鏃讹紝濡傛灉瀛愭祦绋嬭妭鐐规病鏈?embeddedFlowId:"flow_id" 鍒欏厛鍒涘缓瀛愭祦绋嬭妭鐐?      let node = arg;
      if (node.type == NODETYPE.FLOW) {
        if (this.rank >= 4) {
          return this.$Message.warning(this.$t('message.workflow.process.rankLimit'));
        }

        //  鑺傜偣鍘熷鏁版嵁
        if (!node.jobContent) {
          node.jobContent = {};
        }
        // 濡傛灉瀛愭祦绋嬭妭鐐圭殑node.jobContent.embeddedFlowId涓虹┖琛ㄦ槑杩樻湭瀛愭祦绋嬭繕鏈垱寤虹敓鎴恌lowID
        const reg = /^[a-zA-Z][a-zA-Z0-9_]*$/;
        if (!node.title.match(reg)) {
          return this.$Message.warning(this.$t('message.workflow.validNameDesc'));
        }

        if (!node.jobContent.embeddedFlowId) {
          // 璋冪敤鎺ュ彛鍒涘缓
          const result = await api.fetch(`${this.$API_PATH.WORKFLOW_PATH}addFlow`, {
            name: node.title,
            description: node.desc,
            parentFlowID: Number(this.flowId),
            workspaceName: this.getCurrentWorkspaceName(),
            projectName: this.$route.query.projectName,
            version: this.version,
            userName: this.getUserName(),
            labels: {route: this.getCurrentDsslabels()},

          }).then((res) => {
            this.$Notice.success({
              desc: this.$t('message.workflow.process.createSubSuccess'),
            });
            node.jobContent.embeddedFlowId = res.flow.id;
            return true
          });
          if (result !== true) {
            return
          }
        } else {
          await api.fetch(`${this.$API_PATH.WORKFLOW_PATH}updateFlowBaseInfo`, {
            id: node.jobContent.embeddedFlowId,
            name: node.title,
            description: node.desc,
            labels: {route: this.getCurrentDsslabels()}
          }, 'post').then(() => {
          })
        }
      } else {
        // iframe鑺傜偣
        await this.saveCommonIframe(node);
      }

      // 涓轰簡琛ㄥ崟鏍￠獙锛屽熀纭€淇℃伅寮圭獥淇濆瓨鐨勮妭鐐瑰凡涓嶅啀鏄搷搴斿紡锛岄渶閲嶆柊璧嬪€肩粰json

      this.json.nodes = this.json.nodes.map((item) => {
        if (item.key === node.key) {
          item.title = node.title;
          item.desc = node.desc;
          item.ecConfTemplateName = node.ecConfTemplateName;
          item.ecConfTemplateId = node.ecConfTemplateId;
          item.jobContent = node.jobContent;
          item.resources = node.resources || [];
          item.params = node.params; // 鑺傜偣鍙傛暟鐜板湪瀛樺湪杩欓噷锛屽拰jobparams涓€鏍?          item.appTag = node.appTag;
          item.businessTag = node.businessTag;
          item.modifyUser = this.getUserName();
          item.modifyTime = Date.now();
          item.disabled = false;
          if (item.params && node.params.configuration && node.params.configuration.special) {
            item.disabled = node.params.configuration.special['auto.disabled'] === 'true';
          }
        }
        item.selected = item.key === node.key
        return item;
      });
      this.originalData = {...this.json};
      this.jsonChange = true;
      this.addNodeShow = false;
      // 淇濆瓨宸ヤ綔娴?      this.autoSave('paramsSave', false);
      // 琛ㄦ牸妯″紡鏇存柊
      if (this.viewMode === 'table') {
        const ifr = this.$refs.ifr;
        if (ifr) {
          setTimeout(()=> {
            ifr.contentWindow.postMessage(JSON.stringify({
              type: 'dss_change_node'
            }), "*");
          }, 600)
        }
      }
    },
    /**
     * 淇濆瓨宸ヤ綔娴?     */
    handleSave: debounce(function () {
      this.save()
    }, 1500),
    save() {
      if (this.workflowIsExecutor) return;
      // 妫€鏌SON
      if (!this.validateJSON()) {
        return;
      }
      // 妫€鏌ュ綋鍓峧son鏄惁鏈夊瓙鑺傜偣鏈繚瀛?      const subArray = this.openFiles[this.name] || [];
      const changeList = this.tabs.filter((item) => {
        return subArray.includes(item.key) && item.node.isChange;
      });
        // 淇濆瓨鏃跺叧闂帶鍒跺彴
      this.openningNode = null;
      if (changeList.length > 0) {
        this.$Modal.confirm({
          title: this.$t('message.workflow.process.cancelNotice'),
          content: this.$t('message.workflow.process.noSaveHtml'),
          okText: this.$t('message.workflow.process.confirmSave'),
          cancelText: this.$t('message.workflow.cancel'),
          onOk: () => {
            this.saveModal = false;
            let json = JSON.parse(JSON.stringify(this.json));
            if (this.viewMode !== 'table' && this.$refs.process) {
              json.nodes.forEach((node) => {
                this.$refs.process.setNodeRunState(node.key, {
                  borderColor: '#6A85A7',
                })
              })
            }
            this.autoSave(this.$t('message.workflow.Manually'), false);
          },
          onCancel: () => {
          },
        });
      } else {
        this.saveModal = false;
        let json = JSON.parse(JSON.stringify(this.json));
        if (this.viewMode !== 'table' && this.$refs.process) {
          json.nodes.forEach((node) => {
            this.$refs.process.setNodeRunState(node.key, {
              borderColor: '#6A85A7',
            })
          })
        }
        this.autoSave(this.$t('message.workflow.Manually'), false);
      }
    },
    autoSave(comment, f) {
      // 妫€鏌SON
      if (!this.validateJSON()) {
        this.loading = false;
        return false;
      }
      if (this.viewMode === 'cyeditor' && comment === 'deleteSave') {
        this.originalData = this.json;
      }
      let json = JSON.parse(JSON.stringify(this.json));
      let flage = false;
      // 鑺傜偣杩炵嚎淇濆瓨鏁版嵁key鐧藉悕鍗?      json.nodes =  json.nodes.map((node) => {
        const keys = [
          'ecConfTemplateId',
          'ecConfTemplateName',
          'jobContent',
          'key',
          'title',
          'desc',
          'layout',
          'params',
          'resources',
          'createTime',
          'modifyTime',
          'modifyUser',
          'id',
          'jobType',
          'businessTag',
          'type',
          'appTag'
        ]
        const data = {}
        keys.forEach(it => {
          data[it] = node[it]
        })
        const reg = /^[a-zA-Z][a-zA-Z0-9_]*$/;

        if (!node.title.match(reg)) {
          return flage = true;
        }
        data.id = data.key;
        data.jobType = data.type;
        delete data.type;
        // 灏嗙敤鎴蜂繚瀛樼殑resources鍊间负绌哄瓧绗︿覆杞负绌烘暟缁?        if (!data.resources) {
          data.resources = [];
        }
        return data
      });
      // 鎷栨嫿妯″紡淇濆瓨
      json.config = {
        ...json.config,
        type: this.viewMode === 'table' ? this.preDragViewMode || 'vueprocess' : this.viewMode
      }
      if (flage) return this.$Message.warning(this.$t('message.workflow.validNameDesc'));
      const isFiveNode = json.nodes.filter((item) => {
        return !item.jobContent && item.jobType === NODETYPE.FLOW && this.rank >= 4;
      });
      if (isFiveNode.length > 0) return this.$Message.warning(this.$t('message.workflow.process.deleteNodeSave'));
      return this.saveRequest(json, comment, f);
    },
    // 淇濆瓨璇锋眰
    saveRequest(json, comment, f) {
      const updateTime = Date.now();
      const paramsJson = JSON.parse(JSON.stringify(Object.assign(json, {
        comment: comment,
        type: this.type,
        updateTime,
        updateUser: this.getUserName(),
        props: this.props,
        resources: this.resources,
        scheduleParams: this.scheduleParams,
        contextID: this.contextID,
        orcVersion: this.orcVersion
      })));
      if (this.schedulerAppConnName !== undefined) {
        paramsJson.schedulerAppConnName = this.schedulerAppConnName
      }
      return api.fetch(`${this.$API_PATH.WORKFLOW_PATH}saveFlow`, {
        id: Number(this.flowId),
        json: JSON.stringify(paramsJson),
        projectName: this.$route.query.projectName,
        workspaceName: this.getCurrentWorkspaceName(),
        labels: {
          route: this.getCurrentDsslabels()
        },
        flowEditLock: this.getFlowEditLock()
      }).then((res) => {
        this.loading = false;
        // 灏嗘洿鏂扮殑浜掓枼閿佺殑res.flowEditLock瀛楁瀛樺偍鍒版湰鍦?        let flowEditLock = res.flowEditLock;
        if (flowEditLock) {
          this.setFlowEditLock(flowEditLock)
          this.pollUpdateLock()
        }
        if (!f) {
          this.$Notice.success({
            desc: this.$t('message.workflow.process.saveWorkflowCuccess'),
          });
          this.saveModel.comment = '';
        } else {
          this.$Notice.success({
            desc: this.$t('message.workflow.process.autoSaveWorkflow'),
          });
        }
        this.jsonChange = false; 
        if(this.props && this.props.length > 0) {
          this.flowProxyUser = this.props[0]['user.to.proxy'];
        }
        // 淇濆瓨鎴愬姛鍚庡幓鏇存柊tab鐨勫伐浣滄祦鏁版嵁
        this.$emit('updateWorkflowList');
        if(!this.isFlowSubmit && !this.isFlowPubulish) {
          this.$emit('updateFlowStatus');
        }
        return res;
      }).catch((e) => {
        window.console.error('saveRequest',e)
        this.loading = false;
      });
    },
    /**
     * 鏄剧ず宸ヤ綔娴佸弬鏁伴厤缃〉闈?     */
    showParamView() {
      if (this.workflowIsExecutor) return;
      this.nodebaseinfoShow = false;
      if (!this.isResourceShow && !this.isDispatch) {
        this.isParamModalShow = !this.isParamModalShow;
      } else {
        this.isParamModalShow = true;
        this.isResourceShow = false;
        this.isDispatch = false;
      }
    },
    changeLinkType(type) {
      this.viewOptions = {
        ...this.viewOptions,
        linkType: type
      }
    },
    changeLinkStraight(k) {
      this.$refs.process.changeLinkType(k, 'straight')
    },
    changeLinkCurve(k) {
      this.$refs.process.changeLinkType(k, 'curve')
    },
    setFlowEditLock(flowEditLock) {
      let data = storage.get("flowEditLock") || {}
      const key = this.getUserName()
      const updateList = (data[key] || []).filter(it => it.projectId != this.$route.query.projectID)
      updateList.push({ flowId: this.flowId, lock: flowEditLock, projectId: this.$route.query.projectID })
      storage.set("flowEditLock", {
        [key]: updateList
      });
    },
    getFlowEditLock() {
      const data = storage.get("flowEditLock") || {}
      const key = this.getUserName()
      const item = (data[key] || []).find(it => it.flowId == this.flowId && it.projectId == this.$route.query.projectID)
      return item && item.lock
    },
    updateLock() {
      const flowEditLock = this.getFlowEditLock()
      if (!flowEditLock || this.myReadonly) return
      api.fetch(`${this.$API_PATH.WORKFLOW_PATH}updateFlowEditLock`, {
        flowEditLock,
        labels: this.getCurrentDsslabels()
      }, 'get')
        .then((rst) => {
          if (rst.flowEditLock) {
            this.setFlowEditLock(rst.flowEditLock)
            this.pollUpdateLock()
          }
        }).catch(()=>{
          //
        })
    },
    pollUpdateLock() {
      clearTimeout(this.updateLockTimer)
      this.updateLockTimer = setTimeout(()=>{
        this.updateLock()
      }, 2 * 60 * 1000)
    },
    /**
     * 鏄剧ず璧勬簮瀵煎叆椤甸潰
     */
    showResourceView() {
      if (this.workflowIsExecutor) return;
      this.isDispatch = false;
      this.nodebaseinfoShow = false;
      if (this.isResourceShow) {
        this.isParamModalShow = !this.isParamModalShow;
      } else {
        this.isParamModalShow = true;
        this.isResourceShow = true;
      }
    },
    /**
     * 妫€鏌SON锛屾槸鍚︾鍚堣鑼?     * @return {Boolean}
     */
    validateJSON() {
      if (!this.json) {
        this.$Modal.warning({
          title: this.$t('message.workflow.process.notice'),
          content: this.$t('message.workflow.process.dragNode'),
        });
        return false;
      }
      let edges = this.json.edges;
      let nodes = this.json.nodes;
      if (!nodes || nodes.length === 0) {
        this.$Modal.warning({
          title: this.$t('message.workflow.process.notice'),
          content: this.$t('message.workflow.process.dragNode'),
        });
        return false;
      }
      let headers = [];
      let footers = [];
      let titles = [];
      let repeatTitles = [];
      nodes.forEach((node) => {
        if (titles.includes(node.title)) {
          repeatTitles.push(node.title);
        } else {
          titles.push(node.title);
        }
        if (!edges.some((edge) => {
          return edge.target == node.key;
        })) {
          headers.push(node);
        }
        if (!edges.some((edge) => {
          return edge.source == node.key;
        })) {
          footers.push(node);
        }
      });
      // 鍚庡彴浼氭妸鍚嶇О褰撳仛id澶勭悊锛屾墍浠ュ悕绉板繀椤诲敮涓€
      if (repeatTitles.length > 0) {
        this.repeatTitles = repeatTitles;
        this.repetitionNameShow = true;
        return false;
      }
      if (!this.validateBranchEdges()) {
        return false;
      }
      return true;
    },
    getNodeByKey(key) {
      return (this.json && this.json.nodes || []).find((item) => (item.id || item.key) === key);
    },
    isBranchNode(node) {
      return node && node.type === NODETYPE.BRANCH;
    },
    isSameEdge(left, right) {
      if (!left || !right) return false;
      if ((left.id || left.key) && (right.id || right.key)) {
        return (left.id || left.key) === (right.id || right.key);
      }
      return left.source === right.source && left.target === right.target;
    },
    closeEdgeConfig() {
      this.edgeConfigShow = false;
      this.currentEdge = {};
    },
    saveEdgeConfig() {
      if (!this.currentEdge.source) return;
      if (!this.edgeForm.isDefault && !`${this.edgeForm.condition || ''}`.trim()) {
        return this.$Message.warning('分支条件不能为空');
      }
      const duplicateDefault = (this.json.edges || []).some((item) => {
        return item.source === this.currentEdge.source && !this.isSameEdge(item, this.currentEdge) && (`${item.isDefault}` === 'true' || item.isDefault === true) && this.edgeForm.isDefault;
      });
      if (duplicateDefault) {
        return this.$Message.warning('同一个分支节点只能有一条默认分支');
      }
      this.json.edges = (this.json.edges || []).map((item) => {
        if (!this.isSameEdge(item, this.currentEdge)) return item;
        return {
          ...item,
          branchLabel: this.edgeForm.branchLabel,
          condition: this.edgeForm.isDefault ? '' : `${this.edgeForm.condition || ''}`.trim(),
          priority: this.edgeForm.priority || 1,
          isDefault: !!this.edgeForm.isDefault,
        };
      });
      this.edgeConfigShow = false;
      this.jsonChange = true;
      this.originalData = { ...this.json };
      this.autoSave('edgeConfigSave', false);
    },
    validateBranchEdges() {
      const branchNodes = (this.json.nodes || []).filter((item) => this.isBranchNode(item));
      for (const node of branchNodes) {
        const edges = (this.json.edges || []).filter((edge) => edge.source === (node.id || node.key));
        if (edges.length < 2) {
          this.$Modal.warning({
            title: '分支节点校验失败',
            content: `分支节点【${node.title}】至少需要两条出边`,
          });
          return false;
        }
        const defaultEdges = edges.filter((edge) => `${edge.isDefault}` === 'true' || edge.isDefault === true);
        if (defaultEdges.length !== 1) {
          this.$Modal.warning({
            title: '分支节点校验失败',
            content: `分支节点【${node.title}】必须且只能配置一条默认分支`,
          });
          return false;
        }
        const invalidEdge = edges.find((edge) => (`${edge.isDefault}` !== 'true' && edge.isDefault !== true) && !`${edge.condition || ''}`.trim());
        if (invalidEdge) {
          this.$Modal.warning({
            title: '分支节点校验失败',
            content: `分支节点【${node.title}】的非默认出边必须填写条件表达式`,
          });
          return false;
        }
      }
      return true;
    },
    onPropsChange(value, proxyUser, proxyUserChange) {
      if (proxyUserChange) {
        api.fetch('/dss/framework/workspace/isDismissed', {usernames: [proxyUser]}, 'post').then(rst => {
          if (rst && (rst.isDismissed || []).some(item => Object.values(item)[0])) {
            this.$Message.warning('浠ｇ悊鐢ㄦ埛宸茬鑱屾垨涓嶅瓨鍦ㄧ殑鐢ㄦ埛');
          } else {
            this.jsonChange = true;
            this.props = value;
          }
        })
      } else {
        this.jsonChange = true;
        this.props = value;
      }
    },
    onScheduleChange(value) {
      this.scheduleParams = value;
    },
    handleOutsideClick(e) {
      let paramButton = this.$refs.paramButton;
      let resourceButton = this.$refs.resourceButton;
      let dispatchButton = this.$refs.dispatchButton;
      if ((paramButton && paramButton.contains(e.target)) || e.target === paramButton
                || (resourceButton && resourceButton.contains(e.target)) || e.target === resourceButton || (dispatchButton && dispatchButton.contains(e.target)) || e.target === dispatchButton) {
        return;
      }
      if (this.isParamModalShow) {
        this.isParamModalShow = false;
      }
    },
    handleOutsideClickNode(e) {
      if (e.target.className === 'node-box-content' || e.target.className === 'node-box') return;
      if (this.nodebaseinfoShow) {
        this.nodebaseinfoShow = false;
      }
    },
    updateResources(res) {
      this.resources = res.map((item) => {
        return {
          fileName: item.fileName,
          resourceId: item.resourceId,
          version: item.version,
        };
      });
      this.jsonChange = true;
      this.autoSave('鏇存柊璧勬簮鏂囦欢', false)
    },
    async nodeDelete(node) {
      // 姝ｅ湪鎵ц涓殑鑺傜偣涓嶈兘琚垹闄?      if (node && node.runState && node.runState.status === 1) {
        return;
      }
      node = this.bindNodeBasicInfo(node);
      if (node.type === NODETYPE.FLOW && node.jobContent && node.jobContent.embeddedFlowId) {
        const params = {
          id: +node.jobContent.embeddedFlowId,
          sure: false,
          labels: {
            route: this.getCurrentDsslabels()
          }
        }
        await api.fetch(`${this.$API_PATH.WORKFLOW_PATH}deleteFlow`, params, 'post').then(() => {
          this.$Message.success(this.$t('message.workflow.deleteSuccess'));
          this.$emit('deleteNode', node);
          if (this.$refs.process.deleteNode) {
            this.$refs.process.deleteNode(node.key)
          }
          // 濡傛灉鍒犻櫎鐨勬槸褰撳墠淇敼鍙傛暟鐨勮妭鐐癸紝鍏抽棴渚ц竟鏍?          if (this.clickCurrentNode.key === node.key) {
            this.clickCurrentNode = {};
            this.nodebaseinfoShow = false;
          }

          // 鍒犻櫎浜嬩欢姣攋sonchange鏃舵満鏃?          const timeId = setTimeout(() => {
            this.autoSave('deleteSave', false);
            clearTimeout(timeId);
          }, 500)
        })
      } else {
        if (node.jumpType == 1) {
          const params = {
            nodeType: node.type,
            name: node.title,
            description: node.desc,
            projectID: +this.$route.query.projectID,
            params: node.jobContent,
            labels: {
              route: this.getCurrentDsslabels()
            }
          }
          await api.fetch(`${this.$API_PATH.WORKFLOW_PATH}deleteAppConnNode`,params, 'post').then(() => {
            this.$Message.success(this.$t('message.workflow.deleteSuccess'));
            this.$emit('deleteNode', node);
            if (this.$refs.process.deleteNode) {
              this.$refs.process.deleteNode(node.key)
            }

            // 濡傛灉鍒犻櫎鐨勬槸褰撳墠淇敼鍙傛暟鐨勮妭鐐癸紝鍏抽棴渚ц竟鏍?            if (this.clickCurrentNode.key === node.key) {
              this.clickCurrentNode = {};
              this.nodebaseinfoShow = false;
            }

            // 鍒犻櫎浜嬩欢姣攋sonchange鏃舵満鏃?            const timeId = setTimeout(() => {
              this.autoSave('deleteSave', false);
              clearTimeout(timeId);
            }, 500)
          })
        } else {
          this.$emit('deleteNode', node);
          if (this.$refs.process.deleteNode) {
            this.$refs.process.deleteNode(node.key);
          }
          // 濡傛灉鍒犻櫎鐨勬槸褰撳墠淇敼鍙傛暟鐨勮妭鐐癸紝鍏抽棴渚ц竟鏍?          if (this.clickCurrentNode.key === node.key) {
            this.clickCurrentNode = {};
            this.nodebaseinfoShow = false;
          }

          // 鍒犻櫎浜嬩欢姣攋sonchange鏃舵満鏃?          const timeId = setTimeout(() => {
            this.autoSave('deleteSave', false);
            clearTimeout(timeId);
          }, 500)
        }
      }
    },
    repetitionName() {
      this.repetitionNameShow = false;
    },
    // 鍗曞嚮鑺傜偣鍑烘潵鐨勫彸杈圭殑寮规鐨勪繚瀛樹簨浠?    saveNode(node) { // 淇濆瓨鑺傜偣鍙傛暟閰嶇疆
      const nodeItem = this.json.nodes.find(item => {
        return item.id === node.id
      })
      if (nodeItem && nodeItem.params && nodeItem.params.configuration && nodeItem.params.configuration.startup && node.params && node.params.configuration && node.params.configuration.startup) {
        let hasChange = false;
        for (let key in nodeItem.params.configuration.startup) {
          if (key !== 'wds.linkis.rm.yarnqueue' && node.params.configuration.startup[key] !== nodeItem.params.configuration.startup[key]) {
            hasChange = true;
          }
        }
        if (hasChange) {
          this.$Modal.confirm({ title: '鎻愮ず', content: '璇锋敞鎰忓紩鎿庡弬鏁版湁淇敼锛岃嫢鑺傜偣宸叉墦寮€锛岃鍏抽棴鍚庨噸鏂版墦寮€锛屽悓鏃禟ill寮曟搸鏂规墠鐢熸晥锛? });
        }
      }
      if (this.myReadonly) return this.$Message.warning(this.$t('message.workflow.process.readonly'));
      this.saveNodeBaseInfo(node);
    },
    onContextMenu(menu, data, type) {
      switch(menu) {
        case 'associate':
          this.checkAssociated(data);
          break;
        case 'console':
          this.openConsole(data);
          break;
        case 'mycopy':
          this.copyNode(data);
          break;
        case 'mypaste':
          this.pasteNode(data);
          break;
        case 'allDelete':
          this.allDelete();
          break;
        case 'relySelectUpOne':
          this.relySelect(data, 'up-one');
          break;
        case 'relySelectDownOne':
          this.relySelect(data, 'down-one');
          break;
        case 'relySelectUp':
          this.relySelect(data, 'up');
          break;
        case 'relySelectDown':
          this.relySelect(data, 'down');
          break;
        case 'addDatachecker':
          this.addDatachecker(data);
          break;
        case 'delete':
          if (this.viewMode === 'cyeditor') {
            if (type ==='node') {
              this.nodeDelete(data)
            } else if(type === 'link') {
              this.linkDelete(data)
            }
          }
          break;
        case 'addEdges':
          this.beforeAddEdges(data);
          break;
      }
    },
    getNoBindNode(id) {
      const upstreamIds = this.json.edges.filter(item => item.target === id).map(item => item.source);
      const downstreamIds = this.json.edges.filter(item => item.source === id).map(item => item.target);
      const ids = [ id, ...upstreamIds, ...downstreamIds ];
      const results = this.json.nodes.filter(item => !ids.includes(item.id || item.key));
      return results;
    },
    addDatachecker(node, data){
      if (data && data.length) {
        // 绗竴琛屽簱琛ㄦ斁鍒癱heck.object锛屽叾浣欒鏀惧埌job.desc
        let checkObject = `${data[0].db}.${data[0].table}`;
        if(data[0].partition) {
          checkObject += `{${data[0].partition}}`;
        }
        const jobDesc = data.slice(1).map((item,idx) => {
          return item.partition ? `check.object.${idx+1}=${item.db}.${item.table}{${item.partition}}` : `check.object.${idx+1}=${item.db}.${item.table}`;
        }).join('\n')
        // 娣诲姞datacheck鑺傜偣鍙婅繛绾?        const checkerNode = {
          "type": "linkis.appconn.datachecker",
          "title": `datachecker_${Math.floor(Math.random()*10000)}`,
          "desc": "",
          "image": "/api/rest_j/v1/dss/workflow/nodeIcon/linkis.appconn.datachecker",
          "key": Date.now(),
          "layout": {
            "width": 150,
            "height": 40,
            "x": node.x - Math.random() * 50 - 150 > 100 ? node.x - Math.random() * 50 - 150 : 100,
            "y": node.y - Math.random() * 30 - 60 > 100 ? node.y - Math.random() * 30 - 60 : 100,
          },
          params:{ 
            configuration:  {
              special: {},
              runtime: {
                'check.object': checkObject,
                'job.desc': jobDesc
              },
              startup: {}
            }
          },
          "selected": true,
          "createTime": Date.now()
        }
        const edge = {
          linkType: "straight",
          target: node.key,
          source: checkerNode.key,
          sourceLocation: "bottom",
          targetLocation: "top"
        }
        this.json.edges.push(edge);
        this.json.nodes.push(checkerNode);
        this.originalData = { ...this.json };
        this.jsonChange = true;
      } else {
        this.$refs.datachecker.open(node);
      }
    },
    beforeAddEdges(node) {
      this.addEdgesShow = true;
      this.addEdgesForm.currentNode = node.id || node.key;
      this.addEdgesForm.currentNodeName = node.title;
      const list = this.getNoBindNode(node.id || node.key)
      list.forEach((item) => {
        item.key = item.id || item.key;
      });
      this.upstreamNodeList = list;
      this.downstreamNodeList = list;
    },
    async addEdges() {
      const { currentNode, upstreamNodes, downstreamNodes } = this.addEdgesForm;
      const newEdges = [ ...this.json.edges ];
      upstreamNodes.forEach(item => {
        newEdges.push({
          source: item,
          target: currentNode,
        })
      });
      downstreamNodes.forEach(item => {
        newEdges.push({
          source: currentNode,
          target: item,
        })
      });
      if(hasCycle(newEdges)) {
        this.$Message['warning']({
          content: '鍏宠仈鑺傜偣涓婁笅娓歌妭鐐瑰瓨鍦ㄩ棴鐜?,
          duration: 2,
        });
        return;
      }
      this.json.edges = [ ...newEdges ];
      this.autoSave('addEdges', false);
      this.originalData = { ...this.json };
      this.cancelEdges(false);
    },
    cancelEdges(val) {
      if (!val) {
        this.addEdgesShow = false;
        this.addEdgesForm = {
          currentNode: '',
          currentNodeName: '',
          upstreamNodes: [],
          downstreamNodes: []
        }
        this.upstreamNodeList = [];
        this.downstreamNodeList = [];
      }
    },
    changeNodes(type) {
      const { currentNode, upstreamNodes, downstreamNodes } = this.addEdgesForm;
      const list = this.getNoBindNode(currentNode);
      if (type === 'upstream') {
        this.downstreamNodeList = list.filter(item => !upstreamNodes.includes(item.key))
      } else {
        this.upstreamNodeList = list.filter(item => !downstreamNodes.includes(item.key))
      }
    },
    checkAssociated(node) {
      if (this.myReadonly) return this.$Message.warning(this.$t('message.workflow.process.readonlyNoAssociated'));
      if ([NODETYPE.SPARKSQL, NODETYPE.HQL, NODETYPE.SPARKPY, NODETYPE.SCALA, NODETYPE.PYTHON, NODETYPE.NEBULA].indexOf(node.type) === -1) {
        return this.$Notice.warning({
          desc: this.$t('message.workflow.process.noAssociated'),
        });
      } else if (node.jobContent && node.jobContent.script) {
        this.$Modal.confirm({
          title: this.$t('message.workflow.process.repeateAssociated'),
          content: this.$t('message.workflow.process.repeateAssociatedHtml'),
          okText: this.$t('message.workflow.process.confirmAssociated'),
          cancelText: this.$t('message.workflow.cancel'),
          onOk: () => {
            this.openAssociateScriptModal(node);
          },
          onCancel: () => {
            // this.$Modal.remove();
          },
        });
      } else {
        this.openAssociateScriptModal(node);
      }
    },
    openAssociateScriptModal(node) {
      this.$refs.associateScript.open(node);
    },
    associateScript(node, path, cb) {
      api.fetch('/filesystem/openFile', {
        path,
      }, 'get').then((rst) => {
        const supportModes = this.getSupportModes();
        const time = new Date();
        // 鐢变簬淇敼浜嗚妭鐐圭被鍨嬫墍浠ヤ箣鍓嶈幏鍙栨柟娉曚笉琛?        const type = ext[node.type];
        const match = supportModes.find((item) => item.flowType === type);
        const fileName = `${time.getTime()}${match.ext}`;
        const params = {
          fileName,
          scriptContent: rst.fileContent[0][0],
          metadata: rst.metadata,
          projectName: this.$route.query.projectName || ''
        };
        if (node.resources && node.resources[0]) {
          params.resourceId = node.resources[0].resourceId;
        }
        if (params.metadata && params.metadata.configuration) delete params.metadata.configuration.startup;
        api.fetch('/filesystem/saveScriptToBML', params, 'post')
          .then((res) => {
            this.$Message.success(this.$t('message.workflow.process.associaSuccess'));
            node.params = {...node.params, ...rst.params};
            const params = {
              fileName,
              resourceId: res.resourceId,
              version: res.version,
              projectName: this.$route.query.projectName || ''
            };
            this.$emit('check-opened', node, (isOpened) => {
              this.$emit('save-node', params, node, true);
              if (isOpened) {
                api.fetch('/filesystem/openScriptFromBML', params, 'get').then((res) => {
                  this.dispatch('Workbench:updateFlowsTab', node, {
                    content: res.scriptContent,
                    params: res.metadata,
                  });
                });
              } else {
                // 濡傛灉娌℃墦寮€鑺傜偣锛屾槸鏃犳硶璋冨彇Workbench鐨勬柟娉曠殑
                // 鎵€浠ワ紝鐩存帴璋冪敤IndexedDB娓呯┖缂撳瓨
                this.dispatch('IndexedDB:clearLog', node.key);
                this.dispatch('IndexedDB:clearResult', node.key);
                this.dispatch('IndexedDB:clearProgress', node.key);
              }
            });
            cb(true);
          }).catch(() => {
            cb(false);
            this.$Message.error(this.$t('message.workflow.process.associaError'));
          });
      }).catch(() => {
        cb(false);
      });
    },
    async addNode(node) {
      // 鍏抽棴鍙充晶寮圭獥
      this.nodebaseinfoShow = false;
      // 鏂版嫋鍏ョ殑鑺傜偣锛岃嚜鍔ㄧ敓鎴愭柊鐨勪笉閲嶅鍚嶇О,缁欏悕绉板悗闈㈠姞鍥涗綅闅忔満鏁?      node = this.bindNodeBasicInfo(node);
      const templateList = await this.getTemplateDataByProject(node.type);
      this.clickCurrentNode = JSON.parse(JSON.stringify(node));
      this.clickCurrentNode.title = this.clickCurrentNode.title + '_' + Math.round(Math.random()*10000);
      // 寮圭獥鎻愮ず鐢卞悗鍙版帶鍒?      if (node.shouldCreationBeforeNode) {
        this.addNodeShow = true;
        this.addNodeTitle = this.$t('message.workflow.process.createNode');
      } else {
        // 杩樺緱鍚屾鏇存柊json涓殑node
        this.json.nodes = this.json.nodes.map((subItem) => {
          if (subItem.key === this.clickCurrentNode.key) {
            subItem.title = this.clickCurrentNode.title;
            subItem.modifyUser = this.getUserName();
            subItem.modifyTime = Date.now();
            // 瀵逛簬鏂板鑺傜偣鏍规嵁榛樿鍊兼儏鍐靛仛璧嬪€?            if (this.tabs[0].data.isDefaultReference === '1') {
              templateList.forEach((v) => {
                if(v.workflowDefault) {
                  subItem.ecConfTemplateId = v.templateId;
                  subItem.ecConfTemplateName = v.templateName;
                  subItem.params = {
                    configuration: {
                      special: {},
                      runtime: {},
                      startup: {
                        'ec.conf.templateId': v.templateId,
                      }
                    }
                  }
                }
              });
            }
          }
          return subItem;
        });
        this.originalData = { ...this.json };
        this.autoSave(this.$t('message.workflow.AddNode'), false);
        return;
      }
    },
    validatorName(rule, value, callback) {
      if (value === `${this.name}_`) {
        callback(new Error(this.$t('message.workflow.process.nodeNameValid')));
      } else {
        callback();
      }
    },
    addFlowCancel() {
      this.addNodeShow = false;
      // 鍒犻櫎鏈垱寤烘垚鍔熺殑鑺傜偣
      this.json.nodes = this.json.nodes.filter((subItem) => {
        return this.clickCurrentNode.key != subItem.key;
      });
      this.originalData = this.json;
    },
    // 鍒涘缓鑺傜偣鏃?    addFlowOk() {
      this.$refs.addFlowfoForm.validate((valid) => {
        if (valid) {
          this.addFlowOkFunction()
        }
      });
    },
    // addFlowOk鍑芥暟閲屽彲浠ュ鐢ㄧ殑鎿嶄綔
    addFlowOkFunction() {
      if (this.myReadonly) return this.$Message.warning(this.$t('message.workflow.process.readonlyNoCeated'));
      this.saveNodeBaseInfo(this.clickCurrentNode);
    },
    relySelect(node, dir) {
      /**
       * 1.鑾峰彇褰撳墠鑺傜偣鐨刱ey
       * 2.鏌ユ壘浠ュ綋鍓峩ey涓簊ource鐨勮妭鐐?       * 3.閬嶅巻鏌ユ壘鍑烘潵鐨勮妭鐐规暟缁勶紝鎺ョ潃閫掑綊
       *  */
      let stepArray = [];
      const stepArrayAction = (nodeKey, level = 0) => {
        level++;
        this.json.edges.forEach((item) => {
          if ( dir === 'down' && item.source === nodeKey) {
            stepArray.push({...item, level});
            stepArrayAction(item.target, level);
          } else if(dir === 'up' && item.target === nodeKey) {
            stepArray.push({...item, level});
            stepArrayAction(item.source, level);
          } else if(dir === 'down-one' && item.source === nodeKey) {
            stepArray.push({...item, level});
          } else if(dir === 'up-one' && item.target === nodeKey) {
            stepArray.push({...item, level});
          }
        });
      };
      stepArrayAction(node.key);
      this.json.nodes = this.json.nodes.map((subItem) => {
        let runState
        const inDeps = stepArray.filter((item) => (item.target === subItem.key && dir.includes('down')) || (item.source === subItem.key && dir.includes('up')))
        if (subItem.key === node.key) subItem.selected = true;
        if (inDeps.length ) {
          subItem.selected = true;
          // runState = {
          //   outerText: Math.max(...inDeps.map(it => it.level)),
          //   outerStyle: {
          //     position: 'absolute',
          //     top: '12px',
          //     right: '-30px',
          //     width: '30px',
          //     textAlign: 'center',
          //     color: 'rgb(237, 64, 20)',
          //   }
          // }
        } else {
          subItem.selected = false;
        }
        return {...subItem, runState};
      });
      this.originalData = { ...this.json };
    },
    heartBeat: throttle(() => {
      api.fetch('/user/heartbeat', 'get');
    }, 60000),
    copyNode(node) {
      node = this.bindNodeBasicInfo(node);
      if (node.enableCopy === false) return this.$Message.warning(this.$t('message.workflow.process.noCopy'));
      if (this.myReadonly) return this.$Message.warning(this.$t('message.workflow.process.readonlyNoCopy'));
      this.cacheNode = JSON.parse(JSON.stringify(node));
    },
    pasteNode(e) {
      if (this.myReadonly) return this.$Message.warning(this.$t('message.workflow.process.noPaste'));
      if (!this.cacheNode) {
        return this.$Message.warning(this.$t('message.workflow.process.firstCopy'));
      }
      let tmpTitle = this.cacheNode.title+'_copy'
      const hasNodeTitle = this.json.nodes.filter(it => it.title.indexOf(tmpTitle) > -1)
      if (hasNodeTitle.length) {
        tmpTitle = tmpTitle + hasNodeTitle.length
      }
      if (tmpTitle.length > 150) {
        return this.$Message.warning(this.$t('message.workflow.process.namelength'));
      }
      // 鑾峰彇灞忓箷鐨勭缉鏀惧€?      let pageSize = 1
      if (this.$refs.process.getState) {
        pageSize = this.$refs.process.getState().baseOptions.pageSize;
      }
      const key = '' + new Date().getTime() + Math.ceil(Math.random() * 100);
      this.cacheNode.key = key;
      this.cacheNode.id = key;
      this.cacheNode.selected = true;
      this.cacheNode.title = tmpTitle
      this.cacheNode.createTime = Date.now()
      this.cacheNode.layout = {
        height: this.cacheNode.height,
        width: this.cacheNode.width,
        x: (e.offsetX / pageSize),
        y: (e.offsetY / pageSize),
      };
      // 澶栭儴鑺傜偣浠ラ檺鍒跺鍒讹紝濡傛灉瑕佸鍒惰鍒犻櫎id
      if (this.cacheNode.shouldCreationBeforeNode) {
        delete this.cacheNode.jobContent;
      }
      // 鍒犳帀鑺傜偣鐨勬墽琛屼俊蹇?      if(this.cacheNode.runState) {
        delete this.cacheNode.runState;
      }
      delete this.cacheNode.enableCopy;
      this.json.nodes = this.json.nodes.map((subItem) => {
        subItem.selected = false;
        return subItem;
      });
      this.json.nodes.push(JSON.parse(JSON.stringify(this.cacheNode)));
      this.originalData = { ...this.json };
      this.autoSave(this.$t('message.workflow.Saving'), false);
      this.click(this.cacheNode)
    },
    // 鐢变簬鎻掍欢鐨剆elected涓嶆槸鍝嶅簲寮忥紝鎵€浠ュ緱鎵嬪姩鏀瑰彉
    nodeSelectedFalse(node = {}) {
      this.json.nodes = this.json.nodes.map((subItem) => {
        if (node.key && node.key === subItem.key) {
          subItem.selected = true;
        } else {
          subItem.selected = false;
        }
        return subItem;
      });
      this.originalData = { ...this.json };
    },
    clickBaseInfo() {
      this.nodeSelectedFalse(this.clickCurrentNode);
    },    // 鎵归噺鍒犻櫎閫変腑鑺傜偣
    async allDelete() {
      if (this.myReadonly) return this.$Message.warning(this.$t('message.workflow.process.noDelete'));
      let selectNodes = this.$refs.process.getSelectedNodes();
      const selectNodeLength = selectNodes.length
      if (selectNodeLength < 1) return
      // 鎵归噺鍒犻櫎璺宠繃瀛愬伐浣滄祦鑺傜偣
      selectNodes = selectNodes.filter(node => node.type !== NODETYPE.FLOW)
      const selectNodeKeys = selectNodes.map((item) => item.key);
      selectNodes = selectNodes.map((item) => this.bindNodeBasicInfo(item));
      const nodes = [];
      selectNodes.forEach(node=>{
        if (node.supportJump && node.shouldCreationBeforeNode && node.jobContent) {
          nodes.push({
            nodeType: node.type,
            name: node.title,
            description: node.desc,
            projectID: +this.$route.query.projectID,
            params: node.jobContent,
            labels: {
              route: this.getCurrentDsslabels()
            }
          })
        }
      })
      if (nodes.length) {
        await api.fetch(`${this.$API_PATH.WORKFLOW_PATH}batchDeleteAppConnNode`, {nodes}, 'post')
      }
      let msg = this.$t('message.workflow.deleteSuccess')
      if (selectNodeLength > selectNodes.length) {
        msg = this.$t('message.workflow.BatchDel');
        this.$Message.warning(msg);
      } else {
        this.$Message.success(msg);
      }
      selectNodes.forEach(node=>{
        this.$emit('deleteNode', node);
      })
      this.json.nodes = this.json.nodes.filter((item) => {
        if (selectNodeKeys.includes(item.key)) {
          this.json.edges = this.json.edges.filter((link) => {
            return !(link.source === item.key || link.target === item.key);
          });
        } else {
          item.selected = false;
          return true;
        }
      });
      this.originalData = { ...this.json };
      this.autoSave('allDelete', false);
    },
    /**
     * 鍙抽敭鑿滃崟鐐瑰嚮鎵撳紑绠＄悊鍙?     */
    async openConsole(node) {
      if (this.$refs.bottomTab) this.$refs.bottomTab.closePanel()
      this.openningNode = null;
      this.nodeSelectedFalse(node); // 鏀瑰彉鑺傜偣鐨勯€夋嫨鐘舵€?      // 灏嗘暟鎹粨鏋勯€傞厤鍏ㄥ眬console缁勪欢
      node.runType = 'node'; // 鏂板杩愯绫诲瀷瀛楁
      node.taskID = node.runState.taskID; // 鏂板浠诲姟id
      node.execID = node.runState.execID; // 鏂板鎵цid
      this.consoleHeight = this.$el ? this.$el.clientHeight / 2 : 250
      this.shapeWidth = this.$refs.process && this.$refs.process.state.shapeOptions.viewWidth; // 鑷€傚簲鎺у埗鍙板搴?      this.$nextTick(() => {
        this.openningNode = node; // 浼犵粰鎺у埗鍙扮殑鍙傛暟
        setTimeout(() => {
          this.$refs.currentConsole.checkFromCache();
        }, 50)
      })
    },
    closeConsole() {
      this.openningNode = null;
    },
    toggleShape(shapeFold) {
      // 宸ヤ綔娴乮con鏄惁鏀惰捣
      if (shapeFold) {
        this.shapeWidth = 0;
      } else {
        this.shapeWidth = this.$refs.process && this.$refs.process.state.shapeOptions.viewWidth;
      }
    },
    saveCommonIframe(node) {
      // 鍒涘缓
      if (node.supportJump && node.shouldCreationBeforeNode && !node.jobContent) {
        const newCreateParams = this.getCreatePrams(node);
        const createParams = {
          flowID: this.flowId,
          projectID: +this.$route.query.projectID,
          nodeType: node.type,
          nodeID: node.key,
          params: {
            ...node.jobContent,
            ...newCreateParams
          },
          labels: {
            route: this.getCurrentDsslabels()
          }
        }
        this.loading = true;
        return api.fetch(`${this.$API_PATH.WORKFLOW_PATH}createAppConnNode`, createParams).then((res) => {
          // 鐢变簬vsbi鐨勯敊璇俊鎭繑鍥炵殑杩欓噷锛屾墍浠ュ緱鍒ゆ柇鏄惁鎴愬姛缁欎簣鎻愮ず
          let commomData = {};
          try {
            commomData = JSON.parse(res.result);
          } catch {
            commomData = res.result;
          }
          if (commomData) {
            node.jobContent= commomData;
            this.$Message.success({
              content: this.$t('message.workflow.process.createdSuccess')
            });
          } else {
            this.$Message.error({
              content: commomData.msg,
              duration: 4,
            });
          }
          // 鍒涘缓鎴愬姛鍏抽棴鍙充晶鏍?          this.nodebaseinfoShow = false;
        }).catch(() => {
          this.json.nodes = this.json.nodes.filter((subItem) => {
            return node.key != subItem.key;
          });
          this.originalData = { ...this.json };
        })
      }
      // 鏇存柊
      if (node.jumpType == 1 && node.jobContent) {
        const params = {
          flowID: this.flowId,
          nodeType: node.type,
          projectID: +this.$route.query.projectID,
          params: {
            ...node.jobContent,
            title: node.title,
            desc: node.desc
          },
          labels: {
            route: this.getCurrentDsslabels()
          }
        }
        this.loading = true;
        return api.fetch(`${this.$API_PATH.WORKFLOW_PATH}updateAppConnNode`, params, 'post').then(() => {
          this.$Message.success(this.$t('message.workflow.updataSuccess'))
        }).catch(() => {})
      }
    },
    // 鑾峰彇闇€瑕佸湪鍒涘缓鐨勬椂鍊欏～鍐欑殑鍙傛暟
    getCreatePrams(node) {
      const createParams = {}
      node.nodeUiVOS.filter((item) => item.baseInfo)
        .map(item => item.key).map(item => {
          createParams[item] = node[item];
        })
      return createParams;
    },
    // 鏍规嵁鑺傜偣绫诲瀷灏嗗悗鍙拌妭鐐瑰熀纭€淇℃伅鍔犲叆
    bindNodeBasicInfo(node) {
      if (node.nodeUiVOS) delete node.nodeUiVOS
      this.shapes.forEach((item) => {
        if (item.children.length > 0) {
          item.children.forEach((subItem) => {
            if (subItem.type === node.type || subItem.type === node.jobType) {
              node = Object.assign({}, subItem, node);
            }
          })
        }
      })
      return node;
    },
    // 鐐瑰嚮鑺傛祦
    clickswitch(type){
      if ( type === 'select') {
        let selectNodes = this.$refs.process.getSelectedNodes();
        const selectNodeLength = selectNodes.length
        if (selectNodeLength < 1 ) {
          return this.$Message.error(this.$t('message.workflow.PleaseSelectNode'));
        }
        selectNodes.forEach((node) => {
          this.$refs.process.setNodeRunState(node.key, {
          })
        })
      } else if(!this.workflowIsExecutor) {
        let json = JSON.parse(JSON.stringify(this.json));
        json.nodes.forEach((node) => {
          this.$refs.process.setNodeRunState(node.key, {
          })
        })
      }
      debounce(() => {
        if (this.workflowIsExecutor) {
          this.workflowStop()
        } else {
          this.workflowRun(type)
        }
      }, 1000)()
    },
    // 澶辫触閲嶈窇
    reRun() {
      this.workflowRun('rerun')
    },
    async workflowRun(runFlag) {
      if (this.$refs.bottomTab) this.$refs.bottomTab.closePanel()
      this.retryTimes = 0
      let selectNodes = this.$refs.process.getSelectedNodes();
      this.dispatch('workflowIndexedDB:clearNodeCache');
      // 閲嶆柊鎵ц娓呮帀涓婃鐨勮鏃跺櫒
      clearTimeout(this.excuteTimer);
      clearTimeout(this.executorStatusTimer);
      this.needReRun = false;
      this.openningNode = null;
      // return this.$Message.warning('鎵ц閲嶆瀯涓紝鍗冲皢寮€婧?);
      /**
       * 1.鎵ц涔嬪墠鍏堜繚瀛橈紝鎵ц鏀逛负鍋滄
       * 2.绂佺敤鎿嶄綔锛氬乏渚ц彍鍗曪紝淇濆瓨锛屽弬鏁颁慨鏀癸紝宸ュ叿鏍忥紝鏇村叿鐘舵€佹潵鎿嶄綔鍙抽敭
       * 3.杞鎺ュ彛鑾峰彇鑺傜偣鐘舵€?      */
      // 濡傛灉鏄敓浜т腑蹇冪殑鍙妯″紡涓嶉渶瑕佷繚瀛?      let a = null;
      if (!this.myReadonly) {
        a = await this.autoSave(this.$t('message.workflow.Saving'), false);
        if (!a || !a.flowVersion) return;

      }
      // 淇濆瓨鎴愬姛鍚庡啀璋冩墽琛屾帴鍙?      const parmas = {
        executeApplicationName: "flowexecution",
        executionCode: JSON.stringify({
          flowId: this.flowId,
          version: !this.myReadonly ? a.flowVersion : this.flowVersion
        }),
        runType: "json",
        params: {},
        labels: {
          route: this.getCurrentDsslabels()
        },
        source: {
          projectName: this.$route.query.projectName,
          flowName: this.name
        },
        requestApplicationName: "flowexecution"
      }
      if (runFlag === 'rerun') {
        parmas.isReExecute = true
      }
      const exeUrl = '/dss/flow/entrance/execute'
      if ( runFlag === 'select') {
        parmas.nodeID = selectNodes.map(item=>item.key).join(',')
        parmas.isSelectedExecute = true
      }
      api.fetch(exeUrl, parmas).then((res) => {
        // 姣忔鎵ц涔嬪悗缂撳瓨宸ヤ綔娴侊紝鍏抽棴閲嶆柊鎵撳紑鍐嶆帴鐫€鑾峰彇鐘舵€?
        this.workflowExecutorCache.push({
          flowId: this.flowId,
          execID: res.execID,
          taskID: res.taskID
        })
        // 鏌ヨ鎵ц鑺傜偣鐨勭姸鎬?        let execID = res.execID;
        let taskID = res.taskID;
        this.workflowTaskId = res.taskID;
        this.workflowExeteId = execID;
        this.queryWorkflowExecutor(execID, taskID)
      }).catch(() => {
        this.workflowIsExecutor = false;
      })
      this.workflowIsExecutor = true;
    },
    workflowStop() {
      this.retryTimes = 0
      clearTimeout(this.excuteTimer);
      clearTimeout(this.executorStatusTimer);
      // 娓呮帀褰撳墠宸ヤ綔娴佹墽琛岀殑缂撳瓨
      this.workflowExecutorCache = this.workflowExecutorCache.filter((item) => {
        item.flowId !== this.flowId;
      });
      if (this.task_killing) {
        return this.$Message.error('璇锋眰宸插彂鍑猴紝璇峰嬁閲嶅鐐瑰嚮');
      }
      this.task_killing = true
      api.fetch(`/dss/flow/entrance/${this.workflowExeteId}/kill`, {taskID: this.workflowTaskId, labels: this.getCurrentDsslabels()}, 'get').then(() => {
        this.workflowIsExecutor = false;
        this.flowExecutorNode(this.workflowExeteId, true);
        this.task_killing = false;
      }).catch(() => {
        this.workflowIsExecutor = false;
        this.task_killing = false;
      })
    },
    queryWorkflowExecutor(execID, taskID) {
      this.retryTimes = this.retryTimes || 0;
      api.fetch(`/dss/flow/entrance/${execID}/status`,
        {
          taskID,
          labels: this.getCurrentDsslabels()
        }, 'get').then((res) => {
        this.flowExecutorNode(execID);
        // 鏍规嵁鎵ц鐘舵€佸垽鏂槸鍚﹁疆璇?        const status = res.status;
        if (status === 3) { // 鍋滄鐘舵€佽疆璇?          if (res.message) {
            this.$Message.error(res.message);
          }
          this.workflowIsExecutor = false;
          return
        }
        if (['Inited', 'Scheduled', 'Running'].includes(status)) {
          if (this.excuteTimer) {
            clearTimeout(this.excuteTimer);
            this.excuteTimer = null;
          }
          this.excuteTimer = setTimeout(() => {
            this.queryWorkflowExecutor(execID, taskID);
          }, 1000)
        } else {
          // Succees, Failed, Cancelled, Timeout
          this.workflowIsExecutor = false;
          // 宸ヤ綔娴佹墽琛岀姸鎬佸拰鑺傜偣鎵ц鐘舵€佽疆璇笉鍚屾锛屽伐浣滄祦鎵ц鎴愬姛鍚庯紝鑻ヨ妭鐐规墽琛岀姸鎬佸皻鏈垚鍔燂紝鍐嶆鏌ヨ鏇存柊杩涘害 dpms 312293
          if (this.openningNode) {
            setTimeout(()=> {
              this.$refs.currentConsole.queryState(false);
            }, 1000)
          }
          if (status === 'Succeed') {
            this.$Notice.success({desc: this.$t('message.common.projectDetail.workflowRunSuccess')})
          }
          if (status === 'Failed') {
            this.$Notice.error({desc: this.$t('message.common.projectDetail.workflowRunFail')})
            this.flowExecutorNode(execID, true);
            this.$refs.bottomTab.showPanel('execHistory', res.logPath);
          }
          if (status === 'Cancelled') {
            this.$Notice.error({desc: this.$t('message.common.projectDetail.workflowRunCanceled')})
            this.flowExecutorNode(execID, true);
          }
          if (status === 'Timeout') {
            this.$Notice.error({desc: this.$t('message.common.projectDetail.workflowRunOvertime')})
            this.flowExecutorNode(execID, true);
          }
          // 娓呮帀褰撳墠宸ヤ綔娴佹墽琛岀殑缂撳瓨
          this.workflowExecutorCache = this.workflowExecutorCache.filter((item) => {
            return item.flowId !== this.flowId;
          });
        }
      }).catch(() => {
        // 澶辫触閲嶈瘯5娆?        if (this.retryTimes < 5) {
          clearTimeout(this.excuteTimer);
          this.excuteTimer = null;
          this.excuteTimer = setTimeout(() => {
            this.retryTimes = this.retryTimes + 1;
            this.queryWorkflowExecutor(execID, taskID);
          }, 1000)
        } else {
          this.retryTimes = 0
          this.flowExecutorNode(execID, true);
        }
        this.$Notice.error({desc: this.$t('message.common.projectDetail.workflowRunFail')})
      })
    },
    flowExecutorNode(execID, end = false) {
      api.fetch(`/dss/flow/entrance/${execID}/execution`, {labels: this.getCurrentDsslabels()}, 'get').then((res) => {
        // 銆?锛氭湭鎵ц锛?锛氳繍琛屼腑锛?锛氬凡鎴愬姛锛?锛氬凡澶辫触锛?锛氬凡璺宠繃銆?        const actionStatus = {
          pendingJobs: {color: '#6A85A7', status: 0, iconType: '',
            colorClass: '', isShowTime: false, title: this.$t('message.workflow.Scheduled'), showConsole: false},
          runningJobs: {color: '#2E92F7', status: 1, iconType: 'status-loading',
            colorClass: {'executor-loading': true}, isShowTime: true, title: this.$t('message.workflow.Running'), showConsole: true},
          succeedJobs: {color: '#52C41A', status: 2,iconType: 'status-success',
            colorClass: {'executor-success': true}, isShowTime: false, title: this.$t('message.workflow.ExecuteSuccess'), showConsole: true},
          failedJobs: {color: '#FF4D4F', status: 3, iconType: 'status-fail',
            colorClass: {'executor-faile': true}, isShowTime: false, title: this.$t('message.workflow.ExecuteFailed'), showConsole: true},
          skippedJobs: {color: '#B3C1D3', status: 4, iconType: 'status-skip',
            colorClass: {'executor-skip': true}, isShowTime: false, title: this.$t('message.workflow.Skip'), showConsole: false}
        };
        // 鑾峰彇鑺傜偣鐨勭姸鎬侊紝濡傛灉娌℃湁鎵ц瀹屾垚缁х画鏌ヨ
        const  data = res;
        Object.keys(data).forEach((key) => {
          // 濡傛灉褰撳墠宸ヤ綔娴佸凡缁忔墽琛岀粨鏉燂紝杩樺緱鑾峰彇鐘舵€佸埌娌℃湁鎵ц鐨勮妭鐐逛负姝?          if(end && key === 'runningJobs' && data[key].length > 0) {
            // 鎵嬪姩鍋滄帀鎵ц鍜屽垏鎹㈤〉闈㈠仠姝㈣皟鎺ュ彛
            this.executorStatusTimer = setTimeout(() => {
              this.flowExecutorNode(execID, true);
            }, 2000)
          }
          data[key].forEach((node) => {
            if (this.$refs.process) {
              let time = 0
              if (node.startTime) {
                time = node.nowTime - node.startTime
              }
              this.$refs.process.setNodeRunState(node.nodeID, {
                time: this.timeTransition(time),
                status: actionStatus[key].status,
                borderColor: actionStatus[key].color,
                iconType: actionStatus[key].iconType,
                colorClass: actionStatus[key].colorClass,
                isShowTime: actionStatus[key].isShowTime,
                title: actionStatus[key].title,
                showConsole: actionStatus[key].showConsole,
                execID: node.execID,
                taskID: node.taskID,
                isSvg: true
              }, this.myReadonly && !this.product)
            }
          })
        })
        if (end && data.failedJobs && data.failedJobs.length) {
          this.needReRun = true
        }
      })
    },
    timeTransition(time) {
      time =Math.floor(time / 1000)
      let hour = 0;
      let minute = 0;
      let second = 0;
      // let str ="00:00:00";
      // if (time < 0) return str;
      if (time > 60) {
        minute = Math.floor(time / 60);
        second = Math.floor(time % 60);
        if (minute >= 60) {
          hour = Math.floor(minute / 60);
          minute = Math.floor(minute % 60);
        } else {
          hour = 0;
        }
      } else {
        hour = 0;
        if (time == 60) {
          minute = 1;
          second = 0;
        } else {
          minute = 0;
          second = time;
        }
      }
      const addZero = (num) => {
        let result = num;
        if (num < 10) {
          result = `0${num}`
        }
        return result;
      }
      const timeResult = `${addZero(hour)}:${addZero(minute)}:${addZero(second)}`;
      time=0;
      return timeResult;
    },
    workflowPublishIsShow() {
      // 宸茬粡鍦ㄥ彂甯冧笉鑳藉啀鐐瑰嚮
      if(this.isFlowPubulish) return this.$Message.warning(this.$t('message.workflow.publishing'))
      this.pubulishShow = true;
      this.saveingComment = false;
      this.pubulishFlowComment = ''
      this.publishFlowData = [];
      // 鏈叧鑱擥it鐨勪笉鐢ㄦ煡璇?      if (this.associateGit) {
        api.fetch('/dss/framework/orchestrator/publish/history',
          {
            projectName: this.$route.query.projectName,
            orchestratorId: this.orchestratorId,
            workspaceId: this.$route.query.workspaceId
          }, 
          'get').then((rst) => {
              this.publishFlowData = rst.history.responses || [];
          });
        }
    },
    showDiff() {
      this.pubulishShow = false;
      this.$refs.bottomTab.showPanel('version');
    },
    async handleWorkflowPublish() {
      const params = {
        orchestratorId: this.orchestratorId,
        projectId: this.$route.query.projectID,
      }
      const rst = await api.fetch('/dss/framework/orchestrator/publishFlowCheck', params, 'get');
      if(rst && rst.data && rst.data.notContainsKeywordsNodeList && rst.data.notContainsKeywordsNodeList.length>0){
        const content = `<p class="ellipse-p">宸ヤ綔娴?{rst.data.orchestratorName}涓妭鐐癸細${rst.data.notContainsKeywordsNodeList.join(',')}</p><p>涓嶅寘鍚叧閿瓧insert鎴朿reate table</p>`;
        this.$Modal.confirm({
            title: '鑺傜偣鍏抽敭瀛楁鏌?,
            content: content,
            okText: '缁х画鍙戝竷',
            cancelText:'杩斿洖淇敼',
            onOk: () => {
              this.workflowPublish();
            },
        });
      }else {
        this.workflowPublish()
      }
    },
    async workflowPublish() {
      // 鍙湁鏈帴鍏it鐨勯」鐩彂甯冨墠闇€姹備繚瀛?      if (!this.associateGit) {
        if (this.saveingComment) {
            return
        }
        this.saveingComment = true
        // 鍙戝竷涔嬪墠鍏堜繚瀛?        let a
        try {
          a = await this.autoSave(this.$t('message.workflow.Publishwork'), false);
        } catch (e) {
          this.pubulishShow = false;
          this.isFlowPubulish = false;
        }
        if (!a) {
          this.pubulishShow = false;
          this.isFlowPubulish = false;
          this.saveingComment = false;
          return;
        }  
      }
      if (this.flowProxyUser) {
        let isPassed = true;
        try {
          const rst = await api.fetch('/dss/framework/workspace/isDismissed', {usernames: [this.flowProxyUser]}, 'post');
          if (rst && (rst.isDismissed || []).some(item => Object.values(item)[0])) {
            this.$Message.warning(`${this.name}宸ヤ綔娴佺殑浠ｇ悊鐢ㄦ埛宸茬鑱屾垨涓嶅瓨鍦紝璇风‘璁ゆ槸鍚︿慨鏀逛唬鐞嗙敤鎴穈);
            isPassed = false;
          }
        } catch (e) {
          isPassed = false;
        }
        if (!isPassed) {
          this.saveingComment = false;
          return;
        }
      }
      // 璋冪敤鍙戝竷鎺ュ彛
      const params = {
        orchestratorId: this.orchestratorId,
        orchestratorVersionId: this.orchestratorVersionId,
        dssLabel: this.getCurrentDsslabels(),
        workflowId: Number(this.flowId),
        labels: {route: this.getCurrentDsslabels()},
        comment: this.pubulishFlowComment
      }
      // 璁板綍宸ヤ綔娴佹槸鍚﹀湪鍙戝竷
      this.isFlowPubulish = true;
      api.fetch(`/dss/workflow/publishWorkflow`, params, 'post').then((res) => {
        this.pubulishShow = false;
        // 鍙戝竷涔嬪悗闇€瑕佽疆璇㈢粨鏋?        let queryTime = 0;
        this.saveingComment = false;
        this.checkResult(res.releaseTaskId, queryTime, 'publish');
        this.setTaskId(res.releaseTaskId);
      }).catch(() => {
        this.pubulishShow = false;
        this.isFlowPubulish = false;
        this.saveingComment = false;
        this.$Message.error(this.$t('message.common.projectDetail.publishFailed'));
      })
    },
    // 鍙戝竷鍜屽鍑哄叡鐢ㄦ煡璇㈡帴鍙?    checkResult(id, timeoutValue, type = 'publish') {
      let typeName = this.$t('message.workflow.export')
      if (type === 'publish') {
        typeName = this.$t('message.workflow.process.publish')
      }
      this.timer = setTimeout(() => {
        timeoutValue += 2000;
        getPublishStatus(+id, this.getCurrentDsslabels()).then((res) => {
          if (timeoutValue <= (10 * 60 * 1000)) {
            if (res.status === 'init' || res.status === 'running') {
              clearTimeout(this.timer);
              this.checkResult(id, timeoutValue, type);
            } else if (res.status === 'success') {
              clearTimeout(this.timer);
              this.isFlowPubulish = false;
              this.$emit('updateFlowStatus');
              // 濡傛灉鏄鍑烘垚鍔熼渶瑕佷笅杞芥枃浠?              if (type === 'export' && res.msg) {
                const url = module.data.API_PATH + 'dss/downloadFile/' + res.msg;
                const link = document.createElement('a');
                link.setAttribute('href', url);
                link.setAttribute('download', '');
                const evObj = document.createEvent('MouseEvents');
                evObj.initMouseEvent('click', true, true, window, 0, 0, 0, 0, 0, false, false, true, false, 0, null);
                const flag = link.dispatchEvent(evObj);
                this.$nextTick(() => {
                  if (flag) {
                    this.$Message.success(this.$t('message.workflow.downloadTolocal'));
                  }
                });
              }
              this.$Message.success(this.$t('message.workflow.workflowSuccess', { name: typeName }));
              // 鍙戝竷鎴愬姛鍚庯紝鏍瑰伐浣滄祦id浼氬彉鍖栵紝瀵艰嚧淇敼宸ヤ綔娴佸悗淇濆瓨鐨勮繕鏄棫id
              this.refreshOpen()
            } else if (res.status === 'failed') {
              clearTimeout(this.timer);
              this.isFlowPubulish = false;
              this.$Modal.error({
                title: this.$t('message.workflow.workflowFail', { name: typeName }),
                content: `<p style="word-break: break-all;">${res.errorMsg}</p>`,
                width: 500,
                okText: this.$root.$t('message.workflow.publish.cancel'),
              });
              // 鍙戝竷鎴愬姛鍚庯紝鏍瑰伐浣滄祦id浼氬彉鍖栵紝瀵艰嚧淇敼宸ヤ綔娴佸悗淇濆瓨鐨勮繕鏄棫id
              this.refreshOpen()
            }
          } else {
            clearTimeout(this.timer);
            this.isFlowPubulish = false;
            this.$Message.warning(this.$t('message.common.projectDetail.workflowRunOvertime'));
          }
          // 鎵╁睍鎻掍欢鍙戝竷鍘嗗彶鍒楄〃鏇存柊
          eventbus.emit('get_publish_status', res)
        }).catch(()=> {
          this.isFlowPubulish = false;
          this.refreshOpen()
        });
      }, 2000);
    },
    // 鎻愪氦
    submitGit() {
      if (this.isFlowSubmit) return
      const params = {
        orchestratorId: this.orchestratorId,
        flowId: Number(this.flowId),
        labels: {route: this.getCurrentDsslabels()},
        projectName: this.$route.query.projectName,	
        comment: this.submitDesc,
      };
      this.showSubmit = false;
      this.isFlowSubmit = true;
      this.setTaskId(this.orchestratorId, 'submit');
      api.fetch('/dss/framework/orchestrator/submitFlow', params, 'post').then(res => {
        this.submitDesc = '';
        this.checkSubmitStatus('submit');
      }).catch(() => {
        this.isFlowSubmit = false;
        this.removeTaskId('submit');
      });
    },
    // 妫€鏌ユ彁浜ょ姸鎬?    checkSubmitStatus(flag) {
      const typeName = this.$t('message.workflow.process.submitgit');
      const publishTaskId = this.getTaskId('submit')
      if (publishTaskId && this.orchestratorId == publishTaskId) {
        api.fetch('/dss/framework/orchestrator/submitFlow/status', {orchestratorId: this.orchestratorId,}, 'get').then(res => {
          if (res.status == 'running') {
            this.isFlowSubmit = true;
            setTimeout(() => {
              this.checkSubmitStatus(flag);
            }, 2000)
          } else if (res.status == 'success') {
            this.removeTaskId('submit');
            this.isFlowSubmit = false;
            this.$emit('updateWorkflowList');
            this.$emit('updateFlowStatus');
            if (flag === 'submit') {
              this.$Message.success(this.$t('message.workflow.workflowSuccess', { name: typeName }));
            }
          } else if(res.status == 'failed') {
            this.removeTaskId('submit');
            this.isFlowSubmit = false;
            if (flag === 'submit') {
              this.$Message.warning(this.$t('message.workflow.workflowFail', { name: typeName }));
            }
          }
        }).catch(() => {
          this.isFlowSubmit = false;
        });
      }
    },
    refreshOpen() {
      this.$emit('close')
      this.$emit('open')
    },
    dateFormatter(date) {
      return moment(date).format('YYYY-MM-DD HH:mm:ss');
    },
    exportWorkflow() {
      /*
      1.瀵煎嚭鏃讹紝娣诲姞鎻忚堪锛屽拰閫夋嫨鏄惁鍚屾鍙戠増
      2.鍦ㄥ鍑轰箣鍓嶅緱鍏堜繚瀛樺伐浣滄祦
      3.鍏堣皟鐢ㄥ鍑烘帴鍙ｏ紝鎴愬姛鍚庡啀涓嬭浇鍒版湰鍦?      */
      if(this.isFlowPubulish) return this.$Message.warning(this.$t('message.workflow.warning.api'))
      this.workflowExportShow = true;

    },
    async workflowExportOk() {
      const a = await this.autoSave(this.$t('message.workflow.Export'), false);
      if (!a) return
      this.isFlowPubulish = true;
      const params = {
        rootFlowID: Number(this.flowId),
        // projectVersionID: +this.projectVersionID,
        IOType: 'FLOW',
        comment: this.exportDesc,
        needChangeVersion: this.exportChangeVersion
      }
      api.fetch("export", params, 'post').then(() => {
        // let queryTime = 0;
        // this.checkResult(+this.projectVersionID, queryTime, 'export');
      })
    },
    // 鍒犻櫎宸ヤ綔娴佺殑绾胯Е鍙戣嚜鍔ㄤ繚瀛?    linkDelete() {
      const timerId = setTimeout(() => {
        this.autoSave('deleteLink', true);
        clearTimeout(timerId);
      }, 500);
      if (this.viewMode === 'cyeditor') {
        this.originalData = this.json;
      }
    },
    linkAdd() {
      const timerId = setTimeout(() => {
        this.autoSave('addLink', true);
        clearTimeout(timerId);
      }, 500);
      if (this.viewMode === 'cyeditor') {
        this.originalData = this.json;
      }
    },
    closeParamsBar() {
      // 鍏抽棴鍙傛暟鍙傛暟绐楀彛
      this.nodebaseinfoShow = false;
    },
    // 鑾峰彇鎺у埗鍙拌缃殑鍙傛暟淇℃伅
    getConsoleParams() {
      Promise.all([api.fetch('/configuration/getFullTreesByAppName', {
        engineType: 'spark',
        creator: 'nodeexecution',
      }, 'get'),
      api.fetch('/configuration/getFullTreesByAppName', {
        engineType: '閫氱敤璁剧疆',
        creator: '閫氱敤璁剧疆',
      }, 'get'),
      api.fetch('/configuration/getFullTreesByAppName', {
        engineType: 'hive',
        creator: 'nodeexecution',
      }, 'get')]).then((res) => {
        this.consoleParams = res;
      }).catch(() => {

      })
    },
    // 鑾峰彇宸ヤ綔绌洪棿鍚嶇О
    getCurrentWorkspaceName() {
      const workspaceData = storage.get("currentWorkspace");
      return workspaceData ? workspaceData.name : ''
    },
    getTaskKey(type = 'taskId'){
      const username = this.getUserName();
      const key = `${username}-workflow-${this.orchestratorId}-${type}`;
      return key
    },
    setTaskId(taskId, type) {
      const key = this.getTaskKey(type);
      storage.set(key, taskId);
    },
    getTaskId(type){
      const key = this.getTaskKey(type);
      return storage.get(key);
    },
    removeTaskId(type) {
      const key = this.getTaskKey(type);
      storage.remove(key);
    },
    onKeyUp(e) {
      const isDel = e.keyCode === 46 || e.keyCode === 8 && navigator.userAgent.indexOf('Mac') !== -1
      if (e.altKey && e.ctrlKey) { return }
      if (isDel && this.tabs[this.$parent.active].key === this.activeTabKey && e.target.nodeName!='INPUT' && e.target.nodeName!='TEXTAREA') {
        if (this.myReadonly) return
        let selectNodes = this.$refs.process.getSelectedNodes();
        const selectNodeLength = selectNodes.length
        if (selectNodeLength > 0) {
          if (selectNodeLength > 1) {
            this.allDelete();
          } else {
            this.nodeDelete(selectNodes[0])
          }
        }
      }
    },
    async checkLastPublish(cb) {
      const publishTaskId = this.getTaskId()
      if (publishTaskId && this.orchestratorId == this.$route.query.flowId) {
        let res
        try {
          res = await getPublishStatus(publishTaskId, this.getCurrentDsslabels())
        } catch (error) {
          this.removeTaskId()
        }
        if (res && res.status === 'running') {
          this.isFlowPubulish = true
          this.checkResult(publishTaskId, 0, 'publish')
          // 鎵撳紑鍙戝竷鍘嗗彶panel
          if (cb) {
            cb()
          }
        }
      }
    },
    showSearchPath() {
      this.showNodePathPanel = true
    },
    changeViewMode(mode, isSave = false) {
      if (this.viewMode === mode) return
      if (this.jsonChange) {
        return this.message({
          type: 'error',
          msg: '璇峰厛淇濆瓨'
        })
      }
      if (mode == 'table') {
        this.originalData = this.json;
        this.iframeloading = true
        this.openningNode = null
        if (this.viewMode !== 'table') {
          this.preDragViewMode = this.viewMode
        }
        this.viewMode = mode
        this.$nextTick(()=> {
          const ifr = this.$refs.ifr;
          if (ifr) {
            ifr.onload = () => {
              this.iframeloading = false
            }
          }
        })
      } else if(mode || this.preDragViewMode) {

        this.viewMode = mode || this.preDragViewMode
        if (this.viewMode !== 'table') {
          this.preDragViewMode = this.viewMode
        }
        // 鍒囨崲鑷冲師鎷栨嫿妯″紡锛岃妭鐐逛綅缃礋鍊煎鐞?        if (this.viewMode === 'vueprocess') {
          let x = 0
          let y = 0
          this.json.nodes.map(it => {
            if (it.layout.x < x) {
              x = it.layout.x
            }
            if (it.layout.y < y) {
              y = it.layout.y
            }
          })
          if (x < 0 || y < 0) {
            this.json.nodes.forEach(element => {
              element.layout.x = element.layout.x + x * -1
              element.layout.y = element.layout.y + y * -1
            });
          }
        }
        this.originalData = this.json;
        // 鍒囨崲妯″紡鍚庝繚瀛樻暟鎹紝纭繚妯″紡涔熻鏇存柊
		    if (isSave && !this.product) {
          this.autoSave(this.$t('message.workflow.Save'), false);
        }
      }
    },
    screenSizeChange(fullScreen) {
      this.isfullScreen = fullScreen
    },
    showSubmitGit() {
      if (this.jsonChange) {
        this.autoSave(this.$t('message.workflow.Save'), false);
      }
      this.showSubmit = true;
    },
    handleSwitchViewMode(action, arg) {
      this[action](arg, true)
    },
    handleClickToolbar(action, arg) {
      this[action](arg)
    }
  }
}
</script>
<style src="./index.scss" lang="scss">
.ellipse-p {
  word-break: break-word;
  overflow-wrap: break-word;
}
</style>

