# Firebase Cloud Messaging (FCM) Package

This directory contains the GDScript client for Firebase Cloud Messaging (FCM). It allows Godot projects running on Android to retrieve FCM tokens, subscribe/unsubscribe to topics, receive push notifications in the foreground, and handle notification open/tap events.

---

## How It Works

The FCM implementation spans across Kotlin (Android Plugin) and GDScript (Godot Client) components:

### 1. The Kotlin (Android) Layer
* **`GodotFirebaseMessagingService`**: Extends `FirebaseMessagingService`. It listens for FCM token generation (`onNewToken`) and messages received when the app is in the foreground (`onMessageReceived`).
* **`MessagingBridge`**: Acts as a central singleton listener registry. It receives events from `GodotFirebaseMessagingService` and `GodotPlayServicesActivity` and delegates them to the registered listener.
* **`GodotPlayServicesActivity`**: A custom `GodotActivity` subclass that intercepts Android launch Intents. When a user taps a notification to open the app, it extracts the payload details (title, body, extras) and notifies the bridge.
* **`MessagingProxy`**: Implements `MessagingListener` and registers itself with the bridge. It communicates directly with Godot by emitting signals (`tokenReceived`, `notificationReceived`, `notificationOpened`, etc.) containing the data serialized into JSON strings using Gson.

### 2. The GDScript Layer
* **`FirebaseCloudMessagingClient` (`messaging.gd`)**: A Godot `Node` that acts as the developer-facing interface. It connects to the Android plugin signals and wraps them in clean GDScript signals. It also exposes helper functions like `get_token()`, `subscribe_to_topic()`, and `unsubscribe_from_topic()`.

---

## Setup & Configuration in Godot

To use the FCM package, you must complete the following setup steps:

### 1. Firebase Configuration

Firebase requires configuration parameters (like API keys, App ID, etc.) to establish a connection. There are four ways to provide these parameters:

#### Option A: Project Root Placement (Recommended)
Place your downloaded `google-services.json` file in the root directory of your Godot project (`res://google-services.json`). During initialization, the `GodotPlayGameServices` autoload will automatically read this file and initialize Firebase.

#### Option B: Programmatic Options (GDScript)
Call `initialize_firebase_with_options` programmatically before using the FCM client. Pass your credentials manually:
```gdscript
GodotPlayGameServices.initialize_firebase_with_options(
    "YOUR_API_KEY",
    "YOUR_APP_ID",
    "YOUR_SENDER_ID",
    "YOUR_PROJECT_ID"
)
```

#### Option C: Android Assets Placement
Place `google-services.json` inside the custom Android export template's assets folder (`assets/google-services.json`). The Android plugin will fall back to reading it from there if no default option is found.

---

### 2. Activity Configuration (Required for Notification Open Events)

To trigger the `notification_opened` event when a user taps on a notification, Android must route the launch intent through the custom `GodotPlayServicesActivity`.

1. Enable the **Custom Android Build** in your Godot project (Project -> Export -> Android -> Use Custom Build).
2. Open `android/build/AndroidManifest.xml` in your project folder.
3. Find the main launcher `<activity>` tag (the one containing `<intent-filter>` with `action.android.intent.action.MAIN` and `category.android.intent.category.LAUNCHER`).
4. Change its `android:name` attribute from the default Godot activity to the custom plugin activity:
   ```xml
   android:name="com.jacobibanez.plugin.android.godotplaygameservices.GodotPlayServicesActivity"
   ```

---

### 3. Notification Permissions (Android 13+)

Starting in Android 13 (API Level 33), applications must request runtime permissions to display notifications. 
* The plugin automatically requests the `POST_NOTIFICATIONS` permission when `GodotPlayGameServices.initialize()` is called.
* You can listen to the `notification_permission_result(granted: bool)` signal on the FCM client to check if permission was granted.

---

## GDScript Usage Example

Add the `FirebaseCloudMessagingClient` node to your scene and connect to its signals:

```gdscript
extends Node

var fcm_client: FirebaseCloudMessagingClient

func _ready() -> void:
    # Ensure GodotPlayGameServices autoload is initialized first!
    
    # 1. Instantiate the client
    fcm_client = FirebaseCloudMessagingClient.new()
    add_child(fcm_client)
    
    # 2. Connect to signals
    fcm_client.token_received.connect(_on_token_received)
    fcm_client.token_refreshed.connect(_on_token_refreshed)
    fcm_client.notification_received.connect(_on_notification_received)
    fcm_client.notification_opened.connect(_on_notification_opened)
    fcm_client.topic_subscribed.connect(_on_topic_subscribed)
    fcm_client.topic_unsubscribed.connect(_on_topic_unsubscribed)
    fcm_client.messaging_error.connect(_on_messaging_error)
    fcm_client.notification_permission_result.connect(_on_permission_result)
    
    # 3. Retrieve the registration token
    fcm_client.get_token()

func _on_token_received(token: String) -> void:
    print("FCM Registration Token: ", token)
    # Send this token to your backend to target this device specifically

func _on_token_refreshed(token: String) -> void:
    print("FCM Token Refreshed: ", token)

func _on_notification_received(title: String, body: String, data: String) -> void:
    print("Notification received while in foreground:")
    print("Title: ", title)
    print("Body: ", body)
    var payload = JSON.parse_string(data)
    print("Payload data: ", payload)

func _on_notification_opened(title: String, body: String, data: String) -> void:
    print("App opened via notification tap:")
    print("Title: ", title)
    print("Body: ", body)
    var payload = JSON.parse_string(data)
    print("Payload data: ", payload)

func _on_topic_subscribed(topic: String) -> void:
    print("Subscribed to topic: ", topic)

func _on_messaging_error(message: String) -> void:
    print("FCM error occurred: ", message)

func _on_permission_result(granted: bool) -> void:
    print("Notification permission granted: ", granted)
```
