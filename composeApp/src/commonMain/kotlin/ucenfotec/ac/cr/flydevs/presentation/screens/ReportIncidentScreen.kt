package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.PrimaryButton
import ucenfotec.ac.cr.flydevs.presentation.components.TextField
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.incident.ReportIncidentViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*
import ucenfotec.ac.cr.flydevs.presentation.util.cardsLabel

@Composable
fun ReportIncidentScreen(
    userRole: UserRole,
    orderId: String,
    onBack: () -> Unit = {},
    onSubmitted: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    viewModel: ReportIncidentViewModel = koinViewModel(parameters = { parametersOf(orderId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onSubmitted()
    }

    Scaffold(
        containerColor = BgDarkest,
        topBar = {
            Column {
                Spacer(Modifier.statusBarsPadding())
                TopBar(title = "Reportar incidencia", onBack = onBack)
            }
        },
        bottomBar = {
            BottomNav(
                userRole = userRole,
                currentDestination = FlyNavDestination.Orders,
                onDestinationSelected = onNavSelect,
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentViolet)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(6.dp))

            EvidenceReminderBanner()

            Spacer(Modifier.height(14.dp))

            AffectedOrderCard(
                orderCode = uiState.orderCode,
                cardCount = uiState.cardCount,
                counterpartLabel = uiState.counterpartLabel,
            )

            if (uiState.hasOpenIncident) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Ya reportaste una incidencia para este pedido y sigue abierta. " +
                        "Si enviás otra, el equipo verá ambas.",
                    color = AccentGold,
                    fontSize = 12.sp,
                )
            }

            Spacer(Modifier.height(14.dp))

            Text("¿Qué ocurrió?", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(5.dp))

            TextField(
                value = uiState.description,
                placeholder = "Describí qué pasó con tu pedido…",
                onValueChange = viewModel::onDescriptionChange,
                singleLine = false,
                minHeight = 120,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${uiState.description.length} / ${uiState.maxLength}",
                color = if (uiState.remainingCharacters == 0) AccentGold else TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )

            uiState.errorMessage?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(message, color = AccentRed, fontSize = 13.sp)
            }

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = if (uiState.isSubmitting) "Enviando…" else "Enviar incidencia",
                onClick = viewModel::submit,
                enabled = uiState.canSubmit,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EvidenceReminderBanner() {
    Surface(color = BgCard, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Adjuntá toda la evidencia en la pantalla del pedido antes de reportar la incidencia.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun AffectedOrderCard(
    orderCode: String,
    cardCount: Int,
    counterpartLabel: String,
) {
    Surface(color = BgCard, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Pedido #$orderCode",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${cardsLabel(cardCount)} · $counterpartLabel",
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }
    }
}
