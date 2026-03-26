
<template>
  <li v-if="visualisShow" class="result-tool-visual">
    <Poptip
      :transfer="true"
      :width="220"
      v-model="popup.visualis"
      placement="right-end"
      popper-class="we-poptip"
    >
      <div @click.stop="showResultAction">
        <Icon type="md-analytics" :size="20" />
        <span
          v-if="isIconLabelShow"
          :title="$t('message.common.toolbar.deepAnalysis')"
          class="v-toolbar-icon"
          >{{ $t('message.common.toolbar.deepAnalysis') }}</span
        >
      </div>
      <div slot="content">
        <div>
          <Row>
            {{ $t('message.common.toolbar.model') }}
          </Row>
          <Row>
            <RadioGroup v-model="resultsShowType">
              <Col span="10">
                <Radio label="1">{{
                  $t('message.common.toolbar.graphAnalysis')
                }}</Radio>
              </Col>
              <!-- <Col span="10" offset="4">
                <Radio label="2">{{
                  $t('message.common.toolbar.excelAnalysis')
                }}</Radio>
              </Col> -->
            </RadioGroup>
          </Row>
        </div>
        <Row class="confirm">
          <Col span="10">
            <Button @click="cancelPopup('visualis')">{{
              $t('message.common.cancel')
            }}</Button>
          </Col>
          <Col span="10" offset="4">
            <Button type="primary" @click="confirm('visualis')">{{
              $t('message.common.submit')
            }}</Button>
          </Col>
        </Row>
      </div>
    </Poptip>
  </li>
</template>
<script>
import storage from '@dataspherestudio/shared/common/helper/storage'
export default {
  props: {
    script: {
      type: Object,
      required: true,
    },
    isIconLabelShow: Boolean,
  },
  data() {
    return {
      resultsShowType: '1',
      popup: {
        visualis: false,
      },
    }
  },
  computed: {
    visualisShow() {
      let result = {}
      if (this.script.resultList && this.script.resultList.length > 0) {
        result = this.script.resultList[this.script.resultSet].result || {}
      }
      let isScriptis =
        this.$route.name === 'Home' ||
        (this.$route.name === 'results' && this.$route.query.from === 'Home')
      const baseinfo = storage.get('baseInfo', 'local') || {}
      return (
        baseinfo.visualEnable !== false &&
        (this.script.runType === 'sql' ||
          this.script.runType === 'hql' ||
          (this.script.runType === 'py' && result.type === '2')) &&
        isScriptis &&
        !result.tipMsg
      )
    },
  },
  methods: {
    cancelPopup(type) {
      this.popup[type] = false
    },
    confirm(type) {
      this[`${type}Confirm`]()
      this.cancelPopup(type)
    },
    showResultAction() {
      this.popup.visualis = true
    },
    visualisConfirm() {
      const resultType =
        this.resultsShowType === '1' ? 'VisualAnalysis' : 'DataWrangler'
      this.$emit('event-from-ext', {
        callFn: 'changeViewType',
        params: ['ResultToolbar', resultType],
      })
    },
  },
}
</script>
<style lang="scss">
@import '@dataspherestudio/shared/common/style/variables.scss';
.we-toolbar-wrap {
  li.result-tool-visual.we-toolbar-active {
    i,
    span {
      @include font-color($primary-color, $dark-primary-color);
    }
  }
}
</style>

