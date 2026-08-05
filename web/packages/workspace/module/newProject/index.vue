<template>
  <div class="page-bgc">
    <h3 class="project-header">
      <span class="header-title">{{$t('message.Project.title')}}</span>
      <div class="header-tool">
        <Dropdown class="sort-icon" @on-click="sortTypeChange($event, 1)">
          {{ sortType[1] }}
          <SvgIcon class="icon" icon-class="down" />
          <DropdownMenu slot="list">
            <DropdownItem
              v-for="(item) in sortTypeList"
              :name="item.value"
              :key="item.value"
            >{{ item.lable}}</DropdownItem>
          </DropdownMenu>
        </Dropdown>
        <Select
          style="width:200px;margin-left:20px"
          v-model="viewState"
          @on-change="changeCatType"
        >
          <Option
            value="all"
            key="all"
          >{{ $t('message.workspace.All') }}</Option>
          <Option
            value="owner"
            key="owner"
          >{{ $t('message.workspace.Individual') }}</Option>
          <Option
            value="share"
            key="share"
          >{{ $t('message.workspace.Shared') }}</Option>
          <Option
            value="delete"
            key="delete"
          >{{ $t('message.workspace.Deleted') }}</Option>
        </Select>
        <Input
          search
          class="search-input"
          v-model="searchTxt"
          :placeholder="$t('message.common.projectDetail.SRMCSS')"
          @on-change="searchProject($event, 1)"
        />
      </div>
    </h3>
    <!-- 合作项目列表 -->
    <template v-if="dataList.length > 0">
      <project-content-item
        v-for="item in dataList"
        :viewState="viewState "
        :key="item.name"
        :hide-button-bar="false"
        :hide-publish-andcopy="false"
        :data-list="item.dwsProjectList"
        :current-data="item"
        :precent-list="precentList"
        :applicationAreaMap="applicationAreaMap"
        tag-prop="business"
        @goto="gotoWorkflow"
        @modify="projectModify"
        @copy="copyProject"
        @addProject="addProject"
        @redo="redoProject"
        @delete="deleteProject"
        @gotoGit="gotoGit"
      ></project-content-item>
    </template>
    <ProjectForm
      ref="projectForm"
      :action-type="actionType"
      :project-data="currentProjectData"
      :classify-list="cacheData"
      :workspace-users="workspaceUsers"
      :applicationAreaMap="applicationAreaMap"
      :orchestratorModeList="orchestratorModeList"
      :framework="true"
      @confirm="ProjectConfirm"
    ></ProjectForm>
    <Modal
      v-model="projectModelShow"
      :title="commonTitle"
      :width="'520'"
      @on-ok="commonComfirm"
      @on-cancel="commonCancel"
    >
      <component
        ref="publish"
        :is="currentForm"
        :current-project-data="currentProjectData"
        @modelHidden="modelHidden"
      ></component>
    </Modal>
    <Modal
      v-model="projectExportShow"
      :title="$t('message.workflow.exportWorkflow')"
      @on-ok="projectExportOk"
    >
      <Form :label-width="100" label-position="left" ref="exportForm">
        <FormItem :label="$t('message.workflow.desc')" porp="desc">
          <Input
            v-model="exportDesc"
            type="textarea"
            :placeholder="$t('message.workflow.inputWorkflowDesc')"
          ></Input>
        </FormItem>
        <FormItem porp="changeVersion">
          <Checkbox v-model="exportChangeVersion">{{ $t('message.workflow.synchronousPublishing') }}</Checkbox>
        </FormItem>
      </Form>
    </Modal>
    <Modal
      v-model="deleteProjectShow"
      :title="$t('message.common.projectDetail.deleteProject')"
      :mask-closable="false"
      @on-visible-change="onDeleteModalVisibleChange"
    >
      <!-- E1: 项目概况块（6 字段） -->
      <div class="delete-summary-block">
        <div class="block-title">项目概况</div>
        <div class="summary-row"><span class="summary-label">项目名称</span><span class="summary-value">{{ deleteProjectItem.name }}</span></div>
        <div class="summary-row"><span class="summary-label">创建人</span><span class="summary-value">{{ projectCreator }}</span></div>
        <div class="summary-row"><span class="summary-label">项目描述</span><span class="summary-value">{{ projectDescription }}</span></div>
        <div class="summary-row"><span class="summary-label">工作流/编排</span><span class="summary-value">{{ orchCountDisplay }}</span></div>
        <div class="summary-row"><span class="summary-label">项目成员</span><span class="summary-value">{{ memberCount }} 人</span></div>
        <div class="summary-row"><span class="summary-label">创建时间</span><span class="summary-value">{{ projectCreateTime }}</span></div>
      </div>
      <!-- E2: 风险提示 -->
      <Alert type="warning" show-icon class="delete-risk-alert">此操作需谨慎，确认后项目将被删除</Alert>
      <!-- E3: 输入确认区 -->
      <div class="delete-confirm-input">
        <div class="input-guide">请输入完整项目名称以确认删除操作</div>
        <Input v-model="deleteConfirmInput" :placeholder="deleteProjectItem.name" />
        <div v-if="!isDeleteConfirmed" class="input-hint">请输入完整项目名称「{{ deleteProjectItem.name }}」以确认删除</div>
      </div>
      <!-- 自定义 footer（替代默认 on-ok） -->
      <div slot="footer">
        <Button @click="cancelDeleteProject">取消</Button>
        <Button type="error" :disabled="!isDeleteConfirmed" @click="confirmDeleteProject">确认删除</Button>
      </div>
    </Modal>
    <Modal
      v-model="redoProjectShow"
      :title="$t('message.common.projectDetail.redoProject')"
      @on-ok="redoProjectConfirm"
    >
      <div style="word-wrap: break-word;">{{$t('message.common.projectDetail.confirmRedoProject')}}{{ redoProjectItem.name }}?</div>
    </Modal>
    <Spin v-if="loading" size="large" fix />
  </div>
</template>
<script>
import ProjectForm from '@dataspherestudio/shared/components/projectForm/index.js'
import copyForm from './module/copyForm.vue'
import storage from '@dataspherestudio/shared/common/helper/storage'
import api from '@dataspherestudio/shared/common/service/api'
import projectContentItem from './module/projectItem.vue'
import {
  GetWorkspaceUserList,
  GetDicSecondList,
  GetAreaMap
} from '@dataspherestudio/shared/common/service/apiCommonMethod.js';
import util from '@dataspherestudio/shared/common/util';
import { setVirtualRoles } from '@dataspherestudio/shared/common/config/permissions.js';
import eventbus from '@dataspherestudio/shared/common/helper/eventbus';
export default {
  components: {
    projectContentItem,
    ProjectForm,
    copyForm,
  },
  data() {
    return {
      redoProjectShow: false, // 恢复工程弹窗提示
      redoProjectItem: {},
      deleteProjectShow: false, // 删除工程弹窗展示
      deleteProjectItem: '', // 删除的工程项
      deleteConfirmInput: '', // 删除确认输入框（输入项目名以确认）
      orchCount: 0, // 编排数（null 表示加载失败，展示占位"—"）
      orchLoading: false, // 编排数加载中标志
      actionType: '', // add || modify
      loading: false,
      projectModelShow: false, // 发布,复制，版本的弹窗
      currentProjectData: {
        name: '',
        description: '',
        business: '',
        applicationArea: '',
        product: '',
        editUsers: [],
        accessUsers: [],
        releaseUsers: [],
        devProcessList: [],
        associateGit: false,
        gitUser: '',
        gitToken: ''
      },
      dataList: [
        {
          id: 1,
          name: this.$t('message.common.projectDetail.WCYDXM'),
          dwsProjectList: []
        }
      ],
      cacheData: [],
      currentForm: '',
      commonTitle: '',
      precentList: [],
      sortType: {},

      // 个人工作流工程版本
      projectVersionId: '',
      projectExportShow: false,
      exportDesc: '',
      exportChangeVersion: false,
      applicationAreaMap: [],
      orchestratorModeList: {},
      viewState: 'owner',
      searchTxt: '',
      workspaceUsers: {
        accessUsers: [],
        releaseUsers: [],
        editUsers: []
      }
    }
  },
  computed: {
    sortTypeList() {
      return [
        {
          lable: this.$t('message.common.projectDetail.sortUpdateTime'),
          value: 'updateTime'
        },
        {
          lable: this.$t('message.common.projectDetail.sortName'),
          value: 'name'
        }
      ]
    },
    tips() {
      return this.$t('message.common.projectDetail.tips', {app_name: this.$APP_CONF.app_name})
    },
    // 创建人：createByStr → createBy → "未知"（R1.3）
    projectCreator() {
      const p = this.deleteProjectItem
      if (!p) return '未知'
      return p.createByStr || p.createBy || '未知'
    },
    // 项目描述：空值→"暂无描述"（R1.4）
    projectDescription() {
      const desc = this.deleteProjectItem && this.deleteProjectItem.description
      return (desc && desc.trim()) ? desc : '暂无描述'
    },
    // 编排数展示：加载中/失败→"—"，否则数值（R1.6）
    orchCountDisplay() {
      if (this.orchLoading || this.orchCount === null) return '——'
      return this.orchCount + ' 个'
    },
    // 成员总数：三列去重（R1.7）
    memberCount() {
      const p = this.deleteProjectItem
      if (!p) return 0
      const access = p.accessUsers || []
      const edit = p.editUsers || []
      const release = p.releaseUsers || []
      return new Set([...access, ...edit, ...release]).size
    },
    // 创建时间格式化（R1.8）
    projectCreateTime() {
      const t = this.deleteProjectItem && this.deleteProjectItem.createTime
      if (!t) return '——'
      return this.formatDate(t)
    },
    // E3 输入校验：trim + 完全匹配 + 大小写敏感（R3.1-R3.4）
    isDeleteConfirmed() {
      if (!this.deleteProjectItem) return false
      return this.deleteConfirmInput.trim() === this.deleteProjectItem.name
    }
  },
  watch: {
    // 当切换工作空间之后，重新获取数据
    '$route.query.workspaceId'() {
      this.viewState = 'owner'
      this.searchTxt = ''
    }
  },
  created() {
  },
  mounted() {
    GetAreaMap().then(res => {
      this.applicationAreaMap = res.applicationAreas
    })
    this.$nextTick(() => {
      this.precentList = storage.get('precentList') || []
    })
    eventbus.on('workspace.change', this.initWorkspaceData);
  },
  methods: {
    initWorkspaceData() {
      GetWorkspaceUserList({ workspaceId: +this.$route.query.workspaceId }).then(
        (res) => {
          this.workspaceUsers = res.users;
        }
      );
      // 获取所有分类和工程
      this.getclassListData()
      // 获取编排的数据
      GetDicSecondList(this.$route.query.workspaceId).then((res) => {
        this.orchestratorModeList = res.list;
      });
    },
    changeCatType() {
      this.searchTxt = ''
      if (this.viewState === 'delete') {
        this.viewDeleted()
      } else {
        this.getclassListData()
      }
    },
    viewDeleted() {
      this.loading = true
      api.fetch(`${this.$API_PATH.PROJECT_PATH}getDeletedProjects`, { workspaceId: +this.$route.query.workspaceId }, 'post').then(res => {
        res.projects.map((item) => {
          setVirtualRoles(item, this.getUserName());
        });
        this.dataList[0].dwsProjectList = res.projects
        this.cacheData = this.dataList
        this.dataList.forEach(item => {
          this.sortType[item.id] = this.$t(
            'message.common.projectDetail.updteTime'
          )
        })
        this.sortTypeChange()
        this.loading = false
        this.viewState = 'delete'
        return this.cacheData
      })
        .catch(() => {
          this.loading = false
        })
    },
    getclassListData() {
      this.loading = true
      const curUser = this.getUserName()
      return api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}getAllProjects`,
          { workspaceId: +this.$route.query.workspaceId },
          "post"
        )
        .then(res => {
          res.projects.map((item) => {
            setVirtualRoles(item, this.getUserName());
          });
          res.projects.forEach(item => {
            if (this.viewState === 'owner') {
              item.showInCardList = item.createBy === curUser
            } else if(this.viewState === 'share') {
              item.showInCardList = item.createBy !== curUser
            } else {
              item.showInCardList =  true
            }
          })
          this.dataList[0].dwsProjectList = res.projects
          this.cacheData = this.dataList
          this.dataList.forEach(item => {
            this.sortType[item.id] = this.$t(
              'message.common.projectDetail.updteTime'
            )
          })
          this.sortTypeChange()
          this.loading = false
          storage.set('projectList', this.cacheData, 'local')
          return this.cacheData
        })
        .catch(() => {
          this.loading = false
        })
    },
    // 新增工程
    addProject() {
      this.actionType = 'add'
      this.init()
      this.$refs.projectForm.showProject(this.currentProjectData, 'add')
    },
    // 确认新增工程 || 确认修改
    ProjectConfirm(projectData, callback) {
      projectData.workspaceId = +this.$route.query.workspaceId
      const projectName = projectData.name
      if (
        this.checkName(
          this.cacheData[0].dwsProjectList,
          projectName,
          projectData.id
        )
      ) {
        typeof callback == "function" && callback();
        return this.$Message.warning(
          this.$t('message.common.projectDetail.nameUnrepeatable')
        )
      }
      this.loading = true
      if (this.actionType === 'add') {
        api
          .fetch(
            `${this.$API_PATH.PROJECT_PATH}createProject`,
            projectData,
            'post'
          )
          .then(() => {
            typeof callback == "function" && callback(true);
            this.$Message.success(
              `${this.$t(
                'message.common.projectDetail.createProject'
              )}${this.$t('message.workflow.success')}`
            )
            this.getclassListData().then(data => {
              // 新建完工程进到工作流页
              const currentProject = data[0].dwsProjectList.filter(
                project => project.name === projectName
              )[0]
              if (currentProject) {
                this.gotoWorkflow({}, currentProject)
              }
            })
          })
          .catch(() => {
            typeof callback == "function" && callback()
            this.loading = false
          })
      } else {
        const projectParams = {
          name: projectData.name,
          id: projectData.id,
          applicationArea: projectData.applicationArea,
          business: projectData.business,
          editUsers: projectData.editUsers,
          accessUsers: projectData.accessUsers,
          releaseUsers: projectData.releaseUsers,
          description: projectData.description,
          product: projectData.product,
          workspaceId: projectData.workspaceId,
          devProcessList: projectData.devProcessList,
          orchestratorModeList: projectData.orchestratorModeList,
          associateGit: projectData.associateGit,
          dataSourceList: projectData.dataSourceList,
          gitUser: projectData.gitUser,
          gitToken:  projectData.gitToken
        }
        api
          .fetch(
            `${this.$API_PATH.PROJECT_PATH}modifyProject`,
            projectParams,
            'post'
          )
          .then(() => {
            typeof callback == "function" && callback(true);
            this.$Message.success(
              this.$t('message.common.projectDetail.eidtorProjectSuccess', {
                name: projectParams.name
              })
            )
            this.getclassListData()
          })
          .catch(() => {
            typeof callback == "function" && callback()
            this.loading = false
            this.currentProjectData.business =
              this.$refs.projectForm.originBusiness
          });
      }
    },
    // 恢复单项功能
    redoProject(params) {
      this.redoProjectShow = true
      this.redoProjectItem = params
    },
    redoProjectConfirm() {
      this.loading = true;
      const params = {
        id: this.redoProjectItem.id,
      };
      api
        .fetch(`${this.$API_PATH.PROJECT_PATH}restoreProject`, params, 'post')
        .then(res => {
          this.loading = false;
          this.$Message.success(
              `${this.redoProjectItem.name}${this.$t(
                'message.common.projectDetail.redoSuccess'
              )}`
            )
          this.viewDeleted();
        })
        .catch(error => {
          console.error('restoreProject error:', error);
          this.loading = false;
        });
    },
    // 删除单项工程
    deleteProject(params) {
      this.deleteProjectItem = params
      this.deleteConfirmInput = '' // R3.7 每次打开清空，不残留上次输入
      this.orchCount = 0
      this.orchLoading = false
      this.deleteProjectShow = true
      this.fetchOrchCount() // 异步获取编排数
    },
    // 确认删除单项工程
    deleteProjectConfirm() {
      // 调用删除接口
      this.loading = true
      const params = {
        id: this.deleteProjectItem.id,
        sure: false,
      }
      api
        .fetch(`${this.$API_PATH.PROJECT_PATH}deleteProject`, params, 'post')
        .then(res => {
          this.loading = false
          if (res.warmMsg) {
            this.$Modal.confirm({
              title: this.$t('message.common.projectDetail.deleteTitle'),
              content: res.warmMsg,
              onOk: () => {
                params.sure = true
                this.loading = true
                api
                  .fetch(
                    `${this.$API_PATH.PROJECT_PATH}deleteProject`,
                    params,
                    'post'
                  )
                  .then(() => {
                    this.$Message.success(
                      `${this.$t(
                        'message.common.projectDetail.deleteProject'
                      )}${this.deleteProjectItem.name}${this.$t(
                        'message.workflow.success'
                      )}`
                    )
                    this.getclassListData()
                  })
                  .catch(() => {
                    this.loading = false
                  })
              },
              onCancel: () => {}
            })
          } else {
            this.$Message.success(
              this.$t('message.Project.deleteSuccess', {
                name: this.deleteProjectItem.name
              })
            )
            this.getclassListData()
          }
        })
        .catch(() => {
          this.loading = false
        })
    },
    // 新增：获取编排数量（复用 workflows 模块 getAllOrchestrator 模式）
    fetchOrchCount() {
      this.orchLoading = true
      api.fetch(`${this.$API_PATH.ORCHESTRATOR_PATH}getAllOrchestrator`, {
        workspaceId: +this.$route.query.workspaceId,
        projectId: this.deleteProjectItem.id,
      }, 'post').then(res => {
        // res.page 是 OrchestratorBaseInfo 数组
        this.orchCount = (res.page || []).length
      }).catch(() => {
        // 失败保持占位，不阻断删除（R1.6）
        this.orchCount = null // null 表示加载失败，展示占位"—"
      }).finally(() => {
        this.orchLoading = false
      })
    },
    // 新增：闸门一通过后的入口（替代原 @on-ok 直连）
    confirmDeleteProject() {
      // 双重保险：即使按钮 disabled 被绕过，也再校验一次（R3.8 仅前端校验）
      if (!this.isDeleteConfirmed) return
      // 关闭弹窗（与原 @on-ok 行为一致：点确认→关闭弹窗→执行删除）
      this.deleteProjectShow = false
      // 调用原删除逻辑（闸门二 warmMsg 在此内部，原样保留不动）
      this.deleteProjectConfirm()
    },
    // 新增：取消按钮
    cancelDeleteProject() {
      this.deleteProjectShow = false
    },
    // 新增：弹窗可见性变化（打开后聚焦输入框，可用性优化）
    onDeleteModalVisibleChange(visible) {
      if (visible) {
        this.$nextTick(() => {
          const input = this.$el.querySelector('.delete-confirm-input input')
          if (input) input.focus()
        })
      }
    },
    // 新增：格式化时间
    formatDate(t) {
      if (!t) return '——'
      const d = new Date(t)
      if (isNaN(d.getTime())) return '——'
      return d.toLocaleString()
    },
    init() {
      this.currentProjectData = {
        name: '',
        description: '',
        business: '',
        applicationArea: '',
        product: '',
        editUsers: [],
        accessUsers: [],
        releaseUsers: [],
        devProcessList: [],
        createBy: this.getUserName(),
        associateGit: false,
        gitUser: '',
        gitToken: ''
      }
    },
    // 修改工程
    projectModify(classifyId, project) {
      this.init()
      this.actionType = 'modify'
      this.currentProjectData = project
      this.$refs.projectForm.showProject(project, 'edit')
    },
    // 点击工程跳转到工作流
    gotoWorkflow(item, subItem) {
      if (this.viewState === 'delete') {
        this.$Message.warning('已删除无法查看')
        return
      }
      storage.set('currentProject', subItem)
      const query = {
        workspaceId: this.$route.query.workspaceId,
        projectID: subItem.id,
        projectName: subItem.name,
        notPublish: subItem.notPublish,
        viewState: this.viewState
      }
      const currentModules = util.currentModules();
      this.$router.push({
        name: currentModules.microModule == 'scheduleCenter' ? 'ScheduleCenter' : 'Workflow',
        query
      })
    },
    // 搜索对应的工程
    searchProject(event, id) {
      let tepArray = []
      if (this.viewState === 'delete') {
        tepArray = this.cacheData
      } else {
        tepArray = storage.get('projectList', 'local')
      }
      if (event.target.value) {
        // 工程的搜索
        let dataList = []
        tepArray.forEach(item => {
          let dwsProjectList = []
          if (id === item.id) {
            dwsProjectList = item.dwsProjectList.filter(subItem => {
              return subItem.name.indexOf(event.target.value) != -1
            })
          }
          dataList.push({
            ...item,
            dwsProjectList
          })
        })
        this.dataList = dataList
      } else {
        this.dataList = tepArray
      }
      // 存储到storeage序列化会丢失权限canWrite等
      this.dataList = this.dataList.map((item) => {
        if (!id || id === item.id) {
          item.dwsProjectList = item.dwsProjectList.map((item) => {
            setVirtualRoles(item, this.getUserName())
            return item
          })
        }
        return item;
      })
      this.sortTypeChange()
    },
    // 分类重名检查
    checkName(data, name, id) {
      let flag = false
      data.map(item => {
        if (item.name === name && item.id !== id) {
          flag = true
        }
      })
      return flag
    },
    // 复制工程
    copyProject(classifyId, project) {
      this.init()
      this.currentForm = 'copyForm'
      this.currentProjectData = { ...project };
      this.commonTitle = this.$t('message.common.projectDetail.projectCopy')
      this.projectModelShow = true
    },
    async gotoGit(data) {
      const workspaceData = storage.get("currentWorkspace");
      try {
        let res = await api.fetch(`${this.$API_PATH.ORCHESTRATOR_PATH}gitUrl`, {
          projectName: data.name,
          workspaceName: workspaceData.name,
        }, 'get');
        if (res && res.gitUrl) {
          window.open(res.gitUrl, '_blank');
        }
      } catch (error) {
        //
      }
    },
    projectExport(classifyId, project) {
      this.init()
      this.currentProjectData = project
      this.projectExportShow = true
    },
    commonComfirm() {
      if (this.currentForm === 'copyForm') {
        const copyCheckName = name => {
          let projectList = this.cacheData.filter(item => {
            return item.id === this.currentProjectData.taxonomyID
          })
          if (
            this.checkName(
              projectList[0].dwsProjectList,
              name,
              this.currentProjectData.id
            )
          )
            return this.$Message.warning(
              this.$t('message.common.projectDetail.nameUnrepeatable')
            )
        }
        this.$refs.publish.ProjectCopy(copyCheckName)
      }
    },
    commonCancel() {
      if (this.currentForm === 'copyForm') {
        this.$refs.publish.ProjectCopyReset()
      }
    },
    modelHidden(val) {
      this.getclassListData()
      if (!val) return ''
      this.projectModelShow = val
    },
    'Project:loading'(val) {
      this.loading = val
    },
    'Project:getData'() {
      this.getclassListData()
      this.precentList = storage.get('precentList') || []
    },
    // 将传入的数组根据当前系统语言，按照中文或英文名重新排序，会影响原数组
    arraySortByName(list) {
      if (list === undefined || list === null) return []
      list.sort((a, b) => {
        let strA = a.name
        let strB = b.name
        // 谁为非法值谁在前面
        if (
          strA === undefined ||
          strA === null ||
          strA === '' ||
          strA === ' ' ||
          strA === '　'
        ) {
          return -1
        }
        if (
          strB === undefined ||
          strB === null ||
          strB === '' ||
          strB === ' ' ||
          strB === '　'
        ) {
          return 1
        }
        const charAry = strA.split('')
        for (const i in charAry) {
          if (this.charCompare(strA[i], strB[i]) !== 0) {
            return this.charCompare(strA[i], strB[i])
          }
        }
        // 如果通过上面的循环对比还比不出来，就无解了，直接返回-1
        return -1
      })
      return list.sort(a => {
        if (a.name === this.$t('message.common.projectDetail.WDGZL')) {
          return -1
        }
        if (a.name === this.$t('message.common.projectDetail.WCYDXM')) {
          return -1
        }
      })
    },
    charCompare(charA, charB) {
      // 谁为非法值谁在前面
      if (
        charA === undefined ||
        charA === null ||
        charA === '' ||
        charA === ' ' ||
        charA === '　'
      ) {
        return -1
      }
      if (
        charB === undefined ||
        charB === null ||
        charB === '' ||
        charB === ' ' ||
        charB === '　'
      ) {
        return 1
      }
      return charA.localeCompare(charB)
    },
    sortTypeChange(name = 'updateTime', id) {
      this.sortType[id] =
        name === 'updateTime'
          ? this.$t('message.common.projectDetail.updteTime')
          : this.$t('message.common.projectDetail.name')
      this.dataList = this.dataList.map(item => {
        if (!id || id === item.id) {
          item.dwsProjectList = item.dwsProjectList.sort((a, b) => {
            if (name === 'updateTime') {
              return new Date(b[name]) - new Date(a[name])
            } else {
              return this.charCompare(a[name], b[name])
            }
          })
        }
        return item
      })
    },
    gotoScriptis() {
      this.$router.push('/home')
    },
    projectExportOk() {
      this.isFlowPubulish = true
      const params = {
        IOType: 'PROJECT',
        comment: this.exportDesc,
        needChangeVersion: this.exportChangeVersion
      }
      this.precentList.push(this.currentProjectData)
      storage.set('precentList', this.precentList)
      api.fetch('/dss/export', params, 'post').then(() => {
        let queryTime = 0
        this.checkResult(
          +this.currentProjectData.latestVersion.id,
          queryTime,
          'export',
          this.currentProjectData.id
        )
      })
    },
    // 发布和导出共用查询接口
    checkResult(id, timeoutValue, type, projectId) {
      let typeName = this.$t('message.constants.export')
      if (type === 'puhslish') {
        typeName = this.$t('message.workflow.workflowItem.publish')
      }
      const timer = setTimeout(() => {
        timeoutValue += 8000
        api
          .fetch('/dss/releaseQuery', { projectVersionID: +id }, 'get')
          .then(res => {
            if (timeoutValue <= 10 * 60 * 1000) {
              if (
                res.info.status === 'Inited' ||
                res.info.status === 'Running'
              ) {
                clearTimeout(timer)
                this.checkResult(id, timeoutValue, type, projectId)
              } else if (res.info.status === 'Succeed') {
                clearTimeout(timer)
                this.isFlowPubulish = false
                this.removePercent(projectId)
                // 如果是导出成功需要下载文件
                if (type === 'export' && res.info.msg) {
                  const url =
                    `http://${window.location.host}/api/rest_j/v1/` +
                    'dss/downloadFile/' +
                    res.info.msg
                  const link = document.createElement('a')
                  link.setAttribute('href', url)
                  link.setAttribute('download', '')
                  const evObj = document.createEvent('MouseEvents')
                  evObj.initMouseEvent(
                    'click',
                    true,
                    true,
                    window,
                    0,
                    0,
                    0,
                    0,
                    0,
                    false,
                    false,
                    true,
                    false,
                    0,
                    null
                  )
                  const flag = link.dispatchEvent(evObj)
                  this.$nextTick(() => {
                    if (flag) {
                      this.$Message.success(
                        this.$t('message.workflow.downloadTolocal')
                      )
                    }
                  })
                }
                this.$Message.success(
                  `${this.$t('message.workflow.workflow')}${typeName}${this.$t(
                    'message.workflow.success'
                  )}！`
                )
              } else if (res.info.status === 'Failed') {
                clearTimeout(timer)
                this.isFlowPubulish = false
                this.removePercent(projectId)
                this.$Modal.error({
                  title: `${this.$t('message.workflow.project')}${typeName}${this.$t('message.workflow.fail')}`,
                  content: `<p style="word-break: break-all;">${res.info.msg}</p>`,
                  width: 500,
                  okText: this.$root.$t('message.workflow.publish.cancel')
                })
              }
            } else {
              clearTimeout(timer)
              this.isFlowPubulish = false
              this.removePercent(projectId)
              this.$Message.warning(
                this.$root.$t('message.globalHistory.statusType.timeout')
              )
            }
          })
      }, 8000)
    },
    removePercent(projectId) {
      let precentList = storage.get('precentList')
      if (precentList) {
        precentList = precentList.filter(item => {
          return item.id !== projectId
        })
        storage.set('precentList', precentList)
        this.precentList = precentList
      }
    }
  },
  beforeDestroy() {
    eventbus.off('workspace.change', this.initWorkspaceData);
  }
}
</script>
<style lang="scss" scoped src="./index.scss"></style>
<style lang="css">
.delete-card-item .project-card-item {
  pointer-events: none;
  opacity: .65
}
/* 删除确认弹窗 - 项目概况块 */
.delete-summary-block {
  background: #f8f8f9;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 12px;
}
.delete-summary-block .block-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #515a6e;
}
.delete-summary-block .summary-row {
  display: flex;
  line-height: 28px;
}
.delete-summary-block .summary-label {
  width: 90px;
  color: #808695;
  flex-shrink: 0;
}
.delete-summary-block .summary-value {
  color: #17233d;
  word-break: break-all;
}
/* 删除确认弹窗 - 风险提示 */
.delete-risk-alert {
  margin-bottom: 12px;
}
/* 删除确认弹窗 - 输入确认区 */
.delete-confirm-input .input-guide {
  margin-bottom: 8px;
  color: #515a6e;
}
.delete-confirm-input .input-hint {
  margin-top: 6px;
  color: #ed4014;
  font-size: 12px;
}
</style>
