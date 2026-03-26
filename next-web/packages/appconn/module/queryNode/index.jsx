import { computed, ref, defineComponent } from 'vue';
import { BTablePage, BSearch } from '@fesjs/traction-widget';
import {
  FForm,
  FButton,
  FTable,
  FSpace,
  FModal,
  FMessage,
  FPagination,
  FPopper,
} from '@fesjs/fes-design';
import { WInput } from '@webank/fes-design-material';
import { useTemporaryState } from '../../letgo/useTemporaryState';
import { getNodeGroupList } from '../addNode/api';

import { useRouter, useRoute } from 'vue-router';
import { request } from '@dataspherestudio/shared';

export default defineComponent({
  name: 'QueryNode',
  setup() {
    const router = useRouter();
    const route = useRoute();

    const searchForm = useTemporaryState({
      id: 'searchForm',
      initValue: {},
    });

    const nodeId = useTemporaryState({
      id: 'nodeId',
      initValue: null,
    });
    const pagination = useTemporaryState({
      id: 'pagination',
      initValue: {
        pageNum: 1,
        pageSize: 10,
        total: 0,
      },
    });
    const nodeGroupList = ref([]);
    getNodeGroupList().then((res) => {
      nodeGroupList.value = res.map((item) => ({
        label: item.name,
        value: item.id,
      }));
    });
    const tableShowLists = useTemporaryState({
      id: 'tableShowLists',
      initValue: [],
    });

    const isLoading = useTemporaryState({
      id: 'isLoading',
      initValue: false,
    });

    const actionType = useTemporaryState({
      id: 'actionType',
      initValue: 'loading',
    });

    const getNodeData = (params = {}) => {
      return request
        .fetch(
          'dss/framework/appconnmanager/getnode',
          {
            nodeName: params.name,
          },
          'get'
        )
        .then((res) => {
          if (!res) {
            return {};
          }
          return res.data;
        })
        .catch((error) => {
          //
        });
    };

    const deleteNodeData = (params = {}) => {
      return request
        .fetch(
          'dss/framework/appconnmanager/deletenode',
          {
            nodeId: params.id,
          },
          'post'
        )
        .then((res) => {
          FMessage.success('删除成功');
          handleSearch();
        })
        .catch((error) => {
          console.log(error);
        });
    };

    const fetchTableData = async (data) => {
      if (isLoading.value) {
        return;
      }
      isLoading.value = true;
      actionType.value = 'loading';
      tableShowLists.value = [];
      const { pageNum, pageSize } = pagination.value;
      try {
        const res = await getNodeData({ ...data });
        pagination.value.total = res.total;
        const startIndex = (pageNum - 1) * pageSize;
        const endIndex = Math.min(
          startIndex + pageSize,
          pagination.value.total
        );
        tableShowLists.value = res.nodeList.slice(startIndex, endIndex);
        isLoading.value = false;
        if (pagination.value.total === 0) {
          actionType.value = 'emptyQueryResult';
        }
      } catch (err) {
        isLoading.value = false;
        console.log(err);
      }
    };

    function handleSearch() {
      pagination.value.pageNum = 1;
      fetchTableData(searchForm.value);
    }
    function handleReset() {
      pagination.value.pageNum = 1;
      fetchTableData();
    }
    function toAddNodePage(row) {
      router.push({
        path: '/appconn/addNode',
        query: {
          id: row?.id,
          name: row.name,
          appConnId: route.query.id,
          appConnName: route.query.appConnName,
        },
      });
    }
    function toEditNodePage(row) {
      router.push({
        path: '/appconn/editNode',
        query: {
          id: row?.id,
          name: row.name,
          appConnId: route.query.id,
          appConnName: route.query.appConnName,
        },
      });
    }

    fetchTableData();

    const onDelNode = (row) => {
      FModal.confirm({
        title: '提示',
        content: `删除节点以后，工作流中对应类型的节点会消失，且工作流打开，执行可能会报错，请确认影响后再删除`,
        okText: '确定',
        cancelText: '取消',
        closable: true,
        onOk: async () => {
          try {
            await deleteNodeData(row);
          } catch (err) {
            console.error(err);
          }
        },
      });
    };

    const fTable1Columns10RenderSlots = (slotProps) => {
      return (
        <FSpace inline={false}>
          <FButton
            style={{
              height: '22px',
              padding: '0px',
              minWidth: '28px',
            }}
            onClick={[
              () =>
                toEditNodePage({
                  ...slotProps.row,
                }),
            ]}
            type={`link`}
            size={`middle`}
          >
            编辑
          </FButton>
          <FButton
            type={`link`}
            style={{
              height: '22px',
              padding: '0px',
              minWidth: '28px',
              color: 'red',
            }}
            onClick={[
              (...args) =>
                onDelNode({
                  ...slotProps.row,
                }),
            ]}
          >
            删除
          </FButton>
        </FSpace>
      );
    };
    const nodeUIParams = ref([]);
    const getNodeAttrView = (row) => {
      nodeUIParams.value = [];
      return request
        .fetch(
          'dss/framework/appconnmanager/getui',
          {
            nodeId: row.id,
          },
          'get'
        )
        .then((res) => {
          if (res && res.data && res.data.uiList) {
            nodeUIParams.value = res.data.uiList.map((it) => {
              return {
                label: it.lableName,
                text: it.required ? '必填' : '非必填',
              };
            });
          }
        });
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
                    style={{
                      height: '60px',
                    }}
                    isAdvanceCount={false}
                    isReset
                    onSearch={[(...args) => handleSearch(...args)]}
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
                    onClick={[(...args) => toAddNodePage(...args)]}
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
                        render: (slotProps) => {
                          const g = nodeGroupList.value.find(
                            (it) => it.value == slotProps.row.nodeGroup
                          );
                          return (g && g.label) || '';
                        },
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
                        render: (slotProps) => {
                          return slotProps.row.supportJump == 1 ? '是' : '否';
                        },
                      },
                      {
                        prop: 'jumpType',
                        label: '跳转类型',
                        width: 88,
                        render: (slotProps) => {
                          return slotProps.row.jumpType == 1
                            ? '外部节点'
                            : '内部节点';
                        },
                      },
                      {
                        label: '是否支持发布',
                        prop: 'submitToScheduler',
                        width: 116,
                        render: (slotProps) => {
                          return slotProps.row.submitToScheduler == 1
                            ? '是'
                            : '否';
                        },
                      },
                      {
                        prop: 'enableCopy',
                        label: '是否支持拷贝',
                        width: 116,
                        render: (slotProps) => {
                          return slotProps.row.enableCopy == 1 ? '是' : '否';
                        },
                      },
                      {
                        prop: 'shouldCreationBeforeNode',
                        label: '是否需要弹窗',
                        width: 116,
                        render: (slotProps) => {
                          return slotProps.row.shouldCreationBeforeNode == 1
                            ? '是'
                            : '否';
                        },
                      },
                      {
                        label: '节点属性',
                        width: 88,
                        render: (slotProps) => {
                          const slot = {
                            trigger: () => {
                              return (
                                <FButton
                                  type="link"
                                  onClick={() => getNodeAttrView(slotProps.row)}
                                >
                                  查看
                                </FButton>
                              );
                            },
                          };
                          return (
                            <FPopper
                              placement="bottom"
                              trigger="click"
                              v-slots={slot}
                              arrow={true}
                            >
                              <div style="padding: 15px">
                                {nodeUIParams.value.map((it) => {
                                  return (
                                    <p>
                                      {it.label}: {it.text}
                                    </p>
                                  );
                                })}
                              </div>
                            </FPopper>
                          );
                        },
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
                    totalCount={pagination.value.total}
                    showTotal
                    showSizeChanger
                    style={{
                      display: 'flex',
                      flexDirection: 'row',
                      justifyContent: 'flex-end',
                    }}
                    v-model:currentPage={pagination.value.pageNum}
                    v-model:pageSize={pagination.value.pageSize}
                    onChange={[(...args) => fetchTableData(...args)]}
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
