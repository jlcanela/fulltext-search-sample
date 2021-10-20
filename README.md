# fulltext-search-sample

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