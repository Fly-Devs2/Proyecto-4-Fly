package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.Dropdown
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.FormField
import ucenfotec.ac.cr.flydevs.presentation.components.PhotoUploadZone
import ucenfotec.ac.cr.flydevs.presentation.components.rememberImagePicker
import ucenfotec.ac.cr.flydevs.presentation.components.PriceField
import ucenfotec.ac.cr.flydevs.presentation.components.PrimaryButton
import ucenfotec.ac.cr.flydevs.presentation.components.QuantityStepper
import ucenfotec.ac.cr.flydevs.presentation.components.TextField
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.PublishGameCardViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*

private const val ExpansionPlaceholder = "Seleccionar expansión"
private val ExpansionOptions = listOf("Alpha", "Beta", "Unlimited", "Revised", "Arabian Nights", "Legends")
private val ConditionOptions = listOf("Near Mint (NM)", "Lightly Played (LP)", "Moderately Played (MP)", "Heavily Played (HP)", "Damaged (DMG)")
private val LanguageOptions = listOf("Inglés (EN)", "Español (ES)", "Japonés (JP)", "Alemán (DE)", "Francés (FR)", "Italiano (IT)")

@Composable
fun PublishGameCardScreen(
    modifier: Modifier = Modifier,
    viewModel: PublishGameCardViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val launchImagePicker = rememberImagePicker(onImagePicked = viewModel::onImagePicked)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding(),
    ) {
        TopBar(title = "Publicar carta", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            PhotoUploadZone(
                onClick = launchImagePicker,
                title = when {
                    state.isUploadingImage -> "Subiendo imagen..."
                    state.imageUrl != null -> "Foto lista ✓"
                    else -> "Subir fotos de la carta"
                },
                subtitle = when {
                    state.isUploadingImage -> "Espera un momento"
                    state.imageUrl != null -> state.imageFileName ?: "Toca para cambiarla"
                    else -> "JPG o PNG · máx. 5 MB"
                },
                accentColor = if (state.imageUrl != null) AccentMint else AccentViolet,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 4.dp),
            )
            state.imageError?.let { StatusText(it, AccentRed) }

            Column(
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FormField("Nombre de la carta", required = true) {
                    TextField(state.name, "Ej: Black Lotus", onValueChange = viewModel::onNameChange)
                }
                FormField("Expansión / Set") {
                    Dropdown(
                        value = state.expansion.ifBlank { ExpansionPlaceholder },
                        options = ExpansionOptions,
                        isPlaceholder = state.expansion.isBlank(),
                        onSelect = viewModel::onExpansionChange,
                    )
                }
                FormField("Condición") {
                    Dropdown(
                        value = state.condition,
                        options = ConditionOptions,
                        onSelect = viewModel::onConditionChange,
                    )
                }
                FormField("Idioma") {
                    Dropdown(
                        value = state.language,
                        options = LanguageOptions,
                        onSelect = viewModel::onLanguageChange,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormField("Precio", Modifier.weight(1f), required = true) {
                        PriceField(value = state.price, onValueChange = viewModel::onPriceChange)
                    }
                    FormField("Cantidad", Modifier.weight(1f)) {
                        QuantityStepper(
                            quantity = state.quantity,
                            onDecrease = viewModel::decreaseQuantity,
                            onIncrease = viewModel::increaseQuantity,
                        )
                    }
                }
                FormField("Descripción (opcional)") {
                    TextField(
                        value = state.description,
                        placeholder = "Añade detalles sobre la carta...",
                        singleLine = false,
                        minHeight = 72,
                        onValueChange = viewModel::onDescriptionChange,
                    )
                }
            }

            // ── Mensajes claros de éxito / error del proceso ──
            state.successMessage?.let { StatusText(it, AccentMint) }
            state.errorMessage?.let { StatusText(it, AccentRed) }

            // ── Pista para saber qué falta para habilitar el botón ──
            if (!state.isLoading && state.successMessage == null && state.missingRequired.isNotEmpty()) {
                StatusText(
                    "Para publicar, agrega: ${state.missingRequired.joinToString(", ")}.",
                    AccentRed,
                )
            }

            PrimaryButton(
                text = when {
                    state.isUploadingImage -> "Subiendo imagen..."
                    state.isLoading -> "Publicando..."
                    else -> "Publicar carta"
                },
                onClick = viewModel::publish,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            )
        }

        BottomNav(selected = FlyNavDestination.Sell, onSelect = onNavSelect)
    }
}

@Composable
private fun StatusText(message: String, color: Color) {
    Text(
        message,
        color = color,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
    )
}
