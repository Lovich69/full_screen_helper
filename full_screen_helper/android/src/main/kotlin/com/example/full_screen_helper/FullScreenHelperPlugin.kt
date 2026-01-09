package com.example.full_screen_helper

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/**
 * FullScreenHelperPlugin
 * 
 * This plugin serves two main purposes:
 * 1. Managing the "Full Screen Intent" permission (checking and requesting it on Android 14+).
 * 2. Waking up the device screen when the app is launched (e.g., from a Full Screen Intent notification).
 * 
 * It attaches to the main Activity lifecycle to ensure the screen wakes up immediately upon launch,
 * regardless of whether the Flutter engine has fully loaded.
 */
class FullScreenHelperPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel : MethodChannel
  private var activity: Activity? = null
  private var context: Context? = null

  /**
   * Called when the plugin is attached to the Flutter engine.
   * Sets up the MethodChannel to listen for Dart calls.
   */
  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "full_screen_helper")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  /**
   * Handles method calls from Dart (your Custom Action).
   */
  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "canUseFullScreenIntent") {
       // Android 14 (API 34) introduced restrictions on Full Screen Intents.
       // We must check if the user has granted this permission.
       if (Build.VERSION.SDK_INT >= 34) {
          val nm = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
          if (nm != null) {
              result.success(nm.canUseFullScreenIntent())
          } else {
              result.error("UNAVAILABLE", "NotificationManager not found", null)
          }
       } else {
          // On older versions (Android 13 and below), this permission is granted automatically
          // via the manifest declaration, so we return true.
          result.success(true)
       }
    } else if (call.method == "requestFullScreenIntent") {
       // Helper to open the specific settings page for Full Screen Intents on Android 14+.
       if (Build.VERSION.SDK_INT >= 34) {
          try {
             val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
             intent.data = Uri.parse("package:" + context?.packageName)
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
             context?.startActivity(intent)
             result.success(true)
          } catch (e: Exception) {
             result.error("UNAVAILABLE", "Could not open settings", e.message)
          }
       } else {
          // No settings page needed for older versions.
          result.success(true)
       }
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    context = null
  }

  /**
   * Called when the plugin is attached to the MainActivity.
   * This is the earliest point we can access the Activity to wake the screen.
   */
  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
    wakeUpScreen()
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
    wakeUpScreen()
  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  /**
   * The core logic to wake the screen and show the app over the lock screen.
   * This uses different APIs depending on the Android version.
   */
  private fun wakeUpScreen() {
    val act = activity ?: return
    
    // For Android 8.1 (Oreo MR1) and newer:
    // We use the modern setShowWhenLocked and setTurnScreenOn APIs.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        act.setShowWhenLocked(true)
        act.setTurnScreenOn(true)
    } else {
        // For older Android versions (pre-8.1):
        // We use the legacy Window flags.
        act.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        act.window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }
    
    // Additional flags to keep the screen on while the activity is visible
    // and allow it to function over the lock screen.
    act.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    act.window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
  }
}
