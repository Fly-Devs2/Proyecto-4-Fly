package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.adminUsers.AdminUsersViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun AdminUsersScreen(
    userRole: UserRole,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    onNavSelect: (FlyNavDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminUsersViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding()
    ) {
        TopBar(
            title = "Usuarios del sistema",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = { Text("Buscar...", color = TextMuted, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentViolet,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = BgCard,
                        unfocusedContainerColor = BgCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentViolet
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedRole == null,
                        onClick = { viewModel.onRoleFilterSelected(null) },
                        label = { Text("Todos") },
                        colors = filterChipColors(),
                        border = null,
                        shape = RoundedCornerShape(100.dp)
                    )
                    UserRole.entries.forEach { role ->
                        if ((role == UserRole.BUYER) || (role == UserRole.SELLER)) return@forEach // Skip internal roles if needed or keep them
                        val label = when(role) {
                            UserRole.USER -> "Usuarios"
                            UserRole.DELIVERY -> "Mensajeros"
                            UserRole.STORE -> "Tiendas"
                            UserRole.ADMIN -> "Admins"
                        }
                        val chipColor = when(role) {
                            UserRole.ADMIN -> AccentRed
                            UserRole.STORE -> AccentMint
                            UserRole.DELIVERY -> AccentGold
                            else -> AccentViolet
                        }
                        FilterChip(
                            selected = state.selectedRole == role,
                            onClick = { viewModel.onRoleFilterSelected(role) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = BgCard,
                                labelColor = TextSecondary,
                                selectedContainerColor = chipColor,
                                selectedLabelColor = Color.White
                            ),
                            border = null,
                            shape = RoundedCornerShape(100.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "${state.filteredUsers.size} cuentas · ordenado por actividad",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentViolet)
                    }
                }
            } else {
                items(state.filteredUsers, key = { it.uid }) { user ->
                    AdminUserCard(
                        user = user,
                        onClick = { onUserClick(user.uid) }
                    )
                }
            }
        }

        BottomNav(
            userRole = userRole,
            currentDestination = FlyNavDestination.AdminUsers,
            onDestinationSelected = onNavSelect
        )
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = BgCard,
    labelColor = TextSecondary,
    selectedContainerColor = AccentViolet,
    selectedLabelColor = Color.White
)

@Composable
private fun AdminUserCard(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BgSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = AccentVioletLight,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(user.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(user.email, color = TextMuted, fontSize = 13.sp)
        }

        Column(horizontalAlignment = Alignment.End) {
            val roleColor = when(user.userRole) {
                UserRole.ADMIN -> AccentRed
                UserRole.STORE -> AccentMint
                UserRole.DELIVERY -> AccentGold
                else -> AccentVioletLight
            }
            val roleLabel = when(user.userRole) {
                UserRole.USER -> "USUARIO"
                UserRole.DELIVERY -> "MENSAJERO"
                UserRole.STORE -> "TIENDA"
                UserRole.ADMIN -> "ADMIN"
                else -> user.role.uppercase()
            }
            Text(
                text = roleLabel,
                color = roleColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(roleColor.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = if (!user.isActive) AccentRed else AccentMint
                val statusText = if (!user.isActive) "Bloqueado" else "Activo"
                Box(Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                Spacer(Modifier.width(4.dp))
                Text(statusText, color = statusColor, fontSize = 11.sp)
            }
        }
    }
}
