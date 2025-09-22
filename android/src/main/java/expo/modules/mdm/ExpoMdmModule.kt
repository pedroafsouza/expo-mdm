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
import android.util.Log
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.util.Objects

class ExpoMdmModule : Module() {
    private var restrictionReceiver: BroadcastReceiver? = null

    // Event names used for sending data to the JavaScript layer
    companion object {
        const val APP_CONFIG_CHANGED_EVENT = "onManagedAppConfigChange"
        const val APP_LOCK_STATUS_CHANGED_EVENT = "onAppLockStatusChange"
        private const val TAG = "ExpoMdmModule" // Tag for logging
    }

    // Each module class must implement the definition function.
    override fun definition() = ModuleDefinition {
        Log.d(TAG, "Module definition initializing.")
        // Sets the name of the module that JavaScript code will use.
        Name("ExpoMdm")

        // Defines event names that the module can send to JavaScript.
        Events(APP_CONFIG_CHANGED_EVENT, APP_LOCK_STATUS_CHANGED_EVENT)

        // Defines constant properties on the module, matching the original Java code.
        Constants {
            Log.d(TAG, "Setting constants.")
            mapOf(
                "APP_CONFIG_CHANGED" to APP_CONFIG_CHANGED_EVENT,
                "APP_LOCK_STATUS_CHANGED" to APP_LOCK_STATUS_CHANGED_EVENT
            )
        }

        // This block is executed when the module is initialized and starts observing events.
        // It's the equivalent of the original module's `initialize` and `onHostResume`.
        OnStartObserving {
            Log.d(TAG, "OnStartObserving: Starting to observe.")
            maybeRegisterReceiver()
        }

        // This block is executed when the module stops observing events.
        // It's the equivalent of the original module's `onHostPause` and `onHostDestroy`.
        OnStopObserving {
            Log.d(TAG, "OnStopObserving: Stopping observation.")
            maybeUnregisterReceiver()
        }

        // --- Helper functions from the original Java code ---

        fun isMDMSupported(): Boolean {
            Log.d(TAG, "isMDMSupported: Checking for MDM support.")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "isMDMSupported: SDK version too low (${Build.VERSION.SDK_INT}). Not supported.")
                return false
            }
            val reactContext = appContext.reactContext
            if(reactContext == null){
                Log.d(TAG, "isMDMSupported: React context is null. Not supported.")
                return false
            }
            val restrictionsManager =
                reactContext.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
            val hasRestrictions = restrictionsManager.applicationRestrictions?.size() ?: 0 > 0
            Log.d(TAG, "isMDMSupported: Result = $hasRestrictions, size =" + restrictionsManager.applicationRestrictions?.size())
            return hasRestrictions
        }

        fun getAppRestrictionsBundle(): Bundle {
            Log.d(TAG, "getAppRestrictionsBundle: Fetching app restrictions.")
            val reactContext = appContext.reactContext
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && reactContext != null) {
                val restrictionsManager =
                    reactContext.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
                val restrictions = restrictionsManager.applicationRestrictions
                Log.d(TAG, "getAppRestrictionsBundle: Found restrictions: ${restrictions.keySet()}")
                return restrictions
            }
            Log.d(TAG, "getAppRestrictionsBundle: No restrictions found or unsupported version.")
            return Bundle()
        }

        fun isLockStatePermitted(): Boolean {
            Log.d(TAG, "isLockStatePermitted: Checking if lock state is permitted.")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "isLockStatePermitted: SDK version too low. Not permitted.")
                return false
            }
            val reactContext = appContext.reactContext
            if(reactContext == null){
                Log.d(TAG, "isLockStatePermitted: React context is null. Not permitted.")
                return false
            }
            val dpm =
                reactContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val isPermitted = dpm.isLockTaskPermitted(reactContext.packageName)
            Log.d(TAG, "isLockStatePermitted: Result = $isPermitted for package ${reactContext.packageName}")
            return isPermitted
        }

        fun isLockState(): Boolean {
            Log.d(TAG, "isLockState: Checking current lock state.")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "isLockState: SDK version too low. Not in lock state.")
                return false
            }
            val reactContext = appContext.reactContext
            if(reactContext == null){
                Log.d(TAG, "isLockState: React context is null. Not in lock state.")
                return false
            }
            val am =
                reactContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val isInLockMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
            } else {
                @Suppress("Deprecation")
                am.isInLockTaskMode
            }
            Log.d(TAG, "isLockState: Result = $isInLockMode")
            return isInLockMode
        }

        // --- Asynchronous functions that are exposed to JavaScript ---

        AsyncFunction("isSupported") { promise: Promise ->
            Log.d(TAG, "AsyncFunction 'isSupported' called.")
            promise.resolve(isMDMSupported())
        }

        AsyncFunction("getConfiguration") { promise: Promise ->
            Log.d(TAG, "AsyncFunction 'getConfiguration' called.")
            val data = mutableMapOf<String, Any >()
            if (isMDMSupported()) {
                val appRestrictions = getAppRestrictionsBundle()
                for (key in appRestrictions.keySet()) {
                    data[key] = appRestrictions.get(key) ?: ""
                }
                Log.d(TAG, "getConfiguration: Resolving with data: $data")
                promise.resolve(data)
            } else {
                Log.d(TAG, "getConfiguration: Rejecting because MDM is not supported.")
                promise.resolve(data)
            }
        }

        AsyncFunction("isAppLockingAllowed") { promise: Promise ->
            Log.d(TAG, "AsyncFunction 'isAppLockingAllowed' called.")
            promise.resolve(isLockStatePermitted())
        }

        AsyncFunction("isAppLocked") { promise: Promise ->
            Log.d(TAG, "AsyncFunction 'isAppLocked' called.")
            try {
                promise.resolve(isLockState())
            } catch (e: Exception) {
                Log.e(TAG, "isAppLocked: Failed to check lock state", e)
                promise.reject(CodedException("Failed to check lock state", e))
            }
        }

        AsyncFunction("lockApp") { promise: Promise ->
            Log.d(TAG, "AsyncFunction 'lockApp' called.")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isLockState()) {
                    val activity = appContext.currentActivity
                    if (activity != null) {
                        Log.d(TAG, "lockApp: Activity found, attempting to start lock task.")
                        activity.startLockTask()
                        promise.resolve(true)
                    } else {
                        Log.w(TAG, "lockApp: Unable to lock app: Activity is null")
                        promise.reject(CodedException("Unable to lock app: Activity is null"))
                    }
                } else {
                    Log.d(TAG, "lockApp: App already locked or SDK version too low. Resolving with false.")
                    promise.resolve(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "lockApp: Exception while trying to lock app.", e)
                promise.reject(CodedException("Unable to lock app", e))
            }
        }

        AsyncFunction("unlockApp") { promise: Promise ->
            Log.d(TAG, "AsyncFunction 'unlockApp' called.")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isLockState()) {
                    val activity = appContext.currentActivity
                    if (activity != null) {
                        Log.d(TAG, "unlockApp: Activity found, attempting to stop lock task.")
                        activity.stopLockTask()
                        promise.resolve(true)
                    } else {
                        Log.w(TAG, "unlockApp: Unable to unlock app: Activity is null")
                        promise.reject(CodedException("Unable to unlock app: Activity is null"))
                    }
                } else {
                    Log.d(TAG, "unlockApp: App already unlocked or SDK version too low. Resolving with false.")
                    promise.resolve(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "unlockApp: Exception while trying to unlock app.", e)
                promise.reject(CodedException("Unable to unlock app", e))
            }
        }
    }

    // --- Private methods for BroadcastReceiver management ---

    private fun maybeUnregisterReceiver() {
        Log.d(TAG, "maybeUnregisterReceiver: Attempting to unregister receiver.")
        if (restrictionReceiver == null) {
            Log.d(TAG, "maybeUnregisterReceiver: No receiver to unregister.")
            return
        }
        val reactContext = appContext.reactContext
        if(reactContext == null){
            Log.d(TAG, "maybeUnregisterReceiver: React context is null, cannot unregister.")
            return
        }
        Log.d(TAG, "maybeUnregisterReceiver: Unregistering receiver.")
        reactContext.unregisterReceiver(restrictionReceiver)
        restrictionReceiver = null
    }

    private fun maybeRegisterReceiver() {
        Log.d(TAG, "maybeRegisterReceiver: Attempting to register receiver.")
        val reactContext = appContext.reactContext
        if (reactContext == null) {
            Log.d(TAG, "maybeRegisterReceiver: React context is null, cannot register.")
            return
        }
        if (restrictionReceiver != null) {
            Log.d(TAG, "maybeRegisterReceiver: Receiver already registered.")
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "maybeRegisterReceiver: SDK version too low to register receiver.")
            return
        }

        val restrictionsManager =
            reactContext.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
        val restrictionFilter = IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)

        restrictionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "BroadcastReceiver onReceive: Received ACTION_APPLICATION_RESTRICTIONS_CHANGED.")
                val appRestrictions = restrictionsManager.applicationRestrictions
                val data = mutableMapOf<String, Any>()
                for (key in appRestrictions.keySet()) {
                    data[key] = appRestrictions.getString(key) ?: ""
                }
                Log.d(TAG, "BroadcastReceiver onReceive: Sending event '$APP_CONFIG_CHANGED_EVENT' with data: $data")
                // Use the Expo Module's built-in `sendEvent` function
                sendEvent(APP_CONFIG_CHANGED_EVENT, data)
            }
        }
        Log.d(TAG, "maybeRegisterReceiver: Registering receiver for ACTION_APPLICATION_RESTRICTIONS_CHANGED.")
        reactContext.registerReceiver(restrictionReceiver, restrictionFilter)
    }
}