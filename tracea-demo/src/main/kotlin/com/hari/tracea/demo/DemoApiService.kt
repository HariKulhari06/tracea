package com.hari.tracea.demo

import com.hari.tracea.Tracea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class DemoApiService {
    private val client = OkHttpClient.Builder()
        .addInterceptor(Tracea.okHttpInterceptor())
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private suspend fun executeRequest(request: Request): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(response.body?.string() ?: "")
                } else {
                    Result.failure(Exception("HTTP error ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(): Result<String> {
        val request = Request.Builder()
            .url("https://jsonplaceholder.typicode.com/users")
            .build()
        return executeRequest(request)
    }

    suspend fun postLogin(): Result<String> {
        val json = """{"email":"test@example.com","password":"super_secret_password"}"""
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://httpbin.org/post")
            .post(body)
            .build()
        return executeRequest(request)
    }

    suspend fun putProfile(): Result<String> {
        val json = """{"name":"John Doe","company":"Acme Corp"}"""
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://jsonplaceholder.typicode.com/posts/1")
            .put(body)
            .build()
        return executeRequest(request)
    }

    suspend fun deleteItem(): Result<String> {
        val request = Request.Builder()
            .url("https://jsonplaceholder.typicode.com/posts/1")
            .delete()
            .build()
        return executeRequest(request)
    }

    suspend fun get404(): Result<String> {
        val request = Request.Builder()
            .url("https://httpbin.org/status/404")
            .build()
        return executeRequest(request)
    }

    suspend fun get500(): Result<String> {
        val request = Request.Builder()
            .url("https://httpbin.org/status/500")
            .build()
        return executeRequest(request)
    }

    suspend fun timeout(): Result<String> {
        val request = Request.Builder()
            .url("http://10.255.255.1")
            .build()
        return executeRequest(request)
    }

    suspend fun largeResponse(): Result<String> {
        val request = Request.Builder()
            .url("https://httpbin.org/bytes/500000")
            .build()
        return executeRequest(request)
    }

    suspend fun postWithBody(): Result<String> {
        val json = """
            {
                "id": 123,
                "items": ["item1", "item2"],
                "active": true
            }
        """.trimIndent()
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://httpbin.org/post")
            .post(body)
            .build()
        return executeRequest(request)
    }

    suspend fun redactedHeaders(): Result<String> {
        val request = Request.Builder()
            .url("https://httpbin.org/get")
            .addHeader("Authorization", "Bearer token123456789")
            .addHeader("Cookie", "session_id=abcdef")
            .build()
        return executeRequest(request)
    }

    suspend fun uploadMultipart(): Result<String> {
        val multipartBody = okhttp3.MultipartBody.Builder()
            .setType(okhttp3.MultipartBody.FORM)
            .addFormDataPart("userId", "1042")
            .addFormDataPart("title", "User Avatar Upload")
            .addFormDataPart("description", "Tracea multipart upload test")
            .addFormDataPart(
                "avatar",
                "avatar.png",
                "FAKE_PNG_BINARY_HEADER_DATA_TRACEA_TEST".toByteArray().toRequestBody("image/png".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("https://postman-echo.com/post")
            .post(multipartBody)
            .build()
        return executeRequest(request)
    }

    suspend fun downloadImage(): Result<String> {
        val request = Request.Builder()
            .url("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png")
            .build()
        return executeRequest(request)
    }

    suspend fun manualCapture() {
        val call = Tracea.startRequest("GET", "https://api.example.com/manual-test") ?: return
        call.requestHeaders(mapOf("Accept" to "application/json"))
        delay(200)  // Simulate network delay
        call.response(
            statusCode = 200,
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"manual\": true, \"message\": \"This was manually captured\"}"
        )
    }

    /**
     * Comprehensive scenario populating EVERY single UI section of the Details screen:
     * - Request URL & Info (Method, Host, Scheme, Port, Content-Type, Request Size)
     * - Query Parameters with multiple keys & multi-value keys
     * - Extensive Request Headers (Auth, Content-Type, custom client headers)
     * - Pretty/Raw JSON Request Body
     * - Response Status Header card (200 OK, Duration, Size)
     * - Response Info card (Status Code, Status Message, Content-Type, Response Size)
     * - Extensive Response Headers (Server, Cache-Control, ETag, etc.)
     * - Set-Cookie card with multiple cookies
     * - Pretty/Raw JSON Response Body with nested objects & arrays
     * - High-resolution Timing metrics with Waterfall visualizer (DNS, Connect, TLS, Waiting TTFB, Download)
     */
    suspend fun fullParityAllDataScenario() {
        val fullUrl = "https://api.example.com:8443/v1/orders/checkout" +
                "?userId=usr_98765" +
                "&currency=USD" +
                "&includeItems=true" +
                "&filter=active" +
                "&filter=in_stock" +
                "&coupon=SUPER_PROMO_2026"

        val call = Tracea.startRequest("POST", fullUrl) ?: return

        val requestHeaders = mapOf(
            "Authorization" to "Bearer tracea_super_secret_jwt_token_sample",
            "Content-Type" to "application/json; charset=UTF-8",
            "Accept" to "application/json",
            "X-Client-Version" to "2.4.0-android",
            "X-Platform" to "Android",
            "X-Device-Model" to "Pixel 8 Pro",
            "X-Trace-Id" to "trc-9f8e7d6c-5b4a-3210",
            "User-Agent" to "TraceaSampleDemo/2.4.0 (Linux; Android 14)"
        )
        call.requestHeaders(requestHeaders)

        val requestBodyJson = """
            {
              "checkoutId": "chk_2026_xyz987",
              "customer": {
                "id": "usr_98765",
                "name": "Jane Developer",
                "email": "jane.dev@example.com",
                "loyaltyTier": "Platinum",
                "shippingAddress": {
                  "street": "100 Innovation Way",
                  "city": "San Francisco",
                  "state": "CA",
                  "zip": "94107",
                  "country": "USA"
                }
              },
              "items": [
                {
                  "sku": "PROD-A100",
                  "title": "Ergonomic Mechanical Keyboard",
                  "quantity": 1,
                  "unitPrice": 149.99,
                  "currency": "USD"
                },
                {
                  "sku": "PROD-B200",
                  "title": "USB-C Ultra-Fast Cable (2m)",
                  "quantity": 2,
                  "unitPrice": 19.99,
                  "currency": "USD"
                }
              ],
              "payment": {
                "method": "CREDIT_CARD",
                "cardLastFour": "4242",
                "provider": "Stripe"
              },
              "discountCode": "SUPER_PROMO_2026",
              "notes": "Please leave package at front door."
            }
        """.trimIndent()
        call.requestBody(requestBodyJson, "application/json; charset=UTF-8")

        // Set detailed waterfall timing phases
        call.timing(
            dnsMs = 28L,
            connectMs = 45L,
            tlsMs = 62L,
            waitingMs = 180L,
            downloadMs = 35L
        )

        delay(350) // simulate real network latency

        val responseHeaders = mapOf(
            "Content-Type" to "application/json; charset=UTF-8",
            "Server" to "cloudflare/nginx",
            "Cache-Control" to "no-cache, no-store, must-revalidate",
            "ETag" to "W/\"9f8e7d-6c5b-4a32-10\"",
            "X-Request-Id" to "req_20261006_abcdef",
            "X-RateLimit-Limit" to "1000",
            "X-RateLimit-Remaining" to "998",
            "Set-Cookie" to "session_id=sess_abc123xyz789; Path=/; Secure; HttpOnly; SameSite=Strict\n" +
                    "guest_token=gst_998877; Path=/; Max-Age=86400; Secure\n" +
                    "theme_pref=dark_mode; Path=/"
        )

        val responseBodyJson = """
            {
              "status": "success",
              "orderId": "ord_884920491",
              "orderNumber": "#TRC-2026-9921",
              "totalAmount": 189.97,
              "currency": "USD",
              "summary": {
                "subtotal": 189.97,
                "discount": 20.00,
                "shipping": 0.00,
                "tax": 15.20,
                "grandTotal": 185.17
              },
              "paymentStatus": "PAID",
              "estimatedDelivery": "2026-10-09T18:00:00Z",
              "trackingUrl": "https://shipping.example.com/track/TRC884920491",
              "receiptItems": [
                {
                  "sku": "PROD-A100",
                  "title": "Ergonomic Mechanical Keyboard",
                  "quantity": 1,
                  "totalPrice": 149.99
                },
                {
                  "sku": "PROD-B200",
                  "title": "USB-C Ultra-Fast Cable (2m)",
                  "quantity": 2,
                  "totalPrice": 39.98
                }
              ],
              "metadata": {
                "nodeRegion": "us-west-1",
                "executionTimeMs": 180,
                "timestamp": "2026-10-06T17:30:00Z"
              }
            }
        """.trimIndent()

        call.response(
            statusCode = 200,
            headers = responseHeaders,
            body = responseBodyJson,
            contentType = "application/json; charset=UTF-8"
        )
    }
}


