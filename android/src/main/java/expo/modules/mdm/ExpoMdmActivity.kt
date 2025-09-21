package expo.modules.mdm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity

// Assuming DropInListener and DropInResult are part of your MDM SDK library.
// Make sure you have the correct imports for them.
// import com.your.mdm.sdk.DropInListener
// import com.your.mdm.sdk.DropInResult

class ExpoMdmActivity : FragmentActivity(), DropInListener {

    /**
     * A companion object to hold constants for logging and broadcasting.
     * This makes them easily accessible from other parts of the app without
     * creating an instance of the class.
     */
    companion object {
        // TAG for logging, helps in filtering logs in Logcat
        private const val TAG = "ExpoMdmActivity"

        // Action for the broadcast Intent
        const val MDM_RESULT_ACTION = "expo.modules.mdm.MDM_RESULT"

        // Keys for the data included in the broadcast
        const val EXTRA_SUCCESS = "isSuccess"
        const val EXTRA_RESULT_PAYLOAD = "resultPayload"
        const val EXTRA_ERROR_MESSAGE = "errorMessage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity is being created.")
        // You would typically initialize your MDM SDK here and trigger the "DropIn" UI/process.
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Activity is now visible.")
    }

    /**
     * This callback is triggered when the MDM operation (e.g., enrollment) succeeds.
     */
    override fun onDropInSuccess(dropInResult: DropInResult) {
        // Log the successful result for debugging.
        // The .toString() might provide useful info, but you should inspect the
        // dropInResult object for specific data fields to log.
        Log.i(TAG, "onDropInSuccess: MDM operation successful. Result: $dropInResult")

        // Create an Intent to broadcast the success event.
        val intent = Intent(MDM_RESULT_ACTION).apply {
            putExtra(EXTRA_SUCCESS, true)
            // TODO: Serialize the result or extract relevant data.
            // For now, we send its string representation as a placeholder.
            putExtra(EXTRA_RESULT_PAYLOAD, dropInResult.toString())
        }
        
        // Send the broadcast and finish the activity.
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        finish()
    }

    /**
     * This callback is triggered when the MDM operation fails.
     */
    override fun onDropInFailure(error: Exception) {
        // Log the error with its message and stack trace for easier debugging.
        Log.e(TAG, "onDropInFailure: MDM operation failed.", error)

        // Create an Intent to broadcast the failure event.
        val intent = Intent(MDM_RESULT_ACTION).apply {
            putExtra(EXTRA_SUCCESS, false)
            putExtra(EXTRA_ERROR_MESSAGE, error.message)
        }
        
        // Send the broadcast and finish the activity.
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        finish()
    }
}