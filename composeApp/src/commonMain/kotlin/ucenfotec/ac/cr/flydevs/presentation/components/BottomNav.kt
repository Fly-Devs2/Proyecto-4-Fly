package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.theme.*

/** Destinos de la barra de navegación inferior global. */
enum class FlyNavDestination {
    Home,
    Explore,
    Sell,
    Orders,

    //Compartido
    Profile,

    // Mensajero
    Deliveries,
    DeliveryHistory,
    Earnings,

    // Tienda
    StoreBatchesScreen,
    StoreScan,
    StorePickups,

    // Administrador
    AdminDashboard,
    AdminUsers,
    AdminOrders,
    AdminIncidents,
    AdminSettings

}

/**
 * Configuración de cada opción del BottomNav.
 */
private data class BottomNavOption(
    val destination: FlyNavDestination,
    val label: String,
    val icon: ImageVector,
    val isFloating: Boolean = false,
    /** Color del FAB; solo aplica cuando [isFloating] es true. */
    val floatingColor: Color = AccentViolet
)

/**
 * Opciones de navegación según el rol.
 */
private fun getBottomNavOptions(
    userRole: UserRole
): List<BottomNavOption> {

    return when (userRole) {

        UserRole.USER -> {
            listOf(
                BottomNavOption(
                    destination = FlyNavDestination.Home,
                    label = "Inicio",
                    icon = Icons.Default.Home
                ),
                BottomNavOption(
                    destination = FlyNavDestination.Explore,
                    label = "Explorar",
                    icon = Icons.Default.Explore
                ),
                BottomNavOption(
                    destination = FlyNavDestination.Sell,
                    label = "Vender",
                    icon = Icons.Default.Add,
                    isFloating = true
                ),
                BottomNavOption(
                    destination = FlyNavDestination.Orders,
                    label = "Pedidos",
                    icon = Icons.AutoMirrored.Filled.ListAlt
                ),
                BottomNavOption(
                    destination = FlyNavDestination.Profile,
                    label = "Perfil",
                    icon = Icons.Default.Person
                )
            )
        }

        UserRole.DELIVERY -> {
            listOf(
                BottomNavOption(
                    destination = FlyNavDestination.Deliveries,
                    label = "Entregas",
                    icon = Icons.Default.LocalShipping
                ),

                BottomNavOption(
                    destination = FlyNavDestination.DeliveryHistory,
                    label = "Historial",
                    icon = Icons.Default.History
                ),

                BottomNavOption(
                    destination = FlyNavDestination.Profile,
                    label = "Perfil",
                    icon = Icons.Default.Person
                )
            )
        }

        UserRole.STORE -> {
            listOf(
                BottomNavOption(
                    destination = FlyNavDestination.Home,
                    label = "Inicio",
                    icon = Icons.Default.Home
                ),
                BottomNavOption(
                    destination = FlyNavDestination.StoreBatchesScreen,
                    label = "Lotes",
                    icon = Icons.Default.Inventory2
                ),
                BottomNavOption(
                    destination = FlyNavDestination.StoreScan,
                    label = "Escanear",
                    icon = Icons.Default.QrCodeScanner,
                    isFloating = true,
                    floatingColor = AccentMint
                ),
                BottomNavOption(
                    destination = FlyNavDestination.StorePickups,
                    label = "Retiros",
                    icon = Icons.Default.Verified
                ),
                BottomNavOption(
                    destination = FlyNavDestination.Profile,
                    label = "Perfil",
                    icon = Icons.Default.Person
                )
            )
        }

        UserRole.ADMIN -> {
            listOf(
                BottomNavOption(
                    destination =
                        FlyNavDestination.AdminDashboard,
                    label = "Dashboard",
                    icon = Icons.Default.BarChart
                ),
                BottomNavOption(
                    destination =
                        FlyNavDestination.AdminUsers,
                    label = "Usuarios",
                    icon = Icons.Default.People
                ),
                BottomNavOption(
                    destination =
                        FlyNavDestination.AdminOrders,
                    label = "Pedidos",
                    icon = Icons.Default.Inventory2
                ),
                BottomNavOption(
                    destination =
                        FlyNavDestination.AdminIncidents,
                    label = "Reportes",
                    icon = Icons.Default.WarningAmber
                ),
                BottomNavOption(
                    destination =
                        FlyNavDestination.AdminSettings,
                    label = "Config",
                    icon = Icons.Default.Settings
                )
            )
        }

        else -> {
            listOf(
                BottomNavOption(
                    destination = FlyNavDestination.Home,
                    label = "Inicio",
                    icon = Icons.Default.Home
                ),
                BottomNavOption(
                    destination = FlyNavDestination.Profile,
                    label = "Perfil",
                    icon = Icons.Default.Person
                )
            )
        }
    }
}


@Composable
fun BottomNav(
    userRole: UserRole,
    currentDestination: FlyNavDestination,
    onDestinationSelected: (FlyNavDestination) -> Unit
) {
    val navigationOptions = remember(userRole) {
        getBottomNavOptions(userRole)
    }

    Surface(
        color = BgDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationOptions.forEach { option ->

                if (option.isFloating) {
                    FloatingBottomNavItem(
                        option = option,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDestinationSelected(option.destination)
                        }
                    )
                } else {
                    BottomNavItem(
                        icon = option.icon,
                        label = option.label,
                        isSelected = currentDestination == option.destination,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDestinationSelected(option.destination)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingBottomNavItem(
    option: BottomNavOption,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = option.floatingColor,
            shape = CircleShape,
            modifier = Modifier
                .size(48.dp)
                .offset(y = (-10).dp)
                .clickable(onClick = onClick)
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.label,
                tint = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}


@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) {
                Color.White
            } else {
                TextMuted
            },
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            color = if (isSelected) {
                Color.White
            } else {
                TextMuted
            },
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
