# rabbitmq-vhost-graph
Simple utility that scans given RabbitMQ VHost and represent exchanges and queues as a graph

# Usage
Generate PlantUML diagram definition:
`java -jar target/rabbitmq-graph.jar http://guest:guest@localhost:15672`

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

Or you can generate image:

`java -jar target/rabbitmq-graph.jar http://guest:guest@localhost:15672 graph.png`

Result image:

![graph.png](graph.png)
