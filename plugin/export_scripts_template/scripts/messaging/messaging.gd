@icon("res://addons/GodotPlayGameServices/assets/icons/messaging.svg")
class_name FirebaseCloudMessagingClient
extends Node

## Client exposing Firebase Cloud Messaging functionality.
##
## Supports:
## - FCM registration tokens
## - Topic subscriptions
## - Receiving notifications while the app is running
## - Receiving notification-open events

## Fired when an FCM registration token is obtained.
signal token_received(token: String)

## Fired whenever Firebase issues a new registration token.
signal token_refreshed(token: String)

## Fired when a push notification is received while the app is running.
##
## Parameters:
## title : Notification title
## body  : Notification body
## data  : JSON string containing the notification data payload
signal notification_received(
	title: String,
	body: String,
	data: String
)

## Fired when the application is opened from a notification.
##
## Parameters:
## title : Notification title
## body  : Notification body
## data  : JSON string containing the notification data payload
signal notification_opened(
	title: String,
	body: String,
	data: String
)

## Fired after successfully subscribing to a topic.
signal topic_subscribed(topic: String)

## Fired after successfully unsubscribing from a topic.
signal topic_unsubscribed(topic: String)

## Fired whenever an FCM operation fails.
signal messaging_error(message: String)


func _ready() -> void:
	_connect_signals()


func _connect_signals() -> void:
	if not GodotPlayGameServices.android_plugin:
		return

	GodotPlayGameServices.android_plugin.tokenReceived.connect(
		func(token: String):
			token_received.emit(token)
	)

	GodotPlayGameServices.android_plugin.tokenRefreshed.connect(
		func(token: String):
			token_refreshed.emit(token)
	)

	GodotPlayGameServices.android_plugin.notificationReceived.connect(
		func(title: String, body: String, data: String):
			notification_received.emit(title, body, data)
	)

	GodotPlayGameServices.android_plugin.notificationOpened.connect(
		func(title: String, body: String, data: String):
			notification_opened.emit(title, body, data)
	)

	GodotPlayGameServices.android_plugin.topicSubscribed.connect(
		func(topic: String):
			topic_subscribed.emit(topic)
	)

	GodotPlayGameServices.android_plugin.topicUnsubscribed.connect(
		func(topic: String):
			topic_unsubscribed.emit(topic)
	)

	GodotPlayGameServices.android_plugin.messagingError.connect(
		func(message: String):
			messaging_error.emit(message)
	)


## Retrieves the current FCM registration token.
func get_token() -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.getToken()


## Subscribes the device to an FCM topic.
func subscribe_to_topic(topic: String) -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.subscribeToTopic(topic)


## Unsubscribes the device from an FCM topic.
func unsubscribe_from_topic(topic: String) -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.unsubscribeFromTopic(topic)

