package app

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import app.data.User.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.awt.FileDialog
import java.awt.Frame
import java.awt.MenuShortcut
import java.awt.event.KeyEvent
import java.net.URL
import javax.swing.WindowConstants
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


@OptIn(ExperimentalSerializationApi::class)
private val jsonLenient = Json {
    ignoreUnknownKeys = true
    isLenient
    prettyPrint = true
    prettyPrintIndent = "  "
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class, ExperimentalSerializationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Preview
fun App(context: FrameWindowScope) {
    var text by remember { mutableStateOf("Hello, World!") }
    var fileContents by remember { mutableStateOf("hi") }
    var offset by remember { mutableStateOf(0) }
    var isAnimationRunning by remember { mutableStateOf(true) }
    var isHovering by remember { mutableStateOf(false) }

    val state = MutableStateFlow(0)
    var isDialogOpen by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition()
    val offset2 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                //0.7f at 500
            },
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(isAnimationRunning) {
        while (isAnimationRunning) {
            offset = (sin(System.currentTimeMillis() / 1000.0) * 300).toInt() + 300
//            yield()
            delay(10)
        }
    }

    @Composable
    fun RotatingObject(rpm: Int) {
        var rotation by remember { mutableStateOf(0f) }
        val infiniteTransition = rememberInfiniteTransition()
        val ticker by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                )
            )
        )

        LaunchedEffect(ticker) {
            rotation += 0.1f * rpm
        }

        Surface(
            Modifier
                .size(100.dp)
//                .graphicsLayer { rotationZ = rotation},
                .graphicsLayer { rotationZ = ticker},
            color = Color.Blue
        ) {}
    }

    context.MenuBar {
        Menu("File") {
            Item("Open…") {
                // show system file picker
                val file = FileDialog(null as Frame?, "Title1", FileDialog.LOAD).apply {
                    isMultipleMode = false

                    // windows
                    val allowedExtensions = listOf(".txt", ".kt", ".java")
                    file =  allowedExtensions.joinToString(";") { "*$it" } // e.g. '*.jpg'

                    // linux
                    setFilenameFilter { _, name ->
                        allowedExtensions.any {
                            name.endsWith(it)
                        }
                    }
                    isAlwaysOnTop = true

                    // no-op on mac, but works on linux?
                    background = java.awt.Color(0, 128, 0, 255)
                    isUndecorated = true

                    isVisible = true
                }.files.firstOrNull()

                file ?: return@Item
                try {
                    // read file
                    fileContents = file.absoluteFile.readText()
                } catch (e: Exception) {
                    println(e)
                }

            }
            Item("Animation") {
                isAnimationRunning = !isAnimationRunning
            }
            Item("Save…") {
                state.update {
                    val newNum = it + 1

                    // Update the item name
                    context.window.menuBar.getMenu(0).getItem(2).label = "Save… $newNum"

                    newNum
                }
            }
            Item(
                "Show Alert",
                shortcut = KeyShortcut(Key.S, meta = true)
            ) {
                isDialogOpen = true

                // set the shortcut
                context.window.menuBar.getMenu(0).getItem(3).shortcut = MenuShortcut(KeyEvent.VK_S)
            }
            Separator()
            Item("Quit") {
                context.window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
                context.window.dispatchEvent(java.awt.event.WindowEvent(context.window, java.awt.event.WindowEvent.WINDOW_CLOSING))
            }
        }
    }

    if (true) {
        MaterialTheme {
            NightSkyCard()
//        NightSkyCard2()
        }
    }


    if (false) {
        MaterialTheme {
            RotatingObject(50)
        }
    }

    if (false) {
        MaterialTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Gray)
            ) {
                RotatingObject(50)

                Button(
                    modifier = Modifier
                        .offset { IntOffset(offset.toInt(), 0) }
                        .rotate((offset).toFloat()),
                    onClick = {
                        text = "Hello, Desktop!"
                    }
                ) {
                    Text(text)
                }

                Button(
                    onClick = {
                        // read file : File(System.getProperty("compose.application.resources.dir"))
                        fileContents = javaClass.getResource("/test.txt").readText()
                    },
                    modifier = Modifier.onHover { isHover ->
                        println("isHovering: $isHover")
                        isHovering = isHover
                    }
                ) {
                    Text("Read file")
                }

                AnimatedContent(
                    isHovering,
                    transitionSpec =
//                {
//                    fadeIn() + slideInVertically(animationSpec = tween(400),
//                        initialOffsetY = { fullHeight -> fullHeight }) with
//                    fadeOut(animationSpec = tween(200))
//                }
                    {
                        fadeIn(animationSpec = tween(1000)) with
                                fadeOut(animationSpec = tween(1000))
                    }
                ) { isHovering ->
                    Text(
                        fileContents,
                        fontSize = if (isHovering) 50.sp else 30.sp,
                        color = if (isHovering) Color.Red else Color.Black
                    )
                }

                if (isDialogOpen) {
                    AlertDialog(
                        onDismissRequest = {
                            isDialogOpen = false
                        },
                        confirmButton = {
                            Button(onClick = {
                                // Make a http call to get user info
                                val response = URL("https://jsonplaceholder.typicode.com/users/1").readText()
                                println(response)
                                val user = jsonLenient.decodeFromString<User>(response)
                                println(user)

                                isDialogOpen = false
                            }) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            Button(onClick = {
                                isDialogOpen = false

                                // make a ktor http call
                                scope.launch(Dispatchers.IO) {
                                    val client = HttpClient(CIO) {
                                        install(JsonFeature) {
                                            serializer = KotlinxSerializer()
                                        }
                                    }
                                    val response = client.get<User>("https://jsonplaceholder.typicode.com/users/1")
                                    println(response)
                                }

                            }) {
                                Text("Dismiss")
                            }
                        },
                        text = { Text("Hello text") })
                }

                Text(state.value.toString())

                BoxWithConstraints(
                    Modifier.padding(16.dp)
                        .rotate((offset).toFloat() / 6f)
                ) {
                    val width = maxWidth
                    val height = maxHeight

                    Text("width: $width, height: $height")
                    Box(
                        Modifier
                            .size(width / 2, height / 2)
                            .offset(width / 4, height / 4)
                            .background(Color.Red)
                            .rotate((offset).toFloat() / 6f)
                    ) {
                        BoxWithConstraints {
                            val width = maxWidth
                            val height = maxHeight

                            Text("width: $width, height: $height")
                            Box(
                                Modifier
                                    .size(width / 2, height / 2)
                                    .offset(width / 4, height / 4)
                                    .background(Color.Green)
                                    .rotate((offset).toFloat() / 6f)
                            ) {
                                BoxWithConstraints(Modifier.padding(16.dp)) {
                                    val width = maxWidth
                                    val height = maxHeight

                                    Text("width: $width, height: $height")
                                    Box(
                                        Modifier
                                            .size(width / 2, height / 2)
                                            .offset(width / 4, height / 4)
                                            .background(Color.Blue)
                                            .rotate((offset).toFloat() / 6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Uses canvas to draw - smoothest animation
@Composable
fun NightSkyCard2() {
    Card(
        modifier = Modifier
            .fillMaxSize(),
        elevation = 2.dp,
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color.Blue
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            )
        )

        val stars = remember { mutableStateListOf<Star>() }
        var scaleX by remember { mutableStateOf(0f) }
        var scaleY by remember { mutableStateOf(0f) }
        var scaleZ by remember { mutableStateOf(.5f) }


        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
//                .width(100.dp)
//                .height(100.dp)
                .background(Color.Blue)
        ) {

            SideEffect {
                println("🔥 Recomposing")
            }

            LaunchedEffect(key1 = Unit) {
                repeat(20) {
                    stars.add(
                        Star(
                            Random.nextInt(2, 5).toFloat(),
                            Random.nextInt(0, constraints.maxWidth).toFloat(),
                            Random.nextInt(10, constraints.maxHeight).toFloat(),
                            Random.nextFloat() - .5f
                        )
                    )
                }
            }

//            Box(
//                modifier = Modifier
//                    .offset(x = 50.dp, y = 50.dp)
//                    .rotate(scale * 360f)
//                    .background(Color.Black)
//                    .width(100.dp)
//                    .height(100.dp)
//            )

            Column {

                Canvas(
                    modifier = Modifier
//                        .width(100.dp)
//                        .height(100.dp)
                        .fillMaxSize()
                        .background(Color.Blue)
                ) {
                    if (stars.size == 20) {
                        stars.forEach { star ->
                            drawCircle(
                                Color.White,
                                center = Offset(star.xPos, star.yPos),
                                radius = star.radius * (scale * abs(sin((star.timeOffset + scale) * 4 * PI)).toFloat())
                            )
                        }
                    }

                    Matrix().apply {
                        rotate(
                            scale * 360f,
                            pivot = Offset(
                                center.x + ((scaleX-.5f) * 1000f),
                                center.y + ((scaleY-.5f) * 1000f)
                            )
                        ) {
                            drawRect(
                                Color.Red,
                                topLeft = center.plus(
                                    Offset(
                                        -(scaleZ*1000f/2) + ((scaleX-.5f) * 1000f),
                                        -(scaleZ*1000f/2) + ((scaleY-.5f) * 1000f)
                                    )
                                ),
                                size = Size(scaleZ * 1000f, scaleZ * 1000f)
                            )
                        }
                    }
                }
            }

            Column {
                Slider(
                    value = scaleX,
                    onValueChange = {
                        println("🔥 onValueChange: $it")
                        scaleX = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Slider(
                    value = scaleY,
                    onValueChange = {
                        println("🔥 onValueChange: $it")
                        scaleY = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Slider(
                    value = scaleZ,
                    onValueChange = {
                        println("🔥 onValueChange: $it")
                        scaleZ = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Immutable
data class Star(
    val radius: Float,
    val xPos: Float,
    val yPos: Float,
    val timeOffset: Float
)

// Uses individual launchedEffects to draw - Stutters at times, smoothest with the animateFloat
@Composable
fun NightSkyCard() {
    Card(
        modifier = Modifier
            .fillMaxSize(),
        elevation = 2.dp,
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0xFF111111)
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        // Smoothest with the animateFloat, but still stutters every couple seconds
        val timeOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            var timeOffset1 by remember { mutableStateOf(0f) }

            // Stutters after a while, starts to slow down after a while
            LaunchedEffect(key1 = Unit) {
                while(true) {
                    delay(1)
//                    yield()
                    timeOffset1 += .03f
//                    timeOffset = Random.nextFloat() * 5f
                }
            }

            for (n in 0..200) {
                var size by remember { mutableStateOf(0) }
                var start by remember { mutableStateOf(0f) }
                var top by remember { mutableStateOf(0f) }

                LaunchedEffect(key1 = Unit) {
                    size = Random.nextInt(3, 5)
                    start = Random.nextInt(0, maxWidth.value.toInt()).toFloat()
                    top = Random.nextInt(10, maxHeight.value.toInt()).toFloat()

                    while(true) {
                        delay(2)
                        start += Random.nextFloat() * 2 - 1 + (.25f*sin(timeOffset + (scale * 2 * PI)).toFloat())
                        top += Random.nextFloat() * 2 - 1 + (.25f*cos(timeOffset + (scale * 2 * PI)).toFloat())
                    }
                }
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier
                        //.padding(start = start.dp, top = top.dp)
                        .offset(x = start.dp, y = top.dp)
                        .size(size.dp)
                        .scale((Random.nextFloat() + size * sin(timeOffset + (scale * 2 * PI))).toFloat()),
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = 200.dp, y = 200.dp)
                    .rotate(360 * timeOffset)
                    .background(Color.White)
                    .size(200.dp)
            )
            Box(
                modifier = Modifier
                    .offset(x = 250.dp, y = 250.dp)
                    .rotate(360 * timeOffset)
                    .background(Color.Black)
                    .size(100.dp)
            )
            Box(
                modifier = Modifier
                    .offset(x = 295.dp, y = 295.dp)
                    .rotate(360 * timeOffset)
                    .background(Color.White)
                    .size(10.dp)
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.onHover(block: (Boolean) -> Unit): Modifier =
    then(Modifier.onPointerEvent(
        PointerEventType.Move
    ) {
    }
        .onPointerEvent(PointerEventType.Enter) {
            block(true)
        }
        .onPointerEvent(PointerEventType.Exit) {
            block(false)
        }
    )

fun main() = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose Desktop",
        state = WindowState( size = DpSize(800.dp, 600.dp)),
    ) {
        App(this)
    }
}
