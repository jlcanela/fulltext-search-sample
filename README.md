# fulltext-search-sample

## Install mill

mill is a build tool similar to maven, sbt, npm:
https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html#_installation

## Packaging

To package the batch:
```
mill batch.standalone.assembly
mill batch.assembly
```

To run the program:
```
mill data.run
```


## ElasticSearch troubleshooting

https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html#docker-cli-run-prod-mode

Check elasticsearch is up and running: 
```
curl -X GET "localhost:9200/_cat/nodes?v=true&pretty"
```
 
Stop cluster and clean volumes:
```
docker-compose down -v
```

# Spark history

```
SPARK_HISTORY_OPTS="-Dspark.history.fs.logDirectory=spark-logs" $SPARK_HOME/sbin/start-history-server.sh
```

```
$SPARK_HOME/sbin/stop-history-server.sh
```

http://localhost:18080/

# Performance testing

```
echo '{"query":"query{logs(first:0, size: 100) {count logs { ip datetime http method user size status userAgent}}}","variables":null}' > search.json
ab -p search.json -T application/json -c 1 -n 1 http://localhost:8088/api/graphql

# curl -X POST -d '{"query":"query{logs(first:0, size: 100) {count logs { ip datetime http method user size status userAgent}}}","variables":null}' http://localhost:8088/api/graphql

echo '{"query":"mutation{removeIndex(name:"nestor")}","variables":null}' > removeIndex.json
ab -p removeIndex.json -T application/json -c 1 -n 1 http://localhost:8088/api/graphql

java -XX:+UseLinuxPosixThreadCPUClocks -agentpath:/home/jlcanela/opt/lib/deployed/jdk16/linux-amd64/libprofilerinterface.so=/home/jlcanela/opt/lib,5140 -jar out/api/assembly/dest/out.jar start
