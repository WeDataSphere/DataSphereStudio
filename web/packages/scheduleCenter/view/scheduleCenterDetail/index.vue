<template>
  <div class="scheduleCenterDetailContent">
    <scheduleCenterDetail />
  </div>
</template>

<script>
import scheduleCenterDetail from '../../module/scheduleCenterDetail';

export default {
  components: {
    scheduleCenterDetail: scheduleCenterDetail.component
  }
}
</script>
<style lang="scss" scoped>
  .scheduleCenterDetailContent {
    width: 100%;
    height: 100%;
  }
</style>
