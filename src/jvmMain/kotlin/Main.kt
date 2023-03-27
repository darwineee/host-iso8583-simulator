import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.lordcodes.turtle.shellRun
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    val log by Logger.getLogging().collectAsState()
    var serverPort by remember { mutableStateOf(SERVER_PORT) }
    var clientPort by remember { mutableStateOf(SERVER_PORT) }
    var serverAddress by remember { mutableStateOf(SERVER_ADDRESS) }

    val createNewServer: () -> NettyApplicationEngine = {
        embeddedServer(Netty, port = serverPort, host = serverAddress, module = Application::module)
    }

    val scrollState = rememberScrollState()
    val serverScope = rememberCoroutineScope { Dispatchers.IO }

    var server = remember { createNewServer() }

    LaunchedEffect(log) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .width(160.dp)
                .fillMaxHeight()
                .padding(14.dp),
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    serverScope.launch {
                        try {
                            server = createNewServer()
                            Logger.addLog("server is started at http://$serverAddress:$serverPort")
                            server.start(true)
                        } catch (e: Exception) {
                            Logger.addLog(e.localizedMessage)
                        }
                    }
                }
            ) {
                Text("Run server")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    serverScope.launch {
                        try {
                            server.stop()
                            Logger.addLog("server is shut down")
                        } catch (e: Exception) {
                            Logger.addLog(e.localizedMessage)
                        }
                    }
                }
            ) {
                Text("Stop server")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    Logger.clearLog()
                }
            ) {
                Text("Clear log")
            }

            Spacer(modifier = Modifier.weight(1f))

            Divider(modifier = Modifier.fillMaxWidth(), color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    try {
                        shellRun("adb", listOf("reverse", "tcp:$clientPort", "tcp:$serverPort"))
                        Logger.addLog("successfully map client port $clientPort to local host port $serverPort")
                    } catch (e: Exception) {
                        Logger.addLog(e.localizedMessage)
                    }
                }
            ) {
                Text("Map port")
            }

            Spacer(modifier = Modifier.height(6.dp))

            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color = Color.LightGray)
                    }
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                value = clientPort.toString(),
                onValueChange = { text ->
                    text.toIntOrNull()?.let {
                        if (it.toString().length <= 4) clientPort = it
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))
            Divider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    serverScope.launch {
                        try {
                            server.stop()
                            server = createNewServer()
                            Logger.addLog("server is restarted at http://$serverAddress:$serverPort")
                            server.start(true)
                        } catch (e: Exception) {
                            Logger.addLog(e.localizedMessage)
                        }
                    }
                }
            ) {
                Text("Apply")
            }

            Spacer(modifier = Modifier.height(6.dp))

            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color = Color.LightGray)
                    }
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                value = serverAddress,
                onValueChange = { text ->
                    serverAddress = text
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color = Color.LightGray)
                    }
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                value = serverPort.toString(),
                onValueChange = { text ->
                    text.toIntOrNull()?.let {
                        if (it.toString().length <= 4) serverPort = it
                    }
                }
            )
        }

        SelectionContainer(
            modifier = Modifier
                .background(color = Color.Black)
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                modifier = Modifier.fillMaxSize(),
                text = log,
                color = Color.White,
                style = consoleStyle
            )
        }
    }
}

fun main() = application {
    val defaultScope = rememberCoroutineScope { Dispatchers.Default }

    DisposableEffect(Unit) {
        Logger.init(defaultScope)
        onDispose { }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Host ISO8583:1987 simulator",
        state = rememberWindowState(width = 1000.dp, height = 600.dp, position = WindowPosition.Aligned(Alignment.Center))
    ) { App() }
}
