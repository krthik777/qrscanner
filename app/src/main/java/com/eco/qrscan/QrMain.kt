package com.eco.qrscan

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.eco.qrscan.scanner.BarcodeAnalyser
import java.util.concurrent.Executors

val IsSuccessQr = mutableStateOf(false)
val IsWarningQr = mutableStateOf(false)
val IsErrorQr = mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrMain() {
    Scaffold(
        topBar = {
            topAppBar()
        },
        bottomBar = {},
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(
                    top = pad.calculateTopPadding(),
                    bottom = pad.calculateBottomPadding(),
                )
        ) {
            Column {
                qrAppMetadata()
                qrScannerCameraHole()
                scanResult()
            }
        }
    }
}

@Composable
fun topAppBar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(8.dp)
            .height(65.dp)
            .shadow(4.dp)
            .background(color = Color.White)
            .fillMaxWidth()
            .clip(
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = painterResource(id = R.drawable.icons8_left_arrow_96),
            contentDescription = "Back",
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
        )
        Text(
            text = "QR Scan",
            fontSize = 23.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.icons8_right_arrow_96),
            contentDescription = "More",
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun qrAppMetadata() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.qr_scan),
            contentDescription = null,
            modifier = Modifier
                .width(120.dp)
                .height(120.dp)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Super 30 TIcket Scanner",
            fontSize = 25.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight(900),
        )
    }
}

@Composable
fun qrScannerCameraHole() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
            .padding(top = 1.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(323.dp)
                .padding(
                    19.dp
                )
                .border(
                    width = 3.dp,
                    color = if (IsSuccessQr.value) {
                        Color.Green
                    } else if (IsWarningQr.value) {
                        Color.Yellow
                    } else if (IsErrorQr.value) {
                        Color.Red
                    } else {
                        Color.Black
                    },
                    shape = RoundedCornerShape(4.dp)
                )

        ) {
            AndroidView(
                { context ->
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val previewView = PreviewView(context).also {
                        it.scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                        val imageCapture = ImageCapture.Builder().build()

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, BarcodeAnalyser {
                                    Toast.makeText(context, "Barcode found", Toast.LENGTH_SHORT)
                                        .show()
                                    IsSuccessQr.value = true
                                })
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                context as ComponentActivity,
                                cameraSelector,
                                preview,
                                imageCapture,
                                imageAnalyzer
                            )

                        } catch (exc: Exception) {
                            Log.e("DEBUG", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(context))
                    previewView
                },
                modifier = Modifier
                    .size(width = 300.dp, height = 300.dp)
            )
        }
    }
}

@Composable
fun scanResult() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            if (IsSuccessQr.value) {
                GifSucess()
                Text(
                    text = "Verified Ticket!!",
                    fontSize = 25.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight(900),
                    modifier = Modifier.padding(3.dp)
                )
            } else if (IsWarningQr.value) {
                GifWarn()
                Text(
                    text = "Duplicate Ticket!!",
                    fontSize = 25.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight(900),
                    modifier = Modifier.padding(3.dp)
                )
            } else if (IsErrorQr.value) {
                GifError()
                Text(
                    text = "Invalid Ticket!!",
                    fontSize = 25.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight(900),
                    modifier = Modifier.padding(3.dp)
                )
            } else {
                GifQr()
                Text(
                    text = "Scan QR Code",
                    fontSize = 25.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight(900),
                    modifier = Modifier.padding(3.dp)
                )
            }
        }
    }
}

@Composable
fun GifImage() : ImageLoader {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    return imageLoader
}

@Composable
fun GifSucess() {
    val context = LocalContext.current
    val imageLoader = GifImage()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.icons8_success__2_).apply(block = {
                size(Size.ORIGINAL)
            }).build(), imageLoader = imageLoader
        ),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
    )
}

@Composable
fun GifWarn() {
    val context = LocalContext.current
    val imageLoader = GifImage()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.icons8_warning).apply(block = {
                size(Size.ORIGINAL)
            }).build(), imageLoader = imageLoader
        ),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
    )
}

@Composable
fun GifError() {
    val context = LocalContext.current
    val imageLoader = GifImage()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.icons8_cancel).apply(block = {
                size(Size.ORIGINAL)
            }).build(), imageLoader = imageLoader
        ),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
    )
}

@Composable
fun GifQr() {
    val context = LocalContext.current
    val imageLoader = GifImage()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.icons8).apply(block = {
                size(Size.ORIGINAL)
            }).build(), imageLoader = imageLoader
        ),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
    )
}