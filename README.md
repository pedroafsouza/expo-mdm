# expo-mdm

My new module

# API documentation

- [Documentation for the latest stable release](https://docs.expo.dev/versions/latest/sdk/mdm/)
- [Documentation for the main branch](https://docs.expo.dev/versions/unversioned/sdk/mdm/)

# Installation in managed Expo projects

For [managed](https://docs.expo.dev/archive/managed-vs-bare/) Expo projects, please follow the installation instructions in the [API documentation for the latest stable release](#api-documentation). If you follow the link and there is no documentation available then this library is not yet usable within managed projects &mdash; it is likely to be included in an upcoming Expo SDK release.

# Installation in bare React Native projects

For bare React Native projects, you must ensure that you have [installed and configured the `expo` package](https://docs.expo.dev/bare/installing-expo-modules/) before continuing.

### Add the package to your npm dependencies

```
npm install expo-mdm
```

### Configure for Android




### Configure for iOS

Run `npx pod-install` after installing the npm package.

## Testing with ADB

You can test the managed app configuration on a development build without an MDM/EMM console by using `adb` commands. This is useful for verifying that your app correctly reads the restriction values.

Assuming you have an `app.json` configuration like this:

```json
{
  "expo": {
    "plugins": [
      [
        "expo-mdm",
        {
          "serverUrl": {
            "type": "string",
            "defaultValue": "https://default.server.com",
            "description": "The URL of the server."
          },
          "someBooleanFlag": {
            "type": "bool",
            "defaultValue": false
          }
        }
      ]
    ]
  }
}
```

You can set these restrictions using `adb`.

### 1. Get your application's package name

You can find this in your `app.json` under `expo.android.package`, or in `android/app/build.gradle` as `applicationId`. Let's assume it's `com.example.myapp`.

### 2. Set the restrictions

Use the `adb shell am set-app-restriction` command. The format is `adb shell am set-app-restriction <PACKAGE_NAME> <KEY> <VALUE>`.

**To set a string value:**

```bash
adb shell am set-app-restriction com.example.myapp serverUrl "https://my-test-server.com"
```

**To set a boolean value:**

```bash
adb shell am set-app-restriction com.example.expomdm apiUrl 'http://www.microsoft.com'
```

### 3. Clear all restrictions for your app

To revert to default values, you can clear all restrictions you've set.

```bash
adb shell am clear-app-restrictions com.example.myapp
```

After running these commands, you may need to restart your app for the changes to be picked up by `expo-mdm`.

# Contributing

Contributions are very welcome! Please refer to guidelines described in the [contributing guide]( https://github.com/expo/expo#contributing).
