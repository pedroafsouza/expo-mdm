export type ExpoMdmModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
};

export type ChangeEventPayload = {
  value: string;
};

export type ManagedConfig = Record<string, string>;

export type ManagedConfigChangeEvent = {
  config: ManagedConfig;
};

export type AppLockStatusChangeEvent = {
  isLocked: boolean;
};
