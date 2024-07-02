package org.uglylabs.utils.rabbitmq.graph

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.net.URI
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

	implicit class UriStringContext(private val sc: StringContext) extends AnyVal {
		def uri(args: Any*): URI =
			URI.create(
				sc.parts.take(args.size)
					.zipWithIndex
					.map { case (s, i) => s + args(i).toString }
					.mkString + sc.parts.drop(args.size).mkString

			)
	}

	def normalizePath(path: String): Path = {
		val homeDir = System.getProperty("user.home").replace("\\", "/")
		Paths.get(path.replaceFirst("^~", homeDir))
	}
}
