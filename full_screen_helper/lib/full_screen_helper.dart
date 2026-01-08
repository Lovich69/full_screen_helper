library full_screen_helper;

import 'package:firebase_core/firebase_core.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'dart:convert';
import 'package:url_launcher/url_launcher.dart';

class FullScreenHelper {
  /// Handles background messages and displays a full-screen intent notification.
  static Future<void> handleBackgroundMessage(RemoteMessage message) async {
    // Ensure Firebase is initialized
    try {
      await Firebase.initializeApp();
    } catch (e) {
      // Firebase might already be initialized
    }

    print('[Background FCM] Received message: ${message.messageId}');

    final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
        FlutterLocalNotificationsPlugin();

    const AndroidInitializationSettings initializationSettingsAndroid =
        AndroidInitializationSettings('@mipmap/ic_launcher');

    const InitializationSettings initializationSettings =
        InitializationSettings(android: initializationSettingsAndroid);

    await flutterLocalNotificationsPlugin.initialize(initializationSettings);

    // Ensure channel exists
    const AndroidNotificationChannel channel = AndroidNotificationChannel(
      'ride_request_channel',
      'Ride Requests',
      description: 'High importance channel for ride offers',
      importance: Importance.max,
      playSound: true,
    );

    await flutterLocalNotificationsPlugin
        .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin>()
        ?.createNotificationChannel(channel);

    // Show notification with Full Screen Intent
    const AndroidNotificationDetails androidPlatformChannelSpecifics =
        AndroidNotificationDetails(
      'ride_request_channel',
      'Ride Requests',
      channelDescription: 'High importance channel for ride offers',
      importance: Importance.max,
      priority: Priority.max,
      fullScreenIntent: true, // Wake the screen!
      styleInformation: BigTextStyleInformation(''),
    );

    const NotificationDetails platformChannelSpecifics =
        NotificationDetails(android: androidPlatformChannelSpecifics);

    await flutterLocalNotificationsPlugin.show(
      message.hashCode,
      message.data['title'] ?? 'New Ride Request',
      message.data['body'] ?? 'Tap to accept',
      platformChannelSpecifics,
      payload: jsonEncode(message.data),
    );
  }

  /// Opens the App Settings so the user can manually grant "Full Screen Intent" permissions.
  static Future<void> openNotificationSettings() async {
    final Uri settingsUri = Uri.parse('app-settings:');
    if (await canLaunchUrl(settingsUri)) {
      await launchUrl(settingsUri);
    }
  }
}
