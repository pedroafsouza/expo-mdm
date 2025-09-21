import ExpoModulesCore

public class ExpoMdmModule: Module {
  let managedConfigKey = "com.apple.managed.configuration"

  public override init() {
    super.init()
    NotificationCenter.default.addObserver(self, selector: #selector(userDefaultsDidChange), name: UserDefaults.didChangeNotification, object: nil)
  }

  deinit {
    NotificationCenter.default.removeObserver(self)
  }

  @objc func userDefaultsDidChange() {
    let managedConfig = UserDefaults.standard.dictionary(forKey: managedConfigKey) ?? [:]
    self.sendEvent("onChange", managedConfig)
  }

  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  public func definition() -> ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoMdm')` in JavaScript.
    Name("ExpoMdm")

    // Defines constant property on the module.
    Constant("PI") {
      Double.pi
    }

    // Defines event names that the module can send to JavaScript.
    Events("onChange")

    // Defines a JavaScript synchronous function that runs the native code on the JavaScript thread.
    Function("hello") {
      return "Hello world! 👋"
    }

    // Defines a JavaScript function that always returns a Promise and whose native code
    // is by default dispatched on the different thread than the JavaScript runtime runs on.
    AsyncFunction("getManagedConfigAsync") { () -> [String: Any] in
      return UserDefaults.standard.dictionary(forKey: managedConfigKey) ?? [:]
    }

    // Enables the module to be used as a native view. Definition components that are accepted as part of the
    // view definition: Prop, Events.
    View(ExpoMdmView.self) {
      // Defines a setter for the `url` prop.
      Prop("url") { (view: ExpoMdmView, url: URL) in
        if view.webView.url != url {
          view.webView.load(URLRequest(url: url))
        }
      }

      Events("onLoad")
    }
  }
}
