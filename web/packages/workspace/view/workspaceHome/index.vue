<template>
  <div style="height: 100%;">
    <Alert type="warning" show-icon closable v-if="!hasShow" @on-close="closeNotice">
      {{$t('message.workspace.topNotice.title')}}
      <span slot="desc">{{$t('message.workspace.topNotice.content')}}</span>
    </Alert>
    <bankHome />
  </div>
</template>

<script>
import storage from "@dataspherestudio/shared/common/helper/storage";
import bankHome from '../../module/bankHome';

export default {
  components: {
    bankHome: bankHome.component
  },
  data() {
    const show = storage.get('workspace_top_notice')
    return {
      hasShow: show
    }
  },
  methods: {
    closeNotice() {
      this.hasShow = false
      storage.set('workspace_top_notice', true)
    }
  }
}
</script>
