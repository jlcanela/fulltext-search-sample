Usefulness of data pipeline 

diagram / modelling
- C4 diagram 
- UML
- Enterprise Integration Patterns
- Merise

command line tool
- bash
- scala
- spark: data processing library 

spark
- documentation: https://spark.apache.org/
- spark-submit
- sigmod 2015 paper http://people.csail.mit.edu/matei/papers/2015/sigmod_spark_sql.pdf
- spark UI
- spark history server
- recomputation due to shuffle
- spark streaming http://spark.apache.org/docs/latest/streaming-programming-guide.html
- spark hands on https://github.com/jlcanela/spark-hands-on/wiki
- optimizations https://blog.cloudera.com/how-does-apache-spark-3-0-increase-the-performance-of-your-sql-workloads/

spark-streaming with Kafka
- https://spark.apache.org/docs/latest/streaming-programming-guide.html
- https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html
- https://spark.apache.org/docs/latest/sql-data-sources-avro.html
- https://www.linkedin.com/pulse/spark-structured-stream-kafka-avro-messages-hari-ramesh/
- https://www.confluent.io/blog/kafka-connect-deep-dive-converters-serialization-explained/

devops / architecture
- environment: dev / sit or integration-testing  / qa / preprod / prod 
- cloud patterns https://www.cloudcomputingpatterns.org/

test / QA
- https://softwaretestingfundamentals.com/

data culture
- history of time : https://www.joda.org/joda-time/
- character encoding : http://www.i18nqa.com/debug/bug-utf-8-latin1.html
- let’s dig about time https://www.kaggle.com/olistbr/brazilian-ecommerce

elastic search
- https://www.elastic.co/what-is/elk-stack
- https://medium.com/everythingatonce/architecture-of-elastic-search-installation-part-2-ec56a5a3c192
- https://lucene.apache.org/core/
- https://www.geeksforgeeks.org/inverted-index/#:~:text=An%20inverted%20index%20is%20an,document%20or%20a%20web%20page.

machine learning
- Spark NLP https://www.johnsnowlabs.com/spark-nlp/ and 
- Spark NLP github https://github.com/JohnSnowLabs/spark-nlp#quick-start

mesos / kubernetes and Spark
- http://spark.apache.org/docs/latest/running-on-mesos.html
- https://www.datamechanics.co/blog-post/pros-and-cons-of-running-apache-spark-on-kubernetes

aws services
- lake formation https://aws.amazon.com/lake-formation/?nc1=h_ls&whats-new-cards.sort-by=item.additionalFields.postDateTime&whats-new-cards.sort-order=desc
- managed services 

functional programming
- https://zio.dev/

dag
- airflow https://airflow.apache.org/
- kubeflow https://www.kubeflow.org/
- mlflow https://mlflow.org/
- aws glue https://docs.aws.amazon.com/glue/latest/dg/workflows_overview.html

Kafka
- confluent website
- aphyr https://aphyr.com/posts/293-call-me-maybe-kafka
- getting started https://docs.confluent.io/platform/current/quickstart/ce-docker-quickstart.html


java
- jar file
- classloader https://www.baeldung.com/java-classloaders
- log management https://logging.apache.org/log4j/2.x/

build / make
- mill / sbt (scala)
  - how to build a jar file
    mill spark.jar => simple jar : only the compiled classes
    mill spark.assembly => assembly jar compiled classes with ivy/maven dependencies
    mill spark.standalone.run    standalone jar contains everything including spark dependencies

- ant https://ant.apache.org/manual/using.html
- maven (java) https://maven.apache.org/
- npm (javascript)
- pip (python)
- make
- bazel https://bazel.build/
- maven repository https://mvnrepository.com/, all the jars (libraries) published by open source project

gpdr
- regulation https://ec.europa.eu/info/law/law-topic/data-protection/data-protection-eu_en#fundamental-rights
- https://gdpr.eu/checklist/
- exercise: exemple of data processing you know

storage of data
- csv file
- json splitted files
- excel file
- parquet
- orc https://orc.apache.org/specification/ORCv1/

"database"
- elastic search
- relational database 
- document database

docker 
- docker container 
- docker compose

api
- web server

query data 
- sql
- rest
- graphql 

platform
- local platform
- microsoft azure
- amazon web service
- google gcp 

know-how
- troubleshooting / verifying number
