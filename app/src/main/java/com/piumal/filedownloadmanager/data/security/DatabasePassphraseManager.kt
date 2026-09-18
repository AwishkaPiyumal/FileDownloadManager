package com.piumal.filedownloadmanager.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Securely creates and stores the SQLCipher passphrase in EncryptedSharedPreferences.
 */
object DatabasePassphraseManager {

    private const val PREFS_NAME = "secure_database_keys"
    private const val PASSPHRASE_KEY = "sqlcipher_database_passphrase"
    private const val PASSPHRASE_SIZE_BYTES = 32

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val preferences = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val storedPassphrase = preferences.getString(PASSPHRASE_KEY, null)
        if (storedPassphrase != null) {
            return Base64.decode(storedPassphrase, Base64.NO_WRAP)
        }

        val passphrase = ByteArray(PASSPHRASE_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        preferences.edit()
            .putString(PASSPHRASE_KEY, Base64.encodeToString(passphrase, Base64.NO_WRAP))
            .apply()

        return passphrase
    }
}
