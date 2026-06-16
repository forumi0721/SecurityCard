package kr.stonecold.securitycard.core.data

import kotlinx.serialization.Serializable

@Serializable
data class PlainSecurityCard(
    val name: String,
    val cardNumber: String,
    val callCenter: String?,
    val rowCount: Int,
    val columnCount: Int,
    val codeLength: Int,
    val orientation: String,
    val codes: List<String>,
    val accounts: List<AccountInfo> = emptyList(),
    val displayOrder: Int = 0
)
