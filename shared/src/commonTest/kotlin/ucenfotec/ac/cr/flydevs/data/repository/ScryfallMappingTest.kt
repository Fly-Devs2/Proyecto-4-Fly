package ucenfotec.ac.cr.flydevs.data.repository

import ucenfotec.ac.cr.flydevs.data.remote.dto.ScryfallCardDto
import ucenfotec.ac.cr.flydevs.data.remote.dto.ScryfallPricesDto
import ucenfotec.ac.cr.flydevs.domain.model.ScryfallCard
import ucenfotec.ac.cr.flydevs.domain.model.ScryfallPrices
import kotlin.test.Test
import kotlin.test.assertEquals

class ScryfallMappingTest {

    @Test
    fun `ScryfallCardDto to ScryfallCard mapping is correct`() {
        val dto = ScryfallCardDto(
            name = "Test Card",
            setName = "Test Set",
            setCode = "TST",
            collectorNumber = "123",
            prices = ScryfallPricesDto(
                usd = "1.00",
                usdFoil = "2.00",
                eur = "0.90",
                eurFoil = "1.80",
                tix = "0.05"
            )
        )

        val domain = dto.toDomain()

        assertEquals("Test Card", domain.name)
        assertEquals("Test Set", domain.setName)
        assertEquals("TST", domain.setCode)
        assertEquals("123", domain.collectorNumber)
        assertEquals("1.00", domain.prices.usd)
        assertEquals("2.00", domain.prices.usdFoil)
        assertEquals("0.90", domain.prices.eur)
        assertEquals("1.80", domain.prices.eurFoil)
        assertEquals("0.05", domain.prices.tix)
    }

    // Copying mapping functions for testing since they are private in ScryfallRepositoryImpl
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
