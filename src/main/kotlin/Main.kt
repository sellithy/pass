import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

val helpTexts = "help_texts.properties".asProperties

class Main : CliktCommand() {
    private val accountsFilePath: File by option(envvar = "ACC_FILE_PATH", help = helpTexts["accountsFilePath"])
        .file().required()

    private val passwordsFilePath: File by option(envvar = "PASS_FILE_PATH", help = helpTexts["passwordsFilePath"])
        .file().required()

    override fun run() {
        
    }
}

fun copyToClipboard(txt: String) {
    val clean = txt.trim().replace("'", "'\"'\"'")
    ProcessBuilder.startPipeline(
        listOf(
            ProcessBuilder("echo -n $clean".split(" ")), ProcessBuilder("clip.exe")
        )
    )
}

fun main(args: Array<String>) {
    val argsParsed = Main().apply { main(args) }
}