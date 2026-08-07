package ucenfotec.ac.cr.flydevs.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import ucenfotec.ac.cr.flydevs.data.remote.dto.ScryfallSearchResponse

class ScryfallApiService(private val client: HttpClient) {
    private val baseUrl = "https://api.scryfall.com"

    suspend fun searchCards(query: String, lang: String? = null): ScryfallSearchResponse {
        return try {
            client.get("$baseUrl/cards/search") {
                parameter("q", query)
                parameter("unique", "prints")
                parameter("include_multilingual","true")
                lang?.let { parameter("lang", it) }
            }.body()
        } catch (e: Exception) {
            println("SCRYFALL_API_ERROR: ${e.message}")
            ScryfallSearchResponse()
        }
    }
}
