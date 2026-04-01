# Ollama Mobile - AI Model Hub

A beautiful Android app to **browse, download, and chat with AI models** using Ollama - all locally on your device, completely free!

## Features

### 🤖 Model Browser
- Browse 18+ popular AI models (Llama, Gemma, Mistral, Qwen, Phi, etc.)
- Search and filter models by family
- See model details (size, RAM requirements, description)
- **Download models directly** from the app
- Track download progress

### 💬 Chat Interface
- Chat with any downloaded model
- Beautiful chat bubbles with Material Design 3
- Dark and light theme support
- Clear chat history option

### 📦 Model Management
- View all downloaded models
- Delete models to free up space
- See model size and info
- Quick access to chat with any model

## Popular Models Available

| Model | Size | Description |
|-------|------|-------------|
| TinyLlama | 637MB | Ultra-lightweight, great for testing |
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
2. Ensure Ollama is running: `ollama serve`

### For Local Network Access
```bash
# Find your computer's IP address
# Windows: ipconfig
# Mac/Linux: ifconfig

# Enter URL in app: http://192.168.1.100:11434/
```

### For Android (Ollama on Phone)
Currently, Ollama runs on desktop. For mobile-only usage, you can:
- Use Termux to run Ollama on Android
- Connect to a remote Ollama server

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Tech Stack

- **Kotlin** - Modern Android development
- **Jetpack Compose** - Declarative UI
- **Material Design 3** - Beautiful, modern design
- **Retrofit** - Network requests
- **MVVM Architecture** - Clean separation
- **GitHub Actions** - CI/CD pipeline

## Screenshots

The app features:
- 🎨 Clean, modern Material Design 3 UI
- 🌙 Dark mode support
- 📱 Mobile-optimized layouts
- 🔍 Easy model search
- 📊 Download progress tracking

## How It Works

1. **Browse Models** - See all available models with descriptions
2. **Download** - Tap download to get any model (via Ollama API)
3. **Chat** - Once downloaded, tap "Chat" to start conversing
4. **Manage** - View/delete models from the Downloaded section

## GitHub Actions CI/CD

- ✅ Automatic linting
- ✅ Unit tests
- ✅ Debug APK build
- ✅ Release APK build on merge to main

## License

MIT License - Use freely!

---

Made with ❤️ for AI enthusiasts
