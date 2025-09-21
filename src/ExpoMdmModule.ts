import { NativeModule, requireNativeModule } from 'expo';

import { ExpoMdmModuleEvents } from './ExpoMdm.types';

declare class ExpoMdmModule extends NativeModule<ExpoMdmModuleEvents> {
  getManagedConfigAsync(): Promise<Record<string, any>>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoMdmModule>('ExpoMdm');
