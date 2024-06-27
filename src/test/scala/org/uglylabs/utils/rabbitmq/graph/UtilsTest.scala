package org.uglylabs.utils.rabbitmq.graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test;

import Utils.StringExt

class UtilsTest {
    @Test
    def testCompat(): Unit = {
        assertEquals("abc_DEF_34", "abc_DEF:34".compat)
    }
}