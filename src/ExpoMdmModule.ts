import { NativeModule, requireNativeModule } from 'expo';

import { ExpoMdmModuleEvents } from './ExpoMdm.types';
import { Platform } from 'react-native';
import { ExpoMdmModuleWeb } from './ExpoMdmModule.web';

declare class ExpoMdmModule extends NativeModule<ExpoMdmModuleEvents> {
  getManagedConfigAsync(): Promise<Record<string, any>>;
}

const ExpoMdmModuleToExport = Platform.OS === 'web' ? ExpoMdmModuleWeb : requireNativeModule<ExpoMdmModule>('ExpoMdm');


// This call loads the native module object from the JSI.
export default ExpoMdmModuleToExport;
