package com.eco.qrscan

import android.hardware.camera2.CameraManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.eco.qrscan.scanner.loadDB
import com.eco.qrscan.scanner.loadTicketStat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

var IsCamera = mutableStateOf(false)

@Composable
fun FlashlightComposable() {
    var isFlashlightOn by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column {
        Button(onClick = {
            val cameraManager = ContextCompat.getSystemService(context, CameraManager::class.java)
            val cameraId = cameraManager?.cameraIdList?.get(0)
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, !isFlashlightOn)
            }
            isFlashlightOn = !isFlashlightOn
        }) {
            Text(if (isFlashlightOn) "Turn off flashlight" else "Turn on flashlight")
        }
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadDB()
        loadTicketStat()

        setContent {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = {
                    if (it) {
                        IsCamera.value = true
                    }
                }
            )

            val p = rememberPermissionState(android.Manifest.permission.CAMERA)
            if (p.status == PermissionStatus.Granted) {
                IsCamera.value = true
            }

            if (IsCamera.value) {

                QrMain()
            } else {
                LaunchedEffect(Unit) {
                    launcher.launch(android.Manifest.permission.CAMERA)
                }
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
