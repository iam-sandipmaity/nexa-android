# Ollama Mobile - AI Model Hub

A beautiful Android app to **browse, download, and chat with AI models** using Ollama - all locally on your device, completely free!

## Features

### Model Browser
- Browse 18+ popular AI models (Llama, Gemma, Mistral, Qwen, Phi, etc.)
- Search and filter models by family
- See model details (size, RAM requirements, description)
- Download models directly from the app
- Track download progress

### Chat Interface
- Chat with any downloaded model
- Beautiful chat bubbles with Material Design 3
- Dark and light theme support
- Clear chat history option

### Model Management
- View all downloaded models
- Delete models to free up space
- See model size and info
- Quick access to chat with any model

## Download APK

### Debug APK (Latest)
Download from GitHub Actions artifacts after any push/merge:
1. Go to **Actions** tab
2. Click on any workflow run
3. Click on **debug-apk** or **ollama-mobile-debug.apk** artifact
4. Download and install on your phone

### Release APK (Main branch)
Built automatically when code is merged to `main` branch.
Find it in the Actions artifacts as **ollama-mobile-release.apk**.

## Popular Models

| Model | Size | Description |
|-------|------|-------------|
| TinyLlama | 637MB | Ultra-lightweight, 1.1B params |
| Phi-3.5 | 2.2GB | Microsoft's efficient small model |
| Llama 3.2 | 2.0GB | Latest Meta model |
| Gemma 2B | 1.6GB | Google's efficient model |
| Mistral | 4.1GB | Excellent instruction following |
| Llama 3.1 | 4.3GB | Powerful open-source LLM |
| Qwen 2.5 | 3.3GB | Alibaba's multilingual model |
| Code Llama | 3.8GB | Specialized for code |

## Setup

### Prerequisites
1. Install **Ollama** on your computer: [ollama.ai](https://ollama.ai)
2. Run Ollama: `ollama serve`

### For Local Network Access
```bash
# Find your IP address
# Windows: ipconfig
# Mac/Linux: ifconfig

# Enter URL in app: http://192.168.1.100:11434/
```

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build  
./gradlew assembleRelease
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

## Tech Stack

- Kotlin
- Jetpack Compose
- Material Design 3
- Retrofit for networking
- MVVM Architecture
- GitHub Actions CI/CD

## How It Works

1. **Browse Models** - See all available models with descriptions
2. **Download** - Tap download to get any model (via Ollama API)
3. **Chat** - Once downloaded, tap "Chat" to start conversing
4. **Delete** - Remove models to free up space

## CI/CD

- ✅ Automatic builds on push
- ✅ Debug APK for testing
- ✅ Release APK on main branch merge
- ✅ Direct artifact downloads

## License

MIT License - Use freely!

---

Made with ❤️ for AI enthusiasts
