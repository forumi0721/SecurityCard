package kr.stonecold.securitycard.ui.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.stonecold.securitycard.core.data.SecurityCardDao
import kr.stonecold.securitycard.core.data.SecurityCardEntity
import kr.stonecold.securitycard.core.security.CryptoManager
import javax.inject.Inject

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState
    data class Error(val throwable: Throwable) : MainScreenUiState
    data class Success(val data: List<SecurityCardEntity>) : MainScreenUiState
}

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val securityCardDao: SecurityCardDao,
    private val cryptoManager: CryptoManager
) : ViewModel() {
    val uiState: StateFlow<MainScreenUiState> =
        securityCardDao.getAll()
            .map<List<SecurityCardEntity>, MainScreenUiState> { list -> 
                val decryptedList = list.map { it.copy(name = cryptoManager.decrypt(it.name)) }
                MainScreenUiState.Success(decryptedList) 
            }
            .catch { emit(MainScreenUiState.Error(it)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Loading)

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val list = securityCardDao.getAll().first() // get all items
                val json = Json.encodeToString(list)
                val encryptedJson = cryptoManager.encrypt(json)
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(encryptedJson.toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val encryptedJson = context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(Charsets.UTF_8)
                }
                if (encryptedJson != null) {
                    val json = cryptoManager.decrypt(encryptedJson)
                    val list = Json.decodeFromString<List<SecurityCardEntity>>(json)
                    for (item in list) {
                        securityCardDao.insert(item.copy(id = 0)) // Insert as new
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
