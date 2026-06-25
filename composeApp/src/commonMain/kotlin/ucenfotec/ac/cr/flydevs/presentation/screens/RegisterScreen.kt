package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.FormField
import ucenfotec.ac.cr.flydevs.presentation.components.PrimaryButton
import ucenfotec.ac.cr.flydevs.presentation.register.RegisterViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    onRegisterSuccess: () -> Unit = {},
    onCompleteProfileRequired: () -> Unit = {},
    onLoginClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val passwordState = remember { TextFieldState() }
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }


    LaunchedEffect(uiState.isRegistered, uiState.needsProfileCompletion) {
        if (uiState.isRegistered) {
            onRegisterSuccess()
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
            text = "Crear cuenta",
            style = Typography.titleLarge,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Únete al marketplace de cartas de Costa Rica",
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
                RegisterTextField(
                    value = name,
                    placeholder = "Andrés Torres",
                    onValueChange = { name = it },
                    leadingSymbol = "👤",
                    enabled = !uiState.isLoading
                )
            }

            FormField("Correo electrónico") {
                RegisterTextField(
                    value = email,
                    placeholder = "andres@correo.com",
                    onValueChange = { email = it },
                    leadingSymbol = "✉",
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

            FormField("Contraseña") {
                PasswordTextField(passwordState)
            }
        }

        Spacer(Modifier.height(32.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(color = AccentViolet)
        } else {
            PrimaryButton(
                text = "Crear cuenta",
                onClick = { viewModel.register(name, email, phone, passwordState.text.toString()) }
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
            Text("¿Ya tienes cuenta? ", color = TextSecondary, fontSize = 14.sp)
            Text(
                text = "Inicia sesión",
                color = AccentVioletLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun CRPhoneNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(8) { index ->
                    val char = value.getOrNull(index)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char?.toString() ?: "-",
                            color = if (char != null) TextPrimary else TextMuted,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun RegisterTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    leadingSymbol: String? = null,
    trailingSymbol: String? = null,
    enabled: Boolean = true,
) {
    BasicTextField(
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
                if (trailingSymbol != null) {
                    Spacer(Modifier.width(12.dp))
                    Text(trailingSymbol, color = TextMuted, fontSize = 18.sp)
                }
            }
        }
    )
}

@Composable
fun PasswordTextField(state: TextFieldState) {
    var showPassword by remember { mutableStateOf(false) }
    BasicSecureTextField(
        state = state,
        textObfuscationMode =
            if (showPassword) {
                TextObfuscationMode.Visible
            } else {
                TextObfuscationMode.RevealLastTyped
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
                        Text("Mínimo 12 caracteres", color = TextMuted, fontSize = 14.sp)
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
