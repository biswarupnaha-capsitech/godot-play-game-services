package com.jacobibanez.plugin.android.godotplaygameservices.crashlytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.godotengine.godot.Godot

/** @suppress */
class CrashlyticsProxy(
    private val godot: Godot,
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
) {
    private val tag = CrashlyticsProxy::class.simpleName ?: "CrashlyticsProxy"

    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        Log.d(tag, "Setting Crashlytics collection enabled: $enabled")
        crashlytics.isCrashlyticsCollectionEnabled = enabled
    }

    fun log(message: String) {
        Log.d(tag, "Logging message to Crashlytics: $message")
        crashlytics.log(message)
    }

    fun setCustomKey(key: String, value: String) {
        Log.d(tag, "Setting custom key: $key = $value")
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Int) {
        Log.d(tag, "Setting custom key: $key = $value")
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Boolean) {
        Log.d(tag, "Setting custom key: $key = $value")
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Float) {
        Log.d(tag, "Setting custom key: $key = $value")
        crashlytics.setCustomKey(key, value)
    }

    fun setUserId(userId: String) {
        Log.d(tag, "Setting user ID: $userId")
        crashlytics.setUserId(userId)
    }

    fun recordException(message: String) {
        Log.d(tag, "Recording non-fatal exception: $message")
        crashlytics.recordException(Exception(message))
    }

    fun crash() {
        Log.d(tag, "Forcing a crash for testing purposes")
        throw RuntimeException("Test Crash")
    }
}
