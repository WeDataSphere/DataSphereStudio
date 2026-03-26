import { computed, defineComponent } from 'vue';
import {
  FForm,
  FFormItem,
  FButton,
  FRadioGroup,
  FRadio,
  FDrawer,
  FModal,
  FMessage,
} from '@fesjs/fes-design';
import { WInput, WTable } from '@webank/fes-design-material';
import { useSharedLetgoGlobal } from '../../letgo/useLetgoGlobal';
import { useInstance } from '../../letgo/useInstance';
import { useTemporaryState } from '../../letgo/useTemporaryState';
import { useJSQuery } from '../../letgo/useJSQuery';
import { useRoute } from 'vue-router';

import { request } from '@dataspherestudio/shared';

export default defineComponent({
  name: 'AppConnInstance',
  setup() {
    const route = useRoute();

    const { utils } = useSharedLetgoGlobal();
    const [editAppConnInstanceFormRefRefEl, editAppConnInstanceFormRef] =
      useInstance();

    function successTip(message) {
      utils.FMessage.success(message);
    }

    const getAppConnInstances = useJSQuery({
      id: 'getAppConnInstances',
      query(params) {
        return request.fetch({
          url: 'dss/framework/project/appconn/getAppConnInstances',
          params,
        });
      },
      params: computed(() => ({
        appConnId: route.query.id,
      })),
      enableTransformer: true,
      transformer(data) {
        return data.appConnInstances;
      },

      runCondition: 'manual',
      runWhenPageLoads: true,
    });

    const visibleUpdateModal = useTemporaryState({
      id: 'visibleUpdateModal',
      initValue: false,
    });

    const getAppConnInstancesActiveRow = useTemporaryState({
      id: 'getAppConnInstancesActiveRow',
      initValue: {
        label: 'DEV',
      },
    });

    const editAppConnInstance = useJSQuery({
      id: 'editAppConnInstance',
      query(params) {
        let url = 'addAppConnInstance';
        if (params.id) {
          url = 'editAppConnInstance';
        }
        return request.fetch({
          url: `dss/framework/project/appconn/${url}`,
          data: params,
          method: 'POST',
        });
      },
      params: computed(() => {
        return {
          appConnId: route.query.id,
          homepageUri: getAppConnInstancesActiveRow.value.homepageUri,
          url: getAppConnInstancesActiveRow.value.url,
          label: getAppConnInstancesActiveRow.value.label,
          enhanceJson: getAppConnInstancesActiveRow.value.enhanceJson,
          id: getAppConnInstancesActiveRow.value.id,
        };
      }),
      enableTransformer: true,
      transformer(data) {
        return data;
      },

      runCondition: 'manual',

      successEvent: [
        (...args) => visibleUpdateModal.setValue(false),
        (...args) => getAppConnInstances.trigger(...args),
        (...args) => successTip('保存成功', ...args),
      ],
    });
    const validateJson = (rule, value, callback) => {
      if (value) {
        try {
          JSON.parse(value);
          callback();
        } catch (error) {
          callback(new Error('JSON格式错误'));
        }
      } else {
        callback();
      }
    };

    const editAppConnInstanceRule = useTemporaryState({
      id: 'editAppConnInstanceRule',
      initValue: {
        label: [{ type: 'string', required: true, message: '不能为空' }],
        id: [{ type: 'string', required: true, message: '不能为空' }],
        url: [{ type: 'string', required: true, message: '不能为空' }],
        enhanceJson: [{ validator: validateJson, trigger: 'blur' }],
      },
    });

    function showUpdateModal() {
      editAppConnInstanceFormRef.clearValidate?.();
      visibleUpdateModal.value = true;
    }

    function showAddModal() {
      visibleUpdateModal.value = true;
      editAppConnInstanceFormRef.clearValidate?.();
      getAppConnInstancesActiveRow.setValue({
        id: '',
        label: 'DEV',
        url: '',
        enhanceJson: '',
        homepageUri: '',
      });
    }

    const deleteAppconInstances = (id) => {
      const data = new FormData();
      data.append('id', id);
      return request
        .fetch({
          url: 'dss/framework/project/appconn/deleteAppConnInstance',
          method: 'post',
          data,
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        })
        .then((res) => {
          if (res.data && res.data.status == 0) {
            FMessage.success('删除成功');
          }
        });
    };

    function showDeleteModal() {
      FModal.confirm({
        title: '提示',
        content: `删除实例后，用户将无法通过菜单跳转至该实例，且可能会影响工作流的执行与调度，确认删除实例【${getAppConnInstancesActiveRow.value.id}】？`,
        okText: '确定',
        cancelText: '取消',
        closable: true,
        onOk: async () => {
          try {
            await deleteAppconInstances(getAppConnInstancesActiveRow.value.id);
            await getAppConnInstances.trigger();
            FMessage.success('删除成功');
          } catch (err) {
            console.error(err);
          }
        },
      });
    }

    function submitEditAppConnInstance() {
      editAppConnInstanceFormRef.validate().then(() => {
        editAppConnInstance.trigger();
      });
    }

    const wTable1Columns6RenderSlots = (slotProps) => {
      return (
        <div>
          <FButton
            type={`link`}
            onClick={[
              (...args) => showUpdateModal(...args),
              (...args) => {
                getAppConnInstancesActiveRow.setValue({
                  ...slotProps.row,
                });
              },
            ]}
          >
            编辑
          </FButton>
          <FButton
            type={`link`}
            onClick={[
              (...args) =>
                getAppConnInstancesActiveRow.setValue({
                  ...slotProps.row,
                }),
              (...args) => showDeleteModal(...args),
            ]}
          >
            删除
          </FButton>
        </div>
      );
    };

    return () => {
      return (
        <div
          class="letgo-page"
          style={{
            height: '100%',
            padding: '20px',
            background: '#fff',
          }}
        >
          <FButton
            style={{
              marginBottom: '20px',
            }}
            type={`primary`}
            onClick={[(...args) => showAddModal(...args)]}
          >
            新增实例
          </FButton>
          <WTable
            columns={[
              {
                prop: 'id',
                label: 'ID',
              },
              {
                prop: 'label',
                label: '标签',
              },
              {
                prop: 'url',
                label: 'URL',
              },
              {
                prop: 'enhanceJson',
                label: '额外参数',
              },
              {
                prop: 'homepageUri',
                label: '主页URL',
              },
              {
                prop: '',
                label: '操作',
                align: 'center',
                render: wTable1Columns6RenderSlots,
              },
            ]}
            data={getAppConnInstances.data}
            loading={getAppConnInstances.loading}
            pagination={{
              alwayShow: true,
              pageSize: 20,
              totalCount: getAppConnInstances.data?.length,
              showTotal: true,
            }}
            onChange={(page, pageSize) => {
              console.log(page, pageSize);
            }}
          />
          <FDrawer
            v-model:show={visibleUpdateModal.value}
            width={720}
            footer={true}
            title={
              getAppConnInstancesActiveRow.value.id ? `新建实例` : `新增实例`
            }
            onOk={[(...args) => submitEditAppConnInstance(...args)]}
          >
            <FForm
              ref={editAppConnInstanceFormRefRefEl}
              labelWidth={110}
              model={getAppConnInstancesActiveRow.value}
              layout={`horizontal`}
              rules={editAppConnInstanceRule.value}
            >
              <FFormItem label={`实例标签`} prop={`label`}>
                <FRadioGroup v-model={getAppConnInstancesActiveRow.value.label}>
                  <FRadio labelWidth={40} label={`DEV`} value={`DEV`} />
                  <FRadio labelWidth={40} label={`PROD`} value={`PROD`} />
                </FRadioGroup>
              </FFormItem>

              <WInput
                placeholder={`请输入URL`}
                prop={`url`}
                _label={`URL`}
                v-model={getAppConnInstancesActiveRow.value.url}
              />
              <WInput
                placeholder={`请输入主页URL`}
                prop={`homepageUri`}
                _label={`主页URL`}
                v-model={getAppConnInstancesActiveRow.value.homepageUri}
              />
              <WInput
                placeholder={`请输入额外参数`}
                prop={`enhanceJson`}
                _label={`额外参数`}
                v-model={getAppConnInstancesActiveRow.value.enhanceJson}
              />
            </FForm>
          </FDrawer>
        </div>
      );
    };
  },
});
