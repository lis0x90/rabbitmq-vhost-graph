package org.uglylabs.utils.rabbitmq.graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.uglylabs.utils.rabbitmq.graph.Utils.UriStringContext;


class VHostStructureExtractorTest {
    @Test
    def testUriParse(): Unit = {
        val co = VHostStructureExtractor.apply(uri"http://admin:admin@localhost:16532", Seq("vhost"))
        assertEquals("http://admin:admin@localhost:16532", co.uri.toString)
        assertEquals(Some("Basic YWRtaW46YWRtaW4="), co.auth().header)
    }

    @Test
    def testUriParseDefault(): Unit = {
        val co = VHostStructureExtractor.apply(uri"http://admin:admin@localhost:16532", Seq("/"))
        assertEquals("http://admin:admin@localhost:16532", co.uri.toString)
        assertEquals(Some("Basic YWRtaW46YWRtaW4="), co.auth().header)
    }
}