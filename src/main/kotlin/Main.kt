import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.util.*

val helpTexts = "help_texts.properties".asProperties

typealias accountsFile = Map<String, AccountInfo>
typealias passwordsFile = Map<String, String>

val prettyJson = Json { prettyPrint = true }


class Main : CliktCommand() {
    private val accountsFilePath: File by option(envvar = "ACC_FILE_PATH", help = helpTexts["accountsFilePath"])
        .file().required()

    private val passwordsFilePath: File by option(envvar = "PASS_FILE_PATH", help = helpTexts["passwordsFilePath"])
        .file().required()

    private val accountName: String by argument(help = helpTexts["accountName"])

    override fun run() {
//        TreeMap(String.CASE_INSENSITIVE_ORDER)
        val accounts = TreeMap<String, AccountInfo>(String.CASE_INSENSITIVE_ORDER).apply {
            putAll(prettyJson.decodeFromStream<accountsFile>(accountsFilePath.inputStream()))
        }
        val passwords = prettyJson.decodeFromStream<passwordsFile>(passwordsFilePath.inputStream())
        passwords[accounts[accountName]!!.passwordAlias]!!.toClipboard()
        throw PrintMessage("copied")
    }
}

fun String.toClipboard() {
    trim().replace("'", "'\"'\"'").let {
        ProcessBuilder.startPipeline(
            listOf(
                ProcessBuilder("echo -n $it".split(" ")),
                ProcessBuilder("clip.exe")
            )
        )
    }
}

fun main(args: Array<String>) {
//    val args = arrayOf("paypal")
    val argsParsed = Main().apply { main(args) }
//    val acc = Account("PayPal", "shehab.ellithy@gmail.com", "Random1")
//    println(Json.decodeFromString<Account>("\"C&C: sellithy Random24\""))
}