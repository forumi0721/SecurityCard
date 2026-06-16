package kr.stonecold.securitycard.ui.view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kr.stonecold.securitycard.core.data.SecurityCardDao
import kr.stonecold.securitycard.core.data.SecurityCardEntity
import kr.stonecold.securitycard.core.data.AccountInfo
import kr.stonecold.securitycard.core.security.CryptoManager
import javax.inject.Inject

data class ViewUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val cardNumber: String = "",
    val callCenter: String = "",
    val codes: List<String> = emptyList(),
    val accounts: List<AccountInfo> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ViewViewModel @Inject constructor(
    private val securityCardDao: SecurityCardDao,
    private val cryptoManager: CryptoManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewUiState(isLoading = true))
    val uiState: StateFlow<ViewUiState> = _uiState.asStateFlow()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    private var currentEntity: SecurityCardEntity? = null

    fun loadCard(id: Long) {
        _isDeleted.value = false
        _uiState.value = ViewUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val entity = securityCardDao.getById(id)
                if (entity != null) {
                    currentEntity = entity
                    val decryptedName = cryptoManager.decrypt(entity.name)
                    val decryptedNumber = cryptoManager.decrypt(entity.cardNumber)
                    val decryptedCallCenter = entity.callCenter?.let { cryptoManager.decrypt(it) } ?: ""
                    
                    val decryptedCellsJson = cryptoManager.decrypt(entity.encryptedCells)
                    val decodedList = try {
                        Json.decodeFromString<List<String>>(decryptedCellsJson)
                    } catch (e: Exception) {
                        List(entity.rowCount) { "" }
                    }

                    val decryptedAccountsJson = cryptoManager.decrypt(entity.encryptedAccounts)
                    val decodedAccounts = try {
                        if (decryptedAccountsJson.isNotEmpty()) {
                            Json.decodeFromString<List<AccountInfo>>(decryptedAccountsJson)
                        } else {
                            emptyList()
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }

                    _uiState.value = ViewUiState(
                        isLoading = false,
                        name = decryptedName,
                        cardNumber = decryptedNumber,
                        callCenter = decryptedCallCenter,
                        codes = decodedList,
                        accounts = decodedAccounts
                    )
                } else {
                    _uiState.value = ViewUiState(isLoading = false, error = "카드를 찾을 수 없습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = ViewUiState(isLoading = false, error = "데이터를 불러오는 중 오류가 발생했습니다.")
            }
        }
    }

    fun deleteCard() {
        viewModelScope.launch {
            currentEntity?.let {
                securityCardDao.delete(it)
                _isDeleted.value = true
            }
        }
    }

    fun resetDeletedState() {
        _isDeleted.value = false
    }
}
