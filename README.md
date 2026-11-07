# CloudPlayer

A Jetpack Compose music player that streams audio straight from your Google Drive, with offline downloads, an equalizer and a sleep timer.

## Features

- Sign in with Google and browse your Drive files in a dedicated explorer tab
- Stream or download tracks for offline playback
- Background playback through a Media3 `PlaybackService` with notification controls
- Built-in equalizer dialog and sleep timer
- Plays local device audio as well as Drive content
- Navigation drawer shell built entirely in Compose

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **SDK:** min 24, target 34
- **Package:** `com.example.cloudplayer`
- **Libraries:** Hilt (dependency injection), Media3 / ExoPlayer, Coil, Navigation component, Google Sign-In, Google Drive API, Kotlin coroutines, ViewModel

<sub>23 source files · 2422 lines of code</sub>

## Getting started

```bash
git clone https://github.com/rajabbov/CloudPlayer.git
```

Open the folder in Android Studio, let Gradle sync, and run the `app` configuration on a device or emulator.

### Configuration

The app requests a Google OAuth client for Drive access. Create your own in the Google Cloud Console and add it to `local.properties` before building.

## License

MIT
