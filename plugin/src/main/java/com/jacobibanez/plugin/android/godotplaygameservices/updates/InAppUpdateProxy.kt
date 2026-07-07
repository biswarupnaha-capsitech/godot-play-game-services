package com.jacobibanez.plugin.android.godotplaygameservices.updates

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.jacobibanez.plugin.android.godotplaygameservices.BuildConfig
import com.jacobibanez.plugin.android.godotplaygameservices.signals.UpdateSignals
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin.emitSignal

class InAppUpdateProxy(
    private val godot: Godot
) {

    private val tag = InAppUpdateProxy::class.java.simpleName

    private val activity: Activity
        get() = godot.getActivity()!!

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(activity)
    }

    fun checkForUpdate() {

        Log.d(tag, "Checking for app updates...")

        appUpdateManager.appUpdateInfo
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {

                    Log.e(
                        tag,
                        "Failed to retrieve AppUpdateInfo.",
                        task.exception
                    )

                    emitSignal(
                        godot,
                        BuildConfig.GODOT_PLUGIN_NAME,
                        UpdateSignals.updateCheckFailed,
                        -1,
                        task.exception?.localizedMessage ?: "Unknown error"
                    )

                    return@addOnCompleteListener
                }

                val updateInfo = task.result

                val isUpdateAvailable =
                    updateInfo.updateAvailability() ==
                            UpdateAvailability.UPDATE_AVAILABLE

                val versionCode =
                    updateInfo.availableVersionCode()

                val priority =
                    updateInfo.updatePriority()

                val staleness =
                    updateInfo.clientVersionStalenessDays() ?: 0

                val immediateAllowed =
                    updateInfo.isUpdateTypeAllowed(
                        AppUpdateType.IMMEDIATE
                    )

                val flexibleAllowed =
                    updateInfo.isUpdateTypeAllowed(
                        AppUpdateType.FLEXIBLE
                    )

                Log.d(
                    tag,
                    """
                Update Available : $isUpdateAvailable
                Version Code    : $versionCode
                Priority        : $priority
                Staleness       : $staleness
                Immediate       : $immediateAllowed
                Flexible        : $flexibleAllowed
                """.trimIndent()
                )

                emitSignal(
                    godot,
                    BuildConfig.GODOT_PLUGIN_NAME,
                    UpdateSignals.updateChecked,
                    isUpdateAvailable,
                    versionCode,
                    priority,
                    staleness,
                    immediateAllowed,
                    flexibleAllowed
                )
            }
    }
}