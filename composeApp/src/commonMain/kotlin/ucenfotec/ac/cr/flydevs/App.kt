package ucenfotec.ac.cr.flydevs

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import ucenfotec.ac.cr.flydevs.data.repository.CardCatalogRepositoryImpl
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.CardCatalogViewModel
import ucenfotec.ac.cr.flydevs.presentation.screens.CardMarketplaceScreen
import ucenfotec.ac.cr.flydevs.presentation.theme.FlyAppTheme

@Composable
@Preview
fun App() {
    FlyAppTheme {
        val repository = remember {
            CardCatalogRepositoryImpl()
        }

        val viewModel = remember {
            CardCatalogViewModel(repository)
        }

        val uiState by viewModel.uiState.collectAsState()

        CardMarketplaceScreen(
            uiState = uiState
        )
    }
}