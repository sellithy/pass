import kotlinx.serialization.Serializable

const val DEFAULT_USERNAME = "shehab.ellithy@gmail.com"

@Serializable
data class AccountInfo(val username: String = DEFAULT_USERNAME, val passwordAlias: String)