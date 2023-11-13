package com.eco.qrscan

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.camera.core.Camera
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
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
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.eco.qrscan.scanner.BarcodeAnalyser
import com.eco.qrscan.scanner.DoScanResult
import com.eco.qrscan.scanner.IsErrorQr
import com.eco.qrscan.scanner.IsSuccessQr
import com.eco.qrscan.scanner.IsWarningQr
import com.eco.qrscan.scanner.ticketStat
import com.eco.qrscan.scanner.userMeta
import com.eco.qrscan.ui.theme.grotesk
import com.eco.qrscan.ui.theme.nunito
import java.util.concurrent.Executors

var gcam: Camera? = null
val flash = mutableStateOf(false)
val scannerOpen = mutableStateOf(false)
val darkMode = mutableStateOf(false)

// TODO: Add a button to switch between dark and light mode, and change the background color of the app accordingly.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrMain() {
    Scaffold(
        topBar = {
            TopAppBar()
        },
        bottomBar = {
            //bottomAppBar()
        },
    ) { pad ->
        Box(
            modifier = Modifier.background(color = Color(0xFFD6DFB8))
        ) {
            Box(
                modifier = Modifier
                    .padding(
                        top = pad.calculateTopPadding(),
                    )
                    .padding(
                        horizontal = 8.dp
                    )
                    .background(
                        if (IsSuccessQr.value) {
                            Color(0xFFA9DA75)
                        } else if (IsWarningQr.value) {
                            Color(0xFFDAD173)
                        } else if (IsErrorQr.value) {
                            Color(0xFFF3B0B0)
                        } else if (darkMode.value) {
                            Color(0xFF2E1B58)
                        } else {
                            Color(0xFFE0E1E6)
                        }
                    )
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                        .background(color = Color.White)
                        .clip(
                            shape = RoundedCornerShape(20.dp)
                        )
                        .shadow(
                            shape = RoundedCornerShape(20.dp), elevation = 0.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    qrScannerOpenButton()
                    if (scannerOpen.value) {
                        QrScannerCameraHole()
                    }
                    ScanResult()
                    UserMetaDisplay()
                    QrAppMetadata()
                }
            }
        }
    }
}

@Composable
fun qrScannerOpenButton() {
    if (!scannerOpen.value) {
        Box(
            modifier = Modifier.padding(
                start = 10.dp,
                end = 10.dp,
            )
                .padding(
                    top = 10.dp,
                    bottom = 10.dp
                )
        ) {
            IconButton(
                onClick = {
                    scannerOpen.value = true
                },
                modifier = Modifier
                    .width(130.dp)
                    .height(130.dp)
                    .clip(
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(
                        horizontal = 0.dp, vertical = 0.dp
                    )
                    .border(
                        width = 3.dp, color =
                        if (!darkMode.value) {
                            Color(0xFFFCBF49)
                        } else {
                            Color(0xFF351B6F)
                        }, shape = RoundedCornerShape(20.dp)
                    )
                    .background(
                        color = Color.Transparent,
                    ),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qr_scan),
                    contentDescription = "QR Scanner",
                    modifier = Modifier
                        .width(110.dp)
                        .height(110.dp)
                )
            }
        }
    }
}


@Composable
fun flashButton() {
    Box(
        modifier = Modifier.padding(
            start = 10.dp,
            end = 10.dp,
        )
    ) {
        FloatingActionButton(
            onClick = {
                if (gcam != null && gcam?.cameraInfo?.hasFlashUnit() == true) {
                    flash.value = !flash.value
                    gcam?.cameraControl?.enableTorch(flash.value)
                }
            },
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .clip(
                    shape = RoundedCornerShape(200.dp)
                )
                .shadow(
                    elevation = 5.dp, shape = RoundedCornerShape(20.dp)
                )
                .padding(
                    horizontal = 0.dp, vertical = 0.dp
                )
                .border(
                    width = 3.dp, color =
                    if (flash.value) {
                        Color(0xFF351B6F)
                    } else {
                        Color(0xFFE0A126)
                    }, shape = RoundedCornerShape(2.dp)
                )
                .background(
                    color = if (flash.value) {
                        Color(0xFF172369)
                    } else {
                        Color(0xFFFCBF49)
                    }
                ),
            containerColor = if (flash.value) {
                Color(0xFFACB7FA)
            } else {
                Color(0xFFD8C9AB)
            },
            elevation = FloatingActionButtonDefaults.elevation(20.dp, 2.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("F")
        }
    }
}

@Composable
fun TopAppBar() {
    Box(
        modifier = Modifier.background(color = Color(0xFFD6DFB8))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(8.dp)
                .padding(
                    horizontal = 2.dp
                )
                .height(65.dp)
                .shadow(
                    elevation = 5.dp, shape = RoundedCornerShape(10.dp)
                )
                .background(color = if (!darkMode.value) {Color(0xFFDCEBD0)
                } else {
                    Color(0xFF34313C)
                })
                .fillMaxWidth()
                .clip(
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.qr_scan),
                contentDescription = "Back",
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF9D77A3))
            )
            Text(
                text = "QR Scanner",
                fontSize = 23.sp,
                fontFamily = nunito,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = Color(0xFF2E1B58)
            )
            Image(
                painter = painterResource(id = R.drawable.qr_scan),
                contentDescription = "More",
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF9D77A3))
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
fun QrAppMetadata() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        flashButton()
        Image(
            painter = painterResource(id = R.drawable.super_30_poster),
            contentDescription = null,
            modifier = Modifier
                .width(180.dp)
                .height(180.dp)
        )
        flashButton()
    }
    Row(
        modifier = Modifier
            .background(color = Color.Transparent)
            .clip(
                shape = RoundedCornerShape(20.dp)
            )
            .width(150.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "[ ${ticketStat.value.first} / ${ticketStat.value.second} ]",
            fontSize = 20.sp,
            fontFamily = nunito,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                bottom = 4.dp,
            ),
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
            text = "Super 30 Ticket Scanner",
            fontSize = 23.sp,
            fontFamily = nunito,
            fontWeight = FontWeight(800),
        )
    }
}

@Composable
fun QrScannerCameraHole() {
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
                        top = 230.dp, bottom = 0.dp
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
                        width = 3.dp, color = if (IsSuccessQr.value) {
                            Color.Green
                        } else if (IsWarningQr.value) {
                            Color.Yellow
                        } else if (IsErrorQr.value) {
                            Color.Red
                        } else {
                            Color.Black
                        }, shape = RoundedCornerShape(2.dp)
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
                            val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                            val imageCapture = ImageCapture.Builder().build()

                            val imageAnalyzer = ImageAnalysis.Builder().build().also { it ->
                                    it.setAnalyzer(cameraExecutor, BarcodeAnalyser {
                                        DoScanResult(it)
                                    })
                                }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                val c = cameraProvider.bindToLifecycle(
                                    context as ComponentActivity,
                                    cameraSelector,
                                    preview,
                                    imageCapture,
                                    imageAnalyzer
                                )

                                gcam = c

                            } catch (exc: Exception) {
                                Log.e("DEBUG", "Use case binding failed", exc)
                            }
                        }, ContextCompat.getMainExecutor(context))
                        previewView
                    }, modifier = Modifier.size(width = 300.dp, height = 300.dp)
                )
            }
        }
    }
}

@Composable
fun ScanResult() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (IsSuccessQr.value) {
                GifSuccess()
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
fun gifImage(): ImageLoader {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context).components {
            add(ImageDecoderDecoder.Factory())
        }.build()

    return imageLoader
}

@Composable
fun GifSuccess() {
    val context = LocalContext.current
    val imageLoader = gifImage()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.sucess_o).apply(block = {
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
    val imageLoader = gifImage()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.warn_o).apply(block = {
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
    val imageLoader = gifImage()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.cancel_o).apply(block = {
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
    val imageLoader = gifImage()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.qr_out).apply(block = {
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
    val imageLoader = gifImage()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = R.drawable.output_onlinegiftools)
                .apply(block = {
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

@Composable
fun UserMetaDisplay() {
    if (IsSuccessQr.value) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = userMeta.value.first,
                fontSize = 25.sp,
                fontFamily = grotesk,
                fontWeight = FontWeight(900),
                modifier = Modifier.padding(1.dp)
            )
            Text(
                text = "TicketID: #${userMeta.value.second}",
                fontSize = 25.sp,
                fontFamily = grotesk,
                fontWeight = FontWeight(900),
                modifier = Modifier.padding(1.dp)
            )
            Divider(
                thickness = 3.dp, color = Color.Black
            )
        }
    }
}