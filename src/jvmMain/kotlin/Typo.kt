import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp
import java.io.File

private val font = FontFamily(
    Font(
        file = File("src/jvmMain/resources/font/CONSOLA.TTF"),
        style = FontStyle.Normal,
        weight = FontWeight.W400
    ),
)

val consoleStyle = TextStyle(
    fontFamily = font,
    fontSize = 16.sp,
    fontWeight = FontWeight.W400,
    lineHeight = 20.sp
)