package com.jacobibanez.plugin.android.godotplaygameservices.updates

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import org.godotengine.godot.Godot

/**
 * Handles Google Play In-App Updates.
 */
class InAppUpdateProxy(
    private val godot: Godot
) {

    companion object {
        private const val TAG = "InAppUpdateProxy"
    }

    /**
     * Reference to the Android Activity.
     */
    private val activity: Activity?
        get() = godot.getActivity();

    /**
     * Google Play Core update manager.
     */
    private val appUpdateManager: AppUpdateManager? by lazy {
        activity?.let { AppUpdateManagerFactory.create(it) }
    }

    init {
        Log.d(TAG, "InAppUpdateProxy initialized")
        if (activity == null) {
            Log.e(TAG, "Godot activity is null during initialization!")
        }
    }
}