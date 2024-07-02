package org.uglylabs.utils.rabbitmq.graph

import com.fasterxml.jackson.core.`type`.TypeReference
import com.typesafe.scalalogging.StrictLogging
import org.uglylabs.utils.rabbitmq.graph.Utils.{UriStringContext, mapper}
import requests.RequestAuth

import java.net.{URI, URLEncoder}
import java.nio.charset.StandardCharsets


class VHostStructureExtractor (
	val uri: URI,
	val vhosts: Seq[String],
) extends StrictLogging  {
	def extract(): ExportedStructure = {
		ExportedStructure(
			vhosts = vhosts.map(name => (name, extractVHost(name))).toMap,
			shovels = extractShovels()
		)
	}

	private def extractVHost(vhostName: String): VHost = {
		val defUrl = uri"${uri.getScheme}://${uri.getHost}:${uri.getPort}/api/definitions/${URLEncoder.encode(vhostName, StandardCharsets.UTF_8)}"

		logger.info(s"Request for vhost definition: $defUrl")
		val exported = requests.get(defUrl.toString, auth = auth(), verifySslCerts = false)

		if (exported.statusCode != 200) {
			logger.error(s"Unexpected status code: ${exported.statusCode}")
			throw new RuntimeException(s"Unexpected status code: ${exported.statusCode}")
		}

		logger.info("Parse structure data...")
		mapper.readValue(exported.data.array, classOf[VHost])
	}

	private def extractShovels(): List[Shovel] = {
		logger.info(s"List shovel plugins...")
		val defUrl = uri"${uri.getScheme}://${uri.getHost}:${uri.getPort}/api/shovels/"
		val exported = requests.get(defUrl.toString, auth = auth(), verifySslCerts = false)

		if (exported.statusCode != 200) {
			logger.error(s"Unexpected status code: ${exported.statusCode}")
			throw new RuntimeException(s"Unexpected status code: ${exported.statusCode}")
		}

		logger.info("Parse structure data...")
		mapper.readValue(exported.data.array, new TypeReference[List[Shovel]](){})
	}

	def auth() = {
		uri.getUserInfo().split(":") match {
			case Array(user, password) => RequestAuth.Basic(user, password)
			case _ => throw new IllegalArgumentException(s"Can't parse username and password from given url: $uri")
		}
	}
}