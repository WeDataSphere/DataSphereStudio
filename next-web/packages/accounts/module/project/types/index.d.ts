export interface DataType {
  [key: string]: any;
}

/** 健康标签 code（与后端 ProjectAssetService 输出一致） */
export type HealthLabelCode =
  | 'EMPTY_PROJECT' // 空项目
  | 'STALE' // 长期未更新
  | 'NO_DESCRIPTION' // 描述缺失
  | 'UNAVAILABLE'; // 统计降级，无法评估

export type SearchFormType = {
  workspaceId: string;
  projectNames: string[];
  createUsers: string[];
  releaseUsers: string[];
  editUsers: string[];
  accessUsers: string[];
  /** [新增] 更新时间范围-开始，yyyy-MM-dd */
  updateStartTime: string;
  /** [新增] 更新时间范围-结束，yyyy-MM-dd */
  updateEndTime: string;
  /** [新增] 健康状态多选 */
  healthStatus: HealthLabelCode[];
  sortBy: string;
  orderBy: 'ascend' | 'descend';
};

export type WorkspaceInfoType = {
  workspaceName: string;
  roles: [];
};

/**
 * 列表行（ProjectResponse 扩展字段，全部可选、默认 null）
 */
export interface ProjectRowType extends DataType {
  id: number;
  name: string;
  description?: string;
  createBy?: string;
  workspaceName?: string;
  createTime?: string;
  updateTime?: string;
  releaseUsers?: string[];
  editUsers?: string[];
  accessUsers?: string[];
  editable?: boolean;
  /** [新增] 工作流数（降级 null） */
  workflowCount?: number | null;
  /** [新增] 节点数（降级 null） */
  nodeCount?: number | null;
  /** [新增] 数据源数 */
  dataSourceCount?: number | null;
  /** [新增] 成员数（去重） */
  memberCount?: number | null;
  /** [新增] 最近工作流更新时间（降级 null） */
  latestWorkflowUpdateTime?: string | null;
  /** [新增] 健康标签列表 */
  healthLabels?: HealthLabelCode[];
  /** [新增] 统计降级标记 */
  statsDegraded?: boolean | null;
}

/** 详情抽屉-工作流摘要 */
export interface WorkflowSummaryVO {
  orchestratorId: number;
  name: string;
  updateTime?: string;
  status?: string;
}

/** 详情抽屉-节点类型分布 */
export interface NodeTypeDistributionVO {
  jobType: string;
  nodeTypeName?: string;
  count: number;
}

/** 详情抽屉-数据源摘要行 */
export interface DataSourceSummaryVO {
  dataSourceName?: string;
  dataSourceType?: string;
  [key: string]: any;
}

/** 详情抽屉聚合 VO */
export interface ProjectDetailVO {
  basicInfo?: DataType;
  workflowList?: WorkflowSummaryVO[];
  workflowDegraded?: boolean;
  dataSourceList?: DataSourceSummaryVO[];
  nodeTypeDistribution?: NodeTypeDistributionVO[];
  nodeDegraded?: boolean;
}
