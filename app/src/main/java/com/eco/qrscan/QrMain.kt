package com.eco.qrscan

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.zIndex
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
        bottomBar = {
            bottomAppBar()
        },
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(
                    top = pad.calculateTopPadding(),
                    bottom = pad.calculateBottomPadding(),
                )
                .background(
                    if (IsSuccessQr.value) {
                        Color(0xFFA0D468)
                    } else if (IsWarningQr.value) {
                        Color(0xFFDBD168)
                    } else if (IsErrorQr.value) {
                        Color(0xFFD46868)
                    } else {
                        Color(0xFFF0EFE5)
                    }
                )
        ) {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                qrAppMetadata()
                qrScannerCameraHole()
                scanResult()
                Spacer(modifier = Modifier.height(20.dp))
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
fun bottomAppBar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(0.dp)
            .height(40.dp)
            .shadow(4.dp)
            .background(color = Color.White)
            .fillMaxWidth()
            .clip(
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        Box (
            modifier = Modifier.background(color = Color(0xFFFAF8F8))
                .clip(
                    shape = RoundedCornerShape(20.dp)
                ).width(150.dp),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = "- 0 / 200 -",
                fontSize = 15.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    bottom = 4.dp,
                ),
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
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
                .width(100.dp)
                .height(100.dp)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Super 30 TIcket Scanner",
            fontSize = 23.sp,
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
            modifier = Modifier, contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .zIndex(6f)
                    .padding(
                        top = 230.dp,
                        bottom = 0.dp
                    )
                    .width(
                        40.dp
                    )
                    .height(
                        40.dp
                    ),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GifScan()
            }
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(323.dp)
                    .padding(
                        15.dp
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
                        shape = RoundedCornerShape(2.dp)
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
                                .also { it ->
                                    it.setAnalyzer(cameraExecutor, BarcodeAnalyser {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                            .show()
                                        IsSuccessQr.value = true
                                        Thread {
                                            Thread.sleep(5000)
                                            IsSuccessQr.value = false
                                        }.start()
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

@Composable
fun GifScan() {
    val context = LocalContext.current
    val imageLoader = GifImage()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.output_onlinegiftools).apply(block = {
                size(Size.ORIGINAL)
            }).build(), imageLoader = imageLoader
        ),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
    )
}

