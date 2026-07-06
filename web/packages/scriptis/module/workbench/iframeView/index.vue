<template>
  <div class="iframeClass">
    <iframe
      class="iframeClass"
      :id="iframeId"
      :src="url"
      frameborder="0"
      width="100%"
      height="100%" />
  </div>
</template>
<script>

export default {
  props: {
    url: {
      type: String,
      default: ''
    },
    workId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {}
  },
  computed: {
    iframeId() {
      const normalizedWorkId = String(this.workId || '').replace(/[^a-zA-Z0-9_-]/g, '-')
      return `scriptis-ai-iframe-${normalizedWorkId}`
    }
  },
  mounted() {
  },
  methods: {
  },
  beforeDestroy() {
  }
};

</script>
<style lang="scss" scoped>
  .iframeClass {
    position: relative;
    height: 100%;
    width: 100%;
  }

</style>
