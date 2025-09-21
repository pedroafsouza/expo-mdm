import {
  ConfigPlugin,
  withAndroidManifest,
  withDangerousMod,
  withPlugins,
} from "@expo/config-plugins";
import fs from "fs";
import path from "path";
import { MdmMap } from "./types";
import { ExpoConfig } from "@expo/config-types";

// Assuming MdmMap is defined elsewhere, e.g.:
// export type MdmMap = { [key: string]: { type: string; defaultValue?: any; description?: string; } };

/**
 * Builds the XML content for app_restrictions.xml.
 * This file defines the managed configuration schema.
 * @param restrictions A map of the MDM configuration keys and their properties.
 * @returns A string containing the XML content for app_restrictions.xml.
 */
function buildRestrictionsXml(restrictions: MdmMap) {
  let xml = '<?xml version="1.0" encoding="utf-8"?>\n';
  xml +=
    '<restrictions xmlns:android="http://schemas.android.com/apk/res/android">\n';

  for (const key in restrictions) {
    const restriction = restrictions[key];
    xml += `    <restriction\n`;
    xml += `        android:key="${key}"\n`;
    // The title points to a string resource, e.g., @string/serverUrl
    xml += `        android:title="@string/${key}"\n`;
    xml += `        android:restrictionType="${restriction.type}"\n`;
    if (restriction.defaultValue !== undefined) {
      xml += `        android:defaultValue="${restriction.defaultValue}"\n`;
    }
    if (restriction.description) {
      // The description also points to a string resource, e.g., @string/serverUrlDescription
      xml += `        android:description="@string/${key}Description"\n`;
    }
    xml += `    />\n`;
  }

  xml += "</restrictions>";
  return xml;
}

/**
 * Builds the XML content for strings.xml.
 * This file provides the human-readable titles and descriptions for the restrictions.
 * @param restrictions A map of the MDM configuration keys and their properties.
 * @returns A string containing the XML content for strings.xml.
 */
function buildStringsXml(restrictions: MdmMap) {
  let xml = '<?xml version="1.0" encoding="utf-8"?>\n';
  xml += "<resources>\n";

  for (const key in restrictions) {
    const restriction = restrictions[key];
    // Add the string for the restriction's title. The value is the key itself.
    xml += `    <string name="${key}">${key}</string>\n`;

    // If a description is provided, add a corresponding string for it.
    if (restriction.description) {
      const descriptionKey = `${key}Description`;
      xml += `    <string name="${descriptionKey}">${restriction.description}</string>\n`;
    }
  }

  xml += "</resources>";
  return xml;
}
const processMapping = (mdmMap: MdmMap) => {
  // Define paths for the resource directories
  const resDir = path.join("android", "app", "src", "main", "res");
  const xmlDir = path.join(resDir, "xml");
  const valuesDir = path.join(resDir, "values");

  // Ensure the directories exist
  fs.mkdirSync(xmlDir, { recursive: true });
  fs.mkdirSync(valuesDir, { recursive: true });

  // --- 1. Create and write app_restrictions.xml ---
  const restrictionsXmlString = buildRestrictionsXml(mdmMap);
  const restrictionsXmlPath = path.join(xmlDir, "app_restrictions.xml");
  fs.writeFileSync(restrictionsXmlPath, restrictionsXmlString);
  console.log(`Wrote app restrictions to: ${restrictionsXmlPath}`);

  // --- 2. Create and write strings.xml ---
  // Note: This will overwrite any existing strings.xml file.
  const stringsXmlString = buildStringsXml(mdmMap);
  const stringsPath = path.join(valuesDir, "strings.xml");
  fs.writeFileSync(stringsPath, stringsXmlString);
  console.log(`Wrote restriction strings to: ${stringsPath}`);
};

/**
 * An Expo Config Plugin to automatically generate and add an
 * app_restrictions.xml and a corresponding strings.xml file for
 * Managed Device Configuration (MDM).
 */
export const withAndroidAppRestrictions: ConfigPlugin<MdmMap> = (
  config: any,
  props: MdmMap
) => {
  const mdmMap = props || {};
  return withDangerousMod(config, [
    "android",
    async (config) => {
      await processMapping(mdmMap);
      return config;
    },
  ]);
};
