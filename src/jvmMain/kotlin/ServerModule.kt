import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val SERVER_ADDRESS = "0.0.0.0"
const val SERVER_PORT = 8080

fun Application.module() {
    configureRouting()
}

fun Application.configureRouting() {
    routing {
        get("/") {
            Logger.addLog("someone has pinged to the server")
            call.respondText("ISO server is running")
        }
        post("/authorize") {
            val data = call.receiveText()
            Logger.addLog("received message:\n$data")
            tryParse(data)
            when (call.request.queryParameters["result"]) {
                Success -> {
                    val response = getAuthoriseSuccessRsp()
                    call.respondText(response)
                    Logger.addLog(response)
                }
                FailPinWrong -> {

                }
            }
        }
    }
}

fun tryParse(message: String) = try {
    val data = ISO8583.decode(message)
    val log = buildString {
        append("decoded request message:")
        append("\nMTI = ${data.mti}")
        append("\n\nPrimary bitmap = ${data.primaryBitmap.data.bitArrayToBinary()}")
        append("\n\nSecondary bitmap = ${data.dataElements[0].data}")
        append("\n")
        for (de in data.dataElements) {
            if (de.indexCode.toInt() == 1) continue
            append("\nDE${de.indexCode} (${de.description}) = ${de.data}")
        }
        append("\n")
    }
    Logger.addLog(log)
} catch (e: Exception) {
    Logger.addLog(e.toString())
}