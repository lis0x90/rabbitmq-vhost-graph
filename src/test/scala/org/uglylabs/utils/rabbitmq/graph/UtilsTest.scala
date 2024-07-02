package org.uglylabs.utils.rabbitmq.graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import Utils.{UriStringContext}

class UtilsTest {
    @Test
    def testUriExt(): Unit = {
        val u = uri"${"http"}://${"localhost"}:12345"
        assertEquals("http://localhost:12345", u.toString)
    }
}