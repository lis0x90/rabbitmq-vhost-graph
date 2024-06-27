package org.uglylabs.utils.rabbitmq.graph

import com.typesafe.scalalogging.StrictLogging

object Graph extends StrictLogging {
	def render(structure: ExportedStructure): String  = {
		logger.info("Render PlantUML graph definition.")
		val lines: List[String] = structure.exchanges.map(formatExchange) ++
			structure.queues.map(formatQueue) ++
			structure.bindings.map(formatBinding) ++
			structure.queues.flatMap(formatQueueDlx)

		val graphBuilder = new StringBuilder()
		graphBuilder.append(
			"""
			  |@startuml
			  |left to right direction
			  |!define exchange(e_alias, e_type) hexagon "e_alias\n<size:12><e_type></size>" as e_alias
			  |
			  |""".stripMargin)
		lines.foreach(l => graphBuilder.append(l).append("\n"))
		graphBuilder.append("@enduml\n")

		graphBuilder.toString()
	}

	private def formatExchange(e : Exchange) =
		s"""exchange("${e.name}","${e.exchangeType}")"""

	private def formatQueue(q: Queue): String =
		s"""queue "${q.name}"""" + (if (q.durable) "" else " #line.dotted")

	private def formatQueueDlx(q: Queue): Option[String] =
		q.dlx().map(dlx => s""""${q.name}" --> "$dlx" #line.dashed""")

	private def formatBinding(b: Binding): String =
		s""""${b.source}" --> "${b.destination}"""" +
			b.routingKey().map(s => s""" : $s""").getOrElse("")
}
