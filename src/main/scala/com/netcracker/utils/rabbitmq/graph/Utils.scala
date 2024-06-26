package com.netcracker.utils.rabbitmq.graph


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.net.{URI, URL}
import java.nio.file.{Path, Paths}

object Utils {
	val mapper = new ObjectMapper()
	mapper.registerModule(DefaultScalaModule)

	def using[T <: AutoCloseable, R](in: T)(f : T => R): R = {
		try {
			f(in)
		} finally {
			in.close()
		}
	}

	implicit class UrlStringContext(private val sc: StringContext) extends AnyVal {
		def url(args: Any*): URL =
			URI.create(
				sc.parts.take(args.size)
					.zipWithIndex
					.map { case (s, i) => s + args(i).toString }
					.mkString + sc.parts.drop(args.size).mkString

			).toURL
	}

	def normalizePath(path: String): Path = {
		val homeDir = System.getProperty("user.home").replace("\\", "/")
		Paths.get(path.replaceFirst("^~", homeDir))
	}
}
