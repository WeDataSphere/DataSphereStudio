<template>
  <div
    ref="table"
    :style="style"
    class="we-table">
    <div class="we-table-outflow">
      <!-- {{this.positionMemoryKey}} -->
      <div
        class="we-table-header">
        <template-header
          @sort-click="handleSortClick"
          @dbl-click="handleDblClick"
          ref="headerCom"
          class="header-list"
          :cache="cache"
          :item-size-getter="itemSizeGetter"
          :estimated-item-size="30"
          :data="headRows">
        </template-header>
      </div>
      <div
        ref="tableBody"
        class="we-table-body">
        <template-list
          id="bottomDiv"
          ref="bodyCom"
          :cache="cache"
          :is-listen-scroll="true"
          :item-size-getter="itemSizeGetter"
          :estimated-item-size="30"
          :data="bodyRows"
          :header="headRows"
          @on-scroll="changeScrollLeft"
          @on-click="onColumnClick"
          @on-tdcontext-munu="onTdContextMenu">
        </template-list>
      </div>
    </div>
  </div>
</template>
<script>
import templateList from './list.vue';
import templateHeader from './header.vue';
import { debounce } from 'lodash';

const prefixCls = 'we-table';
export default {
  name: prefixCls,
  components: {
    templateList,
    templateHeader,
  },
  props: {
    data: Object,
    width: Number,
    height: [Number, String],
    positionMemoryKey: {
      type: String,
      default: '',
    },
  },
  data() {
    return {
      cache: {},
      sort: {
        sorting: false,
        column: null,
        type: 'normal',
        start: 0,
      },
      bodyRows: [],
      headRows: [],
      scrollTop: 0,
      initFlag: true,
      initScrollLeft: true,
    };
  },
  computed: {
    style() {
      const style = {
      };
      if (this.width) {
        // style.width = this.width + 'px';
      }
      if (this.height) {
        style.height = this.height + 'px';
      }
      return style;
    },
  },
  watch: {
    'data': {
      handler(val) {
        this.resize();
        this.headRows = val.headRows;
        this.bodyRows = this.revert(val.bodyRows);
        if (this.positionMemoryKey && sessionStorage.getItem('table_position_'+this.positionMemoryKey) && this.initFlag) {
          let curTablePositionData = JSON.parse(sessionStorage.getItem('table_position_'+this.positionMemoryKey))
          this.$nextTick(() => {
            if(this.$refs.headerCom && this.$refs.headerCom.$refs.list) {
              // 设置表头横向滚动条
              this.$refs.headerCom.$refs.list.scrollLeft = curTablePositionData.scrollLeft;
              this.initScrollLeft = true;
              this.$refs.headerCom.handleScroll(curTablePositionData, curTablePositionData);
            }
            if(this.$refs.bodyCom) {
              // 设置表格内容横向竖向滚动条
              this.$refs.bodyCom.setScroll(curTablePositionData.scrollTop, curTablePositionData.scrollLeft);   
            }
          })
          this.initFlag = false;
        } else if(!this.initFlag) {
          this.handleSaveTablePosition(this)
        }
      },
      immediate: true,
      deep: true
    },

  },
  mounted() {
  },
  methods: {
     // 翻页、页面大小改变、滚动时都会触发,但初始化时不触发
    handleSaveTablePosition: debounce((that) => {
     if (that.positionMemoryKey) {
        that.$emit("on-table-scroll-change", that.positionMemoryKey,that.scrollTop, that.$refs.headerCom.$refs.list.scrollLeft);
      }
    }, 300),
    // 重置初始化信号量
    handleInitFlag(e) {
      this.initFlag = e;
    },
    changeScrollLeft({ v, h }) {
      this.scrollTop=v.scrollTop;
      this.$refs.headerCom.$refs.list.scrollLeft = h.scrollLeft;
      this.$refs.headerCom.handleScroll(v, h);
      if(!this.initScrollLeft) {
        this.handleSaveTablePosition(this)
      } else {
        this.initScrollLeft = false;
      }
    },
    itemSizeGetter(item, i) {
      let div = document.createElement('div');
      document.body.appendChild(div);
      div.style.paddingLeft = '20px';
      div.style.paddingRight = '54px';
      div.style.position = 'absolute';
      div.style.top = '-9999px';
      div.style.left = '-9999px';
      div.innerHTML = item.content;
      let width = div.offsetWidth;
      document.body.removeChild(div);
      this.cache[i] = width;
      if (width > 300) {
        width = 300;
      }
      return width;
    },
    revert(data) {
      let newData = [];
      let firstRowLen = data[0] ? data[0].length : 0;
      for (let i = 0; i < firstRowLen; i++) {
        newData[i] = [];
        for (let j = 0; j < data.length; j++) {
          newData[i][j] = data[j][i];
        }
      }
      return newData;
    },
    computeSortClass(currentHead, type) {
      return [
        `${prefixCls}-sort-caret`,
        type,
        {
          [`${prefixCls}-sort`]: (this.sort.column === currentHead && this.sort.type === type),
        },
      ];
    },
    handleSortClick(args) {
      this.$emit('on-sort-change', args);
    },
    handleDblClick(col) {
      this.$emit('dbl-click', col);
    },
    onColumnClick(index) {
      this.$emit('on-click', index);
    },
    onTdContextMenu(e) {
      this.$emit("on-tdcontext-munu", e);
    },
    resize() {
      this.cache = {};
    }
  },
};
</script>
<style lang="scss" src="../index.scss"></style>
<style>
.list-view {
  width: 100%;
  height: 100%;
  overflow: auto;
  position: relative;
}
</style>
