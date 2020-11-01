# data-ingestion

## spinning up the infrastructure
docker compose -p statement up 

## view container's IP address
docker ps -q | xargs -n 1 docker inspect --format '{{ .Name }} {{range .NetworkSettings.Networks}} {{.IPAddress}}{{end}}' | sed 's#^/##';

## create topics
docker exec -it statement_kafka_1 kafka-topics --bootstrap-server localhost:9092 --create --replication-factor 1 --partitions 10 --topic ingestion

## consume messages
docker exec -it statement_kafka_1 kafka-console-consumer --bootstrap-server localhost:9092 --topic ingestion --from-beginning 

## list consumer groups
docker exec -it statement_kafka_1 kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group data-ingestion

## search for data
curl -X POST "http://240.12.0.2:9200/extrato/_search" -d'{"query":{"query_string":{"query": "8bfca9fa-ecdf-4f18-bfe3-e7f3c0360a40"}}}' -H 'Content-type:application/json'
curl -X POST "http://240.12.0.2:9200/extrato/_search" -d'{"query":{"query_string":{"query": "946eccbc-a87b-4aa5-b856-a13ca34ced98"}}}' -H 'Content-type:application/json'
curl -X POST "http://240.12.0.2:9200/extrato/_search?pretty"