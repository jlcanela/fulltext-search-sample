# Cleaning batch Test Plan

## Functional requirements 

### What is the cleaning batch doing ?

The batch is doing: 
* download the data file 
* read the data file
* parse the line & provide the required fields
* write the data file 
* write logs to inform about the process 
* validation step to ensure file format generated

additionnal business rules: 
* download fails if more than 10 minutes
* monitoring process is up to date and take into account the following: 
  * disk space
  * cluster availability
  * credentials 
  * ...

## Implementation

How we are implementing the requirements ? 

### Download the data file 
```
curl -o" access.log.gz -L https://github.com/jlcanela/spark-hands-on/raw/master/almhuette-raith.log/access.log.gz
```

### Read data file

```
spark.read.text(filepath)
```

### Parse the line 

Use the following regexp:
```
val R = """^(?<ip>[0-9.]+) (?<identd>[^ ]) (?<user>[^ ]) \[(?<datetime>[^\]]+)\] \"(?<request>[^\"]*)\" (?<status>[^ ]*) (?<size>[^ ]*) \"(?<referer>[^\"]*)\" \"(?<useragent>[^\"]*)\" \"(?<unk>[^\"]*)\"""".r
```

### Provide the required fields: 

Use the following structure:
```
case class AccessLog(ip: String, ident: String, user: String, datetime: String, request: String, status: String, size: String, referer: String, userAgent: String, unk: String)
```

### Write the data file:

```
dsExtended.write.option("compression", "gzip").mode("Overwrite").parquet(out)
```

### Write the logs:

default spark logs

specific logs:
```
log.info(s"${dsExtended.schema.toDDL}")
```

using log4j.properties file 
```
log4j.rootLogger=WARN,stdout

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%p\t%d{ISO8601}\t%r\t%c\t[%t]\t%m%n
```


# High level functional testing 

## Download the data file 

"Success Case" :
```
Given I have an url with https protocol 
When I download the file on S3 
Then the file is available on S3
```

```
Given I have an url with *http* protocol 
When I download the file on S3 
Then I get an "not supported http protocol" error 
```

```
Given I have an url with *dkms* protocol 
When I download the file on S3 
Then I get an "unknown dkms protocol" error 
```

```
Given I have an url with unknow.domain domain  
When I download the file on S3 
Then I get an "unknown.domain" dns error 
```

```
Given I have an url with 192.168.0.177 ip  
When I download the file on S3 
Then I get an unreachable ip address error 
```

```
Given I have an url with 192.168.0.14 ip  
When I download the file on S3 
Then I get a timeout error 
```

```
Given I have url "20 mn download time"
When I download the file on S3
Then I get a timeout error after 10 mn
```


                File                       Server     -----   dns  ---       network                  ----   client 

         Present / not available         UP / DOWN        OK - NOTOK     reachable / not reachable          

case 1         Present                      UP              OK                  OK                    =>  result file ok 
case 2         Present                      UP              OK                 not reachable          =>  tcp/ip timeout
case 3         Present                      UP              NOTOK              not reachable          => Could not resolve host

everlasting download error case : 
- reachable through network
- dns is up
- server is up
- file is present
- every 25 seconds, I provide 1 byte of the file
- 10 TB file 

=> 

how do I prevent everlasting download ? 

-> monitor the download rate, and issue an insufficient download rate error 
-> use a 10mn timeout, and issue a timeout error if download is too slow



## Writing the data file 

Directory is not available 
=> log an error with "not available"

Write permission error
=> log a write permission error

Not enough space on disk
=> log a not enough file on disk 

Encoding is not consistant ?
=> 


 Agile methodology 

INVEST stories 

Independant
Negociable / Negociated
Valuable
Estimable
Small
Testable 





System analysis approach : 
* identify the various components
* for each component determine the error case   (developer)
* analyze the impact for the whole process      (data architect, devops, platform engineer => the team)




* download the data file 
* read the data file
* parse the line & provide the required fields
* write the data file 
* write logs to inform about the process 


### Other scenarios 

wrong format, it is not saved
file is empty
after we open the file we need to return if we have no space left on device
we have to map the format we choose
we need to provide a test file with wrong format
file is available accessible and available
test cases of wrong format
some columns to be date, is it the right format ?

testing can be done with tools 


