<template>
  <div class="field-list">
    <div class="field-list-search">
      <Input
        v-model="searchText"
        :placeholder="$t('message.scripts.tableDetails.SSZDMC')">
        <Icon
          slot="prefix"
          type="ios-search"/>
      </Input>
      <div class="field-list-search__button">
        <Button type="success" @click="handleCopy">{{ $t('message.scripts.tableDetails.FZBZDXX') }}</Button>
        <Poptip
          v-model="columnSettingsVisible"
          placement="bottom-end"
          width="300"
          :transfer="false"
          class="column-settings-poptip">
          <Button type="default" style="margin-left: 8px;">
            <Icon type="ios-settings" />
            {{ $t('message.scripts.tableDetails.LSZZ') }}
          </Button>
          <div slot="content" class="column-settings-content">
            <div class="column-settings-header">
              <span>{{ $t('message.scripts.tableDetails.XZXSLZ') }}</span>
              <Button type="text" size="small" @click="resetColumns">{{ $t('message.scripts.tableDetails.CZ') }}</Button>
            </div>
            <draggable
              v-model="sortableColumns"
              handle=".drag-handle"
              @end="onColumnSort"
              class="sortable-columns">
              <div
                v-for="column in sortableColumns"
                :key="column.key"
                class="column-item">
                <Icon type="ios-menu" class="drag-handle" />
                <Checkbox
                  v-model="column.visible"
                  @on-change="onColumnVisibilityChange"
                  :disabled="column.key === 'index'">
                  {{ column.title }}
                </Checkbox>
              </div>
            </draggable>
          </div>
        </Poptip>
      </div>
    </div>
    <div style="position:relative">
      <div class="field-list-header" id="tbheader" :class="{'ovy': searchColList.length > maxSize}">
        <div
          class="field-list-item"
          v-for="(item, index) in columnCalc"
          :key="index"
          :style="{width: item.width? `${item.width}` : 'auto'}"
          @mousemove.prevent.stop="mousemove"
          @mouseup.prevent.stop="mouseup"
        >{{ item.title }}
          <div
            class="resize-bar"
            :data-col-index="index"
            @mousedown="mousedown"
          ></div>
        </div>
      </div>
      <virtual-list
        ref="columnTables"
        :size="46"
        :remain="searchColList.length > maxSize ? maxSize : searchColList.length"
        wtag="ul"
        class="field-list">
        <li
          v-for="(item, index) in searchColList"
          :key="index"
          class="field-list-body"
          @click="clickItem($event, item)"
        >
          <div
            class="field-list-item"
            :title="getPlainTextValue(item, field)"
            v-for="(field, index2) in columnCalc"
            :data-key="field.key"
            :key="index2"
            :style="{width: columnCalc[index2].width? `${columnCalc[index2].width}` : 'auto'}">
            <span v-html="formatValue(item, field)"></span>
          </div>
        </li>
      </virtual-list>
      <div v-if="dragStartX" class="drag-line" :style="{left: dragLine.left, display: dragLine.show}" />
    </div>
    <Modal v-model="editModelShow" :title="$t('message.scripts.createTable.titleModel')" :footer-hide="true">
      <Form :model="fieldModel" :label-width="80">
        <Form-item prop="name" :label="$t('message.scripts.Name')">
          <Input v-model="fieldModel.name" placeholder="" :disabled="true"></Input>
        </Form-item>
        <Form-item prop="type" :label="$t('message.scripts.Type')">
          <RadioGroup v-model="fieldModel.type">
            <Radio label="index" disabled>{{ $t('message.scripts.Metrics') }}</Radio>
            <Radio label="dimension" disabled>{{ $t('message.scripts.Dimensions') }}</Radio>
          </RadioGroup>
        </Form-item>
        <Form-item prop="business" :label="$t('message.scripts.busst')">
          <Input v-model="fieldModel.business" placeholder="" :disabled="true"></Input>
        </Form-item>
        <Form-item prop="calculate" :label="$t('message.scripts.calcst')">
          <Input v-model="fieldModel.calculate" placeholder="" :disabled="true"></Input>
        </Form-item>
        <Form-item prop="formula" :label="$t('message.scripts.Formula')">
          <Input v-model="fieldModel.formula" type="textarea" placeholder="" :disabled="true"></Input>
        </Form-item>
      </Form>
    </Modal>
  </div>
</template>
<script>
import utils from '../utils.js';
import virtualList from '@dataspherestudio/shared/components/virtualList';
import draggable from 'vuedraggable';
import aiInferenceMixin from '../mixins/aiInference.js';
export default {
  mixins: [aiInferenceMixin],
  components: {
    virtualList,
    draggable,
  },
  props: {
    table: {
      type: Array,
    },
    // tableInfo: {
    //   type: Object,
    // },
  },
  data() {
    return {
      searchText: '',
      searchColList: [],
      editModelShow: false,
      fieldModel: {},
      allTableColumns: [
        { title: this.$t('message.scripts.Serial'), key: 'index', width: '5%', visible: true },
        { title: this.$t('message.scripts.tableDetails.BDAPBZD'), key: 'targetColumnName', width: '10%', visible: true },
        { title: this.$t('message.scripts.tableDetails.BDAPBZDMS'), key: 'targetColumnComment', width: '12%', visible: true },
        { title: this.$t('message.scripts.tableDetails.BDAPBZDLX'), key: 'type', width: '10%', visible: true },
        { title: this.$t('message.scripts.tableDetails.SFZJBDAP'), key: 'primary', type: 'boolean', width: '10%', visible: true },
        { title: this.$t('message.scripts.tableDetails.SFFQZDBDAP'), key: 'partitionField', type: 'boolean', width: '10%', visible: true },
        { title: this.$t('message.scripts.tableDetails.MXXXBDAP'), key: 'modeInfo.name', width: '10%', visible: true },
        { title: this.$t('message.scripts.tableDetails.ZDGZBDAP'), key: 'rule', width: '10%', visible: true },
        { title: this.$t('message.scripts.tableDetails.BDPYBZD'), key: 'originColumnName', width: '10%', visible: true },
        { title: this.$t('message.scripts.tableDetails.BDPYBZDMS'), key: 'originColumnComment', width: '12%', visible: true },
        { title: this.$t('message.scripts.tableDetails.BDPYBZDKJJGGZ'), key: 'fieldRule', width: '12%', visible: true },
        { title: this.$t('message.scripts.tableDetails.BDPYBZDSJX'), key: 'dataItemName', width: '10%', visible: true },
        { title: this.$t('message.scripts.tableDetails.BDPYBZDSSSJFL'), key: 'classficationName', width: '12%', visible: true },
        { title: this.$t('message.scripts.tableDetails.TMFS'), key: 'funcDescription', width: '8%', visible: true },
        { title: this.$t('message.scripts.tableDetails.TMHS'), key: 'funcName', width: '8%', visible: true },
      ],
      dragStartX: undefined,
      dragEndX: undefined,
      dragLine: {
        show: 'none',
        diff: 0,
        left: 0
      },
      adjustCol: [],
      columnSettingsVisible: false, // 列设置弹窗显示状态
      columnOrder: [] // 列的排序配置
    };
  },
  computed: {
    maxSize() {
      return Math.floor((window.innerHeight - 242) / 46) - 1;
    },
    tableColumns() {
      // 获取排序后的可见列
      const orderedColumns = this.columnOrder.length > 0
        ? this.columnOrder.map(index => this.allTableColumns[index])
        : this.allTableColumns;

      return orderedColumns.filter(col => col.visible);
    },
    columnCalc() {
      return this.tableColumns.map((item, index) => {
        return this.adjustCol[index] ? {
          ...item,
          width: this.adjustCol[index]
        } : {
          ...item
        }
      })
    },
    sortableColumns: {
      get() {
        return this.columnOrder.length > 0
          ? this.columnOrder.map(index => this.allTableColumns[index])
          : this.allTableColumns;
      },
      set(newValue) {
        // 更新列顺序
        this.columnOrder = newValue.map(col =>
          this.allTableColumns.findIndex(original => original.key === col.key)
        );
        this.saveColumnSettings();
      }
    }
  },
  watch: {
    table: {
      handler(newVal) {
        if (newVal && newVal.length > 0) {
          this.init();
        }
      },
      immediate: true
    },
    searchText(val) {
      this.searchColList = [];
      let specialCharacter = ['\\', '$', '(', ')', '*', '+', '.', '[', '?', '^', '{', '|'];
      specialCharacter.map(v => {
        let reg = new RegExp('\\' + v, 'gim');
        val = val.replace(reg, '\\' + v);
      });
      const regexp = new RegExp(val, 'i');
      const tmpList = this.table;
      if (tmpList && tmpList.length > 0) {
        tmpList.forEach((o, index) => {
          // 需要搜索的字段列表，包括可能的AI推断字段
          const searchFields = [
            o.name,
            o.targetColumnName,
            o.targetColumnComment,
            o.originColumnName,
            o.originColumnComment,
            o.dataItemName,
            o.classficationName,
            o.funcDescription,
            o.funcName,
            o.fieldRule
          ];

          // 检查是否有字段匹配搜索关键字
          const isMatched = searchFields.some(fieldValue => {
            // 如果字段值是AI推断的对象格式，提取其文本内容进行搜索
            if (this.hasAIInference(fieldValue)) {
              const plainText = this.getAIInferencePlainText(fieldValue);
              return regexp.test(plainText || '');
            }
            // 普通字段直接进行正则匹配
            return regexp.test(fieldValue || '');
          });

          if (isMatched) {
            o.index = index + 1
            this.searchColList.push(o);
          }
        });
      }
    },
  },
  mounted() {
    this.loadColumnSettings();
  },
  methods: {
    init() {
      if (this.table && this.table.length > 0) {
        this.searchColList = this.table.map((o, index)=> {
          o.index = index + 1;
          return o
        });
      } else {
        this.searchColList = [];
      }
    },
    formatValue(item, field) {
      // 获取字段值
      let fieldValue = item[field.key];

      // 处理嵌套属性（如 modeInfo.name）
      if (field.key.includes('.')) {
        const keyParts = field.key.split('.');
        fieldValue = item;
        for (const part of keyParts) {
          fieldValue = fieldValue && fieldValue[part];
        }
      }

      // 如果字段值是对象格式 {currentEnv: "", inference: ""}，按AI推断逻辑处理
      if (this.hasAIInference(fieldValue)) {
        return this.formatAIInferenceValue(fieldValue);
      }

      // 其他情况按原有逻辑处理
      return utils.formatValue(item, field);
    },

    getPlainTextValue(item, field) {
      // 获取字段值
      let fieldValue = item[field.key];

      // 处理嵌套属性（如 modeInfo.name）
      if (field.key.includes('.')) {
        const keyParts = field.key.split('.');
        fieldValue = item;
        for (const part of keyParts) {
          fieldValue = fieldValue && fieldValue[part];
        }
      }

      // 如果字段值是对象格式 {currentEnv: "", inference: ""}，按AI推断逻辑处理
      if (this.hasAIInference(fieldValue)) {
        return this.getAIInferencePlainText(fieldValue);
      }

      // 其他情况按原有逻辑处理，但获取纯文本值
      const formattedValue = utils.formatValue(item, field);
      return formattedValue || '';
    },
    clickItem(e, item) {
      if (e && e.target && e.target.dataset.key) {
        if (e.target.dataset.key === 'modeInfo.name') {
          this.fieldModel = item.modeInfo || {}
          if (this.fieldModel.name) this.editModelShow = true
        }
      }
    },
    mousedown(e) {
      if (e && e.target && e.target.dataset.colIndex) {
        this.dragColIndex = e.target.dataset.colIndex - 0
        this.dragStartX = e.clientX
        this.tableLeft = this.$el.getBoundingClientRect().x
      }
    },
    mousemove(e) {
      if (this.dragStartX !== undefined) {
        this.dragEndX = e.clientX
        this.dragLine = {
          left: this.dragEndX - this.tableLeft + 'px',
          diff: this.dragEndX - this.dragStartX,
          show: 'block'
        }
      }
    },
    mouseup(e) {
      if (this.dragStartX !== undefined) {
        this.dragEndX = e.clientX
        this.dragLine = {
          left: this.dragEndX - this.tableLeft + 'px',
          diff: this.dragEndX - this.dragStartX,
          show: 'none'
        }
        this.dragStartX = undefined
        this.adjustColWidth()
      }

    },
    adjustColWidth() {
      const adjustCol = []
      this.$el.querySelectorAll('#tbheader .field-list-item').forEach(item => adjustCol.push(item.getBoundingClientRect().width))
      adjustCol.forEach((item, index) => {
        if (index === this.dragColIndex) {
          adjustCol[index] = adjustCol[index] + this.dragLine.diff + 'px'
        } else {
          adjustCol[index] = adjustCol[index] + this.dragLine.diff / (adjustCol.length - 1) * -1 + 'px'
        }
      })
      this.adjustCol = adjustCol
    },
    handleCopy() {
      const contents = [this.columnCalc.map(item => item.title).join('\t')];
      const columnKeys = this.columnCalc.map(item => item.key);
      this.searchColList.forEach(item => {
        let items = []
        columnKeys.forEach(key => {
          if (['primary','partitionField'].includes(key) && typeof item[key] === 'boolean') {
            items.push(item[key] ? '是' : '否');
          } else {
            // 获取字段值
            let value = this.getCopyFieldValue(item, key);
            items.push(value || '');
          }
        })
        contents.push(items.join('\t'));
      })
      const text = contents.join('\n');
      const textArea = document.createElement("textarea");
      textArea.value = text;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand("copy");
      document.body.removeChild(textArea);
      this.$Message.success(this.$t("message.scripts.paste_successfully"));
    },

    getCopyFieldValue(item, key) {
      // 获取字段值
      let fieldValue = item[key];

      // 处理嵌套属性（如 modeInfo.name）
      if (key.includes('.')) {
        const keyParts = key.split('.');
        fieldValue = item;
        for (const part of keyParts) {
          fieldValue = fieldValue && fieldValue[part];
        }
      }

      // 如果字段值是对象格式 {currentEnv: "", inference: ""}，按AI推断逻辑处理
      if (this.hasAIInference(fieldValue)) {
        return this.getAIInferencePlainText(fieldValue);
      }

      // 其他情况直接返回字段值
      return fieldValue;
    },
    // 列设置相关方法
    loadColumnSettings() {
      const storageKey = 'table_field_column_settings';
      const saved = localStorage.getItem(storageKey);
      if (saved) {
        try {
          const settings = JSON.parse(saved);
          if (settings.visibility) {
            // 更新列的可见性
            this.allTableColumns.forEach((col) => {
              if (Object.prototype.hasOwnProperty.call(settings.visibility, col.key)) {
                col.visible = settings.visibility[col.key];
              }
            });
          }
          if (settings.order && settings.order.length === this.allTableColumns.length) {
            this.columnOrder = settings.order;
          }
        } catch (e) {
          console.error('加载列设置失败:', e);
        }
      }
    },
    saveColumnSettings() {
      const storageKey = 'table_field_column_settings';
      const settings = {
        visibility: {},
        order: this.columnOrder
      };

      this.allTableColumns.forEach(col => {
        settings.visibility[col.key] = col.visible;
      });

      localStorage.setItem(storageKey, JSON.stringify(settings));
    },
    onColumnVisibilityChange() {
      this.saveColumnSettings();
    },
    onColumnSort() {
      // 拖拽排序后自动保存
      this.saveColumnSettings();
    },
    resetColumns() {
      // 重置所有列为可见并恢复默认顺序
      this.allTableColumns.forEach(col => {
        col.visible = true;
      });
      this.columnOrder = [];
      this.saveColumnSettings();
      this.$Message.success(this.$t("message.scripts.tableDetails.CZCG"));
    }
  },
};
</script>
<style lang="scss" scoped>
@import '@dataspherestudio/shared/common/style/variables.scss';
  .field-list {
      height: calc(100% - 52px);
      overflow: hidden;
      width: 100%;
      .field-list-header,
      .field-list-body {
          width: 100%;
          display: flex;
          border: 1px solid #dcdee2;
          @include border-color($border-color-base, $dark-border-color-base);
          height: 46px;
          line-height: 46px;
      }
      .field-list-search {
        display: flex;
        &__button {
          flex: 0 0 150px;
          margin-left: 10px;
        }
      }
      .field-list-header {
          background-color: #5e9de0;
          color: #fff;
          font-weight: bold;
          margin-top: 10px;
          border: none;
      }
      .field-list-body {
          border-bottom: none;
          @include bg-color($light-base-color, $dark-base-color);
          .field-table-mode {
            color: $primary-color
          }
          &:last-child {
              border-bottom: 1px solid $border-color-base;
              @include border-color($border-color-base, $dark-border-color-base);
          }
      }
      .field-list-item {
          width: 200px;
          padding: 0 10px;
          display: inline-block;
          height: 100%;
          text-align: center;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          position: relative;
          min-width: 80px;
          max-width: 30%;
          &:not(:first-child){
              border-left: 1px solid $border-color-base;
              @include border-color($border-color-base, $dark-border-color-base);
          }
      }
  }
  .resize-bar {
    position: absolute;
    width: 10px;
    height: 100%;
    bottom: 0;
    right: -5px;
    cursor: col-resize;
    z-index: 1;
  }
  .drag-line {
    position: absolute;
    top: 0;
    width: 0px;
    border-left: .5px dashed #eee;
    height: 100%;
    z-index: 1;
    display: none;
    pointer-events: none;
  }
  .ovy {
    padding-right: 8px
  }

  .column-settings-content {
    padding: 8px;

    .column-settings-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      font-weight: bold;
    }

    .sortable-columns {
      max-height: 300px;
      overflow-y: auto;

      .column-item {
        display: flex;
        align-items: center;
        padding: 4px 0;
        cursor: pointer;

        .drag-handle {
          margin-right: 8px;
          cursor: move;
          color: #999;

          &:hover {
            color: #666;
          }
        }
      }
    }
  }

  .field-list-search__button {
    display: flex;
    align-items: center;
  }

  ::v-deep .ai-tag {
    display: inline-block;
    height: 18px;
    border-radius: 3px;
    font-size: 10px;
    line-height: 18px;
    text-align: center;
    margin-right: 4px;
    padding: 0 2px;
    background-color: #f0f2f5;
    color: #93949b;
  }

</style>
