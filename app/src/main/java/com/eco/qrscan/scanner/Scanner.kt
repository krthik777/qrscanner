package com.eco.qrscan.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.mutableStateOf
import com.google.gson.annotations.SerializedName
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

val IsSuccessQr = mutableStateOf(false)
val IsWarningQr = mutableStateOf(false)
val IsErrorQr = mutableStateOf(false)
val IsFiveSecTaskRunning = mutableStateOf(false)
val userMeta = mutableStateOf(Pair("", ""))
val ticketStat = mutableStateOf(Pair(0, 200))

var FullDB = listOf<ScanResult>()

data class ScanResult(
    @SerializedName("customer_name") val name: String,
    @SerializedName("ticket_id") val ticketId: String,
    val hex: String,
    @SerializedName("user_admitted") val userAdmitted: Boolean,
)

class BarcodeAnalyser(
    val callback: (String) -> Unit
) : ImageAnalysis.Analyzer {
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = BarcodeScanning.getClient(options)
        val mediaImage = imageProxy.image
        mediaImage?.let {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.size > 0) {
                        callback(barcodes[0].rawValue ?: "")
                    }
                }
                .addOnFailureListener {
                    println("Error: ${it.message}")
                }
        }
        imageProxy.close()
    }
}

data class tikStat(
    val count: Int,
    val total: Int
)

fun loadTicketStat() {
    val url = "https://db.ajce.me/api/count"
    val client = okhttp3.OkHttpClient()

    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
            println("QRApi Error: ${e.message}")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (response.code == 200) {
                val body = response.body?.string()
                val result = com.google.gson.Gson().fromJson(body, tikStat::class.java)
                ticketStat.value = Pair(result.count, result.total)
            }
        }
    })
}

fun loadDB() {
    val url = "https://db.ajce.me/api/count?getall=true"
    val client = okhttp3.OkHttpClient()

    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
            println("QRApi Error: ${e.message}")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (response.code == 200) {
                val body = response.body?.string()
                val result = com.google.gson.Gson().fromJson(body, Array<ScanResult>::class.java)
                FullDB = result.toList()
                Thread {
                    Thread.sleep(10000)
                    loadDB()
                }.start()
            }
        }
    })
}


fun DoScanResult(code: String) {
    var userFound = false
    for (i in FullDB) {
        if (i.hex == code) {
            userFound = true
            if (i.userAdmitted) {
                if (!IsFiveSecTaskRunning.value) {
                    IsWarningQr.value = true
                    IsFiveSecTaskRunning.value = true
                    Thread {
                        Thread.sleep(5000)
                        IsWarningQr.value = false
                        IsFiveSecTaskRunning.value = false
                    }.start()
                }
            } else {
                if (!IsFiveSecTaskRunning.value) {
                    FullDB = FullDB.map {
                        if (it.hex == code) {
                            it.copy(userAdmitted = true)
                        } else {
                            it
                        }
                    }
                    ticketStat.value = Pair(ticketStat.value.first + 1, ticketStat.value.second)
                    userMeta.value = Pair(i.name, i.ticketId)
                    IsSuccessQr.value = true
                    IsFiveSecTaskRunning.value = true
                    Thread {
                        Thread.sleep(5000)
                        IsSuccessQr.value = false
                        IsFiveSecTaskRunning.value = false
                        userMeta.value = Pair("", "")
                    }.start()
                }
            }
        }
    }

    if (!userFound) {
        if (!IsFiveSecTaskRunning.value) {
            IsErrorQr.value = true
            IsFiveSecTaskRunning.value = true
            Thread {
                Thread.sleep(5000)
                IsErrorQr.value = false
                IsFiveSecTaskRunning.value = false
            }.start()
        }
    }

    val url = "https://db.ajce.me/api/admit?hex=$code"
    val client = okhttp3.OkHttpClient()

    val request = okhttp3.Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
            println("QRApi Error: ${e.message}")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            println("QRApi Response: ${response.code}")
        }
    })
}
