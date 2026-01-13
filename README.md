# expo-mdm

A React Native library for integrating Mobile Device Management (MDM) capabilities into Expo applications.

## Overview

This project has been created with the intention of integrating MDM functionality for React Native applications. While there is already a project called `react-native-mdm` that provides similar functionality, this library aims to provide better Expo compatibility and modern implementation.

## Platform Support

- ✅ **Android** - Fully implemented
- ✅ **iOS** - Fully implemented

Both platforms support MDM configuration reading and app locking features.

## Getting Started

### Installation

```bash
npm install expo-mdm
# or
yarn add expo-mdm
```

### Configuration

Add the expo-mdm plugin to your `app.json` or `app.config.js`:

```json
{
  "expo": {
    "plugins": [
      [
        "expo-mdm",
        {
          "android": {
            "QueryPackages": [
              "com.azure.authenticator",
              "UserDetailsClient.Droid",
              "com.microsoft.windowsintune.companyportal"
            ],
            "AppRestrictionsMap": {
              "apiUrl": {
                "title": "API URL",
                "description": "The URL of the API server.",
                "type": "string",
                "defaultValue": "https://api.example.com"
              },
              "enableAnalytics": {
                "title": "Enable Analytics",
                "description": "Whether to enable analytics.",
                "type": "bool",
                "defaultValue": false
              }
            }
          },
          "ios": {
            "AppRestrictionsMap": {
              "apiUrl": {
                "title": "API URL",
                "type": "string",
                "defaultValue": "https://api.example.com"
              },
              "enableAnalytics": {
                "title": "Enable Analytics",
                "type": "bool",
                "defaultValue": false
              }
            }
          }
        }
      ]
    ]
  }
}
```

**Note:** The iOS configuration uses the same `AppRestrictionsMap` structure but does not require `QueryPackages` as iOS handles MDM differently.

#### Plugin Configuration Options

##### QueryPackages

The `QueryPackages` array specifies which package names your app can query for. This is essential for MDM functionality as it allows your app to detect and interact with specific MDM-related applications.

**Supported packages:**
- `com.azure.authenticator` - Microsoft Authenticator app
- `UserDetailsClient.Droid` - User details client for Android
- `com.microsoft.windowsintune.companyportal` - Microsoft Intune Company Portal

**Example:**
```json
"QueryPackages": [
  "com.azure.authenticator",
  "com.microsoft.windowsintune.companyportal"
]
```

##### AppRestrictionsMap

The `AppRestrictionsMap` defines the managed app configuration that can be set by MDM administrators. Each restriction has the following properties:

- `title` - Display name for the restriction
- `description` - Description of what the restriction does
- `type` - Data type (`string`, `bool`, `integer`, `choice`, `multi-select`)
- `defaultValue` - Default value if not set by MDM

**Supported types:**
- `string` - Text input
- `bool` - Boolean (true/false)
- `integer` - Numeric input
- `choice` - Single selection from predefined options
- `multi-select` - Multiple selections from predefined options

**Example configurations:**

```json
"AppRestrictionsMap": {
  "serverUrl": {
    "title": "Server URL",
    "description": "The URL of the backend server",
    "type": "string",
    "defaultValue": "https://api.company.com"
  },
  "debugMode": {
    "title": "Debug Mode",
    "description": "Enable debug logging",
    "type": "bool",
    "defaultValue": false
  },
  "maxRetries": {
    "title": "Max Retries",
    "description": "Maximum number of retry attempts",
    "type": "integer",
    "defaultValue": 3
  }
}
```

## Testing

Testing MDM functionality requires different approaches for each platform. Below are detailed instructions for both Android and iOS.

### Testing on Android

The easiest way to test MDM functionality on Android is to use **TestDPC** (Test Device Policy Controller), a free testing tool provided by Google.

#### Prerequisites

1. **Profile Support**: You can use TestDPC with either work profile or personal profile
2. **Device Admin Setup**: When setting TestDPC as device admin, ensure you have no Google accounts signed in if you're using your personal device
3. **Development Environment**: Make sure you have Android development environment properly configured

#### Setup Steps

1. **Install TestDPC** from Google Play Store
2. **Set up Device Admin**:
   - Open TestDPC
   - Follow the device admin setup wizard
   - Grant all necessary permissions
3. **Configure your test policies**
4. **Install your app** (either via ADB or as a managed app)

#### Testing App Restrictions

1. Open **TestDPC**
2. Navigate to **"Manage app restrictions"**
3. Find your app in the list
4. Configure the restrictions defined in your `AppRestrictionsMap`:
   - Set values for `apiUrl`, `enableAnalytics`, etc.
   - These values will be immediately available to your app
5. **Test in your app**:
   ```javascript
   import { getConfiguration, isSupported } from 'expo-mdm';

   const supported = await isSupported();
   console.log('MDM Supported:', supported);

   const config = await getConfiguration();
   console.log('MDM Config:', config);
   // Output: { apiUrl: "https://...", enableAnalytics: true }
   ```

#### Testing App Lock Mode (Kiosk Mode)

1. In TestDPC, navigate to **"Device Policy Management"** → **"Lock task mode"**
2. Add your app package name to the lock task whitelist
3. In your app, test the lock functions:
   ```javascript
   import { lockApp, unlockApp, isAppLockingAllowed, isAppLocked } from 'expo-mdm';

   const canLock = await isAppLockingAllowed();
   if (canLock) {
     await lockApp(); // Enters kiosk mode
     // User cannot exit app or access other apps

     await unlockApp(); // Exits kiosk mode
   }
   ```

#### Alternative Android Testing with Microsoft Intune

For enterprise testing:
1. Enroll device in Microsoft Intune
2. Create app configuration policy in Intune admin center
3. Deploy configuration to test devices
4. Test with production MDM configuration

### Testing on iOS

iOS MDM testing requires an Apple MDM solution. Here are several options:

#### Option 1: Microsoft Intune (Recommended for Development)

1. **Set up Intune trial account**:
   - Sign up for a free Microsoft 365 trial
   - Enable Intune in the admin center

2. **Enroll your iOS device**:
   - Install **Microsoft Intune Company Portal** from App Store
   - Sign in with your test account
   - Follow enrollment instructions

3. **Configure App Configuration Policy**:
   - In Intune admin center, navigate to **Apps** → **App configuration policies**
   - Create a new policy for iOS/iPadOS managed apps
   - Select your app
   - Add configuration keys matching your `AppRestrictionsMap`:
     ```xml
     <key>apiUrl</key>
     <string>https://api.example.com</string>
     <key>enableAnalytics</key>
     <true/>
     ```
   - Assign to test users/devices

4. **Test in your app**:
   ```javascript
   import { getConfiguration, isSupported } from 'expo-mdm';

   const supported = await isSupported();
   console.log('MDM Supported:', supported);

   const config = await getConfiguration();
   console.log('MDM Config:', config);
   // Output: { apiUrl: "https://...", enableAnalytics: 1 }
   ```

#### Option 2: Apple Business Manager + MDM

For production testing:
1. Enroll in **Apple Business Manager**
2. Connect an MDM solution (Jamf, Workspace ONE, etc.)
3. Create managed app configuration
4. Deploy to test devices

#### Option 3: Manual Testing with Xcode (Development Only)

For quick local testing without MDM enrollment:

1. In your iOS project, add managed configuration to your scheme:
   - Edit Scheme → Run → Arguments
   - Add to **Environment Variables**:
     ```
     com.apple.configuration.managed = {"apiUrl":"https://test.com","enableAnalytics":true}
     ```

2. Or manually set UserDefaults in your app (for development only):
   ```javascript
   // Development testing only - this will be replaced by actual MDM in production
   import { NativeModules } from 'react-native';

   if (__DEV__) {
     // This simulates MDM configuration for testing
     NativeModules.UserDefaults?.setObject(
       { apiUrl: "https://test.com", enableAnalytics: true },
       "com.apple.configuration.managed"
     );
   }
   ```

#### Testing iOS Guided Access (App Lock Mode)

iOS uses **Guided Access** instead of a programmatic lock mode. To test:

1. **Enable Guided Access**:
   - Go to **Settings** → **Accessibility** → **Guided Access**
   - Toggle on and set a passcode

2. **Configure Triple Click**:
   - Settings → **Accessibility** → **Accessibility Shortcut**
   - Select **Guided Access**

3. **Test in your app**:
   ```javascript
   import { isAppLockingAllowed, isAppLocked } from 'expo-mdm';

   const guidedAccessEnabled = await isAppLockingAllowed();
   console.log('Guided Access Enabled:', guidedAccessEnabled);

   const inGuidedAccess = await isAppLocked();
   console.log('Currently in Guided Access:', inGuidedAccess);
   ```

4. **Activate Guided Access**:
   - Triple-click the side/home button while in your app
   - Or go to Settings → Guided Access → Start

**Note**: iOS does not allow programmatic entry/exit of Guided Access for security reasons. The `lockApp()` and `unlockApp()` methods will return `false` on iOS. Users must manually enable/disable Guided Access.

### Testing Event Listeners

Both platforms support listening to MDM configuration changes:

```javascript
import { addEventListener } from 'expo-mdm';

// Listen for configuration changes
const subscription = addEventListener('onManagedAppConfigChange', (event) => {
  console.log('MDM config changed:', event.config);
});

// Listen for lock status changes
const lockSubscription = addEventListener('onAppLockStatusChange', (event) => {
  console.log('Lock status changed:', event.isLocked);
});

// Clean up
subscription.remove();
lockSubscription.remove();
```

## Contributing

We welcome contributions! Please feel free to submit issues, feature requests, or pull requests.

### Areas where we need help:
- Documentation improvements
- Additional features for both platforms
- Testing and bug reports
- Integration examples with different MDM providers

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

If you encounter any issues or have questions, please file an issue on our GitHub repository.