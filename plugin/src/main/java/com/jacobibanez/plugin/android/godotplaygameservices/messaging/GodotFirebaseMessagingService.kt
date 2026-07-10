package com.jacobibanez.plugin.android.godotplaygameservices.messaging

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GodotFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "GodotFCMService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "FCM token refreshed: $token")

        MessagingBridge.dispatchToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Push notification received")

        MessagingBridge.dispatchRemoteMessage(remoteMessage)
    }
}