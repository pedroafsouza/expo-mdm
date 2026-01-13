import ExpoModulesCore
import Foundation
import UIKit

public class ExpoMdmModule: Module {
  // The key used by iOS MDM to store managed configuration
  // This key is read from UserDefaults where MDM providers store app configuration
  let managedConfigKey = "com.apple.configuration.managed"

  // Event names used for sending data to the JavaScript layer
  private let APP_CONFIG_CHANGED_EVENT = "onManagedAppConfigChange"
  private let APP_LOCK_STATUS_CHANGED_EVENT = "onAppLockStatusChange"

  // Observer for configuration changes
  private var configObserver: NSObjectProtocol?
  private var guidedAccessObserver: NSObjectProtocol?

  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  public func definition() -> ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoMdm')` in JavaScript.
    Name("ExpoMdm")

    // Defines event names that the module can send to JavaScript.
    Events(APP_CONFIG_CHANGED_EVENT, APP_LOCK_STATUS_CHANGED_EVENT)

    // Defines constants that can be accessed from JavaScript
    Constants {
      [
        "APP_CONFIG_CHANGED": APP_CONFIG_CHANGED_EVENT,
        "APP_LOCK_STATUS_CHANGED": APP_LOCK_STATUS_CHANGED_EVENT
      ]
    }

    // This block is executed when the module starts observing events
    OnStartObserving {
      self.registerObservers()
    }

    // This block is executed when the module stops observing events
    OnStopObserving {
      self.unregisterObservers()
    }

    // --- Asynchronous functions that are exposed to JavaScript ---

    // Checks if MDM is supported and configured on this device
    AsyncFunction("isSupported") { (promise: Promise) in
      promise.resolve(self.isMDMSupported())
    }

    // Retrieves the MDM configuration as a dictionary
    AsyncFunction("getConfiguration") { (promise: Promise) in
      let config = self.getManagedConfiguration()
      promise.resolve(config)
    }

    // Checks if app locking (Guided Access) is allowed/enabled
    // Note: iOS Guided Access is different from Android's lock task mode
    // It's primarily controlled by system settings and requires manual activation
    AsyncFunction("isAppLockingAllowed") { (promise: Promise) in
      promise.resolve(self.isGuidedAccessEnabled())
    }

    // Checks if the app is currently in Guided Access mode
    AsyncFunction("isAppLocked") { (promise: Promise) in
      promise.resolve(self.isInGuidedAccessMode())
    }

    // iOS doesn't support programmatic locking like Android
    // Guided Access must be enabled manually through Settings or triple-click
    AsyncFunction("lockApp") { (promise: Promise) in
      // On iOS, we cannot programmatically enter Guided Access mode
      // This would require the user to manually enable it via triple-click or Settings
      promise.resolve(false)
    }

    // iOS doesn't support programmatic unlocking
    AsyncFunction("unlockApp") { (promise: Promise) in
      // On iOS, exiting Guided Access requires user authentication (passcode)
      // This cannot be done programmatically for security reasons
      promise.resolve(false)
    }
  }

  // MARK: - Private Helper Methods

  // Checks if MDM is supported by checking if managed configuration exists
  private func isMDMSupported() -> Bool {
    let managedConfig = UserDefaults.standard.dictionary(forKey: managedConfigKey)
    return managedConfig != nil && !managedConfig!.isEmpty
  }

  // Retrieves the managed configuration from UserDefaults
  // MDM providers (like Intune, Jamf, etc.) store configuration here
  private func getManagedConfiguration() -> [String: Any] {
    if let managedConfig = UserDefaults.standard.dictionary(forKey: managedConfigKey) {
      return managedConfig
    }
    return [:]
  }

  // Checks if Guided Access is currently enabled on the device
  private func isGuidedAccessEnabled() -> Bool {
    return UIAccessibility.isGuidedAccessEnabled
  }

  // Checks if the app is currently running in Guided Access mode
  private func isInGuidedAccessMode() -> Bool {
    return UIAccessibility.isGuidedAccessEnabled
  }

  // Registers observers for configuration and Guided Access changes
  private func registerObservers() {
    // Observer for UserDefaults changes (MDM configuration changes)
    configObserver = NotificationCenter.default.addObserver(
      forName: UserDefaults.didChangeNotification,
      object: nil,
      queue: .main
    ) { [weak self] _ in
      guard let self = self else { return }
      let config = self.getManagedConfiguration()
      self.sendEvent(self.APP_CONFIG_CHANGED_EVENT, ["config": config])
    }

    // Observer for Guided Access status changes
    guidedAccessObserver = NotificationCenter.default.addObserver(
      forName: UIAccessibility.guidedAccessStatusDidChangeNotification,
      object: nil,
      queue: .main
    ) { [weak self] _ in
      guard let self = self else { return }
      let isLocked = self.isInGuidedAccessMode()
      self.sendEvent(self.APP_LOCK_STATUS_CHANGED_EVENT, ["isLocked": isLocked])
    }
  }

  // Unregisters all observers
  private func unregisterObservers() {
    if let observer = configObserver {
      NotificationCenter.default.removeObserver(observer)
      configObserver = nil
    }

    if let observer = guidedAccessObserver {
      NotificationCenter.default.removeObserver(observer)
      guidedAccessObserver = nil
    }
  }
}
