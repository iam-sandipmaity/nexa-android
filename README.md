# Nexa - Hybrid AI Chat for Android

Nexa is an Android app for chatting with both cloud-hosted Ollama models and downloaded offline GGUF models.

## Features

- Cloud chat with Ollama models using your API key
- Offline chat with downloaded GGUF models
- Import custom offline models from Hugging Face URLs
- Search and filter models by family
- Stop generation during streaming responses
- Cleaner response rendering for model control tokens

## Setup

1. Create an API key on `ollama.com` for cloud models
2. Open the app Settings
3. Paste your API key and test connection
4. Pick a cloud model or download/import an offline model
5. Start chatting

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

- Cloud mode uses Ollama Cloud APIs
- Offline mode runs local GGUF models through on-device inference integration
- API keys are stored locally on device settings
