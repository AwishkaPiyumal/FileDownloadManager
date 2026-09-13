package com.piumal.filedownloadmanager

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Assert.assertThrows

class BootReceiverTest {

    @Test
    fun testBootReceiverIsProtected() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent("android.intent.action.BOOT_COMPLETED")
        intent.setClassName(context.packageName, "com.piumal.filedownloadmanager.data.download.BootReceiver")

        // Try to send broadcast without the required permission
        // It should throw a SecurityException if the receiver is properly protected
        assertThrows(SecurityException::class.java) {
            context.sendBroadcast(intent)
        }
    }
}
