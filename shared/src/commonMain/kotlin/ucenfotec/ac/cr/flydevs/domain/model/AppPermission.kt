package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppPermission(
    val id: String,
    val name: String,
    val description: String,
    val isEnabled: Boolean = false
)

@Serializable
data class AppRole(
    val id: String,
    val name: String,
    val colorHex: String,
    val accountCount: Int = 0,
    val permissions: List<AppPermission> = emptyList()
)

val DEFAULT_PERMISSIONS = listOf(
    AppPermission(
        id = "buy_cards",
        name = "Comprar cartas",
        description = "Reservar y armar sobres",
        isEnabled = false
    ),
    AppPermission(
        id = "publish_sell",
        name = "Publicar y vender",
        description = "Administrar catálogo propio",
        isEnabled = false
    ),
    AppPermission(
        id = "manage_deliveries",
        name = "Gestionar entregas",
        description = "Aceptar y completar envíos",
        isEnabled = false
    ),
    AppPermission(
        id = "validate_pickups",
        name = "Validar retiros en tienda",
        description = "Escanear QR de entrega",
        isEnabled = false
    ),
    AppPermission(
        id = "access_reports",
        name = "Acceso a reportes",
        description = "Ventas, retiros y entregas",
        isEnabled = false
    ),
    AppPermission(
        id = "manage_users",
        name = "Gestionar usuarios",
        description = "Bloquear, editar, asignar roles",
        isEnabled = false
    )
)
