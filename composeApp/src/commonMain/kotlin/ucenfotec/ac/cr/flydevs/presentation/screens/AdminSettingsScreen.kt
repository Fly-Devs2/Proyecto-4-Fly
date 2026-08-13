package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.navigation.AdminRoles
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.TextMuted
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary

@Composable
fun AdminSettingsScreen(
    userRole: UserRole,
    onBack: () -> Unit,
    onNavSelect: (FlyNavDestination) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            BottomNav(
                userRole = userRole,
                currentDestination = FlyNavDestination.AdminSettings,
                onDestinationSelected = onNavSelect
            )
        },
        containerColor = BgDarkest
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            TopBar(
                title = "Configuración",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Roles y permisos entry
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(AdminRoles) },
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ManageAccounts,
                            contentDescription = "Roles y permisos",
                            tint = AccentViolet,
                            modifier = Modifier.size(28.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Roles y permisos",
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Gestionar roles del sistema",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = AccentViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
