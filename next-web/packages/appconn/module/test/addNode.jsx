import { computed, defineComponent } from 'vue';
import { defineRouteMeta } from '@fesjs/fes';
import { FForm, FButton, FSpace, FTooltip } from '@fesjs/fes-design';
import {
  WInput,
  WSelect,
  WRadioGroup,
  WTable,
} from '@webank/fes-design-material';
import { useInstance } from '../letgo/useInstance';
import { useTemporaryState } from '../letgo/useTemporaryState';
import { useJSQuery } from '../letgo/useJSQuery';
import { letgoRequest } from '../letgo/letgoRequest';

defineRouteMeta({
  name: 'test',
  title: '新增节点',
});

export default defineComponent({
  name: 'AddNode',
  setup() {
    const [fForm1RefEl, fForm1] = useInstance();

    const nodeId = useTemporaryState({
      id: 'nodeId',
      initValue: null,
    });

    const nodeForm = useTemporaryState({
      id: 'nodeForm',
      initValue: {},
    });

    const unbindAttributeApi = useJSQuery({
      id: 'unbindAttributeApi',
      query(params) {
        return letgoRequest(
          '/api/rest_j/v1/dss/framework/appconnmanager/nodedeleteui',
          params,
          {
            method: 'POST',
          }
        );
      },
      params: computed(() => ({
        nodeId: nodeId.value,
        uiId: uiId.value,
      })),

      queryTimeout: 10000,

      cacheType: 'ram',
      runCondition: 'manual',
    });

    const uild = useTemporaryState({
      id: 'uild',
      initValue: null,
    });

    const tableShowLists = useTemporaryState({
      id: 'tableShowLists',
      initValue: {},
    });

    const isLoading = useTemporaryState({
      id: 'isLoading',
      initValue: false,
    });

    const actionType = useTemporaryState({
      id: 'actionType',
      initValue: 'loading',
    });

    const savenode = useJSQuery({
      id: 'savenode',
      query(params) {
        return letgoRequest(
          '/api/rest_j/v1/dss/framework/appconnmanager/savenode',
          params,
          {
            method: 'POST',
          }
        );
      },
      params: computed(() => nodeForm.value),

      queryTimeout: 10000,

      cacheType: 'ram',
      runCondition: 'manual',
    });

    async function resetValidateField() {
      await fForm1?.clearValidate();
    }

    function confirmDeleteAttribute() {
      unbindAttributeApi.trigger();
    }

    function confirmUnbindAttribute() {
      unbindAttributeApi.trigger();
    }

    function handleUnbindAttribute(slotProps) {
      uiId.value = slotProps.row.uiId;
    }

    function handleDeleteAttribute(slotProps) {
      uiId.value = slotProps.row.uiId;
    }

    const deleteAttributeApi = useJSQuery({
      id: 'deleteAttributeApi',
      query(params) {
        return letgoRequest(
          '/api/rest_j/v1/dss/framework/appconnmanager/deleteui',
          params,
          {
            method: 'POST',
          }
        );
      },
      params: computed(() => ({ uild: uild.value })),

      queryTimeout: 10000,

      cacheType: 'ram',
      runCondition: 'manual',
    });

    async function fetchTableData(data) {
      if (isLoading.value) {
        return;
      }
      isLoading.value = true;
      actionType.value = 'loading';
      tableShowLists.value = [];
      const { pageNum, pageSize } = pagination;
      try {
        const res = await getMaskingRuleList({
          pageNum,
          pageSize,
          ...data,
        });
        tableShowLists.value = res.content;
        isLoading.value = false;
        pagination.total = res.totalCount;
        if (pagination.total === 0) {
          actionType.value = 'emptyQueryResult';
        }
      } catch (err) {
        isLoading.value = false;
        console.log(err);
      }
    }

    async function saveNodeBaseInfo() {
      try {
        await fForm1.validate();
        savenode.trigger();
        resetValidateField();
      } catch (err) {
        consol.warn(err);
      }
    }

    const wTable1Columns11RenderSlots = (slotProps) => {
      return (
        <FSpace>
          <FButton
            type={`link`}
            style={{
              minWidth: '28px',
              height: '22px',
              padding: '0px',
            }}
          >
            编辑
          </FButton>
          <FButton
            type={`link`}
            style={{
              minWidth: '28px',
              height: '22px',
              padding: '0px',
            }}
            onClick={[]}
          >
            解绑
          </FButton>
          <FTooltip
            mode={`confirm`}
            content={`属性信息删除后无法恢复`}
            modelValue={false}
            v-slots={{
              title: () => {
                return (
                  <div
                    style={{
                      color: '#0F1222',
                      fontFamily: 'PingFangSC-Semibold',
                      fontWeight: '600',
                    }}
                  >
                    <span>确认删除属性吗？</span>
                  </div>
                );
              },
            }}
          >
            <FButton
              type={`link`}
              size={`middle`}
              style={{
                minWidth: '28px',
                height: '22px',
                padding: '0px',
                color: '#F75F56',
              }}
              onClick={[(...args) => handleDeleteAttribute(slotProps, ...args)]}
            >
              删除
            </FButton>
          </FTooltip>
        </FSpace>
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
          <div>
            <div
              style={{
                marginBottom: '16px',
              }}
            >
              <div
                style={{
                  marginBottom: '16px',
                }}
              >
                <span
                  style={{
                    fontFamily: 'PingFangSC-Medium',
                    fontSize: '14px',
                    color: '#0F1222',
                    lineHeight: '22px',
                    fontWeight: '500',
                  }}
                >
                  属性信息
                </span>
              </div>
              <FForm
                ref={fForm1RefEl}
                labelWidth={98}
                labelPosition={`right`}
                style={{
                  width: '600px',
                }}
                model={nodeForm.value}
                rules={{
                  name: [
                    {
                      required: true,
                      message: '不能为空',
                      trigger: ['change', 'blur'],
                    },
                  ],
                  nodeGroup: [
                    {
                      required: true,
                      message: '不能为空',
                      trigger: ['change', 'blur'],
                    },
                  ],
                  nodeType: [
                    {
                      required: true,
                      message: '不能为空',
                      trigger: ['change', 'blur'],
                    },
                  ],
                  appcoonName: [
                    {
                      required: true,
                      message: '不能为空',
                      trigger: ['change', 'blur'],
                    },
                  ],
                  iconPath: [
                    {
                      required: true,
                      message: '不能为空',
                      trigger: ['change', 'blur'],
                    },
                  ],
                  userCode: [
                    {
                      required: true,
                      message: '不能为空',
                      trigger: ['change', 'blur'],
                    },
                  ],
                  supportJump: [
                    {
                      required: true,
                      type: 'number',
                      message: '不能为空',
                      trigger: 'change',
                    },
                  ],
                  jumpType: [
                    {
                      required: true,
                      type: 'number',
                      message: '不能为空',
                      trigger: 'change',
                    },
                  ],
                  enableCopy: [
                    {
                      required: true,
                      type: 'number',
                      message: '不能为空',
                      trigger: 'change',
                    },
                  ],
                  submitToScheduler: [
                    {
                      required: true,
                      type: 'number',
                      message: '不能为空',
                      trigger: 'change',
                    },
                  ],
                  shouldCreationBeforeNode: [
                    {
                      required: true,
                      type: 'number',
                      message: '不能为空',
                      trigger: 'change',
                    },
                  ],
                }}
                disabled={false}
              >
                <WInput
                  _label={`节点名称`}
                  placeholder={`请输入用户名`}
                  labelWidth={`98`}
                  prop={`name`}
                  v-model={nodeForm.value.name}
                />
                <WSelect
                  _label={`节点分类`}
                  labelWidth={98}
                  options={[
                    {
                      value: 'HuBei',
                      label: '湖北省',
                    },
                    {
                      value: 'GuangDong',
                      label: '广东省',
                    },
                  ]}
                  prop={`nodeGroup`}
                  v-model={nodeForm.value.nodeGroup}
                />
                <WInput
                  _label={`节点全路径名`}
                  type={`password`}
                  placeholder={`请输入密码`}
                  labelWidth={`98`}
                  prop={`nodeType`}
                  v-model={nodeForm.value.nodeType}
                />
                <WSelect
                  _label={`关联AppConn`}
                  labelWidth={98}
                  options={[
                    {
                      value: 'HuBei',
                      label: '湖北省',
                    },
                    {
                      value: 'GuangDong',
                      label: '广东省',
                    },
                  ]}
                  prop={`appcoonName`}
                  v-model={nodeForm.value.appcoonName}
                />
                <WInput
                  _label={`节点图标`}
                  type={`password`}
                  placeholder={`请输入密码`}
                  labelWidth={`98`}
                  prop={`iconPath`}
                  v-model={nodeForm.value.iconPath}
                />
                <WRadioGroup
                  _label={`是否支持跳转`}
                  labelWidth={98}
                  options={[
                    {
                      value: 1,
                      label: '是',
                    },
                    {
                      value: 0,
                      label: '否',
                    },
                  ]}
                  prop={`supportJump`}
                  v-model={nodeForm.value.supportJump}
                />
                <WRadioGroup
                  _label={`跳转类型`}
                  labelWidth={98}
                  options={[
                    {
                      value: 1,
                      label: '内部节点',
                    },
                    {
                      value: 0,
                      label: '外部节点',
                    },
                  ]}
                  prop={`jumpType`}
                  v-model={nodeForm.value.jumpType}
                />
                <WRadioGroup
                  _label={`是否支持拷贝`}
                  labelWidth={98}
                  options={[
                    {
                      value: 1,
                      label: '是',
                    },
                    {
                      value: 0,
                      label: '否',
                    },
                  ]}
                  prop={`enableCopy`}
                  v-model={nodeForm.value.enableCopy}
                />
                <WRadioGroup
                  _label={`是否支持发布`}
                  labelWidth={98}
                  options={[
                    {
                      value: 1,
                      label: '是',
                    },
                    {
                      value: 0,
                      label: '否',
                    },
                  ]}
                  prop={`submitToScheduler`}
                  v-model={nodeForm.value.submitToScheduler}
                />
                <WRadioGroup
                  _label={`是否需要弹窗`}
                  labelWidth={98}
                  options={[
                    {
                      value: 1,
                      label: '是',
                    },
                    {
                      value: 0,
                      label: '否',
                    },
                  ]}
                  prop={`shouldCreationBeforeNode`}
                  v-model={nodeForm.value.shouldCreationBeforeNode}
                />
                <FButton
                  type={`info`}
                  style={{
                    marginLeft: '500px',
                  }}
                  onClick={[(...args) => saveNodeBaseInfo(...args)]}
                >
                  保存基础信息
                </FButton>
              </FForm>
            </div>
            <div>
              <div
                style={{
                  marginBottom: '16px',
                }}
              >
                <span
                  style={{
                    fontFamily: 'PingFangSC-Medium',
                    fontSize: '14px',
                    color: '#0F1222',
                    lineHeight: '22px',
                    fontWeight: '500',
                  }}
                >
                  属性信息
                </span>
              </div>
              <FButton
                type={`info`}
                style={{
                  marginBottom: '16px',
                }}
              >
                添加属性信息
              </FButton>
              <WTable
                columns={[
                  {
                    prop: 'labelName',
                    label: '字段英文名',
                    width: 180,
                  },
                  {
                    prop: 'descriptionEn',
                    label: '字段英文描述',
                    width: 160,
                  },
                  {
                    prop: 'labelName',
                    label: '字段中文名',
                    width: 180,
                  },
                  {
                    prop: 'decriptinon',
                    label: '字段英文描述',
                    width: 160,
                  },
                  {
                    prop: 'uiType',
                    label: '输入类型',
                    width: 102,
                  },
                  {
                    prop: 'required',
                    label: '是否必填',
                    width: 88,
                  },
                  {
                    prop: 'defaultValue',
                    label: '默认值',
                    width: 88,
                  },
                  {
                    prop: 'isHidden',
                    label: '是否隐藏',
                    width: 88,
                  },
                  {
                    prop: 'condition',
                    label: '显示条件',
                    width: 88,
                  },
                  {
                    label: '是否校验',
                    width: 88,
                  },
                  {
                    label: '校验条件',
                    width: 88,
                  },
                  {
                    label: '操作',
                    width: 148,
                    render: wTable1Columns11RenderSlots,
                    fixed: 'right',
                  },
                ]}
                data={tableShowLists}
                rowKey={`id`}
              />
            </div>
          </div>
        </div>
      );
    };
  },
});
