 

export const ExpoMdmModuleWeb = {
  isSupported(): Promise<boolean> {
    return Promise.resolve(false);
  },
  getConfiguration(): Promise<Record<string, string>> {
    return Promise.resolve({});
  },
  isAppLockingAllowed(): Promise<boolean> {
    return Promise.resolve(false);
  },
  isAppLocked(): Promise<boolean> {
    return Promise.resolve(false);
  },
  lockApp(): Promise<boolean> {
    return Promise.resolve(false);
  },
  unlockApp(): Promise<boolean> {
    return Promise.resolve(false);
  }
 };
