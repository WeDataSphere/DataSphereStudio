export interface DataType {
  [key: string]: any;
}

export type SearchFormType = {
  workspaceId: string;
  projectNames: string[];
  createUsers: string[];
  releaseUsers: string[];
  editUsers: string[];
  accessUsers: string[];
  sortBy: string;
  orderBy: 'ascend' | 'descend';
};

export type WorkspaceInfoType = {
  workspaceName: string;
  roles: [];
};
