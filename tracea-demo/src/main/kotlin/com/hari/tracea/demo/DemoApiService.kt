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
        val call = Tracea.startRequest("GET", "https://api.example.com/manual-test")
        call.requestHeaders(mapOf("Accept" to "application/json"))
        delay(200)  // Simulate network delay
        call.response(
            statusCode = 200,
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"manual\": true, \"message\": \"This was manually captured\"}"
        )
    }
}

