package com.netcracker.utils.rabbitmq.graph

import com.netcracker.utils.rabbitmq.graph.Utils.{UrlStringContext, mapper}
import com.typesafe.scalalogging.StrictLogging
import requests.RequestAuth

import java.net.{URI, URL, URLEncoder}
import java.nio.charset.StandardCharsets


class VHostStructureExtractor (
	val url: URL,
	val auth: RequestAuth,
) extends StrictLogging  {
	def extract(): ExportedStructure = {
		logger.info(s"Request for vhost definition: $url")
		val exported = requests.get(url.toString, auth = auth, verifySslCerts = false)

		if (exported.statusCode != 200) {
			logger.error(s"Unexpected status code: ${exported.statusCode}")
			throw new RuntimeException(s"Unexpected status code: ${exported.statusCode}")
		}

		logger.info("Parse structure data...")
		mapper.readValue(exported.data.array, classOf[ExportedStructure])
	}
}

object VHostStructureExtractor {
	def apply(mgmtUrl: String, vhost: String) = {
		val uri = URI.create(mgmtUrl)
		new VHostStructureExtractor(
			url"${uri.getScheme}://${uri.getHost}:${uri.getPort}/api/definitions/${URLEncoder.encode(vhost, StandardCharsets.UTF_8)}",
			uri.getUserInfo().split(":") match {
				case Array(user, password) => RequestAuth.Basic(user, password)
				case _ => throw new IllegalArgumentException(s"Can't parse username and password from given url: $mgmtUrl")
			}
		)
	}
}
