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
		graphBuilder.append("@startuml\n")
		graphBuilder.append("\nleft to right direction\n\n")
		lines.foreach(l => graphBuilder.append(l).append("\n"))
		graphBuilder.append("@enduml\n")

		graphBuilder.toString()
	}

	private def formatExchange(e : Exchange) =
		s"""hexagon "${e.name}""""

	private def formatQueue(q: Queue): String =
		s"""queue "${q.name}"""" + (if (q.durable) "" else " #line.dotted")

	private def formatQueueDlx(q: Queue): Option[String] =
		q.dlx().map(dlx => s""""${q.name}" --> "$dlx" #line.dashed""")

	private def formatBinding(b: Binding): String =
		s""""${b.source}" --> "${b.destination}"""" +
			b.routingKey().map(s => s""" : $s""").getOrElse("")
}
