package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.AppPermission
import ucenfotec.ac.cr.flydevs.domain.model.AppRole
import ucenfotec.ac.cr.flydevs.presentation.components.Icon
import ucenfotec.ac.cr.flydevs.presentation.components.FlyIconType
import ucenfotec.ac.cr.flydevs.presentation.rolePermission.RolePermissionViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.TextMuted
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary
import ucenfotec.ac.cr.flydevs.presentation.theme.TextSecondary
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentMint

fun hexToColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        if (cleanHex.length != 6) {
            return Color.Gray
        }
        val rgb = cleanHex.toInt(16)
        val red = (rgb shr 16) and 0xFF
        val green = (rgb shr 8) and 0xFF
        val blue = rgb and 0xFF
        Color(red = red / 255f, green = green / 255f, blue = blue / 255f, alpha = 1f)
    } catch (e: Exception) {
        Color.Gray
    }
}

@Composable
fun RolePermissionScreen(
    navController: NavController,
    onBack: () -> Unit = { navController.popBackStack() },
    viewModel: RolePermissionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding()
    ) {
        if (uiState.showCreateRoleSheet) {
            CreateRoleView(
                uiState = uiState,
                viewModel = viewModel,
                onBack = { viewModel.onDismissCreateRole() }
            )
        } else {
            RolesListView(
                uiState = uiState,
                viewModel = viewModel,
                onBack = onBack
            )
        }
    }
}

@Composable
private fun RolesListView(
    uiState: ucenfotec.ac.cr.flydevs.presentation.rolePermission.RolePermissionUiState,
    viewModel: RolePermissionViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDarkest)
    ) {
        // Top Bar
        Surface(
            color = BgDarkest,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    type = FlyIconType.Back,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onBack),
                    color = TextPrimary
                )

                Text(
                    text = "Roles y permisos",
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Add button with purple circle background
                Surface(
                    color = AccentViolet,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { viewModel.onShowCreateRole() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear rol",
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        // Summary
        Text(
            text = "${uiState.roles.size} roles definidos · ${uiState.totalAccounts} cuentas",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Roles List
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentViolet)
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = Color(0xFFFF6B6B),
                        fontSize = 14.sp
                    )
                }
            }

            uiState.roles.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay roles definidos",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.roles) { role ->
                        RoleCard(role = role, viewModel = viewModel)
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (uiState.showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDeleteRoleDismissed() },
                title = { Text("Eliminar rol", color = TextPrimary) },
                text = {
                    Text(
                        "¿Estás seguro de que deseas eliminar el rol '${uiState.roleToDelete?.name}'? Esta acción no se puede deshacer.",
                        color = TextMuted
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onDeleteRoleConfirmed() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) { Text("Eliminar", color = Color.White) }
                },
                dismissButton = {
                    Button(
                        onClick = { viewModel.onDeleteRoleDismissed() },
                        colors = ButtonDefaults.buttonColors(containerColor = BgCard)
                    ) { Text("Cancelar", color = TextPrimary) }
                },
                containerColor = BgCard
            )
        }
    }
}

@Composable
private fun RoleCard(role: AppRole, viewModel: RolePermissionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color circle
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(hexToColor(role.colorHex))
            )

            // Role info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = role.name,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${role.accountCount} cuentas",
                    color = TextMuted,
                    fontSize = 13.sp
                )

                // Permissions chips
                if (role.permissions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        role.permissions
                            .filter { it.isEnabled }
                            .forEach { permission ->
                                Surface(
                                    color = BgCard.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.border(
                                        1.dp,
                                        TextMuted.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                ) {
                                    Text(
                                        text = permission.name,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                    }
                }
            }

            // Edit and Delete buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { viewModel.onEditRole(role) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar rol",
                        tint = AccentViolet
                    )
                }

                IconButton(onClick = { viewModel.onDeleteRoleRequested(role) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar rol",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateRoleView(
    uiState: ucenfotec.ac.cr.flydevs.presentation.rolePermission.RolePermissionUiState,
    viewModel: RolePermissionViewModel,
    onBack: () -> Unit
) {
    // NO Scaffold - simple column with top bar and scrollable content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDarkest)
    ) {
        // Top bar
        Surface(
            color = BgDarkest,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    type = FlyIconType.Back,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onBack),
                    color = TextPrimary
                )

                Text(
                    text = if (uiState.editingRole != null) "Editar rol" else "Crear rol",
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Role Name Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NOMBRE DEL ROL",
                    color = AccentViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = uiState.newRoleName,
                    onValueChange = { viewModel.onRoleNameChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Gerente de ventas", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentViolet,
                        unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentViolet
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Color Selection Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "COLOR DE IDENTIFICACIÓN",
                    color = AccentViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                val colorOptions = listOf(
                    "#A855F7" to "Púrpura",
                    "#EAB308" to "Amarillo",
                    "#22C55E" to "Verde",
                    "#EF4444" to "Rojo",
                    "#8B5CF6" to "Violeta",
                    "#38BDF8" to "Azul"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colorOptions.forEach { (colorHex, _) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(hexToColor(colorHex))
                                .clickable { viewModel.onColorSelected(colorHex) }
                                .then(
                                    if (uiState.newRoleColorHex == colorHex) {
                                        Modifier.border(3.dp, Color.White, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
            }

            // Permissions Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PERMISOS",
                    color = AccentViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.newRolePermissions.forEach { permission ->
                        PermissionRow(
                            permission = permission,
                            onToggle = { viewModel.onPermissionToggled(permission.id) }
                        )
                    }
                }
            }

            // Action button (scrolls with content)
            Button(
                onClick = { viewModel.onCreateRole() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isCreatingRole && uiState.newRoleName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentViolet,
                    disabledContainerColor = TextMuted.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isCreatingRole) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (uiState.editingRole != null) "Guardar cambios" else "Crear rol",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionRow(
    permission: AppPermission,
    onToggle: () -> Unit
) {
    val permissionIcon = when (permission.id) {
        "buy_cards" -> Icons.Default.ShoppingCart
        "publish_sell" -> Icons.Default.Store
        "manage_deliveries" -> Icons.Default.LocalShipping
        "validate_pickups" -> Icons.Default.Store
        "access_reports" -> Icons.Default.BarChart
        "manage_users" -> Icons.Default.Shield
        else -> Icons.Default.Shield
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = permissionIcon,
                contentDescription = permission.name,
                tint = if (permission.isEnabled) AccentViolet else TextMuted,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = permission.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = permission.description,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = permission.isEnabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.size(48.dp, 24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentViolet,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = TextMuted.copy(alpha = 0.2f)
                )
            )
        }
    }
}
