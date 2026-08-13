package ucenfotec.ac.cr.flydevs.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScryfallSearchResponse(
    val objectType: String? = null,
    val totalCards: Int? = null,
    val hasMore: Boolean? = null,
    val data: List<ScryfallCardDto> = emptyList()
)

@Serializable
data class ScryfallCardDto(
    val name: String,
    @SerialName("set_name") val setName: String,
    @SerialName("set") val setCode: String,
    @SerialName("collector_number") val collectorNumber: String,
    val prices: ScryfallPricesDto
)

@Serializable
data class ScryfallPricesDto(
    val usd: String? = null,
    @SerialName("usd_foil") val usdFoil: String? = null,
    val eur: String? = null,
    @SerialName("eur_foil") val eurFoil: String? = null,
    val tix: String? = null
)
