package ucenfotec.ac.cr.flydevs.presentation.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.CardCatalogUiState
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentGold
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.BgSurface
import ucenfotec.ac.cr.flydevs.presentation.theme.TextMuted
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary
import ucenfotec.ac.cr.flydevs.presentation.theme.TextSecondary


private data class GameFilter(
    val label: String,
    val firebaseValue: String?
)

private val GameFilters = listOf(
    GameFilter("Todas", null),
    GameFilter("Pokémon", "POKEMON"),
    GameFilter("Magic", "MAGIC"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardMarketplaceScreen(
    uiState: CardCatalogUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onCardClick: (String) -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    onRetryClick: () -> Unit = {},
) {

    var searchQuery by remember { mutableStateOf("") }
    var sortLowPrice by remember { mutableStateOf(true) }
    var showFilters by remember { mutableStateOf(false) }
    val maxCatalogPrice = remember(uiState.cards) {
        uiState.cards.maxOfOrNull { it.price }?.coerceAtLeast(1L) ?: 1L
    }
    var selectedGameFilter by remember { mutableStateOf<GameFilter>(GameFilters.first()) }

    var selectedPriceRange by remember(maxCatalogPrice) {
        mutableStateOf(0f..maxCatalogPrice.toFloat())
    }
    val filterSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val filteredCards = uiState.cards
        .filter { card ->
            val matchesSearch =
                searchQuery.isBlank() ||
                        card.name.contains(searchQuery, ignoreCase = true) ||
                        card.expansion.contains(searchQuery, ignoreCase = true) ||
                        card.condition.contains(searchQuery, ignoreCase = true) ||
                        card.language.contains(searchQuery, ignoreCase = true)

            val matchesPrice =
                card.price.toFloat() in selectedPriceRange.start..selectedPriceRange.endInclusive
            val matchesGame =
                selectedGameFilter.firebaseValue == null ||
                        card.game.equals(selectedGameFilter.firebaseValue, ignoreCase = true)

            matchesSearch && matchesPrice && matchesGame
        }
        .let { cards ->
            if (sortLowPrice) {
                cards.sortedBy { it.price }
            } else {
                cards.sortedByDescending { it.price }
            }
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding()
    ) {
        TopBar(
            title = "Explorar colección",
            onBack = onBack,
            onFilterClick = {
                println("DEBUG_FILTER: Filter button clicked")
                showFilters = true
                onFilterClick()
            }

        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            SearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp)
            )



            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredCards.size} resultados",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                SortChip(
                    text = if (sortLowPrice) "Precio: menor" else "Precio: mayor",
                    onClick = { sortLowPrice = !sortLowPrice }
                )
            }
            when {
                uiState.isLoading -> {
                    CatalogMessage(
                        text = "Cargando cartas..."
                    )
                }

                uiState.errorMessage != null -> {
                    CatalogErrorMessage(
                        text = uiState.errorMessage ?: "Error desconocido",
                        onRetryClick = onRetryClick
                    )
                }

                filteredCards.isEmpty() -> {
                    CatalogMessage(
                        text = "No se encontraron cartas disponibles."
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 14.dp,
                            bottom = 18.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredCards,
                            key = { card -> card.id }
                        ) { card ->
                            MarketplaceCardItem(
                                card = card,
                                onClick = { onCardClick(card.id) }
                            )
                        }
                    }
                }
            }


        }

        BottomNav(
            selected = FlyNavDestination.Explore,
            onSelect = onNavSelect
        )
    }
    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = filterSheetState,
            containerColor = BgCard,
            contentColor = TextPrimary,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(TextMuted.copy(alpha = 0.65f))
                )
            }
        ) {
            FilterBottomSheetContent(
                selectedGameFilter = selectedGameFilter,
                onGameFilterSelected = { selectedGameFilter = it },
                selectedPriceRange = selectedPriceRange,
                maxPrice = maxCatalogPrice.toFloat(),
                onPriceRangeChange = { newRange ->
                    selectedPriceRange = newRange
                },
                onClearClick = {
                    selectedGameFilter = GameFilters.first()
                    selectedPriceRange = 0f..maxCatalogPrice.toFloat()
                    // luego limpiamos filtros reales
                },
                onCloseClick = {
                    showFilters = false
                }
            )
        }
    }

}

@Composable
private fun CatalogMessage(
    text: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CatalogErrorMessage(
    text: String,
    onRetryClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No se pudo cargar el catálogo",
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = text,
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(AccentViolet)
                .clickable { onRetryClick() }
                .padding(horizontal = 18.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Reintentar",
                color = TextPrimary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FilterBottomSheetContent(
    onClearClick: () -> Unit,
    onCloseClick: () -> Unit,
    selectedPriceRange: ClosedFloatingPointRange<Float>,
    maxPrice: Float,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    selectedGameFilter: GameFilter,
    onGameFilterSelected: (GameFilter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {


        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtros",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Limpiar",
                color = AccentViolet,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onClearClick() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "×",
                color = TextSecondary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.clickable { onCloseClick() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "POR SET",
            color = AccentGold,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        FilterChipGroup(
            options = GameFilters,
            selectedOption = selectedGameFilter,
            onOptionSelected = onGameFilterSelected
        )

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "RANGO DE PRECIO",
            color = AccentGold,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        androidx.compose.material3.RangeSlider(
            value = selectedPriceRange,
            onValueChange = onPriceRangeChange,
            valueRange = 0f..maxPrice,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "₡${selectedPriceRange.start.toLong()}",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "₡${selectedPriceRange.endInclusive.toLong()}",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun FilterChipGroup(
    options: List<GameFilter>,
    selectedOption: GameFilter,
    onOptionSelected: (GameFilter) -> Unit,
) {


    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.take(4).forEach { option ->
                FilterOptionChip(
                    text = option.label,
                    selected = selectedOption == option,
                    onClick = { onOptionSelected(option) }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.drop(4).forEach { option ->
                FilterOptionChip(
                    text = option.label,
                    selected = selectedOption == option,
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}
@Composable
private fun FilterOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(50))
            .background(if (selected) AccentViolet else BgSurface)
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) TextPrimary else TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(BgCard)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Spacer(modifier = Modifier.width(8.dp))

        androidx.compose.material3.TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = "Buscar carta, set o número...",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = TextPrimary
            ),
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedContainerColor = BgCard,
                unfocusedContainerColor = BgCard,
                disabledContainerColor = BgCard,
                focusedIndicatorColor = BgCard,
                unfocusedIndicatorColor = BgCard,
                cursorColor = AccentViolet
            ),
            modifier = Modifier.weight(1f)
        )
    }
}



@Composable
private fun SortChip(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(50))
            .background(BgSurface)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⇅",
            color = AccentViolet,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}



@Composable
private fun MarketplaceCardItem(
    card: GameCard,
    onClick: () -> Unit,
) {
    println("DEBUG_IMAGE: Card ${card.name} imageUrl = ${card.imageUrl}")
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.12f)
                .clip(RoundedCornerShape(8.dp))
                .background(BgSurface),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = card.imageUrl,

                contentDescription = card.name,
                contentScale = ContentScale.Crop,
                onSuccess = { println("DEBUG_IMAGE: Successfully loaded image for card ${card.name}") },
                onError = { println("DEBUG_IMAGE: Failed to load image for card ${card.name}. Error: ${it.result.throwable}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.12f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgSurface)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = card.name,
            color = TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = card.condition,
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₡${card.price}",
                color = AccentViolet,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "x${card.quantity}",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}







