// Reexport the native module. On web, it will be resolved to ExpoMdmModule.web.ts
// and on native platforms to ExpoMdmModule.ts
import ExpoMdmModule from './ExpoMdmModule';
export { default as ExpoMdmView } from './ExpoMdmView';
export * from  './ExpoMdm.types';

export const PI = ExpoMdmModule.PI;

export function hello(): string {
  return ExpoMdmModule.hello();
}

export async function getManagedConfigAsync(): Promise<Record<string, any>> {
  return await ExpoMdmModule.getManagedConfigAsync();
}
