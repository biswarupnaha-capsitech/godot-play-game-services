package com.jacobibanez.plugin.android.godotplaygameservices

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.jacobibanez.plugin.android.godotplaygameservices.messaging.MessagingBridge
import org.godotengine.godot.GodotActivity

class GodotPlayServicesActivity : GodotActivity() {

    companion object {
        private const val TAG = "GodotPlayActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "Activity created")

        handleNotificationIntent(intent)
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)

        Log.d(TAG, "New intent received")

        intent = newIntent

        handleNotificationIntent(newIntent)
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "Activity resumed")
    }

    private fun handleNotificationIntent(intent: Intent?) {

        if (intent == null)
            return

        MessagingBridge.dispatchNotificationOpened(intent)
    }
}