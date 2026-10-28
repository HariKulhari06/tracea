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
| **Automatic Capture (Gradle Plugin)** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **3rd-Party SDK Network Capture** | ✅ | ❌ | ❌ | ❌ | ❌ |
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

- ⚡ **Automatic Network Capture (Gradle Plugin)** — Transparently instruments OkHttp (`OkHttpClient.Builder.build()`) and `HttpURLConnection` / `HttpsURLConnection` at the bytecode level across your entire app and all 3rd-party SDKs (Mixpanel, Adobe Analytics, Amplitude, etc.). Zero manual interceptors required.
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

## 📦 Installation & Setup

You can integrate Tracea either **automatically** via the Tracea Gradle Plugin (zero code changes, captures 3rd-party SDKs too), or **manually** by attaching an OkHttp interceptor.

---

### Option A: Automatic Network Capture via Gradle Plugin (Recommended)

The **Tracea Gradle Plugin** uses Android Gradle Plugin (AGP 8.0+) ASM bytecode manipulation to automatically instrument your app at compile-time:
- 🚀 **Zero Interceptor Code** — No need to manually add `.addInterceptor(Tracea.interceptor)` to your `OkHttpClient` builders.
- 🌐 **3rd-Party SDK Capture** — Intercepts requests made by internal/external SDKs (e.g., Adobe Analytics, Mixpanel, Firebase, Amplitude, Coil, Retrofit) whether they use OkHttp or `HttpURLConnection` / `HttpsURLConnection`.
- 🛡️ **Zero Release Overhead** — Bytecode transformation is only applied to debuggable variants. In release builds, nothing is modified.

#### Step 1: Configure `settings.gradle.kts`

Add JitPack to `pluginManagement` and configure the plugin resolution strategy:

```kotlin
pluginManagement {
    repositories {
        maven { url = java.net.URI("https://jitpack.io") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.hari.tracea") {
                useModule("com.github.HariKulhari06.tracea:tracea-gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}
```

#### Step 2: Add Plugin to Root `build.gradle.kts`

```kotlin
plugins {
    id("com.hari.tracea") version "1.7.0-alpha03" apply false
}
```

*(Or via Version Catalog `libs.versions.toml`:)*
```toml
[versions]
tracea = "1.7.0-alpha03"

[plugins]
tracea = { id = "com.hari.tracea", version.ref = "tracea" }
```

#### Step 3: Apply in `app/build.gradle.kts`

```kotlin
plugins {
    id("com.android.application")
    id("com.hari.tracea")
}

dependencies {
    // Debugger in debug builds
    debugImplementation("com.github.HariKulhari06.tracea:tracea:1.7.0-alpha03")

    // Zero-overhead stub in release builds (12 KB, 0 dependencies)
    releaseImplementation("com.github.HariKulhari06.tracea:tracea-noop:1.7.0-alpha03")
}
```

#### Step 4: Initialize in `Application`

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

**That's it!** All network requests — whether direct OkHttp calls, `URL.openConnection()` calls, or background requests fired by third-party analytics/ad SDKs — are now captured automatically!

#### (Optional) Plugin Configuration DSL

You can customize the plugin behavior in `app/build.gradle.kts`:

```kotlin
tracea {
    // Enable or disable bytecode instrumentation (default: true)
    enabled = true

    // Target specific build variants (e.g. QA, internal release, debug)
    // includeVariants = listOf("debug", "qa", "internal")

    // Or exclude specific variants from instrumentation (e.g. Play Store production release)
    // excludeVariants = listOf("playstore", "prodRelease")

    // Or instrument all variants unconditionally (default: false)
    // instrumentAllVariants = false

    // Whether to instrument 3rd-party dependencies (default: true).
    // Set to true to automatically capture network calls from 3rd-party SDKs (Mixpanel, etc.).
    // Set to false to only scan project code for maximum incremental build speed.
    instrumentDependencies = true

    // Optional: exclude specific packages from instrumentation
    excludePackages = listOf(
        "com.example.internal.security"
    )
}
```

---

### Option B: Manual Setup (Without Gradle Plugin)

If you prefer not to use the Gradle plugin, you can manually attach Tracea's interceptor to your `OkHttpClient`:

#### 1. Add Dependencies

In `app/build.gradle.kts`:

```kotlin
dependencies {
    debugImplementation("com.github.HariKulhari06.tracea:tracea:1.7.0-alpha03")
    releaseImplementation("com.github.HariKulhari06.tracea:tracea-noop:1.7.0-alpha03")
}
```

#### 2. Initialize in `Application.onCreate()`

```kotlin
Tracea.initialize(context = this)
```

#### 3. Attach OkHttp Interceptor Manually

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(Tracea.interceptor)
    .eventListenerFactory(Tracea.timingEventListenerFactory) // Optional: Timing waterfall
    .build()
```

> **Note**: Manual setup only captures OkHttp requests made through that specific `OkHttpClient` instance. It will not capture traffic from 3rd-party SDKs or `HttpURLConnection`. Use the Gradle plugin (Option A) for complete automatic coverage.

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

## 🌐 Web Dashboard (Browser Inspector)

Tracea ships with a **full-featured browser-based network inspector** that runs directly on your Android device. Open it on your laptop or desktop for a big-screen debugging experience — no proxy, no VPN tunneling, no Chrome DevTools protocol required.

### How to Connect

**Option A — Wi-Fi (same network):**

The dashboard starts automatically from the in-app UI, or programmatically:

```kotlin
Tracea.startWebServer(context, port = 8080)
val url = Tracea.getWebDashboardUrl(context)
// → "http://192.168.1.50:8080"
```

Open the printed URL on any browser on your laptop.

**Option B — USB Cable (corporate Wi-Fi / VPN):**

If your office Wi-Fi isolates devices, use ADB port forwarding:

```bash
adb forward tcp:8080 tcp:8080
```

Then open `http://localhost:8080` on your laptop browser.

### Dashboard Features

| Feature | Description |
| :--- | :--- |
| **Real-time WebSocket streaming** | Transactions appear instantly as they happen on the device |
| **Search & filter** | Filter by URL, host, method, status code chips (2xx, 3xx, 4xx, 5xx, errors) |
| **Session grouping** | Collapsible session cards with request counts and failure indicators |
| **Sort options** | Sort by newest, oldest, slowest, largest, or status code |
| **Collapsible JSON tree viewer** | Syntax-highlighted, line-numbered, expandable/collapsible JSON with copy support |
| **DevTools timing waterfall** | DNS lookup → TCP connect → TLS handshake → TTFB → download breakdown |
| **Mock indicator badges** | Requests intercepted by the mock engine are tagged with 🎭 MOCK |
| **cURL export** | Copy formatted multi-line cURL commands for any request |
| **HAR export** | Export single request, session, or full traffic as HAR files |
| **Live device screenshot** | Capture the current app screen directly from the browser |
| **Canvas annotation tool** | Draw, highlight, and annotate screenshots with undo and color support |
| **1-Click Jira bug report** | Auto-generates timeline, root-cause cURL, attaches screenshot + HAR, and opens Jira |
| **Remote clear** | Clear all device network history from the browser |

### Programmatic API

```kotlin
// Start the web server (default port 8080)
Tracea.startWebServer(context, port = 8080)

// Get the dashboard URL
val url = Tracea.getWebDashboardUrl(context)

// Stop the web server
Tracea.stopWebServer()
```

> **Note**: In release builds with `tracea-noop`, `startWebServer()` returns `false`, `stopWebServer()` is a no-op, and `getWebDashboardUrl()` returns `""`.

---

## 🏗️ Architecture

Tracea is built with a clean, modular architecture:

| Module | Purpose |
| :--- | :--- |
| **`tracea`** | Public facade API — the single entry point for developers |
| **`tracea-core`** | Data pipeline, models, redaction engine, and mock engine |
| **`tracea-okhttp`** | OkHttp `Interceptor` and `EventListener` adapters |
| **`tracea-urlconnection`** | Bytecode tracker wrapping `HttpURLConnection` and `HttpsURLConnection` |
| **`tracea-gradle-plugin`** | AGP 8.0+ ASM bytecode plugin for automatic OkHttp & HttpURLConnection capture |
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
