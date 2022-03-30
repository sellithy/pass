import kotlinx.serialization.Serializable

//val accountPattern = Regex("^(.+): (.+) (.+)$")
//val passwordPattern = Regex("^(.+): (.+)$")

@Serializable
data class AccountInfo(val username: String, val passwordAlias: String)