package org.uglylabs.utils.rabbitmq.graph

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}

import java.net.URI

case class ExportedStructure(
	vhosts: Map[String, VHost],
	shovels: List[Shovel],
)

@JsonIgnoreProperties(ignoreUnknown = true)
case class VHost(
	queues: List[Queue],
	exchanges: List[Exchange],
	bindings: List[Binding]
)

@JsonIgnoreProperties(ignoreUnknown = true)
case class Exchange(
	name: String,
	@JsonProperty("type") exchangeType: String,
	arguments: Map[String, String],
) {
	def dlx(): Option[String] = arguments.get("alternate-exchange")
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Queue(
	name: String,
	durable: Boolean,
	arguments: Map[String, String]
) {
	def dlx(): Option[String] = arguments.get("x-dead-letter-exchange")
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Binding(
	source: String,
	destination: String,
	@JsonProperty("destination_type") destinationType: String,
	@JsonProperty("routing_key") private val routingKeyRaw: String,
) {
	def routingKey(): Option[String] =
		Option(routingKeyRaw).map(_.trim()).filterNot(_.trim().isBlank)
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Shovel(
	name: String,
	vhost: String,
	@JsonProperty("type") shovelType: String,
	state: String,
	@JsonProperty("src_uri") sourceUri: URI,
	@JsonProperty("dest_uri") destinationUri: URI,
	@JsonProperty("src_queue") sourceQueue: Option[String],
	@JsonProperty("src_exchange") sourceExchange: Option[String],
	@JsonProperty("dest_queue") destinationQueue: Option[String],
	@JsonProperty("dest_exchange") destinationExchange: Option[String],
) {
	def sourceVHost: String =
		sourceUri.getPath.stripPrefix("/")

	def destinationVHost: String =
		destinationUri.getPath.stripPrefix("/")
}
