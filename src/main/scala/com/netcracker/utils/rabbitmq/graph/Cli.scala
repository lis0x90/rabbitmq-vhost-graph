package com.netcracker.utils.rabbitmq.graph

import com.netcracker.utils.rabbitmq.graph.Utils.*
import com.typesafe.scalalogging.StrictLogging
import net.sourceforge.plantuml.SourceStringReader
import scopt.OParser

import java.nio.file.StandardOpenOption.*
import java.nio.file.{Files, Path}

object Cli extends StrictLogging {
	case class RunConfig(
		url: String = "",
		vhost: String = "/",
		image: Option[Path] = None,
	)

	val builder = OParser.builder[RunConfig]
	val argParser = {
		import builder.*
		OParser.sequence(
			programName("rabbitmq-graph"),
			head(
				"""
				  |Image generation usage example:
				  |java -jar rabbitmq-graph.jar http://guest:guest@localhost:15672 image.png
				  |
				  |or if you omit image path, program will print definition in plantUML for default vhost:
				  |java -jar rabbitmq-graph.jar http://guest:guest@localhost:15672
				  |
				  |""".stripMargin),
			arg[String]("url")
				.required()
				.text("http(s)://user:password@host:port url to RabbitMQ management web interface")
				.action((u, c) => c.copy(url = u.stripSuffix("/"))),
			arg[String]("image")
				.optional()
				.text("generate PNG image")
				.action((u, c) => c.copy(image = Some(Utils.normalizePath(u)))),
			opt[String]('v', "vhost")
				.text("vhost name. Defaults: /")
				.action((v, c) => c.copy(vhost = v))
		)
	}

	def main(args: Array[String]): Unit = {
		OParser.parse(argParser, args, RunConfig()) match {
			case Some(config) => print(execute(config))
			case _ => System.exit(1)
		}
		System.exit(0)
	}

	private def execute(runOptions: RunConfig): String = {
		val structure = VHostStructureExtractor(runOptions.url, runOptions.vhost).extract()
		val plantUmlDefinition = Graph.render(structure)


		runOptions.image.map { imagePath =>
			logger.info(s"Create image: $imagePath")
			val description = using(Files.newOutputStream(imagePath, CREATE, WRITE)) { out =>
				val reader = new SourceStringReader(plantUmlDefinition)
				reader.outputImage(out).getDescription()
			}

			s"""Description: $description
			   |Image generated: $imagePath""".stripMargin
		}.getOrElse(plantUmlDefinition)
	}
}

