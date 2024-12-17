# rabbitmq-vhost-graph
A simple utility that scans a given RabbitMQ VHost and represents exchanges and queues as a graph.

# Help 

```commandline
~>java -jar target/rabbitmq-graph.jar --help

Image generation usage example:
java -jar rabbitmq-graph.jar http://guest:guest@localhost:15672 image.png

or if you omit image path, program will print definition in plantUML for default vhost:
java -jar rabbitmq-graph.jar http://guest:guest@localhost:15672


Usage: rabbitmq-graph [options] url [image]

  url                  url to RabbitMQ management web interface in format: http(s)://user:password@host:port
  image                generate PNG image instead of printing PlantUML diagram definition
  -h, --vhost <value>  vhost name. Defaults: /
  -v, --verbose        turn on verbose logging mode. All messages prints to STDERR
  --help               prints this usage text
```


# Usage
Generate PlantUML diagram definition:

```commandline
~>java -jar rabbitmq-graph.jar http://guest:guest@localhost:15672
```

Output:
```
@startuml
left to right direction
!define exchange(e_alias, e_type) hexagon "e_alias\n<size:12><e_type></size>" as e_alias

exchange("ex1","direct")
queue "candy-supplier"
queue "toys-supplier"
"ex1" --> "candy-supplier" : candy
"ex1" --> "toys-supplier" : toy
@enduml
```

Or you can generate image for multiple vhosts linked with shovel plugin:

```commandline
~>java -jar rabbitmq-graph.jar http://guest:guest@localhost:15672 -h agg,a,b graph.png
```

Result image:

![graph.png](graph.png)
