export interface RelationSelectQueryField {
  field: string;
  label: string;
  options?: Array<{ label: string; value: any }>;
  placeholder?: string;
  type?: 'input' | 'select';
}

export interface RelationSelectColumn {
  field: string;
  label: string;
  minWidth?: number;
  width?: number;
}
