export interface DataType {
  [key: string]: any;
}

export type SearchFormType = {
  projectNameList: string[];
  orchestratorName: string;
  nodeTypeNameList: string[];
  nodeNameList: string[];
  refTemplate: boolean;
  templateNameList: string[];
  reuseEngine: boolean;
  // sortBy: string;
  // orderBy: 'ascend' | 'descend';
};

export type advanceSearchFormType = {
  sparkExecutorMemory: string;
  sparkDriverMemory: string;
  sparkConf: string;
  sparkExecutorCore: string;
  sparkExecutorInstances: string;
};
