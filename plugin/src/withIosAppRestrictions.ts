import { ConfigPlugin } from '@expo/config-plugins';
import { MdmConfig } from './types';

/**
 * iOS MDM Configuration Plugin
 *
 * Note: Unlike Android, iOS does not require build-time configuration files for MDM.
 * iOS MDM providers (Intune, Jamf, etc.) push configuration directly to UserDefaults
 * at runtime under the key 'com.apple.configuration.managed'.
 *
 * The MDM configuration schema is defined in the MDM provider's console, not in the app.
 * Therefore, this plugin does not need to modify any iOS files.
 *
 * This is a no-op plugin that exists for API consistency with the Android plugin.
 */
export const withIosAppRestrictions: ConfigPlugin<MdmConfig | void> = (config, _props) => {
  // iOS MDM configuration is handled at runtime by MDM providers
  // No build-time modifications needed
  return config;
};
