import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    val log by Logger.getLogging().collectAsState()
    var counter by remember { mutableStateOf(0) }

    val server = remember(counter) {
        embeddedServer(Netty, port = SERVER_PORT, host = SERVER_ADDRESS, module = Application::module)
    }
    val scrollState = rememberScrollState()

    val scope = rememberCoroutineScope { Dispatchers.IO }

    LaunchedEffect(log) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .padding(14.dp),
        ) {
            Button(
                modifier = Modifier.width(130.dp),
                onClick = {
                    scope.launch {
                        server.start(true)
                    }
                    Logger.addLog("server is started at http://$SERVER_ADDRESS:$SERVER_PORT")
                }
            ) {
                Text("Run server")
            }

            Button(
                modifier = Modifier.width(130.dp),
                onClick = {
                    scope.launch {
                        server.stop()
                    }
                    Logger.addLog("server is shut down")
                    ++counter
                }
            ) {
                Text("Stop server")
            }

            Button(
                modifier = Modifier.width(130.dp),
                onClick = {
                    Logger.clearLog()
                }
            ) {
                Text("Clear log")
            }
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
                color = Color.White
            )
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Host ISO8583:1987 simulator"
    ) { App() }
}
