import {
  withInfoPlist,
  withAndroidManifest,
  withDangerousMod,
  AndroidConfig,
  ConfigPlugin,
  createRunOncePlugin,
} from 'expo/config-plugins';
import fs from 'fs';
import path from 'path';

type MdmRestriction = {
  title: string;
  description?: string;
  type: 'string' | 'bool';
  defaultValue: string | boolean;
};

type MdmConfig = Record<string, MdmRestriction>;

function buildRestrictionsXml(restrictions: MdmConfig): string {
  let xml = '<?xml version="1.0" encoding="utf-8"?>\n';
  xml += '<restrictions xmlns:android="http://schemas.android.com/apk/res/android">\n';

  for (const key in restrictions) {
    const restriction = restrictions[key];
    xml += `    <restriction\n`;
    xml += `        android:key="${key}"\n`;
    xml += `        android:title="${restriction.title}"\n`;
    xml += `        android:restrictionType="${restriction.type}"\n`;
    if (restriction.defaultValue !== undefined) {
      xml += `        android:defaultValue="${restriction.defaultValue}"\n`;
    }
    if (restriction.description) {
      xml += `        android:description="${restriction.description}"\n`;
    }
    xml += `    />\n`;
  }

  xml += '</restrictions>';
  return xml;
}

const withAppRestrictionsXml: ConfigPlugin<MdmConfig> = (config, props) => {
  return withDangerousMod(config, [
    'android',
    async (config) => {
      const { projectRoot } = config.modRequest;
      const xmlDir = path.join(
        projectRoot,
        'android',
        'app',
        'src',
        'main',
        'res',
        'xml'
      );
      fs.mkdirSync(xmlDir, { recursive: true });
      const xmlPath = path.join(xmlDir, 'app_restrictions.xml');
      const xmlString = buildRestrictionsXml(props);
      fs.writeFileSync(xmlPath, xmlString);
      return config;
    },
  ]);
};

const withManagedConfiguration: ConfigPlugin<MdmConfig> = (config, props) => {
  return withInfoPlist(config, (config) => {
    const iosConfig: Record<string, any> = {};
    for (const key in props) {
      const { type, defaultValue } = props[key];
      iosConfig[key] = {
        Type: type === 'bool' ? 'Boolean' : 'String',
        DefaultValue: defaultValue,
      };
    }

    config.modResults['com.apple.managed.configuration'] = iosConfig;
    return config;
  });
};

const withMdm: ConfigPlugin<MdmConfig | void> = (config, props) => {
  const mdmConfig = props || {};

  config = withAppRestrictionsXml(config, mdmConfig);
  config = withManagedConfiguration(config, mdmConfig);

  return config;
};

export default createRunOncePlugin(withMdm, 'expo-mdm', '0.1.0');
