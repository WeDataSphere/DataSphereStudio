export interface BaseType {
  [key: string]: any;
}

export type SignalType = 'datachecker' | 'eventsender' | 'eventreceiver';

export interface ColumnType {
  type: string;
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
  templateName: string;
  status: string;
};

export interface UiItemType {
  key: string;
  uiType: string;
  desc: string;
  lableName: string;
  [key?: string]: any;
}

export type StatusStyleType = {
  color: '#b7b7bc' | '#5384ff' | '#ff4d4f';
  cursor?: 'pointer';
  'pointer-events'?: 'none';
  'margin-right'?: string;
};

export type ViewItem = {
  db: string;
  table: string;
  view: boolean;
};
