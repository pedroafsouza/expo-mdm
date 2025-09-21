// Taken from https://github.com/stashenergy/react-native-msal/tree/master/plugin
import { ConfigPlugin, withPlugins } from "@expo/config-plugins";

import { withAndroidAppRestrictions } from "./withAndroidAppRestrictions";
import { withIosAppRestrictions } from "./withIosAppRestrictions";
import { MdmMap } from "./types";

const withReactNativeMSAL: ConfigPlugin<{
  map: MdmMap;
}> = (config, map) => {
  return withPlugins(config, [
    [withAndroidAppRestrictions, map],
    withIosAppRestrictions,
  ]);
};

export default withReactNativeMSAL;
