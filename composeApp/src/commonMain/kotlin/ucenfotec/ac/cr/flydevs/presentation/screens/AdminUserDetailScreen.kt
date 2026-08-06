package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.adminUserDetail.AdminUserDetailViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun AdminUserDetailScreen(
    userRole: UserRole,
    userId: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onNavSelect: (FlyNavDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminUserDetailViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showBlockConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) onDeleted()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding()
    ) {
        TopBar(title = "Detalle de usuario", onBack = onBack)

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentViolet)
            }
        } else if (state.user != null) {
            val user = state.user!!
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(BgSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = AccentVioletLight,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user.name, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(user.email, color = TextMuted, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val roleLabel = when(user.userRole) {
                                UserRole.USER -> "USUARIO"
                                UserRole.DELIVERY -> "MENSAJERO"
                                UserRole.STORE -> "TIENDA"
                                UserRole.ADMIN -> "ADMIN"
                                else -> user.role.uppercase()
                            }
                            Badge(roleLabel, AccentVioletLight)
                            Badge(if (user.isActive) "ACTIVO" else "BLOQUEADO", if (user.isActive) AccentMint else AccentRed)
                        }
                    }
                }

                // Info Section
                SectionTitle("INFORMACIÓN DE CUENTA")
                InfoRow(Icons.Default.Phone, "Teléfono", user.phone)
                InfoRow(Icons.Default.Person, "Rol principal", user.role.lowercase().replaceFirstChar { it.uppercase() })
                InfoRow(Icons.Default.ShoppingCart, "Compras completadas", state.stats?.completedPurchases?.toString() ?: "0")
                InfoRow(Icons.Default.Store, "Ventas publicadas", state.stats?.publishedSales?.toString() ?: "0")

                // Activity Section
                SectionTitle("ACTIVIDAD Y RIESGO")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(state.stats?.reportCount?.toString() ?: "0", "Reportes", AccentRed, Modifier.weight(1f))
                    StatCard("${state.stats?.reputationPercentage ?: 0}%", "Reputación", AccentGold, Modifier.weight(1f))
                }

                // Admin Actions
                SectionTitle("ACCIONES ADMIN")
                AdminButton(Icons.Default.Edit, "Editar datos del usuario", AccentVioletLight) { /* TODO */ }
                AdminButton(Icons.Default.Group, "Cambiar rol o permisos", AccentVioletLight) { /* TODO */ }
                AdminButton(
                    icon = if (user.isActive) Icons.Default.Lock else Icons.Default.LockOpen,
                    label = if (user.isActive) "Bloquear cuenta" else "Desbloquear cuenta",
                    color = AccentRed,
                    outline = true,
                    loading = state.isActionLoading,
                    onClick = { showBlockConfirm = true }
                )
                AdminButton(Icons.Default.Delete, "Eliminar usuario", AccentRed, dashed = true, onClick = { showDeleteConfirm = true })
                
                Spacer(Modifier.height(20.dp))
            }

            if (showBlockConfirm) {
                val actionText = if (user.isActive) "bloquear" else "desbloquear"
                AlertDialog(
                    onDismissRequest = { showBlockConfirm = false },
                    title = { Text("¿Confirmar acción?") },
                    text = { Text("¿Estás seguro de que deseas $actionText a este usuario? Esta acción afectará su acceso al sistema.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showBlockConfirm = false
                                viewModel.toggleUserStatus()
                            }
                        ) {
                            Text(if (user.isActive) "Bloquear" else "Desbloquear", color = if (user.isActive) AccentRed else AccentMint)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBlockConfirm = false }) {
                            Text("Cancelar")
                        }
                    },
                    containerColor = BgCard,
                    titleContentColor = TextPrimary,
                    textContentColor = TextSecondary
                )
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("¿Eliminar usuario?") },
                    text = { Text("Esta acción es permanente y no se puede deshacer. Todos los datos del usuario serán eliminados.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirm = false
                                viewModel.deleteUser()
                            }
                        ) {
                            Text("Eliminar permanentemente", color = AccentRed)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text("Conservar usuario")
                        }
                    },
                    containerColor = BgCard,
                    titleContentColor = TextPrimary,
                    textContentColor = TextSecondary
                )
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
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = AccentGold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun AdminButton(
    icon: ImageVector,
    label: String,
    color: Color,
    outline: Boolean = false,
    dashed: Boolean = false,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(100.dp),
        color = if (outline || dashed) Color.Transparent else color.copy(alpha = 0.1f),
        border = if (outline) BorderStroke(1.dp, color) else if (dashed) BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = color, strokeWidth = 2.dp)
            } else {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(label, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
