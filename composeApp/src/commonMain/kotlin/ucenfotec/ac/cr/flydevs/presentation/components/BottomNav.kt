package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ucenfotec.ac.cr.flydevs.presentation.theme.*

/** Destinos de la barra de navegación inferior global. */
enum class FlyNavDestination { Home, Explore, Sell, Orders, Profile }

@Composable
fun BottomNav(
    selected: FlyNavDestination,
    onSelect: (FlyNavDestination) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { }
) {
    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(BgSurface))
        Row(
            Modifier.fillMaxWidth().background(BgCard).navigationBarsPadding()
                .height(62.dp).padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem("Inicio", FlyIconType.Home, selected == FlyNavDestination.Home, Modifier.weight(1f)) { onSelect(FlyNavDestination.Home) }
            NavItem("Explorar", FlyIconType.Compass, selected == FlyNavDestination.Explore, Modifier.weight(1f)) { onSelect(FlyNavDestination.Explore) }
            Column(
                Modifier.weight(1f).clickable { onSelect(FlyNavDestination.Sell) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(AccentViolet),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(FlyIconType.Plus, Modifier.size(20.dp), TextPrimary)
                }
                Text(
                    "Vender",
                    color = if (selected == FlyNavDestination.Sell) AccentVioletLight else TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            NavItem("Pedidos", FlyIconType.Package, selected == FlyNavDestination.Orders, Modifier.weight(1f)) { onSelect(FlyNavDestination.Orders) }
            NavItem("Perfil", FlyIconType.Profile, selected == FlyNavDestination.Profile, Modifier.weight(1f)) { onSelect(FlyNavDestination.Profile) }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: FlyIconType,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val color = if (selected) AccentViolet else TextMuted
    Column(
        modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(icon, Modifier.size(20.dp), color)
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}
