import { NativeModule, requireNativeModule } from 'expo';

import { ExpoMdmModuleEvents } from './ExpoMdm.types';

declare class ExpoMdmModule extends NativeModule<ExpoMdmModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoMdmModule>('ExpoMdm');
