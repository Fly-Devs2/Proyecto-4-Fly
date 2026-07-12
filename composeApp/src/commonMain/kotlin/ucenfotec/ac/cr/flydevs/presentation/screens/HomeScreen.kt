package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.presentation.home.HomeViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.OrdersSection

@Preview
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onSignOutSuccess: () -> Unit = {},
    onNavigateToMyCollection: () -> Unit = {},
    onNavigateToOrder: (String) -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onSignOutSuccess()
        }
    }

    Scaffold(
        containerColor = BgDarkest,
        bottomBar = {
            BottomNav(
                currentDestination = FlyNavDestination.Home,
                onDestinationSelected = onNavSelect
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            
            // Header
            HeaderSection(
                userName = uiState.user?.name ?: "Usuario",
                unreadNotifications = uiState.unreadNotifications,
                onSignOutClick = { viewModel.signOut() },
                onNavigateToProfile = onNavigateToProfile,
                onNotificationsClick = onNavigateToNotifications,
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Search Bar
//            SearchBar()
            
            Spacer(Modifier.height(28.dp))
            
            // Featured Section
//            SectionTitle("DESTACADAS DE LA SEMANA")
//            FeaturedCards()
            
            Spacer(Modifier.height(28.dp))
            
//            // Categories
//            SectionTitle("CATEGORÍAS")
//            CategoryChips()
            
            Spacer(Modifier.height(28.dp))
            
            // Orders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("MIS PEDIDOS")
                Text(
                    "Ver todos",
                    color = AccentViolet,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToOrders() }
                        .padding(bottom = 16.dp),
                )
            }
            OrdersSection(
                orders = uiState.orders,
                onOrderClick = onNavigateToOrder
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Actions
            Button(
                onClick = { onNavSelect(FlyNavDestination.Sell) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                shape = RoundedCornerShape(16.dp)
            ) {

                Spacer(Modifier.width(8.dp))
                Text("Publicar carta en venta", style = Typography.labelLarge)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = { onNavigateToMyCollection() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver mi colección", color = TextPrimary)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    unreadNotifications: Int,
    onSignOutClick: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BgSurface)
                .clickable { onNavigateToProfile() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary)
        }
        
        Spacer(Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hola, $userName",
                style = Typography.titleLarge,
                color = TextPrimary
            )
        }
        
        IconButton(onClick = onSignOutClick) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión", tint = AccentRed)
        }
        
        IconButton(onClick = onNotificationsClick) {
            BadgedBox(
                badge = {
                    if (unreadNotifications > 0) {
                        Badge(containerColor = AccentRed) {
                            Text(if (unreadNotifications > 9) "9+" else "$unreadNotifications")
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = TextPrimary)
            }
        }
    }
}

@Composable
private fun SearchBar() {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
            Spacer(Modifier.width(12.dp))
            Text("Buscar cartas, sets, vendedores...", color = TextMuted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = Typography.titleSmall,
        color = AccentGold,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun FeaturedCards() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        FeaturedCardItem("Charizard 1st Ed.", "₡320 000", "RARA")
        FeaturedCardItem("Blue-Eyes W.D.", "₡95 500", "HOLO")
    }
}

@Composable
private fun FeaturedCardItem(name: String, price: String, tag: String) {
    val displayName = if (name.length > 20) name.take(17) + "..." else name
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.width(160.dp).height(210.dp)
    ) {
        Box {
            // Card Content Placeholder
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgSurface)
                )
                Spacer(Modifier.height(12.dp))
                Text(displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(price, color = AccentViolet, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            }
            
            // Tag
            Surface(
                color = AccentGold,
                shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 20.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = BgDarkest,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun CategoryChips() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CategoryChip("Todas", isSelected = true)
        CategoryChip("Pokémon")
        CategoryChip("Yu-Gi-Oh!")
    }
}

@Composable
private fun CategoryChip(text: String, isSelected: Boolean = false) {
    Surface(
        color = if (isSelected) AccentViolet else BgCard,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
