package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.FormField
import ucenfotec.ac.cr.flydevs.presentation.components.PrimaryButton
import ucenfotec.ac.cr.flydevs.presentation.login.LoginViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onCompleteProfileRequired: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val passwordState = remember { TextFieldState() }
    var email by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoginSuccessful, uiState.needsProfileCompletion) {
        if (uiState.isLoginSuccessful) {
            onLoginSuccess()
        } else if (uiState.needsProfileCompletion) {
            onCompleteProfileRequired()
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

        // Logo Spade representation
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AccentViolet),
            contentAlignment = Alignment.Center
        ) {
            Text("♠", color = Color.White, fontSize = 36.sp)
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Inicia sesión",
            style = Typography.titleLarge,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Una sola cuenta para comprar, vender, entregar o gestionar.",
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
            FormField("Correo electrónico") {
                LoginTextField(
                    value = email,
                    placeholder = "andres@correo.com",
                    onValueChange = { email = it },
                    leadingSymbol = "✉",
                    enabled = !uiState.isLoading
                )
            }

            FormField("Contraseña") {
                LoginPasswordTextField(passwordState)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentViolet,
                        uncheckedColor = TextMuted
                    )
                )
                Text("Recordarme", color = TextSecondary, fontSize = 14.sp)
            }
            Text(
                "¿Olvidaste tu contraseña?",
                color = AccentVioletLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* Forgot Password */ }
            )
        }

        Spacer(Modifier.height(32.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(color = AccentViolet)
        } else {
            PrimaryButton(
                text = "Iniciar sesión",
                onClick = { viewModel.login(email, passwordState.text.toString()) }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Divider "o"
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(Modifier.weight(1f), color = TextMuted.copy(alpha = 0.2f))
            Text(
                text = "o",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = TextMuted,
                fontSize = 14.sp
            )
            HorizontalDivider(Modifier.weight(1f), color = TextMuted.copy(alpha = 0.2f))
        }

        Spacer(Modifier.height(24.dp))

        // Google Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(BgCard)
                .clickable { viewModel.onGoogleSignInClicked() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", color = BgDarkest, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Continuar con Google",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(36.dp))

        Row {
            Text("¿No tienes cuenta? ", color = TextSecondary, fontSize = 14.sp)
            Text(
                text = "Registrate",
                color = AccentVioletLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun LoginTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    leadingSymbol: String? = null,
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
        textStyle = TextStyle(color = if (enabled) TextPrimary else TextMuted, fontSize = 14.sp),
        cursorBrush = SolidColor(AccentViolet),
        singleLine = true,
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingSymbol != null) {
                    Text(leadingSymbol, color = TextMuted, fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                }
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = TextMuted, fontSize = 14.sp)
                    }
                    inner()
                }
            }
        }
    )
}

@Composable
private fun LoginPasswordTextField(state: TextFieldState) {
    var showPassword by remember { mutableStateOf(false) }
    androidx.compose.foundation.text.BasicSecureTextField(
        state = state,
        textObfuscationMode =
            if (showPassword) {
                androidx.compose.foundation.text.input.TextObfuscationMode.Visible
            } else {
                androidx.compose.foundation.text.input.TextObfuscationMode.RevealLastTyped
            },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard),
        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
        cursorBrush = SolidColor(AccentViolet),
        decorator = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔒", color = TextMuted, fontSize = 18.sp)
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (state.text.isEmpty()) {
                        Text("********", color = TextMuted, fontSize = 14.sp)
                    }
                    innerTextField()
                }
                Spacer(Modifier.width(12.dp))
                Icon(
                    imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = "Toggle password visibility",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { showPassword = !showPassword }
                )
            }
        }
    )
}
