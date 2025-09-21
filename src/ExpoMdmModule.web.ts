import { registerWebModule, NativeModule } from 'expo';

import { ExpoMdmModuleEvents } from './ExpoMdm.types';

class ExpoMdmModule extends NativeModule<ExpoMdmModuleEvents> {
  PI = Math.PI;
  async getManagedConfigAsync(): Promise<Record<string, any>> {
    console.log('getManagedConfigAsync is not implemented on web.');
    return {};
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoMdmModule, 'ExpoMdmModule');
