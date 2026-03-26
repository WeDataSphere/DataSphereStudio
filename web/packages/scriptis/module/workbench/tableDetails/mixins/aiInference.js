import xss from 'xss';

/**
 * AI推断逻辑处理Mixin
 * 用于处理字段值中包含AI推断信息的显示逻辑
 */
export default {
  methods: {
    /**
     * 格式化AI推断值，支持HTML显示（带AI标签）
     * @param {*} fieldValue - 字段值，可能是对象格式 {currentEnv: "", inference: ""}
     * @param {Object} options - 配置选项
     * @param {string} options.aiTagText - AI标签显示文本，默认为 'AI'
     * @returns {string} 格式化后的HTML字符串
     */
    formatAIInferenceValue(fieldValue, options = {}) {
      const { aiTagText = this.$t ? this.$t('message.scripts.tableDetails.AI') : 'AI' } = options;

      // 如果字段值是对象格式 {currentEnv: "", inference: ""}
      if (typeof fieldValue === 'object' && fieldValue !== null &&
          (fieldValue.hasOwnProperty('currentEnv') || fieldValue.hasOwnProperty('inference'))) {
        if (fieldValue.currentEnv && fieldValue.currentEnv.trim()) {
          // 使用 xss 库进行防护
          return xss(fieldValue.currentEnv);
        } else if (fieldValue.inference && fieldValue.inference.trim()) {
          // 先对推断内容进行 xss 防护，再创建 AI 标签
          const safeInference = xss(fieldValue.inference);
          return `<span class="ai-tag">${aiTagText}</span>${safeInference}`;
        }
        return '';
      }

      // 如果字段值是字符串，使用 xss 库进行防护
      return xss(fieldValue) || '';
    },

    /**
     * 获取AI推断值的纯文本内容，不包含HTML标签
     * @param {*} fieldValue - 字段值，可能是对象格式 {currentEnv: "", inference: ""}
     * @returns {string} 纯文本内容
     */
    getAIInferencePlainText(fieldValue) {
      // 如果字段值是对象格式 {currentEnv: "", inference: ""}
      if (typeof fieldValue === 'object' && fieldValue !== null &&
          (fieldValue.hasOwnProperty('currentEnv') || fieldValue.hasOwnProperty('inference'))) {
        // 有 currentEnv 优先取 currentEnv，没有取 inference，inference 没有就是空字符串
        if (fieldValue.currentEnv && fieldValue.currentEnv.trim()) {
          return fieldValue.currentEnv.trim();
        } else if (fieldValue.inference && fieldValue.inference.trim()) {
          return fieldValue.inference.trim();
        }
        return '';
      }

      // 如果字段值是字符串，直接返回
      return fieldValue || '';
    },

    /**
     * 检查字段值是否包含AI推断信息
     * @param {*} fieldValue - 字段值
     * @returns {boolean} 是否包含AI推断信息
     */
    hasAIInference(fieldValue) {
      return typeof fieldValue === 'object' && fieldValue !== null &&
             (fieldValue.hasOwnProperty('currentEnv') || fieldValue.hasOwnProperty('inference'));
    },

    /**
     * 检查字段值是否使用了AI推断（即只有inference而没有currentEnv）
     * @param {*} fieldValue - 字段值
     * @returns {boolean} 是否使用了AI推断
     */
    isUsingAIInference(fieldValue) {
      return typeof fieldValue === 'object' && fieldValue !== null &&
             fieldValue.inference && !fieldValue.currentEnv;
    }
  }
};
