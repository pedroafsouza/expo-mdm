import { useEvent } from 'expo';
import { getManagedConfigAsync } from 'expo-mdm';
import { useEffect, useState } from 'react';
import { Button, SafeAreaView, ScrollView, Text, View, Platform } from 'react-native';

export default function App() {
  const [config, setConfig] = useState<Record<string, any> | null>(null);

  useEffect(() => {
    getManagedConfigAsync().then(setConfig);
  }, []);

  useEvent(
    {
      addListener: (listener) => {
        const subscription = ExpoMdm.addListener('onChange', listener);
        return subscription;
      },
      removeListener: (subscription) => {
        subscription.remove();
      },
    },
    'onChange',
    (event) => {
      setConfig(event);
    }
  );

  const adbCommand = `adb shell am broadcast -a com.android.application.restrictions.changed -n com.example.expomdm/expo.modules.mdm.ExpoMdmModule\\$1`;

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.container}>
        <Text style={styles.header}>MDM Configuration</Text>
        <Group name="Managed Configuration">
          <Text>{JSON.stringify(config, null, 2)}</Text>
        </Group>
        {Platform.OS === 'android' && (
          <Group name="Testing on Android">
            <Text>Run the following command to test configuration changes:</Text>
            <Text style={styles.code}>{adbCommand}</Text>
          </Group>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

function Group(props: { name: string; children: React.ReactNode }) {
  return (
    <View style={styles.group}>
      <Text style={styles.groupHeader}>{props.name}</Text>
      {props.children}
    </View>
  );
}

const styles = {
  header: {
    fontSize: 30,
    margin: 20,
    textAlign: 'center' as const,
  },
  groupHeader: {
    fontSize: 20,
    marginBottom: 10,
    fontWeight: 'bold' as const,
  },
  group: {
    margin: 20,
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 20,
  },
  container: {
    flex: 1,
    backgroundColor: '#eee',
  },
  code: {
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
    backgroundColor: '#f0f0f0',
    padding: 10,
    borderRadius: 5,
    marginTop: 10,
  },
};
