import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoMdmViewProps } from './ExpoMdm.types';

const NativeView: React.ComponentType<ExpoMdmViewProps> =
  requireNativeView('ExpoMdm');

export default function ExpoMdmView(props: ExpoMdmViewProps) {
  return <NativeView {...props} />;
}
