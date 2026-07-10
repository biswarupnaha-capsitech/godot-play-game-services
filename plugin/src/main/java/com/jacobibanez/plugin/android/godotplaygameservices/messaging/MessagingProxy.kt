package com.jacobibanez.plugin.android.godotplaygameservices.messaging

import MessagingListener
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.jacobibanez.plugin.android.godotplaygameservices.BuildConfig
import com.jacobibanez.plugin.android.godotplaygameservices.signals.MessagingSignals.messagingError
import com.jacobibanez.plugin.android.godotplaygameservices.signals.MessagingSignals.notificationOpened
import com.jacobibanez.plugin.android.godotplaygameservices.signals.MessagingSignals.notificationPermissionResult
import com.jacobibanez.plugin.android.godotplaygameservices.signals.MessagingSignals.notificationReceived
import com.jacobibanez.plugin.android.godotplaygameservices.signals.MessagingSignals.tokenReceived
import com.jacobibanez.plugin.android.godotplaygameservices.signals.MessagingSignals.tokenRefreshed
import com.jacobibanez.plugin.android.godotplaygameservices.signals.MessagingSignals.topicSubscribed
import com.jacobibanez.plugin.android.godotplaygameservices.signals.MessagingSignals.topicUnsubscribed
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin.emitSignal

class MessagingProxy(
    private val godot: Godot,
    private val messagingManager: MessagingManager,
) : MessagingListener{

    private val tag = MessagingProxy::class.java.simpleName
    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 2001
    }

    init {
        MessagingBridge.register(this)
    }
    /**
     * Gets the current Firebase Cloud Messaging registration token.
     */
    fun getToken() {

        messagingManager.getToken(

            onSuccess = { token ->

                emitSignal(
                    godot,
                    BuildConfig.GODOT_PLUGIN_NAME,
                    tokenReceived,
                    token
                )
            },

            onFailure = { exception ->

                emitSignal(
                    godot,
                    BuildConfig.GODOT_PLUGIN_NAME,
                    messagingError,
                    exception?.localizedMessage ?: "Unknown error"
                )
            }
        )
    }

    /**
     * Subscribes to an FCM topic.
     */
    fun subscribeToTopic(topic: String) {

        messagingManager.subscribeToTopic(

            topic,

            onSuccess = {

                emitSignal(
                    godot,
                    BuildConfig.GODOT_PLUGIN_NAME,
                    topicSubscribed,
                    topic
                )
            },

            onFailure = { exception ->

                emitSignal(
                    godot,
                    BuildConfig.GODOT_PLUGIN_NAME,
                    messagingError,
                    exception?.localizedMessage ?: "Unknown error"
                )
            }
        )
    }

    /**
     * Unsubscribes from an FCM topic.
     */
    fun unsubscribeFromTopic(topic: String) {

        messagingManager.unsubscribeFromTopic(

            topic,

            onSuccess = {

                emitSignal(
                    godot,
                    BuildConfig.GODOT_PLUGIN_NAME,
                    topicUnsubscribed,
                    topic
                )
            },

            onFailure = { exception ->

                emitSignal(
                    godot,
                    BuildConfig.GODOT_PLUGIN_NAME,
                    messagingError,
                    exception?.localizedMessage ?: "Unknown error"
                )
            }
        )
    }

    override fun onTokenRefreshed(token: String) {

        Log.d(tag, "FCM token refreshed.")

        emitSignal(
            godot,
            BuildConfig.GODOT_PLUGIN_NAME,
            tokenRefreshed,
            token
        )
    }

    override fun onRemoteMessageReceived(remoteMessage: RemoteMessage) {

        val title = remoteMessage.notification?.title ?: ""

        val body = remoteMessage.notification?.body ?: ""

        val json = Gson().toJson(remoteMessage.data)

        emitSignal(
            godot,
            BuildConfig.GODOT_PLUGIN_NAME,
            notificationReceived,
            title,
            body,
            json
        )
    }

    override fun onNotificationOpened(intent: NotificationIntent) {

        val json = Gson().toJson(intent.payload)

        emitSignal(
            godot,
            BuildConfig.GODOT_PLUGIN_NAME,
            notificationOpened,
            intent.title,
            intent.body,
            json
        )
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode != REQUEST_POST_NOTIFICATIONS)
            return

        val granted =
            grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED

        emitSignal(
            godot,
            BuildConfig.GODOT_PLUGIN_NAME,
            notificationPermissionResult,
            granted
        )
    }

    fun dispose() {
        MessagingBridge.unregister()
    }
}