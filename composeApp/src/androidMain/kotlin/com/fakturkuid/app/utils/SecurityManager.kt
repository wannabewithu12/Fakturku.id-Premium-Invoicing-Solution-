package com.fakturkuid.app.utils

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Centralized security layer for Fakturku.id.
 *
 * Responsibilities:
 *  - Generate and persist a cryptographically strong database passphrase
 *    using Android Keystore (AES256-GCM) via EncryptedSharedPreferences.
 *  - The passphrase is generated ONCE on first launch and never leaves
 *    the device's secure storage.
 *
 * Portfolio value: demonstrates understanding of Android Keystore system,
 * hardware-backed key protection, and defense-in-depth principles.
 */
object SecurityManager {

    private const val PREFS_FILENAME = "fakturku_secure_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"
    private const val PASSPHRASE_BYTES = 32 // 256-bit passphrase

    /**
     * Retrieves the database passphrase from EncryptedSharedPreferences.
     * If none exists, generates a new cryptographically random passphrase
     * and stores it securely using Android Keystore.
     *
     * The master key is backed by hardware security module (HSM) on
     * supported devices, making extraction virtually impossible.
     */
    fun getOrCreateDatabasePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false) // App-level auth, not per-key
            .build()

        val encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val existing = encryptedPrefs.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) {
            return Base64.decode(existing, Base64.DEFAULT)
        }

        // First launch: generate a strong random passphrase
        val passphrase = ByteArray(PASSPHRASE_BYTES)
        SecureRandom().nextBytes(passphrase)

        encryptedPrefs.edit()
            .putString(KEY_DB_PASSPHRASE, Base64.encodeToString(passphrase, Base64.DEFAULT))
            .apply()

        return passphrase
    }

    /**
     * Checks whether the secure preferences file exists (i.e., app has been
     * initialized at least once). Useful for detecting fresh installs.
     */
    fun isInitialized(context: Context): Boolean {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.contains(KEY_DB_PASSPHRASE)
        } catch (e: Exception) {
            false
        }
    }
}
