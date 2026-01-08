# full_screen_helper

A Flutter package that simplifies handling full-screen intent notifications for Android apps, particularly useful for ride-hailing and time-sensitive applications.

## Features

- 🔔 **Full-Screen Intent Notifications**: Display notifications that wake the screen and appear over the lock screen
- 📱 **Background Message Handling**: Automatically handle Firebase Cloud Messaging (FCM) background messages
- ⚙️ **Settings Integration**: Helper method to open notification settings for permission management
- 🚗 **Ride-Hailing Optimized**: Pre-configured for time-sensitive notifications like ride requests

## Getting started

### Prerequisites

- Flutter SDK >=3.0.0
- Firebase project set up with Cloud Messaging
- Android platform (full-screen intents are Android-specific)

### Installation

Add `full_screen_helper` to your `pubspec.yaml`:

```yaml
dependencies:
  full_screen_helper: ^0.0.1
```

Then run:

```bash
flutter pub get
```

### Android Setup

1. Add the following permission to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
```

2. For Android 12+ (API 31+), you'll also need to request the permission at runtime or guide users to grant it in system settings.

## Usage

### Setting up Background Message Handler

In your app's `main.dart`, register the background message handler:

```dart
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:full_screen_helper/full_screen_helper.dart';

// This must be a top-level function
@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await FullScreenHelper.handleBackgroundMessage(message);
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  
  FirebaseMessaging.onBackgroundMessage(firebaseMessagingBackgroundHandler);
  
  runApp(MyApp());
}
```

### Opening Notification Settings

To help users grant the "Full Screen Intent" permission (required on Android 12+), you can open the app's notification settings:

```dart
import 'package:full_screen_helper/full_screen_helper.dart';

// Open notification settings
await FullScreenHelper.openNotificationSettings();
```

## How it works

The package creates a high-priority notification channel with `fullScreenIntent: true`, which allows notifications to appear over the lock screen and wake the device. This is particularly useful for time-sensitive notifications like incoming ride requests.

The notification channel is automatically created with:
- Maximum importance
- Sound enabled
- Full-screen intent capability

## Important Notes

- **Android Only**: Full-screen intents are an Android-specific feature
- **Permissions**: Android 12+ requires users to manually grant "Full Screen Intent" permission in system settings
- **Firebase Required**: This package requires Firebase Cloud Messaging to be set up in your project

## Additional information

For more information about full-screen intents and notification channels, see:
- [Android Full-Screen Intent Documentation](https://developer.android.com/training/notify-user/time-sensitive)
- [Flutter Local Notifications Plugin](https://pub.dev/packages/flutter_local_notifications)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

See the [LICENSE](LICENSE) file for details.
