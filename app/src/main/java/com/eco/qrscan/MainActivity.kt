package com.eco.qrscan

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.eco.qrscan.ui.theme.QrscanTheme
import android.view.View;
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorLong


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrscanTheme {
                A()

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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun A() {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "         QR Scan"
                    , fontSize = 35.sp,
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight(900),
                    )
                }
            }, navigationIcon = {
                FloatingActionButton(onClick = { /*TODO*/ }, content = {
                    Image(
                        painter = painterResource(id = R.drawable.icons8_flashlight_64),
                        contentDescription = "Flashlight",
                        modifier = Modifier.width(
                            50.dp
                        ).height(50.dp)
                    )
                })
            }, actions = {
                FloatingActionButton(onClick = { /*TODO*/ }, content = { Text("B") })
            })
        },
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier =
                    Modifier
                        .width(200.dp)
                        .height(200.dp)
                        .background(
                            Color(0xFF524F4F)
                        )
                        .shadow(
                            elevation = 10.dp,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(
                            10.dp
                        )
                ) {

                        Image(
                            painter = painterResource(id = R.drawable.qr_scan),
                            contentDescription = "  QR Scanner",
                            modifier = Modifier.width(
                                300.dp
                            ).height(
                                300.dp
                            )
                        )
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Composable
fun GreetingPreview() {
    QrscanTheme {
        Greeting("Android")
    }
}