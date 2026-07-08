package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun ProfileScreen(
    onNavSelect: (FlyNavDestination) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDarkest)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Pantalla de Perfil", color = TextPrimary, fontSize = 20.sp)
        }
        
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNav(
                currentDestination = FlyNavDestination.Profile,
                onDestinationSelected = onNavSelect
            )
        }
    }
}
