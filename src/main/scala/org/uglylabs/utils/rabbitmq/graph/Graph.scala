package org.uglylabs.utils.rabbitmq.graph

import com.typesafe.scalalogging.StrictLogging
import Utils.StringExt

object Graph extends StrictLogging {
	def render(structure: ExportedStructure): String  = {
		logger.info("Render PlantUML graph definition.")
		val lines: List[String] = structure.exchanges.map(formatExchange) ++
			structure.queues.flatMap(_.dlx()).map(formatDlxExchange) ++
			structure.queues.map(formatQueue) ++
			structure.bindings.map(formatBinding) ++
			structure.queues.flatMap(formatQueueDlx)

		val graphBuilder = new StringBuilder()
		graphBuilder.append(
			"""
			  |@startuml
			  |left to right direction
			  |!define exchange(e_name, e_alias, e_type) hexagon "e_name\n<size:12><e_type></size>" as e_alias
			  |
			  |""".stripMargin)
		lines.foreach(l => graphBuilder.append(l).append("\n"))
		graphBuilder.append("@enduml\n")

		graphBuilder.toString()
	}

	private def formatExchange(e : Exchange) =
		s"""exchange("${e.name}","e_${e.name.compat}", "${e.exchangeType}")"""

	private def formatDlxExchange(e: String) =
		s"""exchange("${e}","e_${e.compat}", "dlx")"""

	private def formatQueue(q: Queue): String =
		s"""queue "${q.name}" as q_${q.name.compat}""" + (if (q.durable) "" else " #line.dotted")

	private def formatQueueDlx(q: Queue): Option[String] =
		q.dlx().map(dlx => s"""q_${q.name.compat} --> e_${dlx.compat} #line.dashed""")

	private def formatBinding(b: Binding): String = {
		val targetName = (b.destinationType match {
			case "queue" => "q_"
			case "exchange" => "e_"
			case _ => throw new IllegalArgumentException(s"Unknown destination type: ${b.destinationType}" )
		}) + b.destination.compat
		s"""e_${b.source.compat} --> $targetName""" +
			b.routingKey().map(s => s""" : $s""").getOrElse("")
	}
}
