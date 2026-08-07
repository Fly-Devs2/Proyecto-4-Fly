package ucenfotec.ac.cr.flydevs.data.repository

import ucenfotec.ac.cr.flydevs.data.remote.ScryfallApiService
import ucenfotec.ac.cr.flydevs.data.remote.dto.ScryfallCardDto
import ucenfotec.ac.cr.flydevs.data.remote.dto.ScryfallPricesDto
import ucenfotec.ac.cr.flydevs.domain.model.ScryfallCard
import ucenfotec.ac.cr.flydevs.domain.model.ScryfallPrices
import ucenfotec.ac.cr.flydevs.domain.repository.IScryfallRepository

class ScryfallRepositoryImpl(
    private val apiService: ScryfallApiService
) : IScryfallRepository {

    override suspend fun getCardPrints(cardName: String, language: String?): List<ScryfallCard> {
        return try {
            val query = cardName.trim()
            if (query.isBlank()) return emptyList()
            
            val formattedQuery = "!\"$query\""
            val response = apiService.searchCards(formattedQuery, language)
            response.data.map { it.toDomain() }
        } catch (e: Exception) {
            println("ERROR_SCRYFALL: Error for '$cardName' ($language): ${e.message}")
            emptyList()
        }
    }

    private fun ScryfallCardDto.toDomain(): ScryfallCard {
        return ScryfallCard(
            name = name,
            setName = setName,
            setCode = setCode,
            collectorNumber = collectorNumber,
            prices = prices.toDomain()
        )
    }

    private fun ScryfallPricesDto.toDomain(): ScryfallPrices {
        return ScryfallPrices(
            usd = usd,
            usdFoil = usdFoil,
            eur = eur,
            eurFoil = eurFoil,
            tix = tix
        )
    }
}
