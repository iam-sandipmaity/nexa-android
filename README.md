# Ollama Mobile - Cloud Client

Android app for browsing Ollama models and chatting with them through Ollama Cloud.

## Features

- Browse curated and cloud-listed Ollama models
- Search and filter models by family
- Chat directly with models from the app
- Store your Ollama API key locally on the device
- No localhost setup required

## Setup

1. Create an API key on `ollama.com`
2. Open the app settings
3. Paste your API key
4. Test the cloud connection
5. Pick a model and start chatting

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

## Notes

- The app now uses Ollama Cloud instead of a local `localhost:11434` server
- This does not run models fully on-device
- True offline on-device model execution would require integrating a mobile inference engine
