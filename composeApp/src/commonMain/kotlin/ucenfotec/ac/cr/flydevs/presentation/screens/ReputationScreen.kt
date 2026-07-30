package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.ReputationContent
import ucenfotec.ac.cr.flydevs.presentation.reputation.ReputationViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary

@Composable
fun ReputationScreen(
    userId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReputationViewModel =
        koinViewModel()
) {
    val state by
    viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadReputation(userId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = TextPrimary
                )
            }

            Text(
                text = "Reputación",
                color = TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                )
        ) {
            ReputationContent(
                state = state,
                onRoleSelected =
                    viewModel::selectRole,
                onLoadMore =
                    viewModel::loadMoreReviews,
                onRetry =
                    viewModel::retry,
                showIdentity = true
            )
        }
    }
}