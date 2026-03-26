import { computed, defineComponent } from 'vue';
import { defineRouteMeta } from '@fesjs/fes';
import { BTablePage, BSearch } from '@fesjs/traction-widget';
import {
  FForm,
  FButton,
  FTable,
  FSpace,
  FTooltip,
  FPagination,
} from '@fesjs/fes-design';
import { WInput } from '@webank/fes-design-material';
import { useTemporaryState } from '../letgo/useTemporaryState';
import { useJSQuery } from '../letgo/useJSQuery';
import { letgoRequest } from '../letgo/letgoRequest';

defineRouteMeta({
  name: 'testa',
  title: '查询节点',
});

export default defineComponent({
  name: 'QueryNode',
  setup() {
    const searchForm = useTemporaryState({
      id: 'searchForm',
      initValue: {},
    });

    const nodeId = useTemporaryState({
      id: 'nodeId',
      initValue: null,
    });

    const tableShowLists = useTemporaryState({
      id: 'tableShowLists',
      initValue: [],
    });

    const isLoading = useTemporaryState({
      id: 'isLoading',
      initValue: false,
    });

    const getNodeData = useJSQuery({
      id: 'getNodeData',
      query(params) {
        return letgoRequest(
          '/api/rest_j/v1/dss/framework/appconnmanager/getnode',
          params,
          {
            method: 'GET',
          }
        );
      },
      params: computed(() => searchForm.value),

      queryTimeout: 10000,

      cacheType: 'ram',
      runCondition: 'manual',
    });

    const actionType = useTemporaryState({
      id: 'actionType',
      initValue: 'loading',
    });

    const deleteNodeData = useJSQuery({
      id: 'deleteNodeData',
      query(params) {
        return letgoRequest(
          '/api/rest_j/v1/dss/framework/appconnmanager/deletenode',
          params,
          {
            method: 'POST',
          }
        );
      },
      params: computed(() => ({ nodeId: nodeId.value })),

      queryTimeout: 10000,

      cacheType: 'ram',
      runCondition: 'manual',
    });

    const fetchTableData = async () => {
      if (isLoading.value) {
        return;
      }
      isLoading.value = true;
      actionType.value = 'loading';
      tableShowLists.value = [];
      const { pageNum, pageSize } = pagination;
      try {
        const data = getNodeData.trigger();
        tableShowLists.value = data.nodeList;
        isLoading.value = false;
        pagination.total = res.totalCount;
        if (pagination.total === 0) {
          actionType.value = 'emptyQueryResult';
        }
      } catch (err) {
        isLoading.value = false;
        console.log(err);
      }
    };

    function confirmDelete() {
      deleteNodeData.trigger();
    }

    function deleteNode(slotProps) {
      nodeId.value = slotProps.row.nodeId;
    }

    const fetchTableData = async () => {
      if (isLoading.value) {
        return;
      }
      isLoading.value = true;
      actionType.value = 'loading';
      tableShowLists.value = [];
      const { pageNum, pageSize } = pagination;
      try {
        const data = getnodeData.trigger();
        tableShowLists.value = data.nodeList;
        isLoading.value = false;
        pagination.total = res.totalCount;
        if (pagination.total === 0) {
          actionType.value = 'emptyQueryResult';
        }
      } catch (err) {
        isLoading.value = false;
        console.log(err);
      }
    };

    function handleSearch() {
      pagination.pageNum = 1;
      fetchTableData();
    }

    const fTable1Columns10RenderSlots = (slotProps) => {
      return (
        <FSpace inline={false}>
          <FButton
            style={{
              height: '22px',
              padding: '0px',
              minWidth: '28px',
            }}
            type={`link`}
            size={`middle`}
            disabled
          >
            详情
          </FButton>
          <FButton
            style={{
              height: '22px',
              padding: '0px',
              minWidth: '28px',
            }}
            type={`link`}
            size={`middle`}
          >
            编辑
          </FButton>
          <FTooltip
            modelValue
            mode={`confirm`}
            placement={`bottom-start`}
            arrow
            popperClass={``}
            onOk={[(...args) => confirmDelete(...args)]}
            v-slots={{
              content: () => {
                return (
                  <div
                    style={{
                      width: '400px',
                    }}
                  >
                    <span>
                      删除节点以后，工作流中对应类型的节点会消失，且工作流打开，执行可能会报错，请确认影响后再删除
                    </span>
                  </div>
                );
              },
              title: () => {
                return (
                  <div
                    style={{
                      width: '400px',
                    }}
                  >
                    <span
                      style={{
                        color: '#0F1222',
                        fontFamily: 'PingFangSC-Semibold',
                        fontWeight: '600',
                      }}
                    >
                      确认删除节点？
                    </span>
                  </div>
                );
              },
            }}
          >
            <FButton
              style={{
                height: '22px',
                padding: '0px',
                minWidth: '28px',
                color: '#F75F56',
              }}
              type={`link`}
              onClick={[(...args) => deleteNode(slotProps, ...args)]}
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
            background: '#fff',
            margin: '0',
            padding: '0',
          }}
        >
          <BTablePage
            isLoading={isLoading.value}
            actionType={actionType.value}
            isDivider
            v-slots={{
              search: () => {
                return (
                  <BSearch
                    v-model:form={searchForm.value}
                    isAdvance={false}
                    v-model:advanceForm={advanceSearchForm.value}
                    style={{
                      height: '60px',
                    }}
                    isAdvanceCount={false}
                    isReset
                    onSearch={[(...args) => handleSearch(data, ...args)]}
                    onReset={[(...args) => handleReset(...args)]}
                    onAdvance={[(...args) => toggleAdvanceQuery(...args)]}
                    v-slots={{
                      form: () => {
                        return (
                          <FForm
                            labelWidth={220}
                            model={searchForm.value}
                            layout={`inline`}
                            labelPosition={`left`}
                            showMessage
                            style={{
                              width: '250px',
                              display: 'flex',
                              flexDirection: 'row',
                              justifyContent: 'space-between',
                              alignItems: 'flex-start',
                            }}
                            span={11}
                            inlineItemGap={6}
                          >
                            <WInput
                              _label={`节点名称`}
                              placeholder={`请输入`}
                              prop={`name`}
                              v-model={searchForm.value.name}
                              labelWidth={`60`}
                              span={12}
                              style={{
                                width: '235px',
                                height: '32px',
                              }}
                            />
                          </FForm>
                        );
                      },
                    }}
                  />
                );
              },
              operate: () => {
                return (
                  <FButton
                    type={`primary`}
                    onClick={[
                      (...args) => toAddNodePage(...args),
                      (...args) => toAddNodePage(...args),
                    ]}
                  >
                    新增节点
                  </FButton>
                );
              },
              table: () => {
                return (
                  <FTable
                    columns={[
                      {
                        prop: 'name',
                        label: '节点系统名称',
                        width: 120,
                      },
                      {
                        prop: 'nodeGroup',
                        label: '节点分类',
                        width: 102,
                      },
                      {
                        prop: 'appconnName',
                        label: '关联AppConn',
                        width: 200,
                      },
                      {
                        prop: 'iconPath',
                        label: '节点图标',
                        width: 150,
                      },
                      {
                        prop: 'supportJump',
                        label: '是否支持跳转',
                        width: 116,
                      },
                      {
                        prop: 'jumpType',
                        label: '跳转类型',
                        width: 88,
                      },
                      {
                        label: '是否支持发布',
                        prop: 'shubmitToScheduler',
                        width: 116,
                      },
                      {
                        prop: 'enableCopy',
                        label: '是否支持拷贝',
                        width: 116,
                      },
                      {
                        prop: 'shouldCreationBeforeNode',
                        label: '是否需要弹窗',
                        width: 116,
                      },
                      {
                        label: '节点属性',
                        width: 88,
                      },
                      {
                        label: '操作',
                        width: 148,
                        fixed: 'right',
                        render: fTable1Columns10RenderSlots,
                      },
                    ]}
                    data={tableShowLists.value}
                  />
                );
              },
              pagination: () => {
                return (
                  <FPagination
                    totalCount={pager.value.total}
                    showQuickJumper
                    showTotal
                    showSizeChanger
                    style={{
                      display: 'flex',
                      flexDirection: 'row',
                      justifyContent: 'flex-end',
                    }}
                    v-model:currentPage={pager.value.page}
                    v-model:pageSize={pager.value.size}
                    onChange={[(...args) => handleTablePageChange(...args)]}
                  />
                );
              },
            }}
          />
        </div>
      );
    };
  },
});
