package org.uglylabs.utils.rabbitmq.graph

import Utils.*
import ch.qos.logback.classic.{Level, LoggerContext}
import com.typesafe.scalalogging.StrictLogging
import net.sourceforge.plantuml.SourceStringReader
import org.slf4j.LoggerFactory
import scopt.OParser

import java.nio.file.StandardOpenOption.*
import java.nio.file.{Files, Path}

object Cli extends StrictLogging {
	case class RunConfig(
		url: String = "",
		vhost: String = "/",
		image: Option[Path] = None,
		verbose: Boolean = false,
	)

	private val builder = OParser.builder[RunConfig]
	private val argParser = {
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
				.text("url to RabbitMQ management web interface in format: http(s)://user:password@host:port")
				.action((u, c) => c.copy(url = u.stripSuffix("/"))),
			arg[String]("image")
				.optional()
				.text("generate PNG image instead of printing PlantUML diagram definition")
				.action((u, c) => c.copy(image = Some(Utils.normalizePath(u)))),
			opt[String]('h', "vhost")
				.text("vhost name. Defaults: /")
				.action((v, c) => c.copy(vhost = v)),
			opt[Unit]('v', "verbose")
				.text("turn on verbose logging mode. All messages prints to STDERR")
				.action((_, c) => c.copy(verbose = true)),
			help("help").text("prints this usage text"),
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
		if (runOptions.verbose) {
			val loggerContext = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
			loggerContext.getLogger("root").setLevel(Level.DEBUG);
		}

		val structure = VHostStructureExtractor(runOptions.url, runOptions.vhost).extract()
		val plantUmlDefinition = Graph.render(structure)


		runOptions.image.map { imagePath =>
			logger.info(s"Create image: $imagePath")
			val description = using(Files.newOutputStream(imagePath, CREATE, WRITE)) { out =>
				val reader = new SourceStringReader(plantUmlDefinition)
				reader.outputImage(out).getDescription()
			}

			if (description.contains("error")) {
				"Image generation error. Look at the image for more info"
			} else {
				s"""Description: $description
				   |Image generated: $imagePath""".stripMargin
			}
		}.getOrElse(plantUmlDefinition)
	}
}

