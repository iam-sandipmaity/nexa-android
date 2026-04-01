# Ollama Mobile - AI Model Hub

Android app to browse, download, and chat with Ollama models from a phone.

## Features

- Browse popular models such as Llama, Gemma, Mistral, Qwen, and Phi
- View model size, RAM needs, and descriptions
- Download and delete local models
- Chat with downloaded models through a Compose UI
- Configure the Ollama server URL from the app

## Download APK

The project now produces only the release APK.

1. Open the GitHub Actions run
2. Download the `ollama-mobile-release` artifact
3. Install `app-release.apk` on your device

## Setup

1. Install Ollama on your computer
2. Start Ollama with `ollama serve`
3. Enter your server URL in the app, for example `http://192.168.1.100:11434/`

## Build

Linux/macOS:

```bash
./gradlew assembleRelease
```

Windows PowerShell:

```powershell
.\gradlew.bat assembleRelease
```

Release APK output:

`app/build/outputs/apk/release/app-release.apk`

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Retrofit
- MVVM

## CI/CD

- Automatic release APK build on push
- Release artifact upload in GitHub Actions

## License

MIT
