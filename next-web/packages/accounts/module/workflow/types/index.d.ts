export interface BaseType {
  [key: string]: any;
}

export interface ColumnType {
  type: string;
  text?: string;
  prop?: string;
  label?: string;
  sortable?: boolean;
  ellipsis?: boolean;
  minWidth: number;
  formatter?: (row: BaseType) => unknown;
}

export type SearchFormType = {
  orchestratorName: string;
  projectList: string[];
  status: string;
  sortBy: string;
  orderBy: 'ascend' | 'descend';
};

export type StatusStyleType = {
  color: '#b7b7bc' | '#5384ff' | '#ff4d4f';
  cursor?: 'pointer';
  'pointer-events'?: 'none';
};

export type WorkflowEiditFormType = {
  projectId: string;
  orchestratorId: string;
  orchestratorName: string;
  templateIds: string[];
  isDefaultReference: string | null;
  proxyUser: string;
  description: string;
};

export interface TemplateNodeItemType {
  templateId: string;
  templateName: string;
  [key: string]: any;
}

export type TemplateItemType = {
  enginType: string;
  child: TemplateNodeItemType[];
};

export interface FlowDiffParamType {
  workspaceId: string;
  projectName: string;
  orchestratorId: string;
  labels: { route: string };
  filePath: string;
  publish: boolean;
  comment: string;
  dssLabel: string;
  orchestratorList: string[];
  submitRequestList: {
    orchestratorId: string;
    projectName: string;
    labels: { route: string };
    comment: string;
  }[];
}
