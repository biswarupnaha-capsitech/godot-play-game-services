extends Node
## Main Autoload of the plugin, which contains a reference to the android plugin itself.
##
## This Autoload contains the entrypoint to the android code, but you don't need
## to use it directly. Some autoloads exposing the plugin functionality, as a wrapper
## for GDScript code, are also loaded with the plugin.[br]
## [br]
## Remember to call the [method initialize] method of the plugin before
## using it.

## Signal emitted after an image is downloaded and saved to the device.[br]
## [br]
## [param file_path]: The path to the stored file.
signal image_stored(file_path: String)

enum PlayGamesPluginError {
	OK = 0,
	PLUGIN_NOT_FOUND = 1
}

## Main entry point to the android plugin. With this object, you can call the 
## kotlin methods directly.
var android_plugin: Object

## A helper JSON marshaller to safely access JSON data from the plugin.
var json_marshaller := JsonMarshaller.new()

## Call this method manually to initialize the plugin.[br]
## [br]
## You have to call this method before you use any of the clients of the plugin.
## For example, a good place to do so, is in the [code]func _enter_tree() -> void:[/code]
## virtual method, so when the Node is ready, the plugin is already initialized.[br]
## [br]
## Returns [PlayGamesPluginError.OK] if the plugin was initialized properly, or
## [PlayGamesPluginError.PLUGIN_NOT_FOUND] otherwise.
func initialize() -> PlayGamesPluginError:
	var plugin_name := "GodotPlayGameServices"
	
	if not android_plugin:
		if Engine.has_singleton(plugin_name):
			print("GodotPlayGameServices plugin initialized successfully.")
			
			android_plugin = Engine.get_singleton(plugin_name)
			
			if FileAccess.file_exists("res://google-services.json"):
				var json_content := FileAccess.get_file_as_string("res://google-services.json")
				if json_content != "":
					android_plugin.initializeFirebaseWithJson(json_content)
			
			android_plugin.initialize()
			android_plugin.requestNotificationPermission()
			android_plugin.imageStored.connect(func(file_path: String):
				image_stored.emit(file_path)
			)
			return PlayGamesPluginError.OK
		else:
			printerr("GodotPlayGameServices not found. Google Play Games Services will not work.")

	return PlayGamesPluginError.PLUGIN_NOT_FOUND

# Displays the given image in the given texture rectangle.[br]
## [br]
## [param texture_rect]: The texture rectangle control to display the image.[br]
## [param file_path]: The file path of the image, for example user://image.png.
func display_image_in_texture_rect(texture_rect: TextureRect, file_path: String) -> void:
	if FileAccess.file_exists(file_path):
		var image := Image.load_from_file(file_path)
		texture_rect.texture = ImageTexture.create_from_image(image)
	else:
		print("File %s does not exist." % file_path)

## Programmatically initializes Firebase options from GDScript, bypassing the need
## for the google-services Gradle plugin or assets/google-services.json config files.
func initialize_firebase_with_options(api_key: String, app_id: String, sender_id: String, project_id: String) -> void:
	if android_plugin:
		android_plugin.initializeFirebaseWithOptions(api_key, app_id, sender_id, project_id)


func _on_token_received(token: String) -> void:
	print("FCM Token: ", token)
func _on_notification_received(title: String, body: String, data: String) -> void:
	print("Notification Received: ", title, " - ", body)
func _on_notification_opened(title: String, body: String, data: String) -> void:
	print("Notification Opened: ", title)
func _on_messaging_error(err: String) -> void:
	print("Messaging Error: ", err)