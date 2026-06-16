package kr.stonecold.securitycard.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.stonecold.securitycard.core.data.SecurityCardDao
import kr.stonecold.securitycard.core.data.SecurityCardEntity
import kr.stonecold.securitycard.core.security.CryptoManager
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val securityCardDao: SecurityCardDao,
    private val cryptoManager: CryptoManager
) : ViewModel() {

    private var currentId: Long? = null

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _cardNumber = MutableStateFlow("")
    val cardNumber: StateFlow<String> = _cardNumber.asStateFlow()

    private val _callCenter = MutableStateFlow("")
    val callCenter: StateFlow<String> = _callCenter.asStateFlow()

    private val _codeLength = MutableStateFlow(35)
    val codeLength: StateFlow<Int> = _codeLength.asStateFlow()

    private val _codes = MutableStateFlow<List<String>>(List(35) { "" })
    val codes: StateFlow<List<String>> = _codes.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun updateTitle(value: String) {
        _title.value = value
    }

    fun updateCardNumber(value: String) {
        _cardNumber.value = value
    }

    fun updateCallCenter(value: String) {
        _callCenter.value = value
    }

    fun updateCodeLength(length: Int) {
        _codeLength.value = length
        val currentCodes = _codes.value.toMutableList()
        if (length > currentCodes.size) {
            currentCodes.addAll(List(length - currentCodes.size) { "" })
        } else if (length < currentCodes.size) {
            currentCodes.subList(length, currentCodes.size).clear()
        }
        _codes.value = currentCodes
    }

    fun updateCode(index: Int, value: String) {
        if (index in _codes.value.indices) {
            val updated = _codes.value.toMutableList()
            updated[index] = value
            _codes.value = updated
        }
    }

    fun loadCard(id: Long) {
        viewModelScope.launch {
            val entity = securityCardDao.getById(id) ?: return@launch
            currentId = id
            _title.value = cryptoManager.decrypt(entity.name)
            _cardNumber.value = cryptoManager.decrypt(entity.cardNumber)
            _callCenter.value = entity.callCenter?.let { cryptoManager.decrypt(it) } ?: ""
            _codeLength.value = entity.rowCount
            
            val decryptedCellsJson = cryptoManager.decrypt(entity.encryptedCells)
            val decodedList = try {
                Json.decodeFromString<List<String>>(decryptedCellsJson)
            } catch (e: Exception) {
                List(entity.rowCount) { "" }
            }
            
            val currentCodes = _codes.value.toMutableList()
            for (i in decodedList.indices) {
                if (i < currentCodes.size) {
                    currentCodes[i] = decodedList[i]
                }
            }
            _codes.value = currentCodes
        }
    }

    fun saveCard() {
        viewModelScope.launch {
            val entity = SecurityCardEntity(
                id = currentId ?: 0L,
                name = cryptoManager.encrypt(_title.value),
                cardNumber = cryptoManager.encrypt(_cardNumber.value),
                callCenter = cryptoManager.encrypt(_callCenter.value),
                rowCount = _codeLength.value,
                columnCount = 1,
                codeLength = 4,
                orientation = "HORIZONTAL",
                encryptedCells = cryptoManager.encrypt(Json.encodeToString(_codes.value))
            )
            securityCardDao.insert(entity)
            _isSaved.value = true
        }
    }

    fun resetState() {
        currentId = null
        _title.value = ""
        _cardNumber.value = ""
        _callCenter.value = ""
        _codes.value = List(35) { "" }
        _isSaved.value = false
    }
}
