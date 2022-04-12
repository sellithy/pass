import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import kotlin.random.Random

val helpTexts = "help_texts.properties".asProperties
val prettyJson = Json { prettyPrint = true }


class Pass : CliktCommand() {

    init {
        completionOption()
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

    private sealed class ChangeOptions {
        object RandomPass : ChangeOptions()
        object PromptPass : ChangeOptions()
        class ACCOUNT(val value: String) : ChangeOptions()
    }
    private val change: ChangeOptions? by option(
        "--change", "-c", help = helpTexts["change"],
        completionCandidates = CompletionCandidates.Fixed("pass=random", "pass=prompt", "acc=")
    ).splitPair().convert { (accOrPass, value) ->
        if (accOrPass == "pass")
            if (value == "random") ChangeOptions.RandomPass else ChangeOptions.PromptPass
        else if (accOrPass == "acc")
            ChangeOptions.ACCOUNT(value)
        else
            fail("Change is not correctly formatted")
    }

    private lateinit var accounts: LinkedHashMap<String, AccountInfo>
    private lateinit var passwords: LinkedHashMap<String, String>

    override fun run() {
        accounts = prettyJson.decodeFromStream(accountsFilePath.inputStream())
        passwords = prettyJson.decodeFromStream(passwordsFilePath.inputStream())

        val accountInfo = accounts.firstNotNullOfOrNull { (accName, acc) ->
            if (accName.lowercase() == accountName.lowercase()) acc else null
        }
        if (accountInfo == null) {
            if (shouldAddAccount) {
                addNewAccount().copyToClipboard()
                throw PrintMessage("New Password copied to clipboard")
            }
            throw PrintMessage("Account does not exist", true)
        }

        if (change != null) {
            when (val v = change!!) {
                is ChangeOptions.ACCOUNT -> accounts[accountName] = accountInfo.copy(username = v.value)
                ChangeOptions.PromptPass -> passwords[accountInfo.passwordAlias] =
                    prompt(text = "New password", hideInput = true, requireConfirmation = true)!!
                ChangeOptions.RandomPass -> passwords[accountInfo.passwordAlias] = generateRandomPassword()
            }
            encodeAccountsAndPasswords()
            throw PrintMessage("changed successfully")
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

    private fun addNewAccount(): String {
        val lastNum = accounts.values.mapNotNull { randomAliasPattern.matchEntire(it.passwordAlias) }
            .map { it.groupValues[1].toInt() }.maxOf { it }

        val newPassAlias = "Random${lastNum + 1}"
        val newPass = generateRandomPassword()
        accounts[accountName] = AccountInfo(passwordAlias = newPassAlias)
        passwords[newPassAlias] = newPass

        encodeAccountsAndPasswords()
        return newPass
    }

    private fun encodeAccountsAndPasswords() {
        prettyJson.encodeToStream(accounts, accountsFilePath.outputStream())
        prettyJson.encodeToStream(passwords, passwordsFilePath.outputStream())
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
    Pass().main(args)
}