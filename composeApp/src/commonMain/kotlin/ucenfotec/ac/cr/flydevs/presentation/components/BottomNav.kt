package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ucenfotec.ac.cr.flydevs.presentation.theme.*

/** Destinos de la barra de navegación inferior global. */
enum class FlyNavDestination { Home, Explore, Sell, Orders, Profile }

@Composable
fun BottomNav(
    currentDestination: FlyNavDestination = FlyNavDestination.Home,
    onDestinationSelected: (FlyNavDestination) -> Unit = {}
) {
    Surface(
        color = BgDark,
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Inicio",
                isSelected = currentDestination == FlyNavDestination.Home,
                onClick = { onDestinationSelected(FlyNavDestination.Home) }
            )
            BottomNavItem(
                icon = Icons.Default.Explore,
                label = "Explorar",
                isSelected = currentDestination == FlyNavDestination.Explore,
                onClick = { onDestinationSelected(FlyNavDestination.Explore) }
            )

            // Floating Center Button (Sell)
            Surface(
                color = AccentViolet,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .offset(y = (-10).dp)
                    .clickable { onDestinationSelected(FlyNavDestination.Sell) }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Vender",
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }

            BottomNavItem(
                icon = Icons.AutoMirrored.Filled.ListAlt,
                label = "Pedidos",
                isSelected = currentDestination == FlyNavDestination.Orders,
                onClick = { onDestinationSelected(FlyNavDestination.Orders) }
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Perfil",
                isSelected = currentDestination == FlyNavDestination.Profile,
                onClick = { onDestinationSelected(FlyNavDestination.Profile) }
            )
        }
    }
}


@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(top = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else TextMuted,
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            color = if (isSelected) Color.White else TextMuted,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
