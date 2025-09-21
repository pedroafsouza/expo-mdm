package expo.modules.mdm

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.RestrictionsManager
import android.os.Build
import android.os.Bundle
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoMdmModule : Module() {
    private var restrictionReceiver: BroadcastReceiver? = null

    // Event names used for sending data to the JavaScript layer
    companion object {
        const val APP_CONFIG_CHANGED_EVENT = "onManagedAppConfigChange"
        const val APP_LOCK_STATUS_CHANGED_EVENT = "onAppLockStatusChange"
    }

    // Each module class must implement the definition function.
    override fun definition() = ModuleDefinition {
        // Sets the name of the module that JavaScript code will use.
        Name("ExpoMdm")

        // Defines event names that the module can send to JavaScript.
        Events(APP_CONFIG_CHANGED_EVENT, APP_LOCK_STATUS_CHANGED_EVENT)

        // Defines constant properties on the module, matching the original Java code.
        Constants {
            mapOf(
                "APP_CONFIG_CHANGED" to APP_CONFIG_CHANGED_EVENT,
                "APP_LOCK_STATUS_CHANGED" to APP_LOCK_STATUS_CHANGED_EVENT
            )
        }

        // This block is executed when the module is initialized and starts observing events.
        // It's the equivalent of the original module's `initialize` and `onHostResume`.
        OnStartObserving {
            maybeRegisterReceiver()
        }

        // This block is executed when the module stops observing events.
        // It's the equivalent of the original module's `onHostPause` and `onHostDestroy`.
        OnStopObserving {
            maybeUnregisterReceiver()
        }

        // --- Helper functions from the original Java code ---

        fun isMDMSupported(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return false
            }
            val reactContext = appContext.reactContext
            if(reactContext == null){
                return false
            }
            val restrictionsManager =
                reactContext.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
            return restrictionsManager.applicationRestrictions?.size() ?: 0 > 0
        }

        fun getAppRestrictionsBundle(): Bundle {

            val reactContext = appContext.reactContext
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && reactContext != null) {
                val restrictionsManager =
                    reactContext.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
                return restrictionsManager.applicationRestrictions
            }
            return Bundle()
        }

        fun isLockStatePermitted(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return false
            }
            val reactContext = appContext.reactContext
            if(reactContext == null){
                return false
            }
            val dpm =
                reactContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return dpm.isLockTaskPermitted(reactContext.packageName)
        }

        fun isLockState(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return false
            }
            val reactContext = appContext.reactContext
            if(reactContext == null){
                return false
            }
            val am =
                reactContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
            } else {
                @Suppress("Deprecation")
                am.isInLockTaskMode
            }
        }

        // --- Asynchronous functions that are exposed to JavaScript ---

        AsyncFunction("isSupported") { promise: Promise ->
            promise.resolve(isMDMSupported())
        }

        AsyncFunction("getConfiguration") { promise: Promise ->
            if (isMDMSupported()) {
                val appRestrictions = getAppRestrictionsBundle()
                val data = mutableMapOf<String, String>()
                for (key in appRestrictions.keySet()) {
                    data[key] = appRestrictions.getString(key) ?: ""
                }
                promise.resolve(data)
            } else {
                promise.reject(CodedException("Managed App Config is not supported"))
            }
        }

        AsyncFunction("isAppLockingAllowed") { promise: Promise ->
            promise.resolve(isLockStatePermitted())
        }

        AsyncFunction("isAppLocked") { promise: Promise ->
            try {
                promise.resolve(isLockState())
            } catch (e: Exception) {
                promise.reject(CodedException("Failed to check lock state", e))
            }
        }

        AsyncFunction("lockApp") { promise: Promise ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isLockState()) {
                    val activity = appContext.currentActivity
                    if (activity != null) {
                        activity.startLockTask()
                        promise.resolve(true)
                    } else {
                        promise.reject(CodedException("Unable to lock app: Activity is null"))
                    }
                } else {
                    promise.resolve(false)
                }
            } catch (e: Exception) {
                promise.reject(CodedException("Unable to lock app", e))
            }
        }

        AsyncFunction("unlockApp") { promise: Promise ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isLockState()) {
                    val activity = appContext.currentActivity
                    if (activity != null) {
                        activity.stopLockTask()
                        promise.resolve(true)
                    } else {
                        promise.reject(CodedException("Unable to unlock app: Activity is null"))
                    }
                } else {
                    promise.resolve(false)
                }
            } catch (e: Exception) {
                promise.reject(CodedException("Unable to unlock app", e))
            }
        }
    }

    // --- Private methods for BroadcastReceiver management ---

    private fun maybeUnregisterReceiver() {
        if (restrictionReceiver == null) {
            return
        }
        val reactContext = appContext.reactContext
        if(reactContext == null){
            return
        }
        reactContext.unregisterReceiver(restrictionReceiver)
        restrictionReceiver = null
    }

    private fun maybeRegisterReceiver() {
        val reactContext = appContext.reactContext
        if (reactContext == null) {
            return;
        }
        if (restrictionReceiver != null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        val restrictionsManager =
            reactContext?.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
        val restrictionFilter = IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
        restrictionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val appRestrictions = restrictionsManager.applicationRestrictions
                val data = mutableMapOf<String, Any>()
                for (key in appRestrictions.keySet()) {
                    data[key] = appRestrictions.getString(key) ?: ""
                }
                // Use the Expo Module's built-in `sendEvent` function
                sendEvent(APP_CONFIG_CHANGED_EVENT, data)
            }
        }
        reactContext.registerReceiver(restrictionReceiver, restrictionFilter)
    }
}