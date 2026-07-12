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
        private const val KEY_AUTO_RETRY_FAILED = "auto_retry_failed"
        private const val KEY_HIDDEN_COMPLETED_IDS = "hidden_completed_ids"
        private const val KEY_NOTIFY_COMPLETION = "notify_download_completion"
        private const val KEY_NOTIFY_FAILURE = "notify_download_failure"
        private const val KEY_VIBRATE = "notification_vibrate"
        private const val KEY_LIGHT = "notification_light"
        private const val KEY_COMPLETION_RINGTONE = "completion_ringtone"
        private const val KEY_FAILURE_RINGTONE = "failure_ringtone"
        private const val KEY_PARALLEL_DOWNLOAD = "parallel_download"
        private const val KEY_AUTO_FETCH_URL = "auto_fetch_url"
        private const val KEY_ASK_DOWNLOAD_FOLDER = "ask_download_folder"
        private const val DEFAULT_VALUE = false
        private const val DEFAULT_PARALLEL_LIMIT = 3
        private const val DEFAULT_RINGTONE = "DEFAULT"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _autoRemoveCompleted = MutableStateFlow(prefs.getBoolean(KEY_AUTO_REMOVE_COMPLETED, DEFAULT_VALUE))
    private val autoRemoveCompleted = _autoRemoveCompleted.asStateFlow()

    private val _autoRetryFailed = MutableStateFlow(prefs.getBoolean(KEY_AUTO_RETRY_FAILED, DEFAULT_VALUE))
    private val autoRetryFailed = _autoRetryFailed.asStateFlow()

    private fun readHiddenIds(): Set<String> {
        val s = prefs.getString(KEY_HIDDEN_COMPLETED_IDS, "") ?: ""
        return if (s.isBlank()) emptySet() else s.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    private val _hiddenCompletedIds = MutableStateFlow(readHiddenIds())
    private val hiddenCompletedIds = _hiddenCompletedIds.asStateFlow()

    private val _notifyCompletion = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY_COMPLETION, true))
    private val notifyCompletion = _notifyCompletion.asStateFlow()

    private val _notifyFailure = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY_FAILURE, true))
    private val notifyFailure = _notifyFailure.asStateFlow()

    private val _vibrate = MutableStateFlow(prefs.getBoolean(KEY_VIBRATE, true))
    private val vibrate = _vibrate.asStateFlow()

    private val _light = MutableStateFlow(prefs.getBoolean(KEY_LIGHT, true))
    private val light = _light.asStateFlow()

    private val _completionRingtone = MutableStateFlow(prefs.getString(KEY_COMPLETION_RINGTONE, DEFAULT_RINGTONE) ?: DEFAULT_RINGTONE).also {
        android.util.Log.d("SettingsRepository", "Init: _completionRingtone = ${it.value}")
    }
    private val completionRingtone = _completionRingtone.asStateFlow()

    private val _failureRingtone = MutableStateFlow(prefs.getString(KEY_FAILURE_RINGTONE, DEFAULT_RINGTONE) ?: DEFAULT_RINGTONE).also {
        android.util.Log.d("SettingsRepository", "Init: _failureRingtone = ${it.value}")
    }
    private val failureRingtone = _failureRingtone.asStateFlow()

    private val _parallelDownloadLimit = MutableStateFlow(prefs.getInt(KEY_PARALLEL_DOWNLOAD, DEFAULT_PARALLEL_LIMIT))
    private val parallelDownloadLimit = _parallelDownloadLimit.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        when (key) {
            KEY_AUTO_REMOVE_COMPLETED -> _autoRemoveCompleted.value = sp.getBoolean(KEY_AUTO_REMOVE_COMPLETED, DEFAULT_VALUE)
            KEY_AUTO_RETRY_FAILED -> _autoRetryFailed.value = sp.getBoolean(KEY_AUTO_RETRY_FAILED, DEFAULT_VALUE)
            KEY_HIDDEN_COMPLETED_IDS -> _hiddenCompletedIds.value = readHiddenIds()
            KEY_NOTIFY_COMPLETION -> _notifyCompletion.value = sp.getBoolean(KEY_NOTIFY_COMPLETION, true)
            KEY_NOTIFY_FAILURE -> _notifyFailure.value = sp.getBoolean(KEY_NOTIFY_FAILURE, true)
            KEY_VIBRATE -> _vibrate.value = sp.getBoolean(KEY_VIBRATE, true)
            KEY_LIGHT -> _light.value = sp.getBoolean(KEY_LIGHT, true)
            KEY_COMPLETION_RINGTONE -> {
                val newValue = sp.getString(KEY_COMPLETION_RINGTONE, DEFAULT_RINGTONE) ?: DEFAULT_RINGTONE
                android.util.Log.d("SettingsRepository", "Listener triggered: KEY_COMPLETION_RINGTONE = $newValue")
                _completionRingtone.value = newValue
            }
            KEY_FAILURE_RINGTONE -> {
                val newValue = sp.getString(KEY_FAILURE_RINGTONE, DEFAULT_RINGTONE) ?: DEFAULT_RINGTONE
                android.util.Log.d("SettingsRepository", "Listener triggered: KEY_FAILURE_RINGTONE = $newValue")
                _failureRingtone.value = newValue
            }
            KEY_PARALLEL_DOWNLOAD -> _parallelDownloadLimit.value = sp.getInt(KEY_PARALLEL_DOWNLOAD, DEFAULT_PARALLEL_LIMIT)
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
        android.util.Log.d("SettingsRepository", "Init: SharedPreference listener registered")
    }

    override fun observeAutoRemoveCompleted(): Flow<Boolean> = autoRemoveCompleted
    override fun observeAutoRetryFailed(): Flow<Boolean> = autoRetryFailed
    override fun observeHiddenCompletedIds(): Flow<Set<String>> = hiddenCompletedIds

    override fun addHiddenCompletedIds(ids: Set<String>) {
        val current = readHiddenIds().toMutableSet()
        current.addAll(ids)
        val joined = current.joinToString(",")
        prefs.edit().putString(KEY_HIDDEN_COMPLETED_IDS, joined).apply()
        // update flow immediately
        _hiddenCompletedIds.value = current
    }

    override fun observeNotifyDownloadCompletion(): Flow<Boolean> = notifyCompletion
    override fun observeNotifyDownloadFailure(): Flow<Boolean> = notifyFailure
    override fun observeVibrate(): Flow<Boolean> = vibrate
    override fun observeLight(): Flow<Boolean> = light
    override fun observeCompletionRingtone(): Flow<String> = completionRingtone
    override fun observeFailureRingtone(): Flow<String> = failureRingtone

    override fun getParallelDownloadLimit(): Int = prefs.getInt(KEY_PARALLEL_DOWNLOAD, DEFAULT_PARALLEL_LIMIT)
    override fun observeParallelDownloadLimit(): Flow<Int> = parallelDownloadLimit

    override fun isAutoFetchUrlEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_FETCH_URL, true)
    override fun isAskDownloadFolderEnabled(): Boolean = prefs.getBoolean(KEY_ASK_DOWNLOAD_FOLDER, false)
}
