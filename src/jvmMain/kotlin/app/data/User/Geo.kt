import kotlinx.serialization.Serializable

//package data.User

@Serializable
data class Geo(
    val lat: String,
    val lng: String
)