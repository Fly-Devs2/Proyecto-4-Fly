package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest

@Preview
@Composable
fun SearchableDropdownPreview() {
    val options = listOf("Magic: The Gathering", "Pokémon", "Yu-Gi-Oh!", "One Piece", "Lorcana", "Star Wars: Unlimited")
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDarkest)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            FormField("Seleccionar Juego (Searchable)") {
                SearchableDropdown(
                    selected = selectedOption,
                    options = options,
                    label = { it },
                    onSelect = { selectedOption = it },
                    placeholder = "Buscar juego..."
                )
            }
            
            FormField("Regular Dropdown (Comparison)") {
                Dropdown(
                    selected = selectedOption,
                    options = options,
                    label = { it },
                    onSelect = { selectedOption = it },
                    placeholder = "Seleccionar juego"
                )
            }
        }
    }
}
