import kotlinx.serialization.Serializable

@Serializable
data class AccountInfo(val username: String, val passwordAlias: String)