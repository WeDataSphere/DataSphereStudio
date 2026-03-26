<template>
  <div
    class="set ivu-select ivu-select-single ivu-select-small"
    v-clickoutside="handleOutsideClick">
    <div
      tabindex="0"
      @click.stop="showList"
      class="ivu-select-selection">
      <div class="">
        <!-- 融合的输入框和显示区域 -->
        <input
          v-if="show && list.length > 1"
          ref="searchInput"
          v-model="searchKeyword"
          :placeholder="$t('message.common.resultList') + (current + 1)"
          class="fusion-input"
          @input="handleSearch"
          @click.stop
        />
        <span v-else class="ivu-select-selected-value">{{$t('message.common.resultList')}}{{ current - 0 + 1 }}</span>
        <i class="ivu-icon ivu-icon-ios-arrow-down ivu-select-arrow"></i>
      </div>
    </div>
    <div
      v-if="show"
      class="ivu-select-dropdown"
      x-placement="bottom-start">
      <virtual-list
        class="ivu-select-dropdown-list list"
        v-if="displayList.length >= 1"
        ref="vsl"
        :size="18"
        :remain="displayList.length > 8 ? 8 : displayList.length"
        wtag="ul"
        style="overflow-x:hidden"
        @click.native="changeSet">
        <li
          v-for="(item, index) in displayList"
          :class="{current: current-0 === getOriginalIndex(index)}"
          :data-index="getOriginalIndex(index)"
          :key="getOriginalIndex(index)">{{$t('message.common.resultList')}}{{ getOriginalIndex(index)+1 }}</li>
      </virtual-list>
      <!-- 无数据提示 -->
      <div v-else class="no-data-tip">
        {{$t('message.common.noData')}}
      </div>
    </div>
  </div>
</template>
<script>
import virtualList from '@dataspherestudio/shared/components/virtualList';
import clickoutside from '@dataspherestudio/shared/common/helper/clickoutside';
export default {
  name: 'ResultSetList',
  directives: {
    clickoutside,
  },
  components: {
    virtualList,
  },
  props: {
    list: {
      type: Array,
      default: () => [],
    },
    current: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      show: false,
      searchKeyword: '',
      filteredList: [],
      filteredIndices: [],
    };
  },
  computed: {
    displayList() {
      return this.searchKeyword ? this.filteredList : this.list;
    }
  },
  watch: {
    list: {
      handler(newList) {
        this.filteredList = newList;
        this.filteredIndices = newList.map((_, index) => index);
      },
      immediate: true
    },
    show(val) {
      if (val) {
        // 显示时重置搜索
        this.searchKeyword = '';
        this.$nextTick(() => {
          if (this.$refs.searchInput) {
            this.$refs.searchInput.focus();
          }
        });
      }
    }
  },
  methods: {
    getOriginalIndex(filteredIndex) {
      if (!this.searchKeyword) {
        return filteredIndex;
      }
      return this.filteredIndices[filteredIndex] || filteredIndex;
    },
    handleSearch() {
      if (!this.searchKeyword) {
        this.filteredList = this.list;
        this.filteredIndices = this.list.map((_, index) => index);
        return;
      }
      
      const keyword = this.searchKeyword.toLowerCase();
      this.filteredIndices = [];
      this.filteredList = this.list.filter((item, index) => {
        // 支持按索引搜索（数字或结果显示）只做关键字匹配
        const indexMatch = (`${this.$t('message.common.resultList')}${index + 1}`).toLowerCase().includes(keyword);
        if (indexMatch) {
          this.filteredIndices.push(index);
          return true;
        }
        return false;
      });
    },
    changeSet(e) {
      if (e.target) {
        let index = e.target.getAttribute('data-index');
        if (index !== null) {
          this.$emit('change', index);
          this.show = false;
        }
      }
    },
    showList() {
      this.show = !this.show;
    },
    handleOutsideClick() {
      this.show = false;
    },
  },
};
</script>
<style lang="scss" scoped>
.set {
  position: relative;
}
.ivu-select-dropdown {
  min-width: 90px;
  position: absolute;
  will-change: top, left, transform;
  transform-origin: center top;
  top: -5px;
  left: 0px;
  transform: translate(0, -100%);
  z-index: 9999;
  overflow: hidden;
}
.search-input-wrapper {
  padding: 4px;
  border-bottom: 1px solid #e8eaec;
}
.search-input {
  width: 100%;
  height: 24px;
  padding: 0 8px;
  font-size: 12px;
  border: 1px solid #dcdee2;
  border-radius: 4px;
  outline: none;
  &:focus {
    border-color: #2d8cf0;
  }
}
.fusion-input {
  width: calc(100% - 20px);
  height: 22px;
  padding: 0 8px;
  font-size: 12px;
  border: none;
  outline: none;
  background: transparent;
  &::placeholder {
    color: #c5c8ce;
  }
}

.no-data-tip {
  padding: 8px;
  font-size: 12px;
  color: #808695;
  text-align: center;
}

.list {
  font-size: 12px;
  height:18px;
  line-height: 18px;
  li {
    padding-left: 8px;
    cursor: pointer;
  }
  li:hover {
    background-color: #e9e9e9;
  }
  .current{
    color: #2d8cf0;
  }
}
</style>
