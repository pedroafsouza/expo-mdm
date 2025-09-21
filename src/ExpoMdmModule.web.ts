import { registerWebModule, NativeModule } from 'expo';

import { ExpoMdmModuleEvents } from './ExpoMdm.types';

class ExpoMdmModule extends NativeModule<ExpoMdmModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoMdmModule, 'ExpoMdmModule');
