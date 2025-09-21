import { ConfigPlugin, withInfoPlist } from '@expo/config-plugins';
import { MdmConfig } from './types';

export const withIosAppRestrictions: ConfigPlugin<MdmConfig | void> = (config, props) => {
  const mdmConfig = props || {};
  return withInfoPlist(config, (config) => {
    const iosConfig: Record<string, any> = {};
    for (const key in mdmConfig) {
      const { type, defaultValue } = mdmConfig[key];
      iosConfig[key] = {
        Type: type === 'bool' ? 'Boolean' : 'String',
        DefaultValue: defaultValue,
      };
    }

    config.modResults['com.apple.managed.configuration'] = iosConfig;
    return config;
  });
};
