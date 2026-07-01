package com.ruota.uicommon.format

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {
    @Test fun formatsThousands() {
        assertEquals("$0", formatMoney(0))
        assertEquals("$250", formatMoney(250))
        assertEquals("$1,200", formatMoney(1200))
        assertEquals("$12,000", formatMoney(12000))
        assertEquals("$1,000,000", formatMoney(1000000))
        assertEquals("-$500", formatMoney(-500))
    }
}
