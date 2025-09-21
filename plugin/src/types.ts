export type MdmConfig = Record<string, {
  title: string;
  type: 'string' | 'bool' | 'number';
  defaultValue?: string | boolean | number;
  description?: string;
}>;


export type MdmMap = Record<string,MdmConfig>;