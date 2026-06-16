package kr.stonecold.securitycard.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val pinInput by viewModel.pinInput.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState(initial = false)

    val context = LocalContext.current
    
    LaunchedEffect(loginState, biometricEnabled) {
        if (loginState == LoginState.ENTER_PIN && biometricEnabled) {
            val fragmentActivity = context as? FragmentActivity
            if (fragmentActivity != null) {
                val executor = ContextCompat.getMainExecutor(fragmentActivity)
                val biometricPrompt = BiometricPrompt(
                    fragmentActivity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {}
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            viewModel.loginSuccess()
                        }
                        override fun onAuthenticationFailed() {}
                    }
                )
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("생체 인증")
                    .setSubtitle("보안카드 앱에 로그인합니다")
                    .setNegativeButtonText("취소")
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }
        }
    }

    LaunchedEffect(loginState) {
        if (loginState == LoginState.SUCCESS) {
            onLoginSuccess()
        }
    }

    if (loginState == LoginState.LOADING || loginState == LoginState.SUCCESS) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val title = when (loginState) {
            LoginState.SETUP_PIN -> "앱 시작을 위해\nPIN 4자리를 설정해주세요."
            LoginState.CONFIRM_PIN -> "설정한 PIN 4자리를\n한번 더 입력해주세요."
            else -> "PIN 4자리를 입력해주세요."
        }

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(48.dp))

        // PIN Indicators (4 dots)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            for (i in 0 until 4) {
                val isFilled = i < pinInput.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMsg != null) {
            Text(
                text = errorMsg ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        } else {
            Spacer(modifier = Modifier.height(20.dp)) // Maintain space
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Number Pad
        NumberPad(
            onNumberClick = viewModel::onNumberClick,
            onDeleteClick = viewModel::onDeleteClick,
            onOkClick = viewModel::onOkClick
        )
    }
}

@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    onOkClick: () -> Unit
) {
    val buttonModifier = Modifier
        .size(80.dp)
        .padding(4.dp)
    
    val textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PadButton("1", buttonModifier, textStyle) { onNumberClick(1) }
            PadButton("2", buttonModifier, textStyle) { onNumberClick(2) }
            PadButton("3", buttonModifier, textStyle) { onNumberClick(3) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PadButton("4", buttonModifier, textStyle) { onNumberClick(4) }
            PadButton("5", buttonModifier, textStyle) { onNumberClick(5) }
            PadButton("6", buttonModifier, textStyle) { onNumberClick(6) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PadButton("7", buttonModifier, textStyle) { onNumberClick(7) }
            PadButton("8", buttonModifier, textStyle) { onNumberClick(8) }
            PadButton("9", buttonModifier, textStyle) { onNumberClick(9) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PadButton("OK", buttonModifier, textStyle, onClick = onOkClick)
            PadButton("0", buttonModifier, textStyle) { onNumberClick(0) }
            PadButton("Del", buttonModifier, textStyle, onClick = onDeleteClick)
        }
    }
}

@Composable
fun PadButton(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, style = textStyle)
    }
}
