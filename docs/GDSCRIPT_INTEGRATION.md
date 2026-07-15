# GDScript Integration Guide

- #### (Modified by Biswarup Naha)

This guide explains how to integrate the **Godot Play Game Services** plugin into your own game from GDScript. It covers the plugin's architecture, how to initialize it, and the public API of each client Node the plugin ships with. Point to be noted is to keep the 'google-services.json' file in the root of the project.

> This guide assumes you've already [installed and configured the plugin](../README.md#install-the-plugin) and enabled it under `Project > Project Settings > Plugins`.

## Table of contents

- [Architecture overview](#architecture-overview)
- [Initializing the plugin](#initializing-the-plugin)
- [PlayGamesSignInClient](#playgamessigninclient)
- [PlayGamesUpdatesClient](#playgamesupdatesclient)
- [FirebaseCloudMessagingClient](#firebasecloudmessagingclient)
- [Displaying downloaded images](#displaying-downloaded-images)
- [Putting it together: writing your own manager](#putting-it-together-writing-your-own-manager)
- [API quick reference](#api-quick-reference)

## Architecture overview

Since v3.0, the plugin no longer creates one autoload per feature. Instead you get:

- **`GodotPlayGameServices`** — a single autoload that holds the reference to the underlying Android plugin (`android_plugin`) and must be initialized manually before anything else works.
- **Client Nodes** — regular `Node`-derived classes (`PlayGamesSignInClient`, `PlayGamesUpdatesClient`, `FirebaseCloudMessagingClient`, etc.) that you instantiate yourself (either as scene children or with `.new()` + `add_child()`). Each client wraps a slice of the Android plugin's functionality and re-emits its callbacks as GDScript signals.

This means **you own the wiring**. There is no single hardcoded "AuthManager" you're required to use — you can structure your own autoload(s) however you like, as long as you:

1. Call `GodotPlayGameServices.initialize()` before creating any client.
2. Instantiate and `add_child()` whichever clients you need.
3. Connect to their signals to react to results.

The examples on this page use a generic `GameServicesManager` autoload to illustrate the pattern — swap the name for whatever fits your project (an `AuthManager`, a `PlayerManager`, etc.).

## Initializing the plugin

`GodotPlayGameServices` is the entry point. Call `initialize()` once, early in your game's lifecycle — `_enter_tree()` of your main autoload is a good place, since it runs before other autoloads' `_ready()`:

```gdscript
extends Node

func _enter_tree() -> void:
    var result := GodotPlayGameServices.initialize()
    if result != GodotPlayGameServices.PlayGamesPluginError.OK:
        push_warning("GodotPlayGameServices could not be initialized (not on Android, or plugin missing).")
```

`initialize()` will:

- Look for the native `GodotPlayGameServices` Android singleton. If it isn't found (e.g. you're running in the editor or on a non-Android export), it returns `PlayGamesPluginError.PLUGIN_NOT_FOUND` and logs an error — this is expected and safe to ignore outside of Android builds.
- If `res://google-services.json` exists, automatically initialize Firebase from it. If you'd rather not ship that file, use `initialize_firebase_with_options()` instead (see below).
- Request notification permission (Android 13+) and wire up the `image_stored` signal used by the image-download helper.

Because `android_plugin` will be `null` on non-Android platforms, **every client checks for `GodotPlayGameServices.android_plugin` before calling into it**. You should follow the same pattern in your own code, and guard any Play Games-specific calls with `OS.get_name() == "Android"`.

### Initializing Firebase without `google-services.json`

If you prefer to keep credentials out of your repo, or need to configure Firebase at runtime:

```gdscript
GodotPlayGameServices.initialize_firebase_with_options(
    api_key,
    app_id,
    sender_id,
    project_id
)
```

Call this *instead of* relying on the automatic `google-services.json` pickup, right after `initialize()`.

## PlayGamesSignInClient

Wraps the Google Play Games sign-in flow. Unlike previous plugin versions, **sign-in is no longer checked automatically at startup** — you decide when to check and when to prompt.

### Setup

```gdscript
var sign_in_client: PlayGamesSignInClient

func _ready() -> void:
    sign_in_client = PlayGamesSignInClient.new()
    add_child(sign_in_client)

    sign_in_client.user_authenticated.connect(_on_user_authenticated)
    sign_in_client.server_side_access_requested.connect(_on_server_side_access_requested)
```

### Methods

| Method | Description |
|---|---|
| `is_authenticated() -> void` | Checks whether the player is already signed in to Play Games. May show a system popup. Result arrives via `user_authenticated`. |
| `sign_in() -> void` | Presents the manual sign-in flow (account picker) to the user. Result arrives via `user_authenticated`. |
| `request_server_side_access(server_client_id: String, force_refresh_token: bool) -> void` | Requests an OAuth 2.0 authorization code your **backend** (e.g. Firebase) can exchange for tokens. Result arrives via `server_side_access_requested`. |

### Signals

| Signal | Emitted when |
|---|---|
| `user_authenticated(is_authenticated: bool)` | After `is_authenticated()` or `sign_in()` resolves. |
| `server_side_access_requested(token: String)` | After `request_server_side_access()` resolves. `token` is empty on failure. |

### Typical flow (Play Games → Firebase)

```gdscript
func _on_user_authenticated(is_authenticated: bool) -> void:
    if not is_authenticated:
        return
    sign_in_client.request_server_side_access(server_client_id, false)

func _on_server_side_access_requested(token: String) -> void:
    if token.is_empty():
        push_error("Empty Play Games auth code")
        return
    # Exchange `token` with your backend / Firebase Auth here.
```

This is the same pattern used internally to bridge Play Games sign-in into Firebase Authentication — see [Authentication Scopes](../README.md#authentication-scopes) for what permissions `request_server_side_access` can grant.

> ⚠️ Sign-in methods (`is_authenticated`, `sign_in`, `request_server_side_access`) are Android-only. Guard calls with `OS.get_name() == "Android"`.

## PlayGamesUpdatesClient

Wraps Google Play's **In-App Update** API, supporting both the Immediate (blocking, full-screen) and Flexible (background download) flows.

### Setup

```gdscript
var updates_client: PlayGamesUpdatesClient

func _ready() -> void:
    updates_client = PlayGamesUpdatesClient.new()
    add_child(updates_client)

    updates_client.update_checked.connect(_on_update_checked)
    updates_client.update_check_failed.connect(_on_update_check_failed)
    updates_client.update_download_progress.connect(_on_download_progress)
    updates_client.update_download_completed.connect(_on_download_completed)
```

### Methods

| Method | Description |
|---|---|
| `check_for_update() -> void` | Asks Google Play if a newer version is available. Result arrives via `update_checked` / `update_check_failed`. |
| `start_immediate_update() -> void` | Starts the blocking, full-screen Immediate Update flow. |
| `start_flexible_update() -> void` | Starts the Flexible Update flow (downloads in the background). |
| `start_best_update() -> void` | Lets Google Play pick the most appropriate flow automatically. |
| `complete_update() -> void` | Installs a Flexible Update that has finished downloading (typically prompts the user to restart). |
| `resume_update_if_needed() -> void` | Resumes an Immediate Update that was interrupted (e.g. app was killed mid-update). Good to call on startup. |

### Signals

| Signal | Emitted when |
|---|---|
| `update_checked(available, version_code, priority, staleness_days, immediate_allowed, flexible_allowed)` | After `check_for_update()` resolves. `priority` ranges 0–5 and reflects the priority you set in the Play Console when rolling out the update. |
| `update_check_failed(error_code, error_message)` | The update check itself failed. |
| `update_started(immediate: bool)` | An update flow has begun. |
| `update_download_progress(bytes_downloaded, total_bytes)` | During a Flexible Update download. |
| `update_download_completed()` | Flexible Update download finished — call `complete_update()` to install it. |
| `update_install_completed()` | Update was installed successfully. |
| `update_cancelled()` | The user backed out of the update flow. |
| `update_failed(error_code, error_message)` | The update flow itself failed. |

### Example policy

A common approach is to force Immediate Updates above a certain priority, and offer Flexible Updates otherwise:

```gdscript
func _on_update_checked(
    available: bool,
    version_code: int,
    priority: int,
    staleness_days: int,
    immediate_allowed: bool,
    flexible_allowed: bool
) -> void:
    if not available:
        return

    if priority >= 4 and immediate_allowed:
        updates_client.start_immediate_update()
    elif flexible_allowed:
        updates_client.start_flexible_update()

func _on_download_completed() -> void:
    # Prompt the player, then:
    updates_client.complete_update()
```

Call `check_for_update()` on startup, and `resume_update_if_needed()` right after, so interrupted Immediate Updates pick back up.

## FirebaseCloudMessagingClient

Wraps Firebase Cloud Messaging: registration tokens, topic subscriptions, and receiving notifications.

### Setup

```gdscript
var messaging_client: FirebaseCloudMessagingClient

func _ready() -> void:
    messaging_client = FirebaseCloudMessagingClient.new()
    add_child(messaging_client)

    messaging_client.token_received.connect(_on_token_received)
    messaging_client.notification_received.connect(_on_notification_received)
    messaging_client.notification_opened.connect(_on_notification_opened)
```

### Methods

| Method | Description |
|---|---|
| `get_token() -> void` | Requests the current FCM registration token. Result arrives via `token_received`. |
| `subscribe_to_topic(topic: String) -> void` | Subscribes the device to a topic. Result arrives via `topic_subscribed`. |
| `unsubscribe_from_topic(topic: String) -> void` | Unsubscribes from a topic. Result arrives via `topic_unsubscribed`. |

### Signals

| Signal | Emitted when |
|---|---|
| `token_received(token: String)` | After `get_token()` resolves. |
| `token_refreshed(token: String)` | Firebase rotates the registration token on its own (e.g. app reinstall). Send the new token to your backend if you store it there. |
| `notification_received(title, body, data)` | A push notification arrives while the app is running. `data` is a JSON string with the notification's data payload. |
| `notification_opened(title, body, data)` | The app was opened by tapping a notification. |
| `topic_subscribed(topic: String)` | Subscription to `topic` succeeded. |
| `topic_unsubscribed(topic: String)` | Unsubscription from `topic` succeeded. |
| `messaging_error(message: String)` | Any FCM operation failed. |

`notification_received` / `notification_opened`'s `data` payload is a raw JSON string — use `JSON.parse_string(data)` to turn it into a `Dictionary`.

## Displaying downloaded images

`GodotPlayGameServices` also exposes an image-download helper, used e.g. for player avatars fetched from Play Games or Firebase.

```gdscript
GodotPlayGameServices.image_stored.connect(func(file_path: String):
    GodotPlayGameServices.display_image_in_texture_rect($AvatarTextureRect, file_path)
)
```

`display_image_in_texture_rect(texture_rect: TextureRect, file_path: String)` loads an image from a `user://` path (or any valid Godot path) and assigns it to a `TextureRect`. It's safe to call at any point after `image_stored` fires — it checks `FileAccess.file_exists()` internally and prints a warning if the file isn't there.

## Putting it together: writing your own manager

None of the clients require a specific autoload name or shape — the plugin's own demo happens to centralize sign-in, Firebase, and update logic in a single manager Node, but you're free to split things across multiple autoloads, or keep clients as children of specific scenes instead of a global singleton.

A minimal manager that initializes everything and bridges Play Games sign-in into your own backend might look like:

```gdscript
extends Node

var sign_in_client: PlayGamesSignInClient
var updates_client: PlayGamesUpdatesClient
var messaging_client: FirebaseCloudMessagingClient

func _enter_tree() -> void:
    GodotPlayGameServices.initialize()

func _ready() -> void:
    sign_in_client = PlayGamesSignInClient.new()
    add_child(sign_in_client)
    sign_in_client.user_authenticated.connect(_on_user_authenticated)
    sign_in_client.server_side_access_requested.connect(_on_server_side_access_requested)

    updates_client = PlayGamesUpdatesClient.new()
    add_child(updates_client)
    updates_client.update_checked.connect(_on_update_checked)

    messaging_client = FirebaseCloudMessagingClient.new()
    add_child(messaging_client)
    messaging_client.notification_received.connect(_on_notification_received)

    if OS.get_name() == "Android":
        sign_in_client.is_authenticated()
        updates_client.check_for_update()
        updates_client.resume_update_if_needed()

func _on_user_authenticated(is_authenticated: bool) -> void:
    if is_authenticated:
        sign_in_client.request_server_side_access("YOUR_SERVER_CLIENT_ID", false)

func _on_server_side_access_requested(token: String) -> void:
    if token.is_empty():
        return
    # Exchange token with your backend / Firebase Auth here.

func _on_update_checked(available, version_code, priority, staleness_days, immediate_allowed, flexible_allowed) -> void:
    if available and priority >= 4 and immediate_allowed:
        updates_client.start_immediate_update()
    elif available and flexible_allowed:
        updates_client.start_flexible_update()

func _on_notification_received(title: String, body: String, data: String) -> void:
    print_debug("Notification: %s - %s" % [title, body])
```

Add whichever of the three clients you actually need — they're independent of each other. If you're only after in-app updates, for example, you don't need `PlayGamesSignInClient` or `FirebaseCloudMessagingClient` at all.

## API quick reference

| Client | Purpose | Platform |
|---|---|---|
| `GodotPlayGameServices` | Plugin entry point, Firebase init, image display helper | Any (no-ops off Android) |
| `PlayGamesSignInClient` | Play Games sign-in, server-side auth code exchange | Android |
| `PlayGamesUpdatesClient` | In-app update check/install (Immediate & Flexible) | Android |
| `FirebaseCloudMessagingClient` | FCM tokens, topics, notifications | Any (behavior depends on Firebase setup) |

For the full Kotlin-side API reference, see the [Dokka-generated docs](https://godot.jacobibanez.com/).
