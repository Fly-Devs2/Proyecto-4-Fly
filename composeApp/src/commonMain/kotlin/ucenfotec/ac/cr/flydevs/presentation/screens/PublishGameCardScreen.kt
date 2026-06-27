package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.CardCondition
import ucenfotec.ac.cr.flydevs.domain.model.CardGame
import ucenfotec.ac.cr.flydevs.domain.model.CardLanguage
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.CameraCaptureScreen
import ucenfotec.ac.cr.flydevs.presentation.components.Dropdown
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.FormField
import ucenfotec.ac.cr.flydevs.presentation.components.PhotoUploadZone
import ucenfotec.ac.cr.flydevs.presentation.components.PriceField
import ucenfotec.ac.cr.flydevs.presentation.components.PrimaryButton
import ucenfotec.ac.cr.flydevs.presentation.components.QuantityStepper
import ucenfotec.ac.cr.flydevs.presentation.components.TextField
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.PublishCardUiState
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.PublishFeedback
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.PublishGameCardViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun PublishGameCardScreen(
    modifier: Modifier = Modifier,
    viewModel: PublishGameCardViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCamera by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
                PhotoSection(state = state, onTakePhoto = { showCamera = true })
                CardFormFields(state = state, viewModel = viewModel)
                PublishStatus(state = state)

                PrimaryButton(
                    text = publishButtonText(state),
                    onClick = viewModel::publish,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
                )
            }

            BottomNav(selected = FlyNavDestination.Sell, onSelect = onNavSelect)
        }

        if (showCamera) {
            CameraCaptureScreen(
                onImageCaptured = { image ->
                    showCamera = false
                    viewModel.onImagePicked(image)
                },
                onCancel = { showCamera = false },
            )
        }
    }
}

@Composable
private fun PhotoSection(state: PublishCardUiState, onTakePhoto: () -> Unit) {
    PhotoUploadZone(
        onClick = onTakePhoto,
        title = photoTitle(state),
        subtitle = photoSubtitle(state),
        accentColor = if (state.imageUrl != null) AccentMint else AccentViolet,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 4.dp),
    )
    state.imageError?.let { StatusText(imageErrorText(it), AccentRed) }
}

@Composable
private fun CardFormFields(state: PublishCardUiState, viewModel: PublishGameCardViewModel) {
    Column(
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FormField("Nombre de la carta", required = true) {
            TextField(state.name, "Ej: Black Lotus", onValueChange = viewModel::onNameChange)
        }
        FormField("Tipo de juego", required = true) {
            Dropdown(
                selected = state.game,
                options = CardGame.entries,
                label = { it.label },
                placeholder = "Seleccionar juego",
                onSelect = viewModel::onGameChange,
            )
        }
        FormField("Expansión / Set", required = true) {
            Dropdown(
                selected = state.expansion,
                options = state.expansionOptions,
                label = { it },
                placeholder = expansionPlaceholder(state),
                onSelect = viewModel::onExpansionChange,
            )
        }
        FormField("Rareza", required = true) {
            Dropdown(
                selected = state.rarity,
                options = state.rarityOptions,
                label = { it },
                placeholder = rarityPlaceholder(state),
                onSelect = viewModel::onRarityChange,
            )
        }
        FormField("Condición") {
            Dropdown(
                selected = state.condition,
                options = CardCondition.entries,
                label = { it.label },
                onSelect = viewModel::onConditionChange,
            )
        }
        FormField("Idioma") {
            Dropdown(
                selected = state.language,
                options = CardLanguage.entries,
                label = { it.label },
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
}

@Composable
private fun PublishStatus(state: PublishCardUiState) {
    state.feedback?.let { feedback ->
        val color = if (feedback == PublishFeedback.SUCCESS) AccentMint else AccentRed
        StatusText(feedbackText(feedback), color)
    }

    if (!state.isLoading && state.feedback != PublishFeedback.SUCCESS && state.validationErrors.isNotEmpty()) {
        StatusText(missingFieldsText(state.validationErrors), AccentRed)
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
