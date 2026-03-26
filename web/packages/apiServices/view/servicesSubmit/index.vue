<template>
  <div class="submit-wrapper">
    <div style="font-size: 16px;">
      <SvgIcon style="font-size: 16px;display: inline-block;transform: rotate(180deg);"
        color="#444444"
        @click="goBack"
        icon-class="fi-expand-right"/> {{ $t('message.apiServices.servicesSubmit.batchApprove') }}</div>
    <Row class="main">
      <Col span="14">
        <div class="title">{{ $t('message.apiServices.servicesSubmit.apiInfoTitle') }}</div>
        <Form
          ref="submitForm"
          :model="formData"
          :rules="formValid"
          :label-width="120"
        >
          <FormItem
            prop="id"
            :label="$t('message.apiServices.servicesSubmit.selectApi')"
            class="api-select"
          >
            <Select v-model="formData.id"
              filterable
              clearable
              multiple>
              <Option v-for="item in apiList" :key="item.id" :value="item.id+'_-_'+item.name">{{item.name}}</Option>
            </Select>
            <div class="tag-list">
              <Tag v-for="item of selectedApi"
                closable
                @click.native="toggleInfo(item)"
                @on-close="delectSelect(item)"
                :class="{'active': currentApi.id == item.id}"
                :key="item.id">
                {{ item.name }}
              </Tag>
            </div>
          </FormItem>
          <div class="title">{{ $t('message.apiServices.servicesSubmit.approvalInfoTitle') }}</div>
          <FormItem
            prop="approvalName"
            :label="$t('message.apiServices.servicesSubmit.approvalName')">
            <Input v-model="formData.approvalName" />
          </FormItem>
          <FormItem
            prop="applyUser"
            :label="$t('message.apiServices.servicesSubmit.authorizedUser')">
            <Select v-model="formData.applyUser" multiple filterable>
              <Option v-for="(item, index) in applyUserList" :key="item.name + index" :value="item.name">{{item.name}}</Option>
            </Select>
          </FormItem>
          <FormItem
            prop="duration"
            :label="$t('message.apiServices.servicesSubmit.duration')"
          >
            <Input v-model="formData.duration" :placeholder="$t('message.apiServices.servicesSubmit.durationPlaceholder')">
              <template #append><span>{{ $t('message.apiServices.servicesSubmit.day') }}</span></template>
            </Input>
          </FormItem>
          <FormItem
            prop="sensitive"
            :label="$t('message.apiServices.servicesSubmit.sensitiveData')"
          >
            <Select v-model="formData.sensitive" :placeholder="$t('message.apiServices.servicesSubmit.sensitivePlaceholder')">
              <Option value="1">{{ $t('message.apiServices.servicesSubmit.sensitiveOptions.yes') }}</Option>
              <Option value="0">{{ $t('message.apiServices.servicesSubmit.sensitiveOptions.no') }}</Option>
            </Select>
          </FormItem>
          <FormItem
            prop="importance"
            :label="$t('message.apiServices.servicesSubmit.importance')">
            <Select v-model="formData.importance">
              <Option value="1">{{ $t('message.apiServices.servicesSubmit.importanceOptions.high') }}</Option>
              <Option value="2">{{ $t('message.apiServices.servicesSubmit.importanceOptions.medium') }}</Option>
              <Option value="3">{{ $t('message.apiServices.servicesSubmit.importanceOptions.low') }}</Option>
            </Select>
          </FormItem>
          <FormItem
            prop="backgroundDesc"
            :label="$t('message.apiServices.servicesSubmit.background')">
            <Input v-model="formData.backgroundDesc" type="textarea" />
          </FormItem>
          <FormItem
            prop="attentionUser"
            :label="$t('message.apiServices.servicesSubmit.attentionUser')">
            <Select v-model="formData.attentionUser" multiple filterable>
              <Option v-for="(item, index) in applyUserList" :key="item.name + index" :value="item.name">{{item.name}}</Option>
            </Select>
          </FormItem>
          <template v-if="hasStarRocksApi">
            <FormItem
              prop="developerOwner"
              :label="$t('message.apiServices.servicesSubmit.developerOwner')">
              <Select v-model="formData.developerOwner" filterable :placeholder="$t('message.apiServices.servicesSubmit.developerOwnerTip')">
                <Option v-for="(item, index) in applyUserList" :key="item.name + index" :value="item.name">{{item.name}}</Option>
              </Select>
            </FormItem>
            <FormItem
              prop="productInfo"
              :label="$t('message.apiServices.servicesSubmit.productInfo')">
              <Input v-model="formData.productInfo" :placeholder="$t('message.apiServices.servicesSubmit.productInfoTip')" />
            </FormItem>
          </template>
        </Form>
        <Button style="margin-left:100px" type="primary" @click="confirm" :loading="isConfirmLoading">{{ $t('message.apiServices.servicesSubmit.submit') }}</Button>
        <Button style="margin-left:20px" type="default" @click="cancel">{{ $t('message.apiServices.servicesSubmit.cancel') }}</Button>
      </Col>
      <Col span="15" class="info-detail" v-show="showInfo">
        <div class="title">{{ $t('message.apiServices.servicesSubmit.apiInfo.title') }}
          <SvgIcon style="float:right;padding:2px" @click="toggleInfo()" icon-class="close2"/>
        </div>
        <div class="info-item"><span class="label">{{ $t('message.apiServices.servicesSubmit.apiInfo.englishName') }}</span> {{currentApi.name}}</div>
        <div class="info-item"><span class="label">{{ $t('message.apiServices.servicesSubmit.apiInfo.chineseName') }}</span> {{currentApi.aliasName}}</div>
        <div class="info-item"><span class="label">{{ $t('message.apiServices.servicesSubmit.apiInfo.path') }}</span> {{currentApi.path}}</div>
        <div class="info-item"><span class="label">{{ $t('message.apiServices.servicesSubmit.apiInfo.protocol') }}</span> {{currentApi.protocol === 1 ? $t('message.apiServices.servicesSubmit.protocolOptions.http') : $t('message.apiServices.servicesSubmit.protocolOptions.https') }}</div>
        <div class="info-item"><span class="label">{{ $t('message.apiServices.servicesSubmit.apiInfo.method') }}</span> {{currentApi.method}}</div>
        <div class="info-item"><span class="label">{{ $t('message.apiServices.servicesSubmit.apiInfo.scope') }}</span> {{currentApi.scope === 'grantView' ? $t('message.apiServices.servicesSubmit.scopeOptions.grantView') : ''}}</div>
        <div class="info-item"><span class="label">{{ $t('message.apiServices.servicesSubmit.apiInfo.tag') }}</span> {{currentApi.tag}}</div>
        <div class="info-item"><span class="label">{{ $t('message.apiServices.servicesSubmit.apiInfo.description') }}</span> {{currentApi.description}}</div>
        <div class="info-item"><span class="label">{{ $t('message.apiServices.servicesSubmit.apiInfo.comment') }}</span> {{currentApi.comment}}</div>
        <div class="title" style="margin-top:10px">{{ $t('message.apiServices.servicesSubmit.paramInfoTitle') }}</div>
        <Table :columns="paramInfoColumns" :data="currentApi.params">
        </Table>
      </Col>
    </Row>
  </div>
</template>

<script>
import api from '@dataspherestudio/shared/common/service/api';
import { GetWorkspaceUserManagement } from '@dataspherestudio/shared/common/service/apiCommonMethod.js';
import storage from '@dataspherestudio/shared/common/helper/storage';

export default {
  data() {
    return {
      formData: {
        approvalName: '',
        id: [],
        backgroundDesc: '',
        applyUser: [],
        duration: '',
        importance: '',
        sensitive: '',
        attentionUser: [],
        developerOwner: '', // 开发负责人
        productInfo: '' // 关联的产品信息
      },
      applyUserList: [],
      showInfo: false,
      currentApi: {
        params: []
      },
      // 提交按钮的loading
      isConfirmLoading: false,
      paramInfoColumns: [
        {
          title: 'ID',
          key: 'id'
        },
        {
          title: this.$t('message.scripts.apiPublish.paramTable.paramName'),
          key: 'name'
        },
        {
          title: this.$t('message.scripts.apiPublish.paramTable.displayName'),
          key: 'displayName',
        },
        {
          title: this.$t('message.scripts.apiPublish.paramTable.paramType'),
          key: 'type',
          render: (h, params) => {
            return h('div', this.getType(params.row.type));
          }
        },
        {
          title: this.$t('message.scripts.apiPublish.paramTable.require.title'),
          key: 'required',
          render: (h, params) => {
            return h('div', params.row.required == '1' ? this.$t('message.apiServices.servicesSubmit.booleanOptions.yes') : this.$t('message.apiServices.servicesSubmit.booleanOptions.no'));
          }
        },
        {
          title: this.$t('message.scripts.apiPublish.paramTable.describe'),
          key: 'description',
        }
      ],
      apiList: [],
      needCheck: false,
      formValid: {
        id: [
          {
            type: 'array',
            required: true,
            message: this.$t('message.apiServices.servicesSubmit.validation.apiRequired'),
            trigger: "change",
          }
        ],
        approvalName: [
          {
            required: true,
            message: this.$t('message.apiServices.servicesSubmit.validation.approvalNameRequired'),
            trigger: "blur",
          },
          { message: this.$t('message.apiServices.servicesSubmit.validation.approvalNameMax'), max: 200 }
        ],
        applyUser: [
          {
            type: 'array',
            required: true,
            message: this.$t('message.apiServices.servicesSubmit.validation.userRequired'),
            trigger: "change",
          }
        ],
        duration: [
          {
            required: true,
            validator: (rule, value, callback) => {
              value = value.trim()
              if (value === '*') {
                callback()
              }
              if (!value) {
                return callback(new Error(this.$t('message.apiServices.servicesSubmit.validation.durationRequired')))
              }
              if (value <=0 || value > 7300 || value % 1 !== 0) {
                callback(new Error(this.$t('message.apiServices.servicesSubmit.validation.durationRange')))
              }
              return callback()
            }
          }
        ],
        sensitive: [
          {
            required: true,
            trigger: "change",
            validator: (rule, value, callback) => {
              value = value.trim()
              if (!value) {
                return callback(new Error(this.$t('message.apiServices.servicesSubmit.validation.sensitiveRequired')))
              }
              if (value == '1') {
                callback(new Error(this.$t('message.apiServices.servicesSubmit.validation.sensitiveError')))
              }
              return callback()
            }
          }
        ],
        importance: [
          {
            required: true,
            message: this.$t('message.apiServices.servicesSubmit.validation.importanceRequired'),
            trigger: "change",
          }
        ],
        backgroundDesc: [
          {
            required: true,
            message: this.$t('message.apiServices.servicesSubmit.validation.backgroundRequired'),
            trigger: "blur",
          },
          { message: this.$t('message.apiServices.servicesSubmit.validation.backgroundMax'), max: 500 }
        ],
        developerOwner: [
          {
            required: this.needCheck,
            validator: (rule, value, callback) => {
              if (this.needCheck && !value) {
                return callback(new Error(this.$t('message.apiServices.servicesSubmit.validation.developerOwnerRequired')));
              }
              return callback();
            },
            trigger: "change"
          }
        ],
        productInfo: [
          {
            required: this.needCheck,
            validator: (rule, value, callback) => {
              if (this.needCheck && !value) {
                return callback(new Error(this.$t('message.apiServices.servicesSubmit.validation.productInfoRequired')));
              }
              return callback();
            },
            trigger: "blur"
          }
        ]

      }
    }
  },
  computed: {
    selectedApi() {
      return this.formData.id.map(it => {
        return {
          id: it.split('_-_')[0],
          name: it.split('_-_')[1],
        }
      })
    },
    // 检测是否选择了包含StarRocks的API
    hasStarRocksApi() {
      const apiInfos = []
      const hasJDBC =  this.selectedApi.some(api => {
        const apiDetail = this.apiList.find(item => item.id == api.id);
        if (apiDetail) {
          apiInfos.push({
            apiId: apiDetail.id,
            apiVersionId: apiDetail.latestVersionId
          })
        }
        return apiDetail && apiDetail.type && apiDetail.type.includes('jdbc');
      });
      if (hasJDBC) {
        this.checkApiData(apiInfos) 
      }
      return hasJDBC;
    }
  },
  methods: {
    toggleInfo(info) {
      if (info) {
        this.currentApi = {
          ...this.apiList.find(it => it.id == info.id)
        }
        this.showInfo = true
      } else {
        this.currentApi = {
          params: []
        }
        this.showInfo = false
      }
    },
    getApiData() {
      api.fetch('/dss/apiservice/availableSubmitApi', {
        workspaceId: this.$route.query.workspaceId
      }, 'get').then((res) => {
        this.apiList = res.availableSubmitApiList;
      }).finally(()=> {
      })
    },
    checkApiData(submitApiInfos) {
      api.fetch('/dss/apiservice/checkSubmitApi', {
        submitApiInfos        
      }, 'post').then((res) => {
        this.needCheck = res.data && res.data.status == 0
      }).finally(()=> {
      })
    },
    confirm() {
      this.$refs.submitForm.validate(valid=>{
        if(valid) {
          const submitApiInfos = this.formData.id.map(it => {
            const api = this.apiList.find(item => item.id == it.split('_-_')[0])
            return api && {
              apiId: api.id,
              apiVersionId: api.latestVersionId
            }
          })
          const params = {
            approvalName: this.formData.approvalName,
            backgroundDesc: this.formData.backgroundDesc,
            applyUser: this.formData.applyUser.join(','),
            duration: this.formData.duration.trim() === '*' ? '*' : this.formData.duration.trim() - 0 + '',
            importance: this.formData.importance,
            sensitive: this.formData.sensitive,
            attentionUser: this.formData.attentionUser.join(','),
            creator: this.getUserName(),
            submitApiInfos,
            workspaceId: this.$route.query.workspaceId
          }
          
          // 如果选择了包含StarRocks的API，添加额外字段
          if (this.hasStarRocksApi) {
            params.devPrincipals = this.formData.developerOwner;
            params.productInfo = this.formData.productInfo;
          }
          this.isConfirmLoading = true
          api.fetch('/dss/apiservice/submit', params, 'post').then(() => {
            this.$Message.success(this.$t('message.apiServices.servicesSubmit.submitSuccess'))
            this.isConfirmLoading = false
            this.$router.push({ name: 'Apiservices', query: { workspaceId: this.$route.query.workspaceId} })
          }).catch(() => {
            this.isConfirmLoading = false
          })
        }
      })
    },
    cancel() {
      this.$refs.submitForm.resetFields();
      this.goBack();
    },
    delectSelect(item) {
      this.showInfo = false;
      this.formData.id = this.formData.id.filter(it => {
        return it.split('_-_')[0] !== item.id
      })
    },
    getType(type) {
      switch (type) {
        case '1':
          return 'String';
        case '2':
          return 'Number';
        case '3':
          return 'Date';
        case '4':
          return 'Array';
        default:
          return '';
      }
    },
    getApplyUserList() {
      if (this.$route.query.workspaceId) {
        GetWorkspaceUserManagement( {
          workspaceId: this.$route.query.workspaceId
        }).then((res) => {
          this.applyUserList = [];
          let userMap = {};
          res.workspaceUsers.forEach((item) => {
            if (item.name && !userMap[item.name]) {
              this.applyUserList.push(item);
              userMap[item.name] = 1;
            }
          })
          userMap = null;
        })
      }
    },
    goBack() {
      this.$router.go(-1)
    },
    getUserName() {
      return storage.get("baseInfo", "local")
        ? storage.get("baseInfo", "local").username
        : '';
    }
  },
  mounted() {
    this.getApiData()
    this.getApplyUserList()
  }
}
</script>
<style lang="scss" scoped>
@import '@dataspherestudio/shared/common/style/variables.scss';

.submit-wrapper {
  height: 100%;
  padding: 20px;
  background: #eee;
}
.main {
  background: #fff;
  padding: 20px;
  margin-top: 10px;
  height: calc(100% - 30px);
  overflow: auto;
}
.title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}
.info-item {
  padding: 4px 0;
}
.label {
  display: inline-block;
  width: 120px;
}
.info-detail {
  padding: 20px;
  position: fixed;
  right: 10px;
  bottom: 0;
  border: 1px solid #eee;
  box-shadow: 0 0 5px 3px #eee;
  background: #fff;
  top: 59px;
  z-index: 999;
  overflow: auto;
}
.submit-wrapper {
  .api-select {
    ::v-deep .ivu-select-multiple .ivu-tag-checked {
      display: none;
    }
  }

  .active {
    ::v-deep .ivu-tag-text, ::v-deep .ivu-icon {
      color: $primary-color;
    }
  }
}
.tag-list {
  margin-top: 5px;
}

.form-item-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

</style>
