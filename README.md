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