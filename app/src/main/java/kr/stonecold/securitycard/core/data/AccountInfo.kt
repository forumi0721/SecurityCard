package kr.stonecold.securitycard.core.data

import kotlinx.serialization.Serializable

@Serializable
data class AccountInfo(
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String
)
