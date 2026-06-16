package kr.stonecold.securitycard.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kr.stonecold.securitycard.core.data.AppSettingsRepository
import kr.stonecold.securitycard.core.security.CryptoManager
import javax.inject.Inject

enum class LoginState {
    LOADING,
    SETUP_PIN,
    CONFIRM_PIN,
    ENTER_PIN,
    SUCCESS
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val cryptoManager: CryptoManager
) : ViewModel() {

    val biometricEnabled = appSettingsRepository.biometricEnabled

    private val _loginState = MutableStateFlow(LoginState.LOADING)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private var tempPin = ""

    init {
        checkPinStatus()
    }

    private fun checkPinStatus() {
        viewModelScope.launch {
            val isSet = appSettingsRepository.pinHash.map { it != null }.first()
            if (isSet) {
                _loginState.value = LoginState.ENTER_PIN
            } else {
                _loginState.value = LoginState.SETUP_PIN
            }
        }
    }

    fun onNumberClick(number: Int) {
        if (_pinInput.value.length < 4) {
            _pinInput.value += number.toString()
            _errorMsg.value = null
        }
    }

    fun onDeleteClick() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
            _errorMsg.value = null
        }
    }

    fun onOkClick() {
        val currentInput = _pinInput.value
        if (currentInput.length != 4) {
            _errorMsg.value = "PIN은 4자리여야 합니다."
            return
        }

        viewModelScope.launch {
            when (_loginState.value) {
                LoginState.SETUP_PIN -> {
                    tempPin = currentInput
                    _pinInput.value = ""
                    _loginState.value = LoginState.CONFIRM_PIN
                }
                LoginState.CONFIRM_PIN -> {
                    if (currentInput == tempPin) {
                        val salt = cryptoManager.generateSalt()
                        val hash = cryptoManager.hashPin(currentInput, salt)
                        appSettingsRepository.setPin(hash, salt)
                        _loginState.value = LoginState.SUCCESS
                    } else {
                        _errorMsg.value = "PIN이 일치하지 않습니다. 다시 설정해주세요."
                        _pinInput.value = ""
                        _loginState.value = LoginState.SETUP_PIN
                        tempPin = ""
                    }
                }
                LoginState.ENTER_PIN -> {
                    val savedHash = appSettingsRepository.pinHash.first()
                    val savedSalt = appSettingsRepository.pinSalt.first()
                    
                    if (savedHash != null && savedSalt != null) {
                        val inputHash = cryptoManager.hashPin(currentInput, savedSalt)
                        if (inputHash == savedHash) {
                            _loginState.value = LoginState.SUCCESS
                            return@launch
                        }
                    }
                    _errorMsg.value = "PIN이 일치하지 않습니다."
                    _pinInput.value = ""
                }
                else -> {}
            }
        }
    }

    fun loginSuccess() {
        _loginState.value = LoginState.SUCCESS
    }
}
