export interface DataType {
  [key: string]: any;
}

export interface NodeTypeDetail {
  nodeTypes?: any[];
  isWhite?: boolean;
}

export type SearchFormType = {
  projectNameList: string[];
  orchestratorName: string;
  nodeTypeNameList: string[];
  nodeNameList: string[];
  viewId: string;
  // sortBy: string;
  // orderBy: 'ascend' | 'descend';
};

export type EditNodeType = {
  nodeId: string;
  nodeName: string;
  viewId: string;
  nodeDesc: string;
  // sortBy: string;
  // orderBy: 'ascend' | 'descend';
};
