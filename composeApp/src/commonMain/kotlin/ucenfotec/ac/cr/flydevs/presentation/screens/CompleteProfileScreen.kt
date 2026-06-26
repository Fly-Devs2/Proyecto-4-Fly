package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.FormField
import ucenfotec.ac.cr.flydevs.presentation.components.PrimaryButton
import ucenfotec.ac.cr.flydevs.presentation.register.RegisterViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Preview
@Composable
fun CompleteProfileScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    onSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDarkest)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AccentViolet),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 36.sp)
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Casi listo",
            style = Typography.titleLarge,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Completa tu información para continuar",
            style = Typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = AccentRed,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FormField("Nombre completo") {
                SimpleTextField(
                    value = name,
                    placeholder = "Tu nombre",
                    onValueChange = { name = it },
                    enabled = !uiState.isLoading
                )
            }

            FormField("Teléfono (SINPE)") {
                CRPhoneNumberField(
                    value = phone,
                    onValueChange = { if (it.length <= 8 && it.all { char -> char.isDigit() }) phone = it },
                    enabled = !uiState.isLoading
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(color = AccentViolet)
        } else {
            PrimaryButton(
                text = "Finalizar",
                onClick = { viewModel.completeProfile(name, phone) }
            )
        }
    }
}

@Composable
private fun SimpleTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) BgCard else BgCard.copy(alpha = 0.5f)),
        textStyle = androidx.compose.ui.text.TextStyle(color = if (enabled) TextPrimary else TextMuted, fontSize = 14.sp),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(AccentViolet),
        singleLine = true,
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, color = TextMuted, fontSize = 14.sp)
                }
                inner()
            }
        }
    )
}
