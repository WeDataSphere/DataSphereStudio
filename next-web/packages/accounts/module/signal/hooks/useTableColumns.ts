import { useI18n } from 'vue-i18n';

import { ref } from 'vue';
import type { BaseType } from '../types/index';

export const useTableColumns = (
  fillText: (row: { [index: string]: unknown }) => unknown
): BaseType => {
  const { t: $t } = useI18n();
  const language = localStorage.getItem('locale');
  const datachecker = [
    {
      type: 'selection',
      minWidth: 30,
    },
    {
      type: 'text',
      prop: 'projectName',
      label: $t('_.项目名称'),
      ellipsis: true,
      minWidth: 160,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'orchestratorName',
      label: $t('_.工作流名称'),
      ellipsis: true,
      minWidth: 160,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'nodeName',
      label: $t('_.节点名'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'nodeDesc',
      label: $t('_.节点描述'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'sourceType',
      label: 'source.type',
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'checkObject',
      label: 'check.object',
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'maxCheckHours',
      label: 'max.checkout.hours',
      ellipsis: true,
      minWidth: 200,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'jobDesc',
      label: 'job.desc',
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'qualitisCheckText',
      label: $t('_.使用qualities校验'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'nodeId',
      label: $t('_.节点ID'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'action',
      label: $t('_.操作'),
      minWidth: language === 'zh-CN' ? 148 : 166,
    },
  ];

  const eventsender = [
    {
      type: 'selection',
      minWidth: 30,
    },
    {
      type: 'text',
      prop: 'projectName',
      label: $t('_.项目名称'),
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'orchestratorName',
      label: $t('_.工作流名称'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'nodeName',
      label: $t('_.节点名'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'nodeDesc',
      label: $t('_.节点描述'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgType',
      label: 'msg.type',
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgSender',
      label: 'msg.sender',
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgBody',
      label: 'msg.body',
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgTopic',
      label: 'msg.topic',
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgName',
      label: 'msg.name',
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'nodeId',
      label: $t('_.节点ID'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'action',
      label: $t('_.操作'),
      minWidth: language === 'zh-CN' ? 148 : 166,
    },
  ];

  const eventreceiver = [
    {
      type: 'selection',
      minWidth: 30,
    },
    {
      type: 'text',
      prop: 'projectName',
      label: $t('_.项目名称'),
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'orchestratorName',
      label: $t('_.工作流名称'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'nodeName',
      label: $t('_.节点名'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'nodeDesc',
      label: $t('_.节点描述'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgType',
      label: 'msg.type',
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgReceiver',
      label: 'msg.resceiver',
      ellipsis: true,
      minWidth: 140,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgTopic',
      label: 'msg.topic',
      ellipsis: true,
      minWidth: 140,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgName',
      label: 'msg.name',
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'queryFrequency',
      label: 'query.frequency',
      ellipsis: true,
      minWidth: 160,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'maxReceiveHours',
      label: 'msg.receive.hours',
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgSaveKey',
      label: 'msg.saveKey',
      ellipsis: true,
      minWidth: 116,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'onlyReceiveTodayText',
      label: 'only.receive.today',
      ellipsis: true,
      minWidth: 160,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'msgReceiveUseRunDateText',
      label: 'msg.receive.use.rundate',
      ellipsis: true,
      minWidth: 170,
      formatter: fillText,
    },
    {
      type: 'text',
      prop: 'nodeId',
      label: $t('_.节点ID'),
      ellipsis: true,
      minWidth: 180,
      formatter: fillText,
    },
    {
      type: 'action',
      label: $t('_.操作'),
      minWidth: language === 'zh-CN' ? 148 : 166,
    },
  ];

  const columnMap = ref<BaseType>({
    datachecker,
    eventsender,
    eventreceiver,
  });

  return {
    columnMap,
  };
};
