import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.util.*
import kotlin.random.Random

val helpTexts = "help_texts.properties".asProperties

typealias accountsFile = Map<String, AccountInfo>
typealias passwordsFile = Map<String, String>

val prettyJson = Json { prettyPrint = true }


class Pass : CliktCommand() {

    init {
        eagerOption("--random", "-r", help = helpTexts["random"]) {
            generateRandomPassword().toClipboard()
            throw PrintMessage("Random password copied to clipboard")
        }
    }

    private val accountsFilePath: File by option(
        envvar = "ACC_FILE_PATH", help = helpTexts["accountsFilePath"]
    ).file().required()

    private val passwordsFilePath: File by option(
        envvar = "PASS_FILE_PATH", help = helpTexts["passwordsFilePath"]
    ).file().required()

    private val accountName: String? by argument(
        help = helpTexts["accountName"]
    ).optional()


    override fun run() {
        val accounts = TreeMap<String, AccountInfo>(String.CASE_INSENSITIVE_ORDER).apply {
            putAll(prettyJson.decodeFromStream<accountsFile>(accountsFilePath.inputStream()))
        }
        val passwords = prettyJson.decodeFromStream<passwordsFile>(passwordsFilePath.inputStream())
        passwords[accounts[accountName]!!.passwordAlias]!!.toClipboard()
        throw PrintMessage("Password copied to clipboard")
    }
}

private fun String.toClipboard() {
    trim().replace("'", "'\"'\"'").let {
        ProcessBuilder.startPipeline(
            listOf(
                ProcessBuilder("echo -n $it".split(" ")), ProcessBuilder("clip.exe")
            )
        )
    }
}

fun generateRandomPassword() = mutableListOf<Char>().run {
    val strangeChars = listOf('$', '#', '!', '@', '%', '^')
    val numbers = (0..9).map { it.digitChar }
    val asciiLetters = ('a'..'z').toList() + ('A'..'Z').toList()

    plusAssign(choices(strangeChars, Random.nextInt(from = 3, until = 5)))
    plusAssign(choices(numbers, Random.nextInt(from = 2, until = 4)))
    plusAssign(choices(asciiLetters, 16 - size))
    shuffle()
    toCharArray().concatToString()
}

fun main(args: Array<String>) {
//    val args = arrayOf("paypal")
     Pass().apply { main(args) }
}