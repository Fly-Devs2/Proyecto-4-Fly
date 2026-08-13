package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ScryfallCard(
    val name: String,
    val setName: String,
    val setCode: String,
    val collectorNumber: String,
    val prices: ScryfallPrices
)

@Serializable
data class ScryfallPrices(
    val usd: String? = null,
    val usdFoil: String? = null,
    val eur: String? = null,
    val eurFoil: String? = null,
    val tix: String? = null
)
