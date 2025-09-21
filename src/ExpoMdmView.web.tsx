import * as React from 'react';

import { ExpoMdmViewProps } from './ExpoMdm.types';

export default function ExpoMdmView(props: ExpoMdmViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
