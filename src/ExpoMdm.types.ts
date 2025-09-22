export type ExpoMdmModuleEvents = {
  onChange: (params: MDMChangeEventPayload) => void;
};

export type MDMChangeEventPayload = {
  value: string;
};

export type MDMManagedConfig = Record<string, string | boolean | number>;

export type MDMManagedConfigChangeEvent = {
  config: MDMManagedConfig;
};

export type MDMAppLockStatusChangeEvent = {
  isLocked: boolean;
};
