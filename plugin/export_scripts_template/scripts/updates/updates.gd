@icon("res://addons/GodotPlayGameServices/assets/icons/update_client.svg")
class_name PlayGamesUpdatesClient
extends Node
## Client with Google Play In-App Update functionality.
##
## This client exposes methods and signals for checking and installing
## Google Play in-app updates.
##
## Supports both Immediate and Flexible update flows.

## Emitted after calling check_for_update().
##
## Parameters:
## [param available]: True if an update is available.
## [param version_code]: Available version code.
## [param priority]: Update priority (0-5).
## [param staleness_days]: Number of days the installed version has been stale.
## [param immediate_allowed]: True if an Immediate Update is allowed.
## [param flexible_allowed]: True if a Flexible Update is allowed.
signal update_checked(
	available: bool,
	version_code: int,
	priority: int,
	staleness_days: int,
	immediate_allowed: bool,
	flexible_allowed: bool
)

## Emitted when checking for updates fails.
signal update_check_failed(error_code: int, error_message: String)

## Emitted when an update flow has started.
signal update_started(immediate: bool)

## Emitted during a Flexible Update download.
signal update_download_progress(bytes_downloaded: int, total_bytes: int)

## Emitted when the Flexible Update download finishes.
signal update_download_completed()

## Emitted after the update has been successfully installed.
signal update_install_completed()

## Emitted when the user cancels the update.
signal update_cancelled()

## Emitted when the update flow fails.
signal update_failed(error_code: int, error_message: String)


func _ready() -> void:
	_connect_signals()


func _connect_signals() -> void:
	if GodotPlayGameServices.android_plugin:

		GodotPlayGameServices.android_plugin.update_checked.connect(
			func(
				available: bool,
				version_code: int,
				priority: int,
				staleness_days: int,
				immediate_allowed: bool,
				flexible_allowed: bool
			):
				update_checked.emit(
					available,
					version_code,
					priority,
					staleness_days,
					immediate_allowed,
					flexible_allowed
				)
		)

		GodotPlayGameServices.android_plugin.update_check_failed.connect(
			func(error_code: int, error_message: String):
				update_check_failed.emit(error_code, error_message)
		)

		GodotPlayGameServices.android_plugin.update_started.connect(
			func(immediate: bool):
				update_started.emit(immediate)
		)

		GodotPlayGameServices.android_plugin.update_download_progress.connect(
			func(bytes_downloaded: int, total_bytes: int):
				update_download_progress.emit(bytes_downloaded, total_bytes)
		)

		GodotPlayGameServices.android_plugin.update_download_completed.connect(
			func():
				update_download_completed.emit()
		)

		GodotPlayGameServices.android_plugin.update_install_completed.connect(
			func():
				update_install_completed.emit()
		)

		GodotPlayGameServices.android_plugin.update_cancelled.connect(
			func():
				update_cancelled.emit()
		)

		GodotPlayGameServices.android_plugin.update_failed.connect(
			func(error_code: int, error_message: String):
				update_failed.emit(error_code, error_message)
		)


## Checks Google Play for available updates.
func check_for_update() -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.checkForUpdate()


## Starts an Immediate Update flow.
func start_immediate_update() -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.startImmediateUpdate()


## Starts a Flexible Update flow.
func start_flexible_update() -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.startFlexibleUpdate()


## Starts the best update type supported by Google Play.
func start_best_update() -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.startBestUpdate()


## Completes a downloaded Flexible Update.
func complete_update() -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.completeUpdate()


## Resumes an interrupted Immediate Update if necessary.
func resume_update_if_needed() -> void:
	if GodotPlayGameServices.android_plugin:
		GodotPlayGameServices.android_plugin.resumeUpdateIfNeeded()