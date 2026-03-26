<template>
  <div class="appcon-page">
    <FForm
      ref="elFormRef"
      layout="inline"
      :model="formData"
      size="small"
      label-width="140px"
    >
      <FFormItem label="AppConn名称" prop="appConnName">
        <FInput v-model="formData.appConnName" placeholder="请输入名称" />
      </FFormItem>
      <FFormItem label="AppConn物料包主类" prop="className">
        <FInput v-model="formData.className" placeholder="请输入物料包主类" />
      </FFormItem>
      <FFormItem label-width="0">
        <FButton type="primary" style="margin: 0 15px" @click="getAppconns">
          查询
        </FButton>
        <FButton type="primary" @click="reset"> 重置 </FButton>
      </FFormItem>
    </FForm>
    <FButton type="primary" @click="openAddTemplate"> 新建AppConn </FButton>
    <FTable border class="table-container" :data="pageAppconns">
      <FTableColumn prop="appConnName" label="AppConn名称" />
      <FTableColumn v-slot="{ row }" prop="ifIframe" label="是否支持iframe嵌入">
        {{ row.ifIframe ? '是' : '否' }}
      </FTableColumn>
      <FTableColumn v-slot="{ row }" prop="isMicroApp" label="是否微前端应用">
        {{ row.isMicroApp ? '是' : '否' }}
      </FTableColumn>
      <FTableColumn prop="resourceFetchMethod" label="资源获取方式" />
      <FTableColumn v-slot="{ row }" prop="reference" label="关联AppConn">
        {{ row.reference ? row.reference : '--' }}
      </FTableColumn>
      <FTableColumn v-slot="{ row }" prop="className" label="AppConn物料包主类">
        {{ row.className ? row.className : '--' }}
      </FTableColumn>
      <FTableColumn
        v-slot="{ row }"
        label="操作"
        align="center"
        fixed="right"
        :width="60"
      >
        <FDropdown
          :options="tableMoreOptions"
          trigger="focus"
          @click="clickTableMore($event, row)"
        >
          <FButton class="table-operation-item">
            <template #icon>
              <MoreCircleOutlined />
            </template>
          </FButton>
        </FDropdown>
      </FTableColumn>
    </FTable>
    <FPagination
      v-model:currentPage="pagination.current"
      v-model:pageSize="pagination.size"
      style="justify-content: end"
      show-size-changer
      show-total
      :total-count="Appconns.length"
    />
  </div>
  <AppconnDrawer
    v-if="operateDrawerShow.mode == 'add' || operateDrawerShow.mode == 'edit'"
    v-model:show="operateDrawerShow.show"
    :appconn="operateDrawerShow.data"
    :mode="operateDrawerShow.mode"
    @update-appcons="getAppconns"
  />
  <MenuDrawer
    v-if="operateDrawerShow.mode == 'menu'"
    v-model:show="operateDrawerShow.show"
    :appconn="operateDrawerShow.data"
    :mode="operateDrawerShow.mode"
    @update-appcons="getAppconns"
  />
</template>
<script lang="ts" setup>
import { useRouter } from 'vue-router';
import { ref, reactive, computed, onMounted } from 'vue';
import { request } from '@dataspherestudio/shared';
import {
  FModal,
  FForm,
  FFormItem,
  FInput,
  FButton,
  FTable,
  FTableColumn,
  FMessage,
} from '@fesjs/fes-design';
import { MoreCircleOutlined } from '@fesjs/fes-design/icon';
import AppconnDrawer from './modal.vue';
import MenuDrawer from './menu.vue';

const formData = reactive({
  className: '',
  appConnName: '',
});
const Appconns = reactive([]);
const elFormRef = ref(null);
const operateDrawerShow = reactive({
  show: true,
  mode: '',
  data: {},
});
const router = useRouter();
async function getAppconns() {
  return await request
    .fetch(
      'dss/framework/project/appconn/getAppConns',
      {
        appConnName: formData.appConnName,
        className: formData.className,
      },
      'get'
    )
    .then((resp) => {
      let { data } = resp;
      Appconns.length = 0;
      Appconns.push(...data.appConnInfos);
    });
}

const reset = () => {
  formData.appConnName = '';
  formData.className = '';
  getAppconns();
};

const deleteAppcons = (id: number) => {
  const data = new FormData();
  data.append('id', id.toString());
  return request
    .fetch({
      url: 'dss/framework/project/appconn/deleteAppConn',
      method: 'post',
      data,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    })
    .then((res: any) => {
      if (res.data && res.data.status == 0) {
        FMessage.success('删除成功');
      }
    });
};

const pageAppconns = computed(() => {
  return Appconns.slice(
    (pagination.current - 1) * pagination.size,
    pagination.current * pagination.size
  );
});

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});

const tableMoreOptions = ref([
  {
    value: 'edit',
    label: '编辑',
  },
  {
    value: 'instance',
    label: '实例管理',
  },
  {
    value: 'menu',
    label: '配置菜单',
  },
  {
    value: 'nodes',
    label: '节点管理',
  },
  {
    value: 'delete',
    label: '删除',
  },
]);
// 更多操作
const clickTableMore = async (
  value: string,
  row: { appConnName: string; id: number }
) => {
  operateDrawerShow.data = { ...row };
  // 打开应用抽屉
  if (value === 'instance') {
    router.push({
      path: '/appconn/instance',
      query: {
        id: row.id,
      },
    });
  } else if (value === 'menu') {
    operateDrawerShow.show = true;
    operateDrawerShow.mode = 'menu';
  } else if (value === 'edit') {
    operateDrawerShow.show = true;
    operateDrawerShow.mode = 'edit';
  } else if (value === 'nodes') {
    router.push({
      path: '/appconn/queryNode',
      query: {
        id: row.id,
        appConnName: row.appConnName,
      },
    });
  } else if (value === 'delete') {
    FModal.confirm({
      title: '提示',
      content: `删除Appconn会同步删除与其关联的应用实例, 是否继续删除【${row.appConnName}】？`,
      okText: '确定',
      cancelText: '取消',
      closable: true,
      onOk: async () => {
        try {
          await deleteAppcons(row.id);
          await getAppconns();
          FMessage.success('删除成功');
        } catch (err) {
          console.error(err);
        }
      },
    });
  }
};

// 打开新增模板
const openAddTemplate = () => {
  operateDrawerShow.data = {};
  operateDrawerShow.mode = 'add';
  operateDrawerShow.show = true;
};
onMounted(() => {
  getAppconns();
});
</script>
<style>
.appcon-page {
  padding: 20px;
  background-color: #fff;
}
.table-container {
  margin-top: 20px;
  margin-bottom: 10px;
}
</style>
