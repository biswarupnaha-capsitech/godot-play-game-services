package com.jacobibanez.plugin.android.godotplaygameservices.messaging

data class NotificationIntent(

    val title: String,

    val body: String,

    val payload: Map<String, String>,

    val messageId: String?
)