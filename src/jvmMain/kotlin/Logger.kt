import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object Logger {
    private val loggingFlow = MutableStateFlow(AnnotatedString(""))
    private lateinit var coroutineScope: CoroutineScope

    fun init(scope: CoroutineScope) {
        coroutineScope = scope
    }

    fun getLogging(): StateFlow<AnnotatedString> = loggingFlow

    fun addLog(value: String) = coroutineScope.launch {
        val log = loggingFlow.value + buildAnnotatedString {
            append("\n${getCurrentDateTime()}: ")
            withStyle(
                style = SpanStyle(
                    color = Color.Green
                )
            ) {
                append(value)
            }
        }
        loggingFlow.emit(log)
    }

    fun clearLog() = coroutineScope.launch {
        loggingFlow.emit(AnnotatedString(""))
    }
}