<template>
  <div>
    <ul
      class="user-menu">
      <li class="user-menu-arrow"/>
      <li
        class="user-menu-item"
        v-for="(menu) in menuList"
        :key="menu.id"
        @click="handleClick(menu.id)">
        <Icon
          class="user-menu-item-icon"
          :type="menu.icon">
        </Icon>
        <span>{{ menu.name }}</span>
      </li>
    </ul>
    <Drawer title="错误日志" v-model="showDrawer" width="580" draggable
        class-name="custom-drawer-style" @on-close="showDrawer = false">
        <Form  class="dialogFromMain" :model="formMake">
          <FormItem label="时间范围" prop="timeRange">
              <DatePicker v-model="formMake.timeRange"
                style="width: 250px; margin-right: 10px;"
                type="datetimerange"
                range-separator="-"
                format="yyyy-MM-dd HH:mm:ss"></DatePicker>
          </FormItem>
          <!-- <FormItem label="异常信息" prop="keyword">
              <Input v-model="formMake.keyword" style="width: 250px; margin-right: 10px;" placeholder="关键词" />
          </FormItem> -->
        </Form>
        <Button style="margin-bottom:10px" type="primary" @click="findlog" :loading="isSearching">搜索</Button>
        <Table :columns="columnData" :data="pageData"></Table>
        <Page
          style="position: relative;"
          :total="page.totalSize"
          :page-size-opts="page.sizeOpts"
          :page-size="page.pageSize"
          :current="page.pageNow"
          class-name="page"
          size="small"
          show-total
          show-sizer
          @on-change="change"
          @on-page-size-change="changeSize" />
    </Drawer>
  </div>

</template>
<script>
import api from '@dataspherestudio/shared/common/service/api';
import storage from '@dataspherestudio/shared/common/helper/storage';
import mixin from '@dataspherestudio/shared/common/service/mixin';
import eventbus from '@dataspherestudio/shared/common/helper/eventbus';
import moment from 'moment';
export default {
    name: 'dssMenu',
    mixins: [mixin],
    data() {
        const theme = localStorage.getItem('theme') === 'dark' ? 'light' : 'dark';
        const startTime = moment().startOf('day').format('YYYY-MM-DD HH:mm:ss');
        const endTime = moment().startOf('day').format('YYYY-MM-DD HH:mm:ss');
        return {
            showDrawer: false,
            isSearching: false,
            formMake: {
              keyword: '',
              timeRange: [
                startTime,
                endTime
              ]
            },
            tableData: [

            ],
            page: {
                totalSize: 0,
                sizeOpts: [10, 20, 50],
                pageSize: 10,
                pageNow: 1
            },
            columnData: [
                {
                    title: '时间',
                    key: 'createTime',
                    width: 100
                },
                {
                    title: '异常信息',
                    key: 'responseBody'
                }
            ],
            menuList: [
                // {
                //     id: 'user-management',
                //     name: '用户管理',
                //     icon: 'ios-person-outline',
                // },
                {
                    id: 'clearCache',
                    name: this.$t('message.common.clearCache'),
                    icon: 'ios-trash-outline',
                },
                {
                    id: 'findlog',
                    name: this.$t('message.common.findlog'),
                    icon: 'ios-search-outline',
                },
                {
                    id: 'changeLang',
                    name: localStorage.getItem('locale') === 'zh-CN' ? 'English' : '简体中文',
                    icon: 'md-repeat',
                },
                {
                    id: 'changeTheme',
                    name: this.$t(`message.common.theme.${theme}`),
                    icon: 'md-repeat',
                },
                {
                    id: 'logout',
                    name: this.$t('message.common.logOut'),
                    icon: 'ios-log-out',
                }
            ],
        };
    },
    computed: {
      pageData() {
        return this.tableData.slice((this.page.pageNow -1 ) * this.page.pageSize, this.page.pageNow * this.page.pageSize)
      }
    },
    watch: {
        showDrawer: {
            immediate: true,
            handler: function (v) {
                if (v) {
                    const startTime = moment().startOf('day').format('YYYY-MM-DD HH:mm:ss');
                    const endTime = moment().endOf('day').format('YYYY-MM-DD HH:mm:ss');
                    this.formMake.timeRange = [startTime, endTime];
                    this.findlog()
                }
            }
        }
    },
    methods: {
        handleClick(type) {
            switch (type) {
                case 'user-management':
                    this.openUserManagement();
                    break;
                case 'clearCache':
                    this.clearCache();
                    break;
                case 'findlog':
                    this.showDrawer = true;
                    break;
                case 'logout':
                    this.logout();
                    break;
                case 'changeLang':
                    this.changeLang();
                    break;
                case 'changeTheme':
                    this.changeTheme();
                    break;
            }
        },
        openUserManagement() {
            this.$Message.info(this.$t('message.common.userMenu.comingSoon'));
        },
        clearCache() {
            localStorage.setItem('cacheGuide', null);
            this.$Modal.confirm({
                title: this.$t('message.common.userMenu.title'),
                content: this.$t('message.common.userMenu.content'),
                onOk: () => {
                    this.dispatch('dssIndexedDB:deleteDb');
                    this.$Message.success(this.$t('message.common.userMenu.clearCacheSuccess'));
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                },
                onCancel: () => {
                },
            });
        },
        // 切换分页
        change(val) {
            this.page.pageNow = val;
        },
        // 页容量变化
        changeSize(val) {
            this.page.pageSize = val;
            this.page.pageNow = 1;
        },
        findlog() {
          const startTime = moment(this.formMake.timeRange[0]).format('x') - 0;
          const endTime = moment(this.formMake.timeRange[1]).format('x') - 0;
          if (this.formMake.timeRange[0] === '' || this.formMake.timeRange[1] === '') {
            this.$Message.error('请选择时间范围');
            return
          }
          if(endTime -startTime > 24 * 60 * 60 * 1000) {
            this.$Message.error('查询时间不能超过24小时');
            return
          }
          this.isSearching = true;
          api.fetch('/dss/guide/solution/getAllProblemReport', {
            startTime,
            endTime,
          }, 'get').then((res) => {
            this.isSearching = false;
            for(let i = 0; i < 11; i++) {
                res.reportList.push(res.reportList[0]);
            }
            this.tableData = (res.reportList || []).sort((a,b) => {
                return b.createTime - a.createTime;
            }).map(it => {
              it.createTime = moment(it.createTime).format('YYYY-MM-DD HH:mm:ss');
              return it
            })
            this.page.totalSize = this.tableData.length
          }).catch(() => {
            this.isSearching = false;
          });
        },
        closeDrawer() {
            this.showDrawer = false
        },
        async logout() {
            const unsave = await eventbus.emit('check.scriptis.unsave');
            if (unsave)
                return;
            if (window.$Wa) {
                window.$Wa.track('user_logout', {
                    userName: window.username,
                    timestamp: Date.now()
                });
            }
            api.fetch('/user/logout', {}).then(() => {
                this.$emit('clear-session');
                storage.set('need-refresh-proposals-hql', true);
                storage.set('need-refresh-proposals-python', true);
                // 手动退出清掉baseInfo
                storage.remove('baseInfo', 'local');
                window.username = undefined;
                this.$router.push({ path: '/login', query: { 'notcheck': true } });
            });
        },
        changeLang() {
            // 中文切换英文
            if (localStorage.getItem('locale') === 'zh-CN') {
                localStorage.setItem('locale', 'en');
                localStorage.setItem('fes_locale', 'en-US');
            }
            else {
                localStorage.setItem('locale', 'zh-CN');
                localStorage.setItem('fes_locale', 'zh-CN');
            }
            window.location.reload();
        },
        changeTheme() {
            document.querySelector('body').classList.add('notransition');
            if (localStorage.getItem('theme') === 'dark') {
                window.document.documentElement.setAttribute('data-theme', '');
                localStorage.setItem('theme', '');
                eventbus.emit('theme.change', 'light');
                eventbus.emit('watermark.refresh');
            }
            else {
                window.document.documentElement.setAttribute('data-theme', 'dark');
                localStorage.setItem('theme', 'dark');
                eventbus.emit('theme.change', 'dark');
                eventbus.emit('watermark.refresh');
            }
            setTimeout(() => {
                document.querySelector('body').classList.remove('notransition');
            }, 1000);
            this.menuList = this.menuList.map(i => {
                if (i.id == 'changeTheme') {
                    return {
                        id: 'changeTheme',
                        name: localStorage.getItem('theme') === 'dark' ? this.$t(`message.common.theme.light`) : this.$t(`message.common.theme.dark`),
                        icon: 'md-repeat',
                    };
                }
                else {
                    return i;
                }
            });
        }
    }
};
</script>
<style scoped lang="scss">
@import '@dataspherestudio/shared/common/style/headerUserMenu.scss';
</style>
<style lang="less">
.custom-drawer-style {
    z-index: 1004 !important;
    .ivu-drawer{
        max-width: 78%;
        .ivu-drawer-body {
            height: calc(100% - 100px);
            overflow: auto;
        }
        .ivu-drawer-content {
            top: 55px;
        }
    }
}
</style>
