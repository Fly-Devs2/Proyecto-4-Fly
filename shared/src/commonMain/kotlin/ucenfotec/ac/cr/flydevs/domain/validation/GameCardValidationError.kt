package ucenfotec.ac.cr.flydevs.domain.validation

enum class GameCardValidationError {
    NAME_REQUIRED,
    GAME_REQUIRED,
    EXPANSION_REQUIRED,
    RARITY_REQUIRED,
    PRICE_NOT_POSITIVE,
    IMAGE_REQUIRED,
}
