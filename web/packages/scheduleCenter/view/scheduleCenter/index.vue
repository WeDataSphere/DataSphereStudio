<template>
  <div class="workflow-wrap">
    <div class="workflow-nav-tree" :class="{ 'tree-fold': treeFold }">
      <div class="workflow-nav-tree-switch" @click="handleTreeToggle">
        <span class="project-nav-tree-top-t-icon">
          <SvgIcon
            icon-class="dev_center_flod"
            style="opacity: 0.65"
          />
        </span>
      </div>
      <div class="project-nav-tree">
        <div class="project-nav-tree-top">
          <div class="project-nav-tree-top-t">
            <span class="project-nav-tree-top-t-txt">{{ $t('message.scheduleCenter.Project') }}</span>
            <span class="project-nav-tree-top-t-icon">
              <SvgIcon
                icon-class="dev_center_flod"
                style="opacity: 0.65"
                @click="handleTreeToggle"
              />
            </span>
          </div>
        </div>
        <div class="list-container">
          <div
            class="list-item"
            v-for="item in projectsTree"
            :key="item.id"
          >
            <div class="list-content" :class="{ 'list-content-active': currentTreeId == item.id }">
              <div class="list-name" @click="handleTreeClick(item)">
                {{ item.name }}
              </div>
              <Dropdown v-if="menulist.length"  @on-click="menuAction($event, item)" placement="left-start">
                <div style="margin-right:10px">...</div>
                <DropdownMenu slot="list">
                  <DropdownItem
                    v-for="(item) in menulist"
                    :name="item.value"
                    :key="item.value"
                  >{{ item.text }}</DropdownItem>
                </DropdownMenu>
              </Dropdown>
            </div>
          </div>
        </div>
        <Spin v-show="loadingTree" size="large" fix />
      </div>
    </div>
    <div class="workflowTabContainer" :class="{ 'tree-fold': treeFold }">
      <div v-if="selectDevprocess.length" class="tap-bar" :class="{ 'tree-fold': treeFold }">
        <div class="bottomTapList">
          <div class="bottomRightContainer">
            <div class="tap-menu">
              <Dropdown @on-click="handleChangeButton">
                <a style="text-decoration: none;">
                  {{ currentButton.dicName }}
                  <Icon type="ios-arrow-down"></Icon>
                </a>
                <Dropdown-menu slot="list">
                  <Dropdown-item
                    v-for="item in selectDevprocess"
                    :key="item.dicKey"
                    :name="item.dicValue"
                  >{{ item.dicName }}</Dropdown-item
                  >
                </Dropdown-menu>
              </Dropdown>
            </div>
          </div>
        </div>
      </div>
      <div class="defaultSlot">
        <template v-if="modeOfKey === 'streamis_prod'">
          <Streamis class="streamisContainer" :project-name="$route.query.projectName"/>
        </template>
        <ScheduleCenter v-else></ScheduleCenter>
      </div>
      <Spin v-if="loading" size="large" fix />
    </div>
  </div>
</template>
<script>
import storage from '@dataspherestudio/shared/common/helper/storage';
import api from '@dataspherestudio/shared/common/service/api';
import { DEVPROCESS, ORCHESTRATORMODES } from '@dataspherestudio/shared/common/config/const.js';
import {
  GetDicSecondList,
  GetDicList
} from '@dataspherestudio/shared/common/service/apiCommonMethod.js';
import { setVirtualRoles } from '@dataspherestudio/shared/common/config/permissions.js';
import eventbus from "@dataspherestudio/shared/common/helper/eventbus";
import ScheduleCenter from '../../module/scheduleCenter';
import plugin from '@dataspherestudio/shared/common/util/plugin';
import Streamis from '@dataspherestudio/workflows/module/innerIframe';

export default {
  components: {
    ScheduleCenter: ScheduleCenter.component,
    Streamis: Streamis.component,
  },
  data() {
    return {
      modeOfKey: DEVPROCESS.PRODUCTCENTER,
      orchestratorModeList: {},
      currentProjectData: {
        name: "",
        description: "",
        business: "",
        applicationArea: "",
        product: "",
        editUsers: [],
        accessUsers: [],
        devProcessList: [],
        orchestratorModeList: [],
        releaseUsers: [],
      },
      devProcessBase: [],
      DEVPROCESS,
      ORCHESTRATORMODES,
      loading: false,
      loadingTree: false,
      projectsTree: [],
      treeFold: false,
      currentTreeId: +this.$route.query.projectID, // tree中active节点
      menulist: []
    };
  },
  watch: {
    "$route.query.projectID"(v) {
      this.currentTreeId = +v
      this.getProjectData();
    }
  },
  async created() {
    storage.set("currentDssLabels", this.modeOfKey);
    const params = {
      parentKey: "p_develop_process",
      workspaceId: this.$route.query.workspaceId,
    };
    const res = await GetDicList(params)
    this.devProcessBase = res.list;
    this.getDicSecondList();
    this.getAllProjects(() => {});
  },
  async mounted() {
    this.menulist = await plugin.emitHook('scheduler_center_project_menu') || []
    this.getProjectData();
  },
  computed: {
    selectDevprocess() {
      return this.devProcessBase
        ? this.devProcessBase.filter((item) =>
          this.currentProjectData.devProcessList.includes(item.dicValue)
        )
        : [];
    },
    currentButton() {
      return this.selectDevprocess.find(
        (item) => item.dicValue === this.modeOfKey
      ) || {}
    }
  },
  methods: {
    menuAction(action, item) {
      const menuItem = this.menulist.find(it => it.value === action)
      if (menuItem && menuItem.func) {
        menuItem.func(this, item)
      }
    },
    handleTreeToggle() {
      this.treeFold = !this.treeFold;
    },
    // 获取编排模式的基本信息
    getDicSecondList() {
      GetDicSecondList(this.$route.query.workspaceId).then((res) => {
        this.orchestratorModeList = res.list;
      });
    },
    // 获取工程的数据
    getProjectData() {
      if (!this.$route.query.projectID) {
        return
      }
      api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}getAllProdProjects`,
          {
            workspaceId: +this.$route.query.workspaceId,
            id: +this.$route.query.projectID,
          },
          "post"
        )
        .then((res) => {
          if (res && res.projects[0]) {
            const project = res.projects[0];
            setVirtualRoles(project, this.getUserName());
            this.currentProjectData = {
              ...res.projects[0],
              canWrite: project.canWrite(),
            };
            this.loading = false;
          }
        });
    },
    // 获取所有project展示tree
    // 生产中心项目列表
    getAllProjects(callback) {
      this.loadingTree = true;
      api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}getAllProdProjects`,
          {
            workspaceId: +this.$route.query.workspaceId,
          },
          "post"
        )
        .then((res) => {
          this.loadingTree = false;
          if (this.$route.query.projectID) {
            let index
            let it
            res.projects.find((item, idx) => {
              if (item.id == this.$route.query.projectID) {
                index = idx
                it = item
              }
            })
            if (it) {
              res.projects.splice(index, 1)
              res.projects.unshift(it)
            }
          }
          this.projectsTree = res.projects
            .filter((n) => {
              return (
                n.devProcessList &&
                n.devProcessList.includes("prod")
              );
            })
            .map((n) => {
              setVirtualRoles(n, this.getUserName());
              return {
                id: n.id,
                name: n.name,
                type: "prod",
                canWrite: n.canWrite(),
              };
            });
          callback();
        });
    },
    handleTreeClick(node) {
      if (node.type === "project" || node.type === "prod") {
        this.currentTreeId = node.id;
        const SKEY = this.getTabStorageKey()
        let schedulerTabList = JSON.parse(sessionStorage.getItem(SKEY)) || []
        if( schedulerTabList.findIndex(i => i.id == node.id) < 0 ) {
          schedulerTabList.push(node)
          sessionStorage.setItem(SKEY, JSON.stringify(schedulerTabList))
          eventbus.emit('scheduler_tab_list_change', schedulerTabList)
        }
        if (node.id != this.$route.query.projectID) {
          // 跨工程，会监听projectID
          const query = {
            workspaceId: this.$route.query.workspaceId,
            projectID: node.id,
            projectName: node.name,
          };
          this.$router.replace({
            name: "ScheduleCenter",
            query,
          })
        }
      }
    },
    getTabStorageKey() {
      return 'scheduler_tab_list_' + this.getUserName()
    },
    // 切换开发流程
    handleChangeButton(item) {
      console.log(item)
      if ( item ==  this.modeOfKey ) {
        return
      }
      // 当前流程的value
      this.modeOfKey = item;
      // 使用的地方很多，存在缓存全局获取
      storage.set("currentDssLabels", this.modeOfKey);
      const routerMap =  { scheduler: 'Scheduler', dev: 'Workflow', prod: 'ScheduleCenter'}
      this.$router.push({
        name: routerMap[item],
        query: this.$route.query,
      });
    },
    getUserName() {
      return storage.get("baseInfo", "local")
        ? storage.get("baseInfo", "local").username
        : null;
    }
  },
};
</script>
<style lang="scss" scoped>
@import "index.scss";
@import "@dataspherestudio/shared/common/style/variables.scss";
.item-header {
  font-size: $font-size-base;
  margin: 10px 25px;
  font-weight: bold;
  padding-left: 5px;
  border-left: 3px solid $primary-color;
  @include border-color($primary-color, $dark-primary-color);
}
.rightCardContainer {
  will-change: auto;
  padding: 20px;
  .cardItem {
    box-shadow: 0 0 6px $shadow-color;
    position: relative;
    padding: 10px;
    line-height: 1;
    display: flex;
    align-items: center;
    &:not(:last-child) {
      margin-bottom: 50px;
    }
    .ios-arrow-round-down {
      position: absolute;
      bottom: -45px;
      left: 50%;
      // color: $primary-color;
      @include font-color($primary-color, $dark-primary-color);
      font-size: 40px;
      transform: translateX(-50%);
    }
    .cardItemText {
      margin-left: 10px;
      font-size: $font-size-base;
      font-weight: 700;
    }
  }
}
.list-item{
  white-space: nowrap;
  outline: none;
  .list-content {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 0 10px;
    @include font-color($light-text-color, $dark-text-color);
    &:hover {
      @include bg-color(#edf1f6, $dark-active-menu-item);
    }
    &-active {
      @include bg-color(#edf1f6, $dark-active-menu-item);
      @include font-color($primary-color, $dark-primary-color);
    }
    .list-name {
      display: block;
      flex: 1;
      line-height: 32px;
      padding: 0 6px;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
    }
  }
}
</style>
