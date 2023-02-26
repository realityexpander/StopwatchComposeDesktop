import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.yield
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.WindowConstants
import kotlin.math.sin


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
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
            Item("Show Alert") {
                isDialogOpen = true
            }
            Separator()
            Item("Quit") {
                context.window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
                context.window.dispatchEvent(java.awt.event.WindowEvent(context.window, java.awt.event.WindowEvent.WINDOW_CLOSING))
            }
        }
    }

    LaunchedEffect(isAnimationRunning) {
        while (isAnimationRunning) {
            offset = (sin(System.currentTimeMillis() / 1000.0) * 300).toInt() + 300
            yield()
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Gray)
        ) {
            Button(
                modifier = Modifier
                    .offset { IntOffset(offset, 0) }
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
                    fontSize = if(isHovering) 50.sp else 30.sp,
                    color = if (isHovering) Color.Red else Color.Black
                )
            }

            if(isDialogOpen) {
                AlertDialog(
                    onDismissRequest = {
                        isDialogOpen = false
                    },
                    confirmButton = {
                        Button(onClick = {
                            isDialogOpen = false
                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            isDialogOpen = false
                        }) {
                            Text("Dismiss")
                        }
                    },
                    text = { Text("Hello text") })
            }

            Text(state.value.toString())
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
    ) {
        App(this)
    }
}
