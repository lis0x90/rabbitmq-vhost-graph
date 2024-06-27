package org.uglylabs.utils.rabbitmq.graph

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}

@JsonIgnoreProperties(ignoreUnknown = true)
case class ExportedStructure(
	queues: List[Queue],
	exchanges: List[Exchange],
	bindings: List[Binding]
)

@JsonIgnoreProperties(ignoreUnknown = true)
case class Exchange(
	name: String,
	@JsonProperty("type") exchangeType: String,
	@JsonProperty("auto_delete") autoDelete: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = true)
case class Queue(
	name: String,
	durable: Boolean,
	@JsonProperty("auto_delete") autoDelete: Boolean,
	arguments: Map[String, String]
) {
	def dlx(): Option[String] = arguments.get("x-dead-letter-exchange")
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Binding(
	source: String,
	destination: String,
	@JsonProperty("destination_type") destinationType: String,
	@JsonProperty("routing_key") _routingKey: String,
) {
	def routingKey(): Option[String] =
		Option(_routingKey).map(_.trim()).filterNot(_.trim().isBlank)
}