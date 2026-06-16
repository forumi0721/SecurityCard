package kr.stonecold.securitycard

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data object Main : NavKey
@Serializable data class Edit(val id: Long? = null) : NavKey
@Serializable data class View(val id: Long) : NavKey
@Serializable data object Settings : NavKey
