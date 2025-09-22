# expo-mdm

A React Native library for integrating Mobile Device Management (MDM) capabilities into Expo applications.

## Overview

This project has been created with the intention of integrating MDM functionality for React Native applications. While there is already a project called `react-native-mdm` that provides similar functionality, this library aims to provide better Expo compatibility and modern implementation.

## Platform Support

- ✅ **Android** - Fully implemented
- ⏳ **iOS** - Coming soon (contributions welcome!)

Currently, the code has only been developed for Android. We welcome contributions for iOS implementation or will implement it in future releases.

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
          }
        }
      ]
    ]
  }
}
```

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

### Android Testing with TestDPC

The easiest way to test MDM functionality on Android is to use TestDPC (Test Device Policy Controller). Here are some important considerations:

#### Prerequisites

1. **Profile Support**: You can use TestDPC with either work profile or personal profile
2. **Device Admin Setup**: When setting TestDPC as device admin, ensure you have no Google accounts signed in if you're using your personal device
3. **Development Environment**: Make sure you have Android development environment properly configured

#### Setup Steps

1. Install TestDPC from Google Play Store
2. Follow the device admin setup process
3. Configure your test policies
4. Test your expo-mdm integration

#### Testing App Restrictions

1. Open TestDPC
2. Navigate to "Manage app restrictions"
3. Find your app in the list
4. Configure the restrictions defined in your `AppRestrictionsMap`
5. Apply the settings and test in your app

## Contributing

We welcome contributions! Please feel free to submit issues, feature requests, or pull requests.

### Areas where we need help:
- iOS implementation
- Documentation improvements
- Additional Android features
- Testing and bug reports

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

If you encounter any issues or have questions, please file an issue on our GitHub repository.