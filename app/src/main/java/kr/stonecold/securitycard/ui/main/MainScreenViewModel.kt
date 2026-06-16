package kr.stonecold.securitycard.ui.main

import android.content.Context
import android.net.Uri
import android.widget.Toast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.stonecold.securitycard.core.data.SecurityCardDao
import kr.stonecold.securitycard.core.data.SecurityCardEntity
import kr.stonecold.securitycard.core.data.PlainSecurityCard
import kr.stonecold.securitycard.core.data.AccountInfo
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
                val list = securityCardDao.getAll().first()
                val plainList = list.map { entity ->
                    val decryptedCellsJson = cryptoManager.decrypt(entity.encryptedCells)
                    val decodedCodes = try {
                        Json.decodeFromString<List<String>>(decryptedCellsJson)
                    } catch (e: Exception) {
                        List(entity.rowCount) { "" }
                    }
                    val decryptedAccountsJson = cryptoManager.decrypt(entity.encryptedAccounts)
                    val decodedAccounts = try {
                        if (decryptedAccountsJson.isNotEmpty()) {
                            Json.decodeFromString<List<AccountInfo>>(decryptedAccountsJson)
                        } else emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    PlainSecurityCard(
                        name = cryptoManager.decrypt(entity.name),
                        cardNumber = cryptoManager.decrypt(entity.cardNumber),
                        callCenter = entity.callCenter?.let { cryptoManager.decrypt(it) },
                        rowCount = entity.rowCount,
                        columnCount = entity.columnCount,
                        codeLength = entity.codeLength,
                        orientation = entity.orientation,
                        codes = decodedCodes,
                        accounts = decodedAccounts,
                        displayOrder = entity.displayOrder
                    )
                }
                val plainJson = Json.encodeToString(plainList)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write(plainJson.toByteArray(Charsets.UTF_8))
                    }
                }
                Toast.makeText(context, "데이터를 성공적으로 내보냈습니다.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "내보내기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val plainJson = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use {
                        it.readBytes().toString(Charsets.UTF_8)
                    }
                }
                if (plainJson != null) {
                    val list = try {
                        Json.decodeFromString<List<PlainSecurityCard>>(plainJson)
                    } catch (e: Exception) {
                        // Fallback for encrypted legacy format if needed
                        val json = cryptoManager.decrypt(plainJson)
                        val legacyList = Json.decodeFromString<List<SecurityCardEntity>>(json)
                        legacyList.map { entity ->
                            val decryptedCellsJson = cryptoManager.decrypt(entity.encryptedCells)
                            val decodedCodes = try {
                                Json.decodeFromString<List<String>>(decryptedCellsJson)
                            } catch (ex: Exception) {
                                List(entity.rowCount) { "" }
                            }
                            val decryptedAccountsJson = cryptoManager.decrypt(entity.encryptedAccounts)
                            val decodedAccounts = try {
                                if (decryptedAccountsJson.isNotEmpty()) {
                                    Json.decodeFromString<List<AccountInfo>>(decryptedAccountsJson)
                                } else emptyList()
                            } catch (e: Exception) {
                                emptyList()
                            }
                            PlainSecurityCard(
                                name = cryptoManager.decrypt(entity.name),
                                cardNumber = cryptoManager.decrypt(entity.cardNumber),
                                callCenter = entity.callCenter?.let { cryptoManager.decrypt(it) },
                                rowCount = entity.rowCount,
                                columnCount = entity.columnCount,
                                codeLength = entity.codeLength,
                                orientation = entity.orientation,
                                codes = decodedCodes,
                                accounts = decodedAccounts,
                                displayOrder = entity.displayOrder
                            )
                        }
                    }
                    for (item in list) {
                        val encryptedCells = cryptoManager.encrypt(Json.encodeToString(item.codes))
                        val encryptedAccounts = cryptoManager.encrypt(Json.encodeToString(item.accounts))
                        val entity = SecurityCardEntity(
                            id = 0,
                            name = cryptoManager.encrypt(item.name),
                            cardNumber = cryptoManager.encrypt(item.cardNumber),
                            callCenter = item.callCenter?.let { cryptoManager.encrypt(it) },
                            rowCount = item.rowCount,
                            columnCount = item.columnCount,
                            codeLength = item.codeLength,
                            orientation = item.orientation,
                            encryptedCells = encryptedCells,
                            encryptedAccounts = encryptedAccounts,
                            displayOrder = item.displayOrder
                        )
                        securityCardDao.insert(entity)
                    }
                    Toast.makeText(context, "데이터를 성공적으로 가져왔습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "파일을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "가져오기 실패: 올바른 파일이 아닙니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveOrder(cards: List<SecurityCardEntity>) {
        viewModelScope.launch {
            val updatedCards = cards.mapIndexed { index, card ->
                card.copy(displayOrder = index)
            }
            securityCardDao.updateAll(updatedCards)
        }
    }
}
