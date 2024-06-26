package com.netcracker.utils.rabbitmq.graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test;


class VHostStructureExtractorTest {
    @Test
    def testUriParse(): Unit = {
        val co = VHostStructureExtractor.apply("http://admin:admin@localhost:16532", Some("vhost"))
        assertEquals("http://localhost:16532/api/definitions/vhost", co.url.toString)
        assertEquals(Some("Basic YWRtaW46YWRtaW4="), co.auth.header)
    }
}