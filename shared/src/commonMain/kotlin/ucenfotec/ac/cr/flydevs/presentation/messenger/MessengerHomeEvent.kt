package ucenfotec.ac.cr.flydevs.presentation.messenger

sealed interface MessengerHomeEvent {

    data object Refresh : MessengerHomeEvent

    data class AcceptBatch(
        val batchId: String
    ) : MessengerHomeEvent

    data class ConfirmPickup(
        val batchId: String
    ) : MessengerHomeEvent

    data class StartRoute(
        val batchId: String
    ) : MessengerHomeEvent

    data class ConfirmDelivery(
        val batchId: String
    ) : MessengerHomeEvent

    data object ClearMessage : MessengerHomeEvent
}