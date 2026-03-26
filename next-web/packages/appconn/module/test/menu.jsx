import { defineComponent, ref } from 'vue';
import {
  FTable,
  FForm,
  FFormItem,
  FInput,
  FButton,
  FModal,
} from '@fesjs/fes-design';
import { useInstance } from '../../letgo/useInstance';
import { useTemporaryState } from '../../letgo/useTemporaryState';

export default defineComponent({
  name: 'Menuxx',
  setup() {
    const [fTableRefEl, fTable] = useInstance();
    const [formRefEl] = useInstance();
    const [deleteInstanceFModalRefEl, deleteInstanceFModal] = useInstance();
    const [confirmDeleteButtonRefEl, confirmDeleteButton] = useInstance();
    const [cancelButtonRefEl, cancelButton] = useInstance();
    function handleEdit(row) {
      selectedInstance.value = row;
      editInstanceModalVisible.value = true;
    }

    const columns = useTemporaryState({
      id: 'columns',
      initValue: [
        { prop: 'id', label: 'ID' },
        { prop: 'label', label: '标签' },
        { prop: 'url', label: 'URL' },
        { prop: 'homepageUri', label: '主页URI' },
        { prop: 'extra', label: '额外参数' },
        {
          prop: 'action',
          label: '操作',
          render: (slotProps) => {
            return (
              <FButton type={`link`} onClick={[]}>
                编辑
              </FButton>
            );
          },
        },
      ],
    });

    function handleSubmit(e) {
      e.preventDefault();
      this.$refs['form'].validate((valid) => {
        if (valid) {
          alert('submit!');
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    }

    const rules = useTemporaryState({
      id: 'rules',
      initValue: {
        id: [{ required: true, message: '请输入ID', trigger: 'blur' }],
        label: [{ required: true, message: '请输入标签', trigger: 'blur' }],
        url: [{ required: true, message: '请输入URL', trigger: 'blur' }],
        homepageUri: [
          {
            required: true,
            message: '请输入主页URI',
            trigger: 'blur',
          },
        ],
        extraParams: [
          {
            required: true,
            message: '请输入额外参数',
            trigger: 'blur',
          },
        ],
      },
    });

    const form = useTemporaryState({
      id: 'form',
      initValue: {
        id: '',
        label: '',
        url: '',
        homepageUri: '',
        extraParams: '',
      },
    });
    const deleteInstanceModalVisible = ref(false);

    deleteInstanceModalVisible.value = false;

    // deleteInstance(selectedInstance.value.id).then(() => {
    //     deleteInstanceModalVisible.value = false;
    // });

    return () => {
      return (
        <div class="letgo-page">
          <FTable
            ref={fTableRefEl}
            data={[{}]}
            columns={columns.value}
            onEdit={[
              (...args) =>
                fTable(
                  {
                    mock: null,
                    type: 'JSExpression',
                    value: 'row',
                  },
                  ...args
                ),
            ]}
            onDelete={[
              (...args) =>
                fTable(
                  {
                    mock: null,
                    type: 'JSExpression',
                    value: 'row',
                  },
                  ...args
                ),
            ]}
          />
          <FForm
            ref={formRefEl}
            model={form.value}
            rules={rules.value}
            onSubmit={[(...args) => form(...args)]}
          >
            <FFormItem label={`ID`} prop={`id`}>
              <FInput v-model={form.value.id} placeholder={`请输入ID`} />
            </FFormItem>
            <FFormItem label={`标签`} prop={`label`}>
              <FInput v-model={form.value.label} placeholder={`请输入标签`} />
            </FFormItem>
            <FFormItem label={`URL`} prop={`url`}>
              <FInput v-model={form.value.url} placeholder={`请输入URL`} />
            </FFormItem>
            <FFormItem label={`主页URI`} prop={`homepageUri`}>
              <FInput
                v-model={form.value.homepageUri}
                placeholder={`请输入主页URI`}
              />
            </FFormItem>
            <FFormItem label={`额外参数`} prop={`extraParams`}>
              <FInput
                v-model={form.value.extraParams}
                placeholder={`请输入额外参数`}
              />
            </FFormItem>
            <FFormItem>
              <FButton type={`primary`} htmlType={`submit`}></FButton>
            </FFormItem>
          </FForm>
          <FModal
            ref={deleteInstanceFModalRefEl}
            v-model:show={deleteInstanceModalVisible.value}
            title={`删除实例`}
            onOk={[(...args) => deleteInstanceFModal(...args)]}
            onCancel={[(...args) => deleteInstanceFModal(...args)]}
          >
            <p></p>
            <FButton
              ref={confirmDeleteButtonRefEl}
              type={`danger`}
              onClick={[(...args) => confirmDeleteButton(...args)]}
            ></FButton>
            <FButton
              ref={cancelButtonRefEl}
              type={`default`}
              onClick={[(...args) => cancelButton(...args)]}
            ></FButton>
          </FModal>
        </div>
      );
    };
  },
});
