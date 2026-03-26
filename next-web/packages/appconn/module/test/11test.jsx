import { computed, defineComponent } from 'vue';
import { FButton, FModal, FForm } from '@fesjs/fes-design';
import { WTable, WInput, WInputNumber } from '@webank/fes-design-material';
import { useSharedLetgoGlobal } from '../../letgo/useLetgoGlobal';
import { useInstance } from '../../letgo/useInstance';
import { useTemporaryState } from '../../letgo/useTemporaryState';
import { useJSQuery } from '../../letgo/useJSQuery';
import { letgoRequest } from '../../letgo/letgoRequest';

export default defineComponent({
  name: 'Test',
  setup() {
    const { utils } = useSharedLetgoGlobal();
    const [editAppConnInstanceFormRefRefEl, editAppConnInstanceFormRef] =
      useInstance();

    const getAppConnInstancesParams = useTemporaryState({
      id: 'getAppConnInstancesParams',
      initValue: {},
    });

    function successTip(message) {
      utils.FMessage.success(message);
    }

    const getAppConnInstances = useJSQuery({
      id: 'getAppConnInstances',
      query(params) {
        return letgoRequest(
          '/api/rest_j/v1/dss/framework/project/appconn/getAppConnInstances',
          params,
          {
            method: 'GET',
          }
        );
      },
      params: computed(() => ({
        ...getAppConnInstancesParams.value,
      })),
      enableTransformer: true,
      transformer(data) {
        return data.data.appConnIntances;
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
      initValue: {},
    });

    const editAppConnInstance = useJSQuery({
      id: 'editAppConnInstance',
      query(params) {
        return letgoRequest(
          '/api/rest_j/v1/dss/framework/project/appconn/editAppConnInstance',
          params,
          {
            method: 'POST',
          }
        );
      },
      params: computed(() => getAppConnInstancesActiveRow.value),
      enableTransformer: true,
      transformer(data) {
        return data.data;
      },

      runCondition: 'manual',

      successEvent: [
        (...args) => visibleUpdateModal.setValue(false),
        (...args) => getAppConnInstances.trigger(...args),
        (...args) => successTip('修改成功', ...args),
      ],
    });

    const editAppConnInstanceRule = useTemporaryState({
      id: 'editAppConnInstanceRule',
      initValue: {
        label: [{ type: 'string', required: true, message: '不能为空' }],
        id: [{ type: 'string', required: true, message: '不能为空' }],
        url: [{ type: 'string', required: true, message: '不能为空' }],
        enhanceJson: [{ type: 'string', required: true, message: '不能为空' }],
        homepageUri: [{ type: 'string', required: true, message: '不能为空' }],
        appConnId: [{ type: 'number', required: true, message: '不能为空' }],
      },
    });

    function showUpdateModal() {
      editAppConnInstanceFormRef.clearValidate?.();
      visibleUpdateModal.value = true;
    }

    function submitEditAppConnInstance() {
      editAppConnInstanceFormRef.validate().then(() => {
        editAppConnInstance.trigger();
      });
    }

    const wTable1Columns6RenderSlots = (slotProps) => {
      return (
        <FButton
          type={`link`}
          onClick={[
            (...args) => showUpdateModal(...args),
            (...args) =>
              getAppConnInstancesActiveRow.setValue({
                ...slotProps.row,
              }),
          ]}
        >
          编辑
        </FButton>
      );
    };

    return () => {
      return (
        <div
          class="letgo-page"
          style={{
            height: '100%',
            padding: '20px',
          }}
        >
          <FButton>按钮</FButton>
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
                prop: 'appConnId',
                label: 'AppConn的ID',
              },
              {
                prop: '',
                label: '操作',
                align: 'center',
                render: wTable1Columns6RenderSlots,
              },
            ]}
            data={[{}]}
            loading={getAppConnInstances.loading}
            remote
            pagination={{
              alwayShow: true,
              pageSize: 20,
              showTotal: true,
            }}
          />
          <FModal
            v-model:show={visibleUpdateModal.value}
            width={720}
            title={`编辑`}
            onOk={[(...args) => submitEditAppConnInstance(...args)]}
          >
            <FForm
              ref={editAppConnInstanceFormRefRefEl}
              labelWidth={110}
              model={getAppConnInstancesActiveRow.value}
              layout={`horizontal`}
              rules={editAppConnInstanceRule.value}
              labelPosition={`right`}
            >
              <WInput
                placeholder={`label`}
                prop={`label`}
                _label={`label`}
                v-model={getAppConnInstancesActiveRow.value.label}
              />
              <WInput
                placeholder={`id`}
                prop={`id`}
                _label={`id`}
                v-model={getAppConnInstancesActiveRow.value.id}
              />
              <WInput
                placeholder={`url`}
                prop={`url`}
                _label={`url`}
                v-model={getAppConnInstancesActiveRow.value.url}
              />
              <WInput
                placeholder={`enhanceJson`}
                prop={`enhanceJson`}
                _label={`enhanceJson`}
                v-model={getAppConnInstancesActiveRow.value.enhanceJson}
              />
              <WInput
                placeholder={`homepageUri`}
                prop={`homepageUri`}
                _label={`homepageUri`}
                v-model={getAppConnInstancesActiveRow.value.homepageUri}
              />
              <WInputNumber
                placeholder={`appConnId`}
                prop={`appConnId`}
                _label={`appConnId`}
                v-model={getAppConnInstancesActiveRow.value.appConnId}
              />
            </FForm>
          </FModal>
        </div>
      );
    };
  },
});
