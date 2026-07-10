package com.jacobibanez.plugin.android.godotplaygameservices.messaging

import android.content.Intent

object IntentParser {

    fun parse(intent: Intent): NotificationIntent? {

        val extras = intent.extras ?: return null

        val payload = mutableMapOf<String, String>()

        for (key in extras.keySet()) {

            val value = extras.get(key)

            if (value is String) {
                payload[key] = value
            }
        }

        return NotificationIntent(
            title = extras.getString("gcm.notification.title", ""),
            body = extras.getString("gcm.notification.body", ""),
            payload = payload,
            messageId = extras.getString("google.message_id")
        )
    }
}