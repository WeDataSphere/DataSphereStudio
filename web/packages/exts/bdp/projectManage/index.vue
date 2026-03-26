<template>
  <div class="main schedule-center-manage">
    <div style="margin-bottom:15px">
      <span @click="goBack" class="back">
        <Icon type="ios-arrow-back"></Icon>
        {{ $t('message.scheduleCenter.back') }}
      </span>
      <h4 style="display:inline-block">{{ $t('message.ext.bdp.Manage') }}</h4>
    </div>
    <Row class="search-bar" :gutter="40">
      <Col span="5" class="search-item">
        <span class="lable">{{ $t('message.ext.bdp.Operation') }}</span>
        <Select v-model="opType" clearable style="width:100px">
          <Option v-for="(item, idx) in opList" :value="item.code" :key="idx">{{ item.text }}</Option>
        </Select>
      </Col>
      <Col span="5" class="search-item">
        <span class="lable">{{ $t('message.ext.bdp.Status') }}：</span>
        <Select v-model="status" clearable style="width:100px">
          <Option value="Success">Success</Option>
          <Option value="Failed">Failed</Option>
          <Option value="Running">Running</Option>
        </Select>
      </Col>
      <Col span="12" class="search-item">
        <Button
          class="search"
          type="primary"
          style="margin-right:20px"
          @click="search(1)"
        >{{$t("message.scheduleCenter.find")}}</Button>
        <Button
          type="warning"
          style="margin-right:20px"
          @click="importShow"
        >{{$t("message.scheduleCenter.import")}}</Button>
        <Button type="warning" style="margin-right:20px" @click="exportShow">{{ $t('message.ext.bdp.Export') }}</Button>
        <Button type="warning" @click="publishShow">{{ $t('message.ext.bdp.Publish') }}</Button>
      </Col>
    </Row>
    <Table class="table-content" :columns="columns" :data="tableData" :loading="loading">
      <template v-if="row.status" slot-scope="{row}" slot="status">
        {{ row.status && row.status.text }}
      </template>
      <template v-if="row.operateType" slot-scope="{row}" slot="operateType">
        {{ row.operateType && row.operateType.text }}
      </template>
      <template  slot-scope="{row}" slot="content">
        <div style="overflow: hidden;white-space: nowrap;text-overflow: ellipsis;display:inline-block;width:90%" :title="row.content">
          {{ row.content }}
        </div>
        <a @click="showMore(row)" style="vertical-align:top">查看</a>
      </template>
    </Table>
    <div class="page-bar">
      <Page
        ref="page"
        :total="page.totalSize"
        :page-size-opts="page.sizeOpts"
        :page-size="page.pageSize"
        :current="page.pageNow"
        class-name="page"
        size="small"
        show-total
        show-sizer
        @on-change="change"
        @on-page-size-change="changeSize"
      />
    </div>
    <Modal v-model="exportModal" width="550" class="table-row">
      <div slot="header">{{ $t('message.ext.bdp.Export') }}</div>
      <Form ref="formExportRef" :model="formExpState" label-position="top">
        <FormItem :label="$t('message.ext.bdp.Workflow')"  :rules="[{required:true,message:this.$t('message.ext.bdp.tipworkflow')}]" prop="exportFlows">
          <Select v-model="formExpState.exportFlows" multiple clearable>
            <Option v-for="(item, idx) in workflows" :value="item.orchestratorId" :key="idx">{{ item.orchestratorName }}</Option>
          </Select>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" :disabled="importSubmiting" :loading="importSubmiting" @click="confirm('export')">{{ $t('message.ext.bdp.Confirm') }}</Button>
      </div>
    </Modal>
    <Modal v-model="importModal" width="500" class="table-row">
      <div slot="header">{{ $t('message.ext.bdp.Import') }}</div>
      <Form
        ref="formRef"
        :rules="ruleValidate"
        :model="formState"
        label-position="left"
        :label-width="110"
      >
        <FormItem :label="$t('message.ext.bdp.ImportType')" prop="importType">
          <Select v-model="formState.importType">
            <Option value="file">{{ $t('message.ext.bdp.zipfile') }}</Option>
            <Option value="hdfs">{{ $t('message.ext.bdp.hdfs') }}</Option>
          </Select>
        </FormItem>
        <FormItem v-if="formState.importType == 'file'" :label="$t('message.ext.bdp.Upload')" prop="packageFile">
          <Upload
            ref="uploadZip"
            type="drag"
            :before-upload="handleUpload"
            :format="['zip']"
            :max-size="2001000"
            action=""
          >
            <div class="upload-box">
              <Icon type="ios-cloud-upload" size="52" style="color: #3399ff"></Icon>
              <p>{{ $t('message.orchestratorModes.clickOrDragFile') }}</p>
            </div>
          </Upload>
          <div>{{formState.packageFile}}</div>
        </FormItem>
        <FormItem v-if="formState.importType =='hdfs'" :label="$t('message.ext.bdp.Zip')" prop="zipUrl">
          <Input v-model="formState.zipUrl" :placeholder="$t('message.ext.bdp.zippath')" />
        </FormItem>
        <FormItem :label="$t('message.ext.bdp.Verify')" prop="code">
          <Input v-model="formState.code" :placeholder="$t('message.ext.bdp.inputVerifyCode')" />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" :disabled="importSubmiting" :loading="importSubmiting" @click="confirm('import')">{{ $t('message.ext.bdp.Confirm') }}</Button>
      </div>
    </Modal>
    <Modal v-model="publishModal" width="500" class="table-row">
      <div slot="header">{{ $t('message.ext.bdp.Publish') }}</div>
      <Form
        ref="formPublishRef"
        :model="formPub"
        :rules="pubValidate"
        label-position="left"
        :label-width="90"
      >
        <FormItem :label="$t('message.ext.bdp.ITSMID')" prop="itsmCode">
          <Input v-model="formPub.itsmCode" :placeholder="$t('message.ext.bdp.inputItsm')" />
        </FormItem>
        <FormItem :label="$t('message.ext.bdp.Description：')" prop="desc">
          <Input v-model="formPub.desc" :placeholder="$t('message.ext.bdp.Please')" />
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="primary" :disabled="importSubmiting" :loading="importSubmiting" @click="confirm('publish')">{{ $t('message.ext.bdp.Confirm') }}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import api from '@dataspherestudio/shared/common/service/api'
import moment from 'moment'
export default {
  props: {
  },
  data() {
    return {
      opList: [],
      opType: '',
      columns: [
        // {
        //   title: "ID",
        //   key: 'recordId',
        // },
        {
          title: this.$t('message.ext.bdp.Type'),
          key: 'operateType',
          slot: 'operateType'
        },
        {
          title: this.$t('message.ext.bdp.Status'),
          key: 'status',
          slot: 'status'
        },
        {
          title: this.$t('message.ext.bdp.Content'),
          key: 'content',
          slot: 'content',
          minWidth: 350
        },
        {
          title: this.$t('message.ext.bdp.Operate'),
          key: 'action',
          render: (h, params) => {
            return h("div", [
              h(
                "a",
                {
                  props: {
                    type: "primary",
                    size: "small",
                  },
                  style: {
                    marginRight: "5px",
                    display: params.row.canDownload ? 'inline-block' : 'none'
                  },
                  attrs: {
                    href: `/api/rest_j/v1${this.$API_PATH.PROJECT_PATH}downloadResourcePackage?recordId=${params.row.recordId}`
                  },
                  on: {
                    click: () => {
                      this.$Message.success('请求已发出，下载完成后，请到本地download文件夹查看')
                    }
                  }
                },
                this.$t("message.ext.bdp.download")
              )])
          }
        },
        {
          title: this.$t('message.ext.bdp.User'),
          key: 'creator',
        },
        {
          title: this.$t('message.ext.bdp.OperateTime'),
          key: 'createTime',
          minWidth: 150
        },
      ],
      status: '',
      publishModal: false,
      importModal: false,
      exportModal: false,
      tableData: [],
      workflows: [],
      formExpState: {
        exportFlows: null,
      },
      formState: {
        packageFile: '',
        importType: '',
        zipUrl: '',
        code: '',
      },
      formPub: {
        itsmCode: '',
        desc: '',
      },
      ruleValidate: {
        importType: [{ required: true, message: this.$t('message.ext.bdp.typerequired') }],
        zipUrl: [{ required: true, message: this.$t('message.ext.bdp.Path') }],
        packageFile: [{ required: true, message: this.$t('message.ext.bdp.fileempty') }],
        code: [{ required: true, message: this.$t('message.ext.bdp.VerifyCode') }],
      },
      pubValidate: {
        itsmCode: [{ required: true, message: this.$t('message.ext.bdp.ITSM') }]
      },
      page: {
        totalSize: 0,
        sizeOpts: [15, 30, 45],
        pageSize: 15,
        pageNow: 1
      },
      loading: false,
      importSubmiting: false
    }
  },
  components: {
  },
  created() {
  },
  mounted() {
    this.getOptList()
    this.search()
  },
  watch: {
    '$route.query'() {
      this.getOptList()
      this.search()
    },
  },
  computed: {
  },
  methods: {
    getOptList() {
      api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}listOperateType`,
          {
            projectId: +(this.$route.query.projectID || -1)
          },
          'get'
        )
        .then(res => {
          this.opList = (res.result || []).map(item => {
            if (localStorage.getItem('locale') === 'zh-CN') {
              item.text = item.caption
            } else {
              item.text = item.captionEn
            }
            return item
          });
        })

    },
    // 切换分页
    change(val) {
      this.page.pageNow = val
      this.search()
    },
    // 返回上一页
    goBack() {
      this.$router.go(-1)
    },
    // 页容量变化
    changeSize(val) {
      this.page.pageSize = val
      this.page.pageNow = 1
      this.search()
    },
    search(page) {
      this.loading = true
      if (page) {
        this.page.pageNow = 1
      }
      api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}operateRecordList`,
          {
            projectId: +(this.$route.query.projectID || -1),
            operateType: this.opType,
            status: {Success: 0, Failed: -1, Running: 1}[this.status],
            currentPage: page ? page : this.page.pageNow,
            pageSize: this.page.pageSize,
          },
          'get'
        )
        .then(res => {
          this.tableData = (res.result || []).map(item => {
            item.operateType = {...item.operateType}
            item.status ={...item.status}
            if (localStorage.getItem('locale') === 'zh-CN') {
              item.operateType.text = item.operateType.caption
              item.status.text = item.status.caption
            } else {
              item.operateType.text = item.operateType.captionEn
              item.status.text = item.status.captionEn
            }
            item.createTime =  moment.unix(item.createTime / 1000).format('YYYY-MM-DD HH:mm:ss')
            return item
          })
          this.page.totalSize = res.total
        }).finally(()=>{
          this.loading = false
        })
    },
    exportShow() {
      if (this.workflows.length < 1) {
        this.getWorkflowData()
      }
      this.exportModal = true
    },
    publishShow() {
      this.publishModal = true
    },
    importShow() {
      this.importModal = true
      this.formState = {
        packageFile: '',
        importType: '',
        zipUrl: '',
        code: '',
      }
      this.$refs.formRef.resetFields()
    },
    handleUpload(file) {
      if (file.name.indexOf('.zip') === -1) {
        this.$Message.warning(this.$t('message.orchestratorModes.selectZip'));
        return false;
      }
      this.$refs.formRef.validateField('packageFile')
      this.formState.packageFile = file.name;
      this.importZip = file
      return false;
    },
    confirm(type) {
      switch (type) {
        case 'import':
          this.$refs.formRef.validate((valid) => {
            if (valid) {
              this.importAction()
            }
          })
          break;
        case 'export':
          this.$refs.formExportRef.validate((valid) => {
            if (valid) {
              this.exportAction()
            }
          })
          break;
        case 'publish':
          this.$refs.formPublishRef.validate((valid) => {
            if (valid) {
              this.publishAction()
            }
          })
          break;
      }
    },
    importAction() {
      const params = {
        projectId: +(this.$route.query.projectID || -1),
        importType: this.formState.importType,
        labels: 'prod',
        checkCode: this.formState.code
      }
      if (params.importType == 'file') {
        params.packageFile = this.importZip
      } else {
        params.packageUri = this.formState.zipUrl
      }
      this.importSubmiting = true
      api.fetch(`${this.$API_PATH.PROJECT_PATH}batchImportOrchestrators`, params,
        {
          useForm: true,
          headers: {
            "Content-Type": "multipart/form-data"
          }
        })
        .then(() => {
          this.importModal = false
        }).finally(()=>{
          this.importSubmiting = false
        })
    },
    exportAction() {
      this.importSubmiting = true
      api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}batchExportOrchestrators`, {
            projectId: +(this.$route.query.projectID || -1),
            orchestratorIds: this.formExpState.exportFlows,
            labels: 'prod'
          }, 'post'
        )
        .then(() => {
          this.exportModal = false
          this.$Message.info(this.$t('message.ext.bdp.exportmsg'))
        }).finally(()=>{
          this.importSubmiting = false
        })
    },
    publishAction() {
      this.importSubmiting = true
      api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}publishWholeProject`, {
            projectId: +(this.$route.query.projectID || -1),
            approveId: this.formPub.itsmCode,
            comment: this.formPub.desc,
            labels: 'prod'
          }, 'post'
        )
        .then(() => {
          this.publishModal = false
          this.$Message.info(this.$t('message.ext.bdp.publishmsg'))
        }).finally(()=>{
          this.importSubmiting = false
        })
    },
    getWorkflowData() {
      return api
        .fetch(
          `${this.$API_PATH.PROJECT_PATH}getProdOrchestrators`,
          {
            projectId: +(this.$route.query.projectID || -1),
            workspaceId: this.$route.query.workspaceId,
            dssLabel: 'prod'
          },
          'get'
        )
        .then(res => {
          this.workflows = res.orchestrators
        })
    },
    showMore(row) {
      this.$Modal.info({
        title: 'Content',
        content: `<p style="word-break: break-all;max-height: 470px;overflow-y:auto">${row.content}</p>`,
        closable: true,
        width: 700
      })
    }
  },
}
</script>
<style lang="scss" scoped>
@import '@dataspherestudio/shared/common/style/variables.scss';
.schedule-center-manage {
  padding: 20px;
  overflow-y: auto;
  height: 100%;
  .table-content {
    margin-top: 20px;
  }
  .back {
    color: $primary-color;
    margin-right: 10px;
  }
  .page-bar {
    margin-top: 15px;
    text-align: center;
  }
}
</style>
