package org.uglylabs.utils.rabbitmq.graph

import com.typesafe.scalalogging.StrictLogging

object Graph extends StrictLogging {
	def render(structure: ExportedStructure): String  = {
		logger.info("Render PlantUML graph definition")
		logger.debug(s"Structure: $structure")

		val graphBuilder = new StringBuilder()
		graphBuilder.append(
			"""@startuml
			  |left to right direction
			  |skinparam componentStyle rectangle
			  |!define exchange(e_name, e_alias, e_type) hexagon "e_name\n<size:12><e_type></size>" as e_alias
			  |
			  |""".stripMargin)

		structure.vhosts
			.map { case (name, structure) => renderVHost(name, structure) }
			.foreach(graphBuilder.append)

		structure.shovels
			.map( renderShovel )
			.foreach(graphBuilder.append)

		graphBuilder.append("@enduml\n")

		graphBuilder.toString()
	}

	private def renderVHost(vhostName: String, structure: VHost): String = {
		logger.info(s"Render vhost: $vhostName")
		val host = {
				structure.exchanges.map(e => declareExchange(vhostName, e)) ++
				structure.queues.flatMap(_.dlx()).map(e => declareQueueDlxExchange(vhostName, e)) ++
				structure.queues.map(q => declareQueue(vhostName, q)) ++
				structure.bindings.map(b => formatBinding(vhostName, b)) ++
				structure.queues.flatMap(q => formatQueueDlx(vhostName, q)) ++
				structure.exchanges.flatMap(e => formatExchangeAlternateExchange(vhostName, e))
			}
			.map("\t" + _)
			.mkString("\n")

		s"""package ${vhostName} {
		|${host}
		|}
		|""".stripMargin
	}

	private def declareExchange(vhostName: String, e : Exchange) =
		s"""exchange("${e.name}","${exchangeAlias(vhostName, e.name)}", "${e.exchangeType}")"""

	private def declareQueueDlxExchange(vhostName: String, e: String) =
		s"""exchange("$e","${exchangeAlias(vhostName, e)}", "dlx")"""

	private def declareQueue(vhostName: String, q: Queue): String =
		s"""queue "${q.name}" as ${queueAlias(vhostName, q.name)}""" +
			(if (q.durable) "" else " #line.dotted")

	private def formatQueueDlx(vhostName: String, q: Queue): Option[String] =
		q.dlx().map(dlx => s"""${queueAlias(vhostName, q.name)} --> ${exchangeAlias(vhostName, dlx)} #line.dashed""")

	private def formatBinding(vhostName: String, b: Binding): String = {
		val targetName = b.destinationType match {
			case "queue" => queueAlias(vhostName, b.destination)
			case "exchange" => exchangeAlias(vhostName, b.destination)
			case _ => throw new IllegalArgumentException(s"Unknown destination type: ${b.destinationType}" )
		}

		val filterValue = b.routingKey().map(s => s""" : $s""").getOrElse("")
		s"""${exchangeAlias(vhostName, b.source)} --> $targetName$filterValue"""
	}

	private def formatExchangeAlternateExchange(vhostName: String, e: Exchange): Option[String] = {
		e.dlx().map(dlx => s"""${exchangeAlias(vhostName, e.name)} .> ${exchangeAlias(vhostName, dlx)}""")
	}

	private def renderShovel(definition: Shovel): String = {
		val alias = compat(s"${definition.vhost}_shovel_${definition.name}")

		val source = definition
			.sourceQueue.map(queueAlias(definition.sourceVHost, _))
			.orElse(definition.sourceExchange.map(exchangeAlias(definition.sourceVHost, _)))
			.get

		val destination = definition
			.destinationQueue.map(queueAlias(definition.destinationVHost, _))
			.orElse(definition.destinationExchange.map(exchangeAlias(definition.destinationVHost, _)))
			.get

		s"""package ${definition.vhost} {
			|	Component "${definition.name}" as $alias <<shovel>>
			|	$alias .> $destination
			|	$source ..> $alias
			|}
			|""".stripMargin
	}

	private def exchangeAlias(vhost: String, name: String) = alias(vhost, "e", name)
	private def queueAlias(vhost: String, name: String) = alias(vhost, "q", name)
	private def alias(vhost: String, entityChar: String, name: String) =
		s"${compat(vhost)}_${entityChar}_${compat(name)}"

	private def compat(s: String): String =
		s.replaceAll("[^\\w]", "_")
}
