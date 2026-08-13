package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import ucenfotec.ac.cr.flydevs.presentation.components.PrimaryButton
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.notifications.NotificationPreferencesViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentGold
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentMint
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentRed
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.TextMuted
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary
import ucenfotec.ac.cr.flydevs.presentation.theme.TextSecondary

@Composable
fun NotificationPreferencesScreen(
    onBack: () -> Unit,
    viewModel: NotificationPreferencesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding()
    ) {
        TopBar(
            title = "Notificaciones",
            onBack = onBack
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentViolet)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                "GESTIONAR PREFERENCIAS",
                color = AccentGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )

            PreferenceToggle(
                title = "Estado del pedido",
                description = "Recibe avisos cuando tus pedidos cambien de estado.",
                checked = state.preferences.orderStatusChanged,
                onCheckedChange = viewModel::onOrderStatusChangedToggle
            )

            Spacer(Modifier.height(16.dp))

            PreferenceToggle(
                title = "Recordatorios de SINPE",
                description = "Te recordaremos subir tu comprobante de pago los jueves.",
                checked = state.preferences.sinpeReminders,
                onCheckedChange = viewModel::onSinpeRemindersToggle
            )

            Spacer(Modifier.height(16.dp))

            PreferenceToggle(
                title = "Recordatorios de retiro",
                description = "Te avisamos si dejaste una carta sin retirar en la tienda.",
                checked = state.preferences.pickupReminders,
                onCheckedChange = viewModel::onPickupRemindersToggle
            )

            Spacer(Modifier.height(32.dp))

            if (state.saveSuccess) {
                Text(
                    "✓ Preferencias guardadas",
                    color = AccentMint,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            state.errorMessage?.let { msg ->
                Text(
                    msg,
                    color = AccentRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            PrimaryButton(
                text = if (state.isSaving) "Guardando..." else "Guardar cambios",
                onClick = viewModel::savePreferences,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PreferenceToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentViolet,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = BgDarkest,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun NotificationPreferencesScreenPreview() {
    ucenfotec.ac.cr.flydevs.presentation.theme.FlyAppTheme {
        NotificationPreferencesScreen(onBack = {})
    }
}
