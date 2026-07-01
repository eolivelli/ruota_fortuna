package com.ruota.core

import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceholderTest {
    @Test
    fun gameNameIsItalian() {
        assertEquals("La Ruota Della Fortuna", Placeholder.GAME_NAME)
    }
}
