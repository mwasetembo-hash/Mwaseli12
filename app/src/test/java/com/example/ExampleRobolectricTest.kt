package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MWASE HYBRID", appName)
  }

  @Test
  fun `verify smart barrier negative offset for LOWER contracts`() {
    val higherBarrier = com.example.data.model.SymbolsDatabase.computeSmartBarrier("1HZ100V", isHigher = true)
    val lowerBarrier = com.example.data.model.SymbolsDatabase.computeSmartBarrier("1HZ100V", isHigher = false)
    org.junit.Assert.assertTrue(higherBarrier.startsWith("+"))
    org.junit.Assert.assertTrue(lowerBarrier.startsWith("-"))
  }
}
