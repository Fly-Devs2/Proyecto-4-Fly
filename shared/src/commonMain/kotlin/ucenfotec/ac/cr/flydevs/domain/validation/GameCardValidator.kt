package ucenfotec.ac.cr.flydevs.domain.validation

import ucenfotec.ac.cr.flydevs.domain.model.GameCard

object GameCardValidator {

    fun validate(card: GameCard): List<GameCardValidationError> = buildList {
        if (card.name.isBlank()) add(GameCardValidationError.NAME_REQUIRED)
        if (card.game == null) add(GameCardValidationError.GAME_REQUIRED)
        if (card.expansion.isNullOrBlank()) add(GameCardValidationError.EXPANSION_REQUIRED)
        if (card.rarity.isNullOrBlank()) add(GameCardValidationError.RARITY_REQUIRED)
        if (card.price <= 0L) add(GameCardValidationError.PRICE_NOT_POSITIVE)
        if (card.imageUrl.isBlank()) add(GameCardValidationError.IMAGE_REQUIRED)
    }
}
