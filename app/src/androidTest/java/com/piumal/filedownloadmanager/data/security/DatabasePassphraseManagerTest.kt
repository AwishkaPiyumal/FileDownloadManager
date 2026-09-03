package com.piumal.filedownloadmanager.data.security

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabasePassphraseManagerTest {

    @Test
    fun getOrCreatePassphrase_persistsSamePassphrase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // First call generates and stores
        val passphrase1 = DatabasePassphraseManager.getOrCreatePassphrase(context)

        // Second call should retrieve the same stored passphrase
        val passphrase2 = DatabasePassphraseManager.getOrCreatePassphrase(context)

        assertArrayEquals("Passphrase should be persisted across calls", passphrase1, passphrase2)
    }
}
