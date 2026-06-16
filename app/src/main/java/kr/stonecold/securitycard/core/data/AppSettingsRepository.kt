package kr.stonecold.securitycard.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val pinHash: Flow<String?> = dataStore.data.map { it[PIN_HASH] }
    val pinSalt: Flow<String?> = dataStore.data.map { it[PIN_SALT] }
    val biometricEnabled: Flow<Boolean> = dataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }
    val autoLogoutMinutes: Flow<Int> = dataStore.data.map { it[AUTO_LOGOUT_MINUTES] ?: 0 }
    val inputDelaySeconds: Flow<Int> = dataStore.data.map { it[INPUT_DELAY_SECONDS] ?: 0 }
    val notifyEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFY_ENABLED] ?: true }
    val sortBy: Flow<Int> = dataStore.data.map { it[SORT_BY] ?: 0 }
    val screenshotBlocked: Flow<Boolean> = dataStore.data.map { it[SCREENSHOT_BLOCKED] ?: true }
    val reAuthOnResume: Flow<Boolean> = dataStore.data.map { it[REAUTH_ON_RESUME] ?: true }
    val quitConfirm: Flow<Boolean> = dataStore.data.map { it[QUIT_CONFIRM] ?: true }

    suspend fun setPin(hash: String, salt: String) {
        dataStore.edit { prefs ->
            prefs[PIN_HASH] = hash
            prefs[PIN_SALT] = salt
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setAutoLogoutMinutes(minutes: Int) {
        dataStore.edit { it[AUTO_LOGOUT_MINUTES] = minutes }
    }

    suspend fun setInputDelaySeconds(seconds: Int) {
        dataStore.edit { it[INPUT_DELAY_SECONDS] = seconds }
    }

    suspend fun setNotifyEnabled(enabled: Boolean) {
        dataStore.edit { it[NOTIFY_ENABLED] = enabled }
    }

    suspend fun setSortBy(sortType: Int) {
        dataStore.edit { it[SORT_BY] = sortType }
    }

    suspend fun setScreenshotBlocked(blocked: Boolean) {
        dataStore.edit { it[SCREENSHOT_BLOCKED] = blocked }
    }

    suspend fun setReAuthOnResume(reauth: Boolean) {
        dataStore.edit { it[REAUTH_ON_RESUME] = reauth }
    }

    suspend fun setQuitConfirm(confirm: Boolean) {
        dataStore.edit { it[QUIT_CONFIRM] = confirm }
    }

    companion object {
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AUTO_LOGOUT_MINUTES = intPreferencesKey("auto_logout_minutes")
        val INPUT_DELAY_SECONDS = intPreferencesKey("input_delay_seconds")
        val NOTIFY_ENABLED = booleanPreferencesKey("notify_enabled")
        val SORT_BY = intPreferencesKey("sort_by")
        val SCREENSHOT_BLOCKED = booleanPreferencesKey("screenshot_blocked")
        val REAUTH_ON_RESUME = booleanPreferencesKey("reauth_on_resume")
        val QUIT_CONFIRM = booleanPreferencesKey("quit_confirm")
    }
}
