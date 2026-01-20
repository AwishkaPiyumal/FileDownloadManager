package com.piumal.filedownloadmanager.data.settings

import android.content.Context
import android.content.SharedPreferences
import com.piumal.filedownloadmanager.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SettingsRepository implementation using SharedPreferences.
 * Observes the "auto_remove_completed" key and exposes it as a Flow.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_AUTO_REMOVE_COMPLETED = "auto_remove_completed"
        private const val DEFAULT_VALUE = false
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _autoRemoveCompleted = MutableStateFlow(prefs.getBoolean(KEY_AUTO_REMOVE_COMPLETED, DEFAULT_VALUE))
    private val autoRemoveCompleted = _autoRemoveCompleted.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        if (key == KEY_AUTO_REMOVE_COMPLETED) {
            _autoRemoveCompleted.value = sp.getBoolean(KEY_AUTO_REMOVE_COMPLETED, DEFAULT_VALUE)
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun observeAutoRemoveCompleted(): Flow<Boolean> = autoRemoveCompleted
}
