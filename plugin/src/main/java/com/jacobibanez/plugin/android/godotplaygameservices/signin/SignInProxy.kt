package com.jacobibanez.plugin.android.godotplaygameservices.signin

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.gamessignin.AuthResponse
import com.google.android.gms.games.gamessignin.AuthScope
import com.jacobibanez.plugin.android.godotplaygameservices.BuildConfig.GODOT_PLUGIN_NAME
import com.jacobibanez.plugin.android.godotplaygameservices.signals.SignInSignals.serverSideAccessRequested
import com.jacobibanez.plugin.android.godotplaygameservices.signals.SignInSignals.userAuthenticated
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin.emitSignal
import java.util.Arrays

class SignInProxy(
    private val godot: Godot,
    private val gamesSignInClient: GamesSignInClient =
        PlayGames.getGamesSignInClient(godot.getActivity()!!)
) {

    private val tag: String = SignInProxy::class.java.simpleName
    private var serverAuthCode: String = ""

    fun getServerAuthCode(): String {
        return serverAuthCode
    }

    fun isAuthenticated() {
        Log.d(tag, "Checking if user is authenticated")
        gamesSignInClient.isAuthenticated
            .addOnCompleteListener { task ->
                val authenticated =
                    task.isSuccessful &&
                            task.result.isAuthenticated

                emitSignal(
                    godot,
                    GODOT_PLUGIN_NAME,
                    userAuthenticated,
                    authenticated
                )
            }
    }

    fun signIn() {
        Log.d(tag, "Signing in")

        gamesSignInClient.signIn()
            .addOnCompleteListener { task ->
                val authenticated =
                    task.isSuccessful &&
                            task.result.isAuthenticated

                emitSignal(
                    godot,
                    GODOT_PLUGIN_NAME,
                    userAuthenticated,
                    authenticated
                )
            }
    }

    fun signOut() {
        Log.d(tag, "Signing out")

        val activity = godot.getActivity() ?: return

        GoogleSignIn.getClient(
            activity,
            GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        )
            .signOut()
            .addOnCompleteListener {
                Log.d(tag, "Sign out complete")

                emitSignal(
                    godot,
                    GODOT_PLUGIN_NAME,
                    userAuthenticated,
                    false
                )
            }
    }

    fun signInRequestServerSideAccess(
        serverClientId: String,
        forceRefreshToken: Boolean
    ) {
        Log.d(tag, "Requesting server-side access code")

        gamesSignInClient
            .requestServerSideAccess(
                serverClientId,
                forceRefreshToken,
                listOf(
                    AuthScope.OPEN_ID,
                    AuthScope.EMAIL,
                    AuthScope.PROFILE
                )
            )
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {

                    serverAuthCode = task.result.authCode

                    Log.d(
                        tag,
                        "Server Auth Code received successfully"
                    )

                    emitSignal(
                        godot,
                        GODOT_PLUGIN_NAME,
                        serverSideAccessRequested,
                        serverAuthCode,
//                        task.result.grantedScopes.joinToString(",")
                    )

                } else {
                    Log.e(
                        tag,
                        "Failed requesting access",
                        task.exception
                    )

                    emitSignal(
                        godot,
                        GODOT_PLUGIN_NAME,
                        userAuthenticated,
                        false
                    )
                }
            }
    }
}