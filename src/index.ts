import ExpoMdmModule from "./ExpoMdm";
import {
  MDMManagedConfig,
  MDMManagedConfigChangeEvent,
  MDMAppLockStatusChangeEvent,
} from "./ExpoMdm.types";

export function isSupported(): Promise<boolean> {
  return ExpoMdmModule.isSupported();
}

export function getConfiguration(): Promise<MDMManagedConfig> {
  return ExpoMdmModule.getConfiguration();
}

export function isAppLockingAllowed(): Promise<boolean> {
  return ExpoMdmModule.isAppLockingAllowed();
}

export function isAppLocked(): Promise<boolean> {
  return ExpoMdmModule.isAppLocked();
}

export function lockApp(): Promise<boolean> {
  return ExpoMdmModule.lockApp();
}

export function unlockApp(): Promise<boolean> {
  return ExpoMdmModule.unlockApp();
}

export { MDMManagedConfig, MDMManagedConfigChangeEvent, MDMAppLockStatusChangeEvent };