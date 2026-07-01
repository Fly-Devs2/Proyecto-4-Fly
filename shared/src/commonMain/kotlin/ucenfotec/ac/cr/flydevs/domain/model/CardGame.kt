package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CardGame(val label: String) {
    ONE_PIECE("One Piece"),
    MAGIC("Magic"),
    POKEMON("Pokémon"),
}
