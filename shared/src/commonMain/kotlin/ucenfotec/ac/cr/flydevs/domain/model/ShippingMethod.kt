package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable


@Serializable
enum class ShippingMethod {
    PICKUP,
    DELIVERY
}