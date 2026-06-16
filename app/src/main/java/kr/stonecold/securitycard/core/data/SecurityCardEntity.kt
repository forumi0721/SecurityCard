package kr.stonecold.securitycard.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "security_cards")
data class SecurityCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Encrypted
    val cardNumber: String, // Encrypted
    val callCenter: String?, // Encrypted
    val rowCount: Int,
    val columnCount: Int,
    val codeLength: Int,
    val orientation: String, // HORIZONTAL or VERTICAL
    val encryptedCells: String, // Encrypted JSON
    val encryptedAccounts: String = "", // Encrypted JSON of List<AccountInfo>
    val readCount: Int = 0,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastReadAt: Long = System.currentTimeMillis()
)
