import com.google.firebase.messaging.RemoteMessage
import com.jacobibanez.plugin.android.godotplaygameservices.messaging.NotificationIntent

interface MessagingListener {

    fun onTokenRefreshed(token: String)

    fun onRemoteMessageReceived(remoteMessage: RemoteMessage)

    fun onNotificationOpened(intent: NotificationIntent)
}