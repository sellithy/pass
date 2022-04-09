import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.util.*
import kotlin.random.Random

val helpTexts = "help_texts.properties".asProperties

typealias accountsFile = Map<String, AccountInfo>
typealias passwordsFile = MutableMap<String, String>

val prettyJson = Json { prettyPrint = true }


class Pass : CliktCommand() {

    init {
        eagerOption("--random", "-r", help = helpTexts["random"]) {
            generateRandomPassword().copyToClipboard()
            throw PrintMessage("Random password copied to clipboard")
        }
    }

    private val accountsFilePath: File by option(
        envvar = "ACC_FILE_PATH", help = helpTexts["accountsFilePath"]
    ).file().required()

    private val passwordsFilePath: File by option(
        envvar = "PASS_FILE_PATH", help = helpTexts["passwordsFilePath"]
    ).file().required()

    private val accountName: String by argument(
        help = helpTexts["accountName"]
    )

    private val shouldGetUsername: Boolean by option(
        "--username", "-u", help = helpTexts["shouldGetUsername"]
    ).flag()

    private val shouldAddAccount: Boolean by option(
        "--add", "-a", help = helpTexts["shouldAddAccount"]
    ).flag()

    lateinit var accounts: TreeMap<String, AccountInfo>
    lateinit var passwords: passwordsFile

    override fun run() {
        accounts = TreeMap<String, AccountInfo>(String.CASE_INSENSITIVE_ORDER).apply {
            putAll(prettyJson.decodeFromStream<accountsFile>(accountsFilePath.inputStream()))
        }
        passwords = prettyJson.decodeFromStream(passwordsFilePath.inputStream())

        val accountInfo = accounts[accountName]
        if (accountInfo == null) {
            if (shouldAddAccount) addNewAccount()
            throw PrintMessage("Account does not exist", true)
        }

        if (shouldGetUsername) {
            accountInfo.username.copyToClipboard()
            throw PrintMessage(
                """
                The username is ${accountInfo.username} 
                Username copied to clipboard
                """.trimIndent()
            )
        }

        val password = passwords[accountInfo.passwordAlias]
            ?: throw PrintMessage("Pass alias ${accountInfo.passwordAlias} is not found")
        password.copyToClipboard()
        throw PrintMessage("Password copied to clipboard")
    }

    private fun addNewAccount() {
        val lastNum = accounts.values.mapNotNull { randomAliasPattern.matchEntire(it.passwordAlias) }
            .map { it.groupValues[1].toInt() }.maxOf { it }

        val newPassAlias = "Random${lastNum + 1}"
        accounts[accountName] = AccountInfo(passwordAlias = newPassAlias)
        passwords[newPassAlias] = generateRandomPassword()

        prettyJson.encodeToStream(accounts, accountsFilePath.outputStream())
        prettyJson.encodeToStream(passwords, passwordsFilePath.outputStream())

        throw PrintMessage("New Password copied to clipboard")
    }
}

fun String.copyToClipboard() {
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

    plusAssign(choices(strangeChars, Random.nextInt(from = 3, until = 6)))
    plusAssign(choices(numbers, Random.nextInt(from = 2, until = 5)))
    plusAssign(choices(asciiLetters, 16 - size))
    shuffle()
    toCharArray().concatToString()
}

fun main(args: Array<String>) {
//    val args = arrayOf("paypal")
    Pass().apply { main(args) }
}