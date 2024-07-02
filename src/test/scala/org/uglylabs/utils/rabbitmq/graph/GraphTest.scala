package org.uglylabs.utils.rabbitmq.graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GraphTest {
	@Test
	def testRender1(): Unit = {
		val result = Graph.render(ExportedStructure(Map.empty, List.empty))
		val expected = """@startuml
						 |left to right direction
						 |skinparam componentStyle rectangle
						 |!define exchange(e_name, e_alias, e_type) hexagon "e_name\n<size:12><e_type></size>" as e_alias
						 |
						 |@enduml
						 |""".stripMargin
		assertEquals(expected.normalized, result.normalized)
	}

	@Test
	def testRender2(): Unit = {
		val vhost = VHost(
			List(Queue("orders", true, Map.empty)),
			List(Exchange("ex", "fanout", Map.empty)),
			List(Binding("ex", "orders", "queue", "")),
		)

		val result = Graph.render(ExportedStructure(Map("a" -> vhost), List.empty))
		val expected =
				"""@startuml
				|left to right direction
				|skinparam componentStyle rectangle
				|!define exchange(e_name, e_alias, e_type) hexagon "e_name\n<size:12><e_type></size>" as e_alias
				|
				|package a {
				|	exchange("ex","a_e_ex", "fanout")
				|	queue "orders" as a_q_orders
				|	a_e_ex --> a_q_orders
				|}
				|@enduml
				|""".stripMargin
		assertEquals(expected.normalized, result.normalized)
	}

	implicit class StrTest(var s: String) {
		def normalized = s.trim().replaceAll("\r?\n", "\n")
	}
}