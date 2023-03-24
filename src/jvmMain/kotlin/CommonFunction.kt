import java.text.DateFormat
import java.util.Date

fun getCurrentDateTime(): String {
    val formatter = DateFormat.getDateTimeInstance()
    return formatter.format(Date())
}