package com.example

import com.example.data.model.AnonymousMessageEntity
import com.example.util.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun passwordHashing_isDeterministic() {
    val hash1 = SecurityUtils.hashPassword("secret123")
    val hash2 = SecurityUtils.hashPassword("secret123")
    val hash3 = SecurityUtils.hashPassword("other")

    assertEquals(hash1, hash2)
    assertNotEquals(hash1, hash3)
    assertEquals(64, hash1.length) // SHA-256 hex string
  }

  @Test
  fun countdownFormatting_displaysCorrectDaysAndHours() {
    val fourDaysMs = 4L * 24L * 60L * 60L * 1000L
    val countdown = SecurityUtils.formatCountdown(fourDaysMs)
    assertTrue(countdown.contains("4j 00h 00m 00s"))
  }

  @Test
  fun messageEntity_fourDaysCalculation() {
    val msg = AnonymousMessageEntity(
      recipientUserId = 1L,
      messageText = "Test",
      receivedAt = 1000L,
      unlockAt = 1000L + AnonymousMessageEntity.FOUR_DAYS_MS
    )
    assertEquals(4L * 24L * 60L * 60L * 1000L, msg.unlockAt - msg.receivedAt)
  }
}

