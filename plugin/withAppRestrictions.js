const {
  withDangerousMod,
  withInfoPlist,
  createRunOncePlugin,
} = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

function buildRestrictionsXml(restrictions) {
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

const withAppRestrictionsXml = (config, mdmConfig) => {
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
      const xmlString = buildRestrictionsXml(mdmConfig);
      fs.writeFileSync(xmlPath, xmlString);
      return config;
    },
  ]);
};

const withManagedConfiguration = (config, mdmConfig) => {
  return withInfoPlist(config, (config) => {
    const iosConfig = {};
    for (const key in mdmConfig) {
      const { type, defaultValue } = mdmConfig[key];
      iosConfig[key] = {
        Type: type === 'bool' ? 'Boolean' : 'String',
        DefaultValue: defaultValue,
      };
    }

    config.modResults['com.apple.managed.configuration'] = {
      configuration: iosConfig,
    };
    return config;
  });
};

const withMdm = (config) => {
  const mdmConfig = config.mdm || {};

  config = withAppRestrictionsXml(config, mdmConfig);
  config = withManagedConfiguration(config, mdmConfig);

  return config;
};

module.exports = createRunOncePlugin(withMdm, 'expo-mdm', '0.1.0');
