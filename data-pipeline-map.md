

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
- documentation: https://spark.apache.org/
- spark-submit
- sigmod 2015 paper http://people.csail.mit.edu/matei/papers/2015/sigmod_spark_sql.pdf
- spark UI
- spark history server
- recomputation due to shuffle
- spark streaming http://spark.apache.org/docs/latest/streaming-programming-guide.html
- spark hands on https://github.com/jlcanela/spark-hands-on/wiki

data culture
- history of time : https://www.joda.org/joda-time/
- character encoding : http://www.i18nqa.com/debug/bug-utf-8-latin1.html
- let’s dig about time https://www.kaggle.com/olistbr/brazilian-ecommerce

machine learning
- Spark NLP https://www.johnsnowlabs.com/spark-nlp/

java
- jar file
- classloader https://www.baeldung.com/java-classloaders
- log management https://logging.apache.org/log4j/2.x/

build / make
- mill / sbt (scala)
  - how to build a jar file
    mill spark.jar => simple jar : only the compiled classes
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
- exercise : exemple of data processing you know

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
