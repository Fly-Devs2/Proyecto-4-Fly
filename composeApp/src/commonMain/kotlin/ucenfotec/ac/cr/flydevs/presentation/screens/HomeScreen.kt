package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
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


@Preview
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onSignOutSuccess: () -> Unit = {},
    onNavigateToMyCollection: () -> Unit = {}
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
            BottomNavigationBar()
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
                onSignOutClick = { viewModel.signOut() }
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Search Bar
            SearchBar()
            
            Spacer(Modifier.height(28.dp))
            
            // Featured Section
            SectionTitle("DESTACADAS DE LA SEMANA")
            FeaturedCards()
            
            Spacer(Modifier.height(28.dp))
            
            // Categories
            SectionTitle("CATEGORÍAS")
            CategoryChips()
            
            Spacer(Modifier.height(28.dp))
            
            // Orders
            SectionTitle("MIS PEDIDOS")
            OrdersSection()
            
            Spacer(Modifier.height(32.dp))
            
            // Actions
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Publicar carta en venta", style = Typography.labelLarge)
            }
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { onNavigateToMyCollection() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver mi colección (132 cartas)", color = TextPrimary)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    onSignOutClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(BgSurface),
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
            Text(
                text = "Coleccionista · Nivel 4",
                style = Typography.bodySmall,
                color = TextSecondary
            )
        }
        
        IconButton(onClick = onSignOutClick) {
            Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión", tint = AccentRed)
        }
        
        IconButton(onClick = { }) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = TextPrimary)
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
                Text(name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2)
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

@Composable
private fun OrdersSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OrderItem("Pedido #FA-1042", "3 cartas · Vendedor: CardKingCR", "EN RUTA", AccentGold)
        OrderItem("Pedido #FA-1037", "1 carta · Entregado 08 jun", "ENTREGADO", AccentMint)
        OrderItem("Pedido #FA-1029", "Disputa abierta · En revisión", "DISPUTA", AccentRed)
    }
}

@Composable
private fun OrderItem(id: String, desc: String, status: String, statusColor: Color) {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(BgSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (status == "DISPUTA") Icons.Default.Warning else Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(id, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = TextSecondary, fontSize = 12.sp)
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar() {
    Surface(
        color = BgDark,
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Default.Home, "Inicio", isSelected = true)
            BottomNavItem(Icons.Default.Explore, "Explorar")
            
            // Floating Center Button
            Surface(
                color = AccentViolet,
                shape = CircleShape,
                modifier = Modifier.size(48.dp).offset(y = (-10).dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
            }
            
            BottomNavItem(Icons.AutoMirrored.Filled.ListAlt, "Pedidos")
            BottomNavItem(Icons.Default.Person, "Perfil")
        }
    }
}

@Composable
private fun BottomNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else TextMuted, modifier = Modifier.size(24.dp))
        Text(label, color = if (isSelected) Color.White else TextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
