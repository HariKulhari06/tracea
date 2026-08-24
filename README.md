# 📡 Tracea — Android In-App Network Inspector & API Mocking

<p align="center">
  <img src="logo.png" width="160" alt="Tracea Logo"/>
</p>

<p align="center">
  <a href="https://jitpack.io/#HariKulhari06/tracea"><img src="https://jitpack.io/v/HariKulhari06/tracea.svg" alt="JitPack"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License"/></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform"/></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.1.20-purple.svg" alt="Kotlin"/></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg" alt="Jetpack Compose"/></a>
  <a href="https://square.github.io/okhttp/"><img src="https://img.shields.io/badge/OkHttp-5.x-orange.svg" alt="OkHttp"/></a>
</p>

<p align="center">
  <b>Inspect, mock, and debug Android network traffic directly inside your app — no proxy setup, no SSL certificates, no Charles/Proxyman required.</b>
</p>

---

## 📱 Visual Overview

| Network Inspector | Request Details | API Mocking |
| :---: | :---: | :---: |
| <img src="screenshot/Screenshot_20260813_093825.png" width="280" alt="Android Network Inspector - Request List"/> | <img src="screenshot/Screenshot_20260813_093838.png" width="280" alt="HTTP Request Response Details"/> | <img src="screenshot/Screenshot_20260813_093900.png" width="280" alt="API Mock Rules Engine"/> |

---

## 🤔 Why Tracea? (vs Chucker, Charles Proxy, Stetho, Flipper)

Existing Android network debugging tools each have tradeoffs. Tracea is built to eliminate them:

| Feature | Tracea | Chucker | Charles Proxy | Stetho | Flipper |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **In-App UI (No external tool)** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Zero proxy/SSL setup** | ✅ | ✅ | ❌ | ✅ | ✅ |
| **Client-side API mocking** | ✅ | ❌ | ✅ | ❌ | ❌ |
| **Collapsible JSON tree viewer** | ✅ | ❌ | ✅ | ✅ | ✅ |
| **HAR file export** | ✅ | ❌ | ✅ | ❌ | ❌ |
| **cURL command copy** | ✅ | ✅ | ✅ | ❌ | ✅ |
| **Timing waterfall (DNS/TLS/TTFB)** | ✅ | ❌ | ✅ | ❌ | ❌ |
| **Automatic token/password redaction** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Floating overlay with live counter** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Session management** | ✅ | ❌ | ✅ | ❌ | ❌ |
| **Web Inspector (browser dashboard)** | ✅ | ❌ | ❌ | ✅ | ❌ |
| **Multipart & binary stream support** | ✅ | ✅ | ✅ | ❌ | ✅ |
| **No-op release artifact (0 KB impact)** | ✅ | ✅ | N/A | ❌ | ❌ |
| **Jetpack Compose UI** | ✅ | ❌ | N/A | ❌ | ❌ |
| **Actively maintained (2026)** | ✅ | ✅ | ✅ | ❌ | ⚠️ |

> **TL;DR**: Tracea combines the best of Chucker (in-app simplicity) + Charles (mocking & HAR export) + modern Compose UI — all in one lightweight library.

---

## ✨ Features

- 🔍 **Real-time HTTP/HTTPS Traffic Inspection** — Monitor all OkHttp & Retrofit network calls with color-coded status badges, request/response headers, and formatted body payloads.
- 🎭 **Dynamic API Response Mocking** — Intercept requests by URL path and HTTP method. Override status codes (401, 500, etc.), inject custom JSON bodies, and simulate network latency — all without touching backend code.
- 📉 **DevTools-Style Timing Waterfall** — Visualize per-request latency with DNS lookup, TCP connect, TLS handshake, TTFB, and download breakdowns.
- 🔒 **Automatic Privacy & Redaction** — Masks sensitive headers (`Authorization`, `Cookie`, `X-API-Key`) and JSON keys (`password`, `token`, `secret`) recursively before display or storage.
- 🎛️ **Session Management** — Organize network transactions by debugging sessions. Name, expand/collapse, and export sessions individually.
- 🔌 **Draggable Floating Debug Overlay** — A floating badge with live transaction count. Tap to launch the inspector from any screen. Fully draggable.
- 📋 **1-Click Export** — Copy **cURL** commands, export single-request or full-session **HAR** files, share plain text summaries.
- 🌐 **Embedded Web Inspector** — Access a full browser-based network dashboard at `http://<phone-ip>:8080` over Wi-Fi. No USB cables needed.
- 🎨 **Modern Dark-Mode Compose UI** — Beautiful, minimalist interface built entirely with Jetpack Compose. Custom JSON syntax highlighting with collapsible tree and line numbers.
- 📦 **Production-Safe `tracea-noop`** — A 12 KB empty stub artifact for release builds with zero runtime overhead, zero dependencies, and zero security risk.

---

## 📦 Installation

### Step 1: Add JitPack Repository

Add the **JitPack** repository to your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: Add Tracea Dependency

```kotlin
dependencies {
    // Full debugger in debug builds
    debugImplementation("com.github.HariKulhari06:tracea:1.2.0")

    // (Optional) Zero-overhead stub for release builds
    releaseImplementation("com.github.HariKulhari06:tracea-noop:1.2.0")
}
```

> **Note**: The `tracea-noop` artifact is optional but recommended. It ensures all `Tracea.*` calls compile in release builds as empty stubs with zero APK size impact.

---

## 🚀 Quick Start (OkHttp / Retrofit)

### 1. Initialize in Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Tracea.initialize(
            context = this,
            config = TraceaConfig(
                enabled = BuildConfig.DEBUG,
                showFloatingButton = true
            )
        )
    }
}
```

### 2. Attach OkHttp Interceptor

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(Tracea.interceptor)
    .eventListenerFactory(Tracea.timingEventListenerFactory) // Optional: Timing waterfall
    .build()
```

That's it! Every network call made through this OkHttp client will now appear in the Tracea inspector.

---

## 🛡️ Manual Capture API (Non-OkHttp Clients)

For network stacks that don't use OkHttp (Ktor, WebSockets, GraphQL subscriptions, or legacy HTTP clients):

```kotlin
// 1. Start tracking a request
val call = Tracea.startManualRequest(method = "POST", url = "https://api.example.com/v1/users")

// 2. Set request details
call.requestHeaders(mapOf("Content-Type" to "application/json", "Authorization" to "Bearer ..."))
    .requestBody("""{"username": "johndoe"}""", "application/json")

// 3. Record the response
call.response(
    statusCode = 201,
    headers = mapOf("Content-Type" to "application/json"),
    body = """{"id": 42, "status": "created"}""",
    contentType = "application/json"
)

// Or log failures/cancellations
// call.failure(IOException("Connection timeout"))
// call.cancel()
```

---

## ⚙️ Configuration & Redaction

```kotlin
Tracea.initialize(
    context = this,
    config = TraceaConfig(
        enabled = true,
        showFloatingButton = true,
        redactionConfig = RedactionConfig(
            sensitiveHeaders = setOf("Authorization", "Cookie", "Set-Cookie", "X-Api-Key"),
            sensitiveJsonKeys = setOf("password", "token", "access_token", "secret")
        ),
        storageConfig = StorageConfig(
            maxRequests = 500 // Auto-purges oldest events
        )
    )
)
```

---

## 🎭 API Mocking

Tracea includes a built-in mock engine that intercepts matching requests at the OkHttp layer and returns custom responses — no backend changes required.

**Use cases:**
- Test error handling: Force `401 Unauthorized`, `500 Internal Server Error`
- Test empty states: Return `[]` or `{}` for any endpoint
- Test slow networks: Add 3–5 second artificial latency
- Frontend-first development: Build UI before the backend is ready

Mock rules are configured through the in-app Mocking UI and persisted to disk.

---

## 🏗️ Architecture

Tracea is built with a clean, modular architecture:

| Module | Purpose |
| :--- | :--- |
| **`tracea`** | Public facade API — the single entry point for developers |
| **`tracea-core`** | Data pipeline, models, redaction engine, and mock engine |
| **`tracea-okhttp`** | OkHttp `Interceptor` and `EventListener` adapters |
| **`tracea-manual`** | Builder API for non-OkHttp network stacks |
| **`tracea-storage`** | Room database persistence with automatic retention |
| **`tracea-ui`** | Dark-mode Jetpack Compose inspector interface |
| **`tracea-web`** | Embedded Ktor web server and browser-based dashboard |
| **`tracea-noop`** | 12 KB production-safe stub artifact (zero dependencies) |

---

## 📊 Production Impact

| Metric | Debug Build | Release Build (`tracea-noop`) |
| :--- | :--- | :--- |
| **Library Size** | ~920 KB | **12 KB** |
| **APK Size Impact** | Minimal (debug only) | **0 MB** |
| **Transitive Dependencies** | Room, OkHttp, Compose, Ktor | **None** |
| **Method Count** | ~2,000 | **~15** |
| **Runtime Overhead** | Negligible | **Zero** |
| **Security Risk** | Debug-only (no production exposure) | **Zero** |

---

## 📄 License

Licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <b>Built with ❤️ by <a href="https://github.com/HariKulhari06">Hari Kulhari</a></b>
</p>
