class_name FirebaseCrashlyticsClient extends Node
## Client with Firebase Crashlytics functionality.
##
## This autoload exposes methods to log messages, set custom keys, and record
## non-fatal exceptions in Firebase Crashlytics.

## Enables or disables Crashlytics collection.[br]
## [br]
## [param enabled]: True to enable, false to disable.
func set_crashlytics_collection_enabled(enabled: bool) -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.setCrashlyticsCollectionEnabled(enabled)

## Logs a message that will appear in the Crashlytics report.[br]
## [br]
## [param message]: The message to log.
func log(message: String) -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.crashlyticsLog(message)

## Sets a custom key and value for the Crashlytics report.[br]
## [br]
## [param key]: The key.[br]
## [param value]: The string value.
func set_custom_key(key: String, value: String) -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.setCrashlyticsCustomKey(key, value)

## Sets a custom integer key and value for the Crashlytics report.[br]
## [br]
## [param key]: The key.[br]
## [param value]: The integer value.
func set_custom_key_int(key: String, value: int) -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.setCrashlyticsCustomKeyInt(key, value)

## Sets a custom boolean key and value for the Crashlytics report.[br]
## [br]
## [param key]: The key.[br]
## [param value]: The boolean value.
func set_custom_key_bool(key: String, value: bool) -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.setCrashlyticsCustomKeyBool(key, value)

## Sets a custom float key and value for the Crashlytics report.[br]
## [br]
## [param key]: The key.[br]
## [param value]: The float value.
func set_custom_key_float(key: String, value: float) -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.setCrashlyticsCustomKeyFloat(key, value)

## Sets a user ID for the Crashlytics report.[br]
## [br]
## [param user_id]: The user ID.
func set_user_id(user_id: String) -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.setCrashlyticsUserId(user_id)

## Records a non-fatal exception.[br]
## [br]
## [param message]: The exception message.
func record_exception(message: String) -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.recordCrashlyticsException(message)

## Forces a crash for testing purposes.
func force_crash() -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.forceCrash()
