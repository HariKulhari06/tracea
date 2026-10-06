package com.hari.tracea.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hari.tracea.Tracea
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val apiService = DemoApiService()
        
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DemoScreen(apiService)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(apiService: DemoApiService) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("") }
    
    fun showToast(message: String) {
        statusText = message
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tracea Demo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Open Debugger button — matches iOS "Open Tracea Debugger UI" blue banner
            Button(
                onClick = { Tracea.show(context) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Open Tracea Debugger UI")
            }
            
            // Status banner — matches iOS real-time response status display
            if (statusText.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            DemoButton("1. GET Users (200 OK)") {
                scope.launch {
                    val result = apiService.getUsers()
                    showToast(if (result.isSuccess) "Real API Success!" else "Real API Failed: ${result.exceptionOrNull()?.message}")
                }
            }
            
            DemoButton("2. POST Login (Redaction Test)") {
                scope.launch {
                    val result = apiService.postLogin()
                    showToast(if (result.isSuccess) "POST Login Success" else "POST Login Failed")
                }
            }
            
            DemoButton("3. PUT Update Profile") {
                scope.launch {
                    val result = apiService.putProfile()
                    showToast(if (result.isSuccess) "PUT Profile Success" else "PUT Profile Failed")
                }
            }
            
            DemoButton("4. DELETE Item") {
                scope.launch {
                    val result = apiService.deleteItem()
                    showToast(if (result.isSuccess) "DELETE Item Success" else "DELETE Item Failed")
                }
            }
            
            DemoButton("5. GET 404 (Client Error)") {
                scope.launch {
                    val result = apiService.get404()
                    showToast("Status: ${if (result.isSuccess) "Success (Unexpected)" else "Caught Error"}")
                }
            }
            
            DemoButton("6. GET 500 (Server Error)") {
                scope.launch {
                    val result = apiService.get500()
                    showToast("Status: ${if (result.isSuccess) "Success (Unexpected)" else "Caught Error"}")
                }
            }
            
            DemoButton("7. Timeout (Network Error)") {
                scope.launch {
                    val result = apiService.timeout()
                    showToast(if (result.isSuccess) "Success (Unexpected)" else "Caught Timeout")
                }
            }
            
            DemoButton("8. Large Response (~500KB)") {
                scope.launch {
                    val result = apiService.largeResponse()
                    showToast(if (result.isSuccess) "Large Download Success" else "Large Download Failed")
                }
            }
            
            DemoButton("9. POST with JSON Body") {
                scope.launch {
                    val result = apiService.postWithBody()
                    showToast(if (result.isSuccess) "POST Body Success" else "POST Body Failed")
                }
            }
            
            DemoButton("10. Manual Capture Test") {
                scope.launch {
                    apiService.manualCapture()
                    showToast("Manual Capture Completed")
                }
            }
            
            DemoButton("11. Redacted Headers Test") {
                scope.launch {
                    val result = apiService.redactedHeaders()
                    showToast(if (result.isSuccess) "Redacted Headers Success" else "Redacted Headers Failed")
                }
            }

            DemoButton("12. POST Multipart Form-Data (File Upload)") {
                scope.launch {
                    val result = apiService.uploadMultipart()
                    showToast(if (result.isSuccess) "Multipart Upload Success" else "Multipart Upload Failed")
                }
            }

            DemoButton("13. GET Image Download (PNG Binary)") {
                scope.launch {
                    val result = apiService.downloadImage()
                    showToast(if (result.isSuccess) "Image Download Success" else "Image Download Failed")
                }
            }

            DemoButton("14. 🎯 Full Detail Test (All Fields & Data)") {
                scope.launch {
                    apiService.fullParityAllDataScenario()
                    showToast("🎯 Full Detail Test Emitted (Headers, Query, Bodies, Cookies, Waterfall)")
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Run All Transactions — matches iOS sequential runner
            Button(
                onClick = {
                    scope.launch {
                        showToast("Running all transactions...")
                        apiService.getUsers()
                        kotlinx.coroutines.delay(200)
                        apiService.postLogin()
                        kotlinx.coroutines.delay(200)
                        apiService.putProfile()
                        kotlinx.coroutines.delay(200)
                        apiService.deleteItem()
                        kotlinx.coroutines.delay(200)
                        apiService.get404()
                        kotlinx.coroutines.delay(200)
                        apiService.get500()
                        kotlinx.coroutines.delay(200)
                        apiService.timeout()
                        kotlinx.coroutines.delay(200)
                        apiService.largeResponse()
                        kotlinx.coroutines.delay(200)
                        apiService.postWithBody()
                        kotlinx.coroutines.delay(200)
                        apiService.uploadMultipart()
                        kotlinx.coroutines.delay(200)
                        apiService.downloadImage()
                        kotlinx.coroutines.delay(200)
                        apiService.manualCapture()
                        kotlinx.coroutines.delay(200)
                        apiService.redactedHeaders()
                        showToast("All sequence calls completed")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Run All Transactions")
            }
            
            // Mockable Endpoint — matches iOS mockable test
            DemoButton("Mockable Endpoint") {
                scope.launch {
                    val result = apiService.getUsers()
                    showToast(if (result.isSuccess) "Mockable Success" else "Mockable Failed")
                }
            }
            
            // Stress Test — matches iOS burst test
            Button(
                onClick = {
                    scope.launch {
                        showToast("Starting stress test: 150 burst calls...")
                        val jobs = (1..150).map {
                            async {
                                apiService.getUsers()
                            }
                        }
                        jobs.forEach { it.await() }
                        showToast("⚡ Stress test complete: 150 calls finished")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("⚡ Stress Test (150 Burst Calls)")
            }
        }
    }
}

@Composable
fun DemoButton(text: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
