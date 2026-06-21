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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.Dropdown
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.FormField
import ucenfotec.ac.cr.flydevs.presentation.components.PhotoUploadZone
import ucenfotec.ac.cr.flydevs.presentation.components.PriceField
import ucenfotec.ac.cr.flydevs.presentation.components.PrimaryButton
import ucenfotec.ac.cr.flydevs.presentation.components.QuantityStepper
import ucenfotec.ac.cr.flydevs.presentation.components.TextField
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.theme.*

private const val ExpansionPlaceholder = "Seleccionar expansión"
private val ExpansionOptions = listOf("Alpha", "Beta", "Unlimited", "Revised", "Arabian Nights", "Legends")
private val ConditionOptions = listOf("Near Mint (NM)", "Lightly Played (LP)", "Moderately Played (MP)", "Heavily Played (HP)", "Damaged (DMG)")
private val LanguageOptions = listOf("Inglés (EN)", "Español (ES)", "Japonés (JP)", "Alemán (DE)", "Francés (FR)", "Italiano (IT)")

@Composable
fun PublishGameCardScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onUploadPhoto: () -> Unit = {},
    onPublish: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
) {
    var name by remember { mutableStateOf("") }
    var expansion by remember { mutableStateOf(ExpansionPlaceholder) }
    var condition by remember { mutableStateOf("Near Mint (NM)") }
    var language by remember { mutableStateOf("Inglés (EN)") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }
    var description by remember { mutableStateOf("") }

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
                onClick = onUploadPhoto,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 4.dp),
            )

            Column(
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FormField("Nombre de la carta") {
                    TextField(name, "Ej: Black Lotus", onValueChange = { name = it })
                }
                FormField("Expansión / Set") {
                    Dropdown(
                        value = expansion,
                        options = ExpansionOptions,
                        isPlaceholder = expansion == ExpansionPlaceholder,
                        onSelect = { expansion = it },
                    )
                }
                FormField("Condición") {
                    Dropdown(
                        value = condition,
                        options = ConditionOptions,
                        onSelect = { condition = it },
                    )
                }
                FormField("Idioma") {
                    Dropdown(
                        value = language,
                        options = LanguageOptions,
                        onSelect = { language = it },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormField("Precio", Modifier.weight(1f)) {
                        PriceField(value = price, onValueChange = { price = it.filter(Char::isDigit) })
                    }
                    FormField("Cantidad", Modifier.weight(1f)) {
                        QuantityStepper(
                            quantity = quantity,
                            onDecrease = { if (quantity > 1) quantity-- },
                            onIncrease = { quantity++ },
                        )
                    }
                }
                FormField("Descripción (opcional)") {
                    TextField(
                        value = description,
                        placeholder = "Añade detalles sobre la carta...",
                        singleLine = false,
                        minHeight = 72,
                        onValueChange = { description = it },
                    )
                }
            }

            PrimaryButton(
                text = "Publicar carta",
                onClick = onPublish,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            )
        }

        BottomNav(selected = FlyNavDestination.Sell, onSelect = onNavSelect)
    }
}
