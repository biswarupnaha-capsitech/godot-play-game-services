package com.jacobibanez.plugin.android.godotplaygameservices.messaging

import MessagingListener
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import java.lang.ref.WeakReference
import android.content.Intent

object MessagingBridge {

    private const val TAG = "MessagingBridge"
    private var lastMessageId: String? = null
    private var listener: WeakReference<MessagingListener>? = null

    fun register(listener: MessagingListener) {
        this.listener = WeakReference(listener)

        Log.d(TAG, "Messaging listener registered.")
    }

    fun unregister() {
        listener?.clear()
        listener = null

        Log.d(TAG, "Messaging listener unregistered.")
    }

    fun dispatchToken(token: String) {
        Log.d(TAG, "Dispatching refreshed token.")

        listener?.get()?.onTokenRefreshed(token)
    }

    fun dispatchRemoteMessage(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Dispatching RemoteMessage.")

        listener?.get()?.onRemoteMessageReceived(remoteMessage)
    }


    fun dispatchNotificationOpened(intent: Intent) {

        val notification = IntentParser.parse(intent) ?: return
        val id = intent.getStringExtra("google.message_id")

        if (id != null && id == lastMessageId) {
            return
        }

        lastMessageId = id
        listener?.get()?.onNotificationOpened(notification)
    }

}