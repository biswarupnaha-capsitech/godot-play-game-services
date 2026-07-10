package com.jacobibanez.plugin.android.godotplaygameservices.messaging

import android.app.Activity
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class MessagingManager(
    private val activity: Activity?
) {

    private val tag = MessagingManager::class.java.simpleName

    private val firebaseMessaging = FirebaseMessaging.getInstance()


    fun getToken(
        onSuccess: (String) -> Unit,
        onFailure: (Exception?) -> Unit
    ) {

        Log.d(tag, "Requesting FCM token")

        firebaseMessaging.token
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    onSuccess(task.result)

                } else {

                    onFailure(task.exception)

                }
            }
    }

    fun subscribeToTopic(
        topic: String,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {

        firebaseMessaging.subscribeToTopic(topic)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    onSuccess()

                } else {

                    onFailure(task.exception)

                }
            }
    }

    fun unsubscribeFromTopic(
        topic: String,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {

        firebaseMessaging.unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    onSuccess()

                } else {

                    onFailure(task.exception)

                }
            }
    }
}